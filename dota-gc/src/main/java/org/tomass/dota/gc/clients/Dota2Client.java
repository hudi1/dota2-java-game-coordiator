package org.tomass.dota.gc.clients;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.tomass.dota.gc.config.SteamClientConfig;
import org.tomass.dota.gc.handlers.ClientGCMsgHandler;
import org.tomass.dota.gc.handlers.Dota2Chat;
import org.tomass.dota.gc.handlers.Dota2Lobby;
import org.tomass.dota.gc.handlers.Dota2Match;
import org.tomass.dota.gc.handlers.Dota2Party;
import org.tomass.dota.gc.handlers.Dota2SharedObjects;
import org.tomass.dota.gc.handlers.callbacks.ConnectionStatusCallback;
import org.tomass.dota.gc.handlers.callbacks.NotReadyCallback;
import org.tomass.dota.gc.handlers.callbacks.ReadyCallback;
import org.tomass.dota.steam.handlers.GameCoordinator;
import org.tomass.dota.steam.handlers.SteamUser;
import org.tomass.protobuf.dota.DotaGcmessagesClient.CMsgDOTAWelcome;
import org.tomass.protobuf.dota.DotaGcmessagesClient.CMsgDOTAWelcome.CExtraMsg;
import org.tomass.protobuf.dota.DotaGcmessagesCommon.EDOTAGCSessionNeed;
import org.tomass.protobuf.dota.GcsdkGcmessages.CMsgClientHello;
import org.tomass.protobuf.dota.GcsdkGcmessages.CMsgClientWelcome;
import org.tomass.protobuf.dota.GcsdkGcmessages.CMsgConnectionStatus;
import org.tomass.protobuf.dota.GcsdkGcmessages.ESourceEngine;
import org.tomass.protobuf.dota.GcsdkGcmessages.GCConnectionStatus;
import org.tomass.protobuf.dota.GcsdkGcmessages.PartnerAccountType;
import org.tomass.protobuf.dota.Gcsystemmsgs.EGCBaseClientMsg;

import com.google.protobuf.ByteString;

import in.dragonbra.javasteam.base.ClientGCMsgProtobuf;
import in.dragonbra.javasteam.base.ClientMsgProtobuf;
import in.dragonbra.javasteam.base.IClientGCMsg;
import in.dragonbra.javasteam.base.IPacketGCMsg;
import in.dragonbra.javasteam.base.IPacketMsg;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgClientPlayingSessionState;
import in.dragonbra.javasteam.steam.handlers.steamuser.callback.LoggedOnCallback;
import in.dragonbra.javasteam.types.JobID;
import in.dragonbra.javasteam.util.compat.Consumer;

public class Dota2Client extends CommonSteamClient implements ClientGCMsgHandler {

    private boolean ready = false;

    private GCConnectionStatus connectionStatus = GCConnectionStatus.GCConnectionStatus_NO_SESSION;

    private static final Integer APP_ID = 570;

    protected GameCoordinator gameCoordinator;

    private SteamUser user;

    private Map<Integer, Consumer<IPacketGCMsg>> dispatchMap;

    private CompletableFuture<Void> retryWelcomeLoop;

    protected Dota2Lobby lobbyHandler;

    protected Dota2Party partyHandler;

    protected Dota2Chat chatHandler;

    protected Dota2Match matchHandler;

    protected Dota2SharedObjects sharedObjectsHandler;

    public Dota2Client(SteamClientConfig config) {
        super(config);
        dispatchMap = new HashMap<>();
        dispatchMap.put(EGCBaseClientMsg.k_EMsgGCClientWelcome_VALUE, packetMsg -> handleWelcome(packetMsg));
        dispatchMap.put(EGCBaseClientMsg.k_EMsgGCClientConnectionStatus_VALUE, packetMsg -> handleStatus(packetMsg));

        gameCoordinator.getDispatchMap().put(EMsg.ClientPlayingSessionState,
                packetMsg -> handlePlaySessState(packetMsg));
    }

    private void handleWelcome(IPacketGCMsg msg) {
        try {
            setConnectionStatus(GCConnectionStatus.GCConnectionStatus_HAVE_SESSION);
            ClientGCMsgProtobuf<CMsgClientWelcome.Builder> welcome = new ClientGCMsgProtobuf<>(CMsgClientWelcome.class,
                    msg);
            CMsgDOTAWelcome dotaWelcome = CMsgDOTAWelcome.parseFrom(welcome.getBody().getGameData());
            // client.postCallback(new GCWelcomeCallback(wel.getBody()));

            for (CExtraMsg extraMessage : dotaWelcome.getExtraMessagesList()) {
                processGcMessage(extraMessage.getId(), extraMessage.getContents());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleStatus(IPacketGCMsg msg) {
        ClientGCMsgProtobuf<CMsgConnectionStatus.Builder> status = new ClientGCMsgProtobuf<>(CMsgConnectionStatus.class,
                msg);
        setConnectionStatus(status.getBody().getStatus());
    }

    private void handlePlaySessState(IPacketMsg msg) {
        ClientMsgProtobuf<CMsgClientPlayingSessionState.Builder> sessionState = new ClientMsgProtobuf<>(
                CMsgClientPlayingSessionState.class, msg);

        if (ready && sessionState.getBody().getPlayingApp() != APP_ID)
            setConnectionStatus(GCConnectionStatus.GCConnectionStatus_NO_SESSION);
    }

    private void handleDisconnect() {
        logger.info("handleDisconnect " + retryWelcomeLoop);
        exit();
    }

    private void setConnectionStatus(GCConnectionStatus gcConnectionStatus) {
        GCConnectionStatus prevStatus = this.connectionStatus;
        this.connectionStatus = gcConnectionStatus;

        // client.postCallback(new GCWelcomeCallback(wel.getBody()));

        if (this.connectionStatus.equals(prevStatus)) {
            postCallback(new ConnectionStatusCallback(gcConnectionStatus));
        }

        if (gcConnectionStatus.equals(GCConnectionStatus.GCConnectionStatus_HAVE_SESSION) && !ready) {
            ready = true;
            postCallback(new ReadyCallback());
        } else if (!gcConnectionStatus.equals(GCConnectionStatus.GCConnectionStatus_HAVE_SESSION) && ready) {
            ready = false;
            postCallback(new NotReadyCallback());
        }
    }

    private void processGcMessage(int id, ByteString contents) {
        logger.info("TODO: ProcessGcMessage: " + id + " with contents: " + contents);
    }

    private void knockOnGc() {
        while (true) {
            if (!ready) {
                sayHello();
            }
            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void launch() {
        if (!logged) {
            return;
        }
        logger.info("Launching GC");
        try {
            if (retryWelcomeLoop == null && !user.getCurrentGamesPlayed().contains(APP_ID)) {
                user.gamesPlayed(Arrays.asList(APP_ID));
                retryWelcomeLoop = CompletableFuture.runAsync(() -> knockOnGc());
            }
        } catch (Exception e) {
            logger.error("!!launch: ", e);
            e.printStackTrace();
        }
    }

    public void exit() {
        if (retryWelcomeLoop != null) {
            retryWelcomeLoop.cancel(true);
            retryWelcomeLoop = null;
        }

        if (user.getCurrentGamesPlayed().contains(APP_ID)) {
            user.getCurrentGamesPlayed().remove(APP_ID);
            user.gamesPlayed(user.getCurrentGamesPlayed());
        }
        setConnectionStatus(GCConnectionStatus.GCConnectionStatus_NO_SESSION);
    }

    @Override
    public void send(IClientGCMsg msg) {
        gameCoordinator.send(msg);
    }

    @Override
    public JobID sendJob(IClientGCMsg msg) {
        JobID jobID = getNextJobID();
        msg.setSourceJobID(jobID);
        send(msg);
        return jobID;
    }

    @Override
    public Object sendJobAndWait(IClientGCMsg msg, long timeout) {
        sendJob(msg);
        return registerAndWait(msg.getSourceJobID(), timeout);
    }

    private void sayHello() {
        ClientGCMsgProtobuf<CMsgClientHello.Builder> hello = new ClientGCMsgProtobuf<>(CMsgClientHello.class,
                EGCBaseClientMsg.k_EMsgGCClientHello_VALUE);
        hello.getBody().setClientSessionNeed(EDOTAGCSessionNeed.k_EDOTAGCSessionNeed_UserInUINeverConnected_VALUE);
        hello.getBody().setClientLauncher(PartnerAccountType.PARTNER_NONE);
        hello.getBody().setEngine(ESourceEngine.k_ESE_Source2);
        hello.getBody().setSecretKey("");
        send(hello);
    }

    @Override
    protected void init() {
        super.init();
        addHandler(gameCoordinator = new GameCoordinator(APP_ID));
        addHandler(user = new SteamUser());
        gameCoordinator.addHandler(this);
        gameCoordinator.addHandler(sharedObjectsHandler = new Dota2SharedObjects(this));
        gameCoordinator.addHandler(chatHandler = new Dota2Chat(this));
        gameCoordinator.addHandler(matchHandler = new Dota2Match(this));
        gameCoordinator.addHandler(partyHandler = new Dota2Party(this));
        gameCoordinator.addHandler(lobbyHandler = new Dota2Lobby(this));
    }

    @Override
    public void handleGCMsg(IPacketGCMsg packetGCMsg) {
        Consumer<IPacketGCMsg> dispatcher = dispatchMap.get(packetGCMsg.getMsgType());
        if (dispatcher != null) {
            dispatcher.accept(packetGCMsg);
        }
    }

    @Override
    protected void onClientDisconnected(boolean userInitiated) {
        handleDisconnect();
        super.onClientDisconnected(userInitiated);
    }

    @Override
    protected void onLoggedOn(LoggedOnCallback callback) {
        super.onLoggedOn(callback);
        launch();
    }

    public GameCoordinator getGameCoordinator() {
        return gameCoordinator;
    }

    public Dota2Lobby getLobbyHandler() {
        return lobbyHandler;
    }

    public Dota2Party getPartyHandler() {
        return partyHandler;
    }

    public Dota2Chat getChatHandler() {
        return chatHandler;
    }

    public Dota2Match getMatchHandler() {
        return matchHandler;
    }

    public Object registerAndWait(JobID responseId, long timeout) {
        try {
            CompletableFuture<Object> future = new CompletableFuture<>();
            gameCoordinator.getSubscribers().put(responseId, future);
            return future.get(timeout, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
