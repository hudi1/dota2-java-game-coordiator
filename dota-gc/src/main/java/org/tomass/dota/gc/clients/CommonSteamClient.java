package org.tomass.dota.gc.clients;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tomass.dota.gc.config.AppConfig;
import org.tomass.dota.gc.config.SteamClientConfig;

import in.dragonbra.javasteam.base.IClientMsg;
import in.dragonbra.javasteam.enums.EFriendRelationship;
import in.dragonbra.javasteam.enums.EPersonaState;
import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.steam.handlers.steamfriends.Friend;
import in.dragonbra.javasteam.steam.handlers.steamfriends.SteamFriends;
import in.dragonbra.javasteam.steam.handlers.steamfriends.callback.FriendsListCallback;
import in.dragonbra.javasteam.steam.handlers.steamuser.LogOnDetails;
import in.dragonbra.javasteam.steam.handlers.steamuser.MachineAuthDetails;
import in.dragonbra.javasteam.steam.handlers.steamuser.OTPDetails;
import in.dragonbra.javasteam.steam.handlers.steamuser.SteamUser;
import in.dragonbra.javasteam.steam.handlers.steamuser.callback.AccountInfoCallback;
import in.dragonbra.javasteam.steam.handlers.steamuser.callback.LoggedOffCallback;
import in.dragonbra.javasteam.steam.handlers.steamuser.callback.LoggedOnCallback;
import in.dragonbra.javasteam.steam.handlers.steamuser.callback.LoginKeyCallback;
import in.dragonbra.javasteam.steam.handlers.steamuser.callback.UpdateMachineAuthCallback;
import in.dragonbra.javasteam.steam.steamclient.SteamClient;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackManager;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.steam.steamclient.callbacks.ConnectedCallback;
import in.dragonbra.javasteam.steam.steamclient.callbacks.DisconnectedCallback;
import in.dragonbra.javasteam.types.JobID;

public class CommonSteamClient extends SteamClient {

    public static final String JOB_ID_PREFIX = "JobId_";

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected SteamClientConfig config;

    protected AppConfig appConfig;

    protected SteamUser steamUser;

    protected SteamFriends steamFriends;

    protected CallbackManager manager;

    protected boolean running;

    protected boolean logged;

    private CompletableFuture<Void> managerLoop;

    private Map<String, CompletableFuture<Object>> subscribers = new HashMap<>();

    public CommonSteamClient(SteamClientConfig config, AppConfig appConfig) {
        this.config = config;
        this.appConfig = appConfig;
        init();
    }

    protected void init() {
        steamUser = getHandler(SteamUser.class);
        steamFriends = getHandler(SteamFriends.class);

        manager = new CallbackManager(this);
        manager.subscribe(ConnectedCallback.class, this::onConnected);
        manager.subscribe(DisconnectedCallback.class, this::onDisconnected);

        manager.subscribe(LoggedOnCallback.class, this::onLoggedOn);
        manager.subscribe(LoggedOffCallback.class, this::onLoggedOff);

        manager.subscribe(UpdateMachineAuthCallback.class, this::onMachineAuth);
        manager.subscribe(LoginKeyCallback.class, this::onLoginKey);

        manager.subscribe(AccountInfoCallback.class, this::onAccountInfo);
        manager.subscribe(FriendsListCallback.class, this::onFriendList);
    }

    private void onConnected(ConnectedCallback callback) {
        logger.info("Connected to Steam! Logging in " + config.getUser() + "...");

        LogOnDetails details = new LogOnDetails();
        details.setUsername(config.getUser());

        File loginKeyFile = new File(config.getLoginKey());
        if (loginKeyFile.exists()) {
            try (Scanner s = new Scanner(loginKeyFile)) {
                details.setLoginKey(s.nextLine());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            details.setPassword(config.getPass());
        }

        details.setTwoFactorCode(config.getTwoFactorAuth());
        details.setAuthCode(config.getAuthCode());
        details.setShouldRememberPassword(true);
        try {
            File sentry = new File(config.getSentry());
            if (sentry.exists())
                details.setSentryFileHash(calculateSHA1(sentry));
        } catch (Exception e) {
            e.printStackTrace();
        }

        steamUser.logOn(details);
    }

    private void onDisconnected(DisconnectedCallback callback) {
        if (config.isReconnectOnDisconnect() && logged) {
            logger.info("Disconnected from Steam, reconnecting in 5...");
            try {
                Thread.sleep(5000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            connect();
        } else {
            logger.info("Disconnected from Steam");
        }
    }

    protected void onLoggedOn(LoggedOnCallback callback) {
        boolean isSteamGuard = callback.getResult() == EResult.AccountLogonDenied;
        boolean is2Fa = callback.getResult() == EResult.AccountLoginDeniedNeedTwoFactor;

        if (isSteamGuard || is2Fa) {
            logger.info("This account is SteamGuard protected.");
            disconnect();
            return;
        }
        if (callback.getResult() != EResult.OK) {
            logger.info("Unable to logon to Steam: " + callback.getResult() + " / " + callback.getExtendedResult());
            disconnect();
            return;
        }

        logger.info("Successfully logged on! ");
        logged = true;
    }

    private void onLoggedOff(LoggedOffCallback callback) {
        logger.info("Logged off of Steam: " + callback.getResult());
        logged = false;
    }

    private void onMachineAuth(UpdateMachineAuthCallback callback) {
        File sentry = new File(config.getSentry());
        try (FileOutputStream fos = new FileOutputStream(sentry)) {
            FileChannel channel = fos.getChannel();
            channel.position(callback.getOffset());
            channel.write(ByteBuffer.wrap(callback.getData(), 0, callback.getBytesToWrite()));

            OTPDetails otpDetails = new OTPDetails();
            otpDetails.setIdentifier(callback.getOneTimePassword().getIdentifier());
            otpDetails.setType(callback.getOneTimePassword().getType());

            MachineAuthDetails details = new MachineAuthDetails();
            details.setJobID(callback.getJobID());
            details.setFileName(callback.getFileName());
            details.setBytesWritten(callback.getBytesToWrite());
            details.setFileSize((int) sentry.length());
            details.setOffset(callback.getOffset());
            details.seteResult(EResult.OK);
            details.setLastError(0);
            details.setOneTimePassword(otpDetails);
            details.setSentryFileHash(calculateSHA1(sentry));

            steamUser.sendMachineAuthResponse(details);
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private void onAccountInfo(AccountInfoCallback callback) {
        steamFriends.setPersonaState(EPersonaState.Online);
        if (config.getPersonaName() != null && !config.getPersonaName().equals(callback.getPersonaName())) {
            steamFriends.setPersonaName(config.getPersonaName());
        }
    }

    private void onFriendList(FriendsListCallback callback) {
        for (Friend friend : callback.getFriendList()) {
            if (friend.getRelationship() == EFriendRelationship.RequestRecipient) {
                steamFriends.addFriend(friend.getSteamID());
            }
        }
    }

    private void onLoginKey(LoginKeyCallback callback) {
        try (FileWriter fw = new FileWriter(config.getLoginKey())) {
            fw.write(callback.getLoginKey());
            steamUser.acceptNewLoginKey(callback);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] calculateSHA1(File file) throws NoSuchAlgorithmException, IOException {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        try (InputStream fis = new FileInputStream(file)) {
            int n = 0;
            byte[] buffer = new byte[8192];
            while (n != -1) {
                n = fis.read(buffer);
                if (n > 0) {
                    digest.update(buffer, 0, n);
                }
            }
            return digest.digest();
        }
    }

    @Override
    public void connect() {
        super.connect();
        running = true;
        if (managerLoop == null) {
            managerLoop = CompletableFuture.runAsync(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            manager.runWaitCallbacks(1000L);
                        } catch (Exception e) {
                            logger.error("!!managerLoop: ", e);
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }

    @Override
    public void disconnect() {
        running = false;
        super.disconnect();
    }

    @Override
    public void postCallback(CallbackMsg msg) {
        if (msg.getJobID().getValue() > 0) {
            postCallback(msg, JOB_ID_PREFIX + msg.getJobID().getValue());
        } else {
            super.postCallback(msg);
        }
    }

    public void postCallback(CallbackMsg msg, String key) {
        submitResponse(key, msg);
        super.postCallback(msg);
    }

    public JobID sendJob(IClientMsg msg) {
        JobID jobID = getNextJobID();
        msg.setSourceJobID(jobID);
        send(msg);
        return jobID;
    }

    public <T> T sendJobAndWait(IClientMsg msg, long timeout) {
        sendJob(msg);
        return registerAndWait(JOB_ID_PREFIX + msg.getSourceJobID().getValue(), timeout);
    }

    public void submitResponse(String key, Object result) {
        logger.info(">>submitResponse: " + key);
        if (this.subscribers.get(key) != null)
            this.subscribers.get(key).complete(result);
    }

    @SuppressWarnings("unchecked")
    public <T> T registerAndWait(String key, Long timeout) {
        try {
            CompletableFuture<Object> future = new CompletableFuture<>();
            subscribers.put(key, future);
            return (T) future.get(timeout, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public CallbackManager getManager() {
        return manager;
    }

    public Map<String, CompletableFuture<Object>> getSubscribers() {
        return subscribers;
    }

    public SteamClientConfig getConfig() {
        return config;
    }

    public AppConfig getAppConfig() {
        return appConfig;
    }

    public void setAppConfig(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

}