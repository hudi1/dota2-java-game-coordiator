package org.tomass.dota.gc.clients;

import java.util.HashMap;
import java.util.Map;

import org.tomass.dota.gc.config.AppConfig;
import org.tomass.dota.gc.config.SteamClientConfig;
import org.tomass.dota.gc.handlers.ClientGCMsgHandler;
import org.tomass.dota.gc.handlers.callbacks.ConnectionStatusCallback;
import org.tomass.dota.gc.handlers.callbacks.GCWelcomeCallback;
import org.tomass.dota.gc.handlers.callbacks.NotReadyCallback;
import org.tomass.dota.gc.handlers.callbacks.ReadyCallback;
import org.tomass.dota.gc.handlers.features.Dota2Chat;
import org.tomass.dota.gc.handlers.features.Dota2Lobby;
import org.tomass.dota.gc.handlers.features.Dota2Match;
import org.tomass.dota.gc.handlers.features.Dota2Party;
import org.tomass.dota.gc.handlers.features.Dota2Player;
import org.tomass.dota.gc.handlers.features.Dota2SharedObjects;
import org.tomass.dota.steam.handlers.Dota2SteamGameCoordinator;
import org.tomass.dota.steam.handlers.SteamUser;
import org.tomass.protobuf.dota.BaseGcmessages.CMsgGCError;
import org.tomass.protobuf.dota.BaseGcmessages.EGCBaseMsg;
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
import in.dragonbra.javasteam.base.IPacketGCMsg;
import in.dragonbra.javasteam.base.IPacketMsg;
import in.dragonbra.javasteam.base.PacketClientGCMsg;
import in.dragonbra.javasteam.base.PacketClientGCMsgProtobuf;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgClientPlayingSessionState;
import in.dragonbra.javasteam.steam.handlers.steamuser.callback.LoggedOnCallback;
import in.dragonbra.javasteam.util.MsgUtil;
import in.dragonbra.javasteam.util.compat.Consumer;
import in.dragonbra.javasteam.util.event.ScheduledFunction;

public class Dota2Client extends CommonSteamClient implements ClientGCMsgHandler {

    private boolean ready = false;

    private GCConnectionStatus connectionStatus = GCConnectionStatus.GCConnectionStatus_NO_SESSION;

    public static final Integer APP_ID = 570;

    protected Dota2SteamGameCoordinator gameCoordinator;

    private SteamUser user;

    private Map<Integer, Consumer<IPacketGCMsg>> dispatchMap;

    private ScheduledFunction welcomeFunc;

    protected Dota2Lobby lobbyHandler;

    protected Dota2Party partyHandler;

    protected Dota2Chat chatHandler;

    protected Dota2Match matchHandler;

    protected Dota2Player playerHandler;

    protected Dota2SharedObjects sharedObjectsHandler;

    public Dota2Client(SteamClientConfig config, AppConfig appConfig) {
        super(config, appConfig);
        dispatchMap = new HashMap<>();
        dispatchMap.put(EGCBaseClientMsg.k_EMsgGCClientWelcome_VALUE, packetMsg -> handleWelcome(packetMsg));
        dispatchMap.put(EGCBaseClientMsg.k_EMsgGCClientConnectionStatus_VALUE, packetMsg -> handleStatus(packetMsg));
        dispatchMap.put(EGCBaseMsg.k_EMsgGCError_VALUE, packetMsg -> handleError(packetMsg));

        gameCoordinator.getDispatchMap().put(EMsg.ClientPlayingSessionState,
                packetMsg -> handlePlaySessState(packetMsg));
        welcomeFunc = new ScheduledFunction(new Runnable() {
            @Override
            public void run() {
                if (!ready)
                    sayHello();
            }
        }, 5000);
    }

    private void handleError(IPacketGCMsg msg) {
        ClientGCMsgProtobuf<CMsgGCError.Builder> error = new ClientGCMsgProtobuf<>(CMsgGCError.class, msg);
        logger.error("GC error " + error.getBody().getErrorText());
    }

    private void handleWelcome(IPacketGCMsg msg) {
        try {
            logger.info("welcome");
            ClientGCMsgProtobuf<CMsgClientWelcome.Builder> welcome = new ClientGCMsgProtobuf<>(CMsgClientWelcome.class,
                    msg);
            CMsgDOTAWelcome dotaWelcome = CMsgDOTAWelcome.parseFrom(welcome.getBody().getGameData());
            postCallback(new GCWelcomeCallback(welcome.getBody()));

            for (CExtraMsg extraMessage : dotaWelcome.getExtraMessagesList()) {
                processGcMessage(extraMessage.getId(), extraMessage.getContents());
            }
            setConnectionStatus(GCConnectionStatus.GCConnectionStatus_HAVE_SESSION);
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
        logger.info("handleDisconnect " + welcomeFunc);
        exit();
    }

    private void setConnectionStatus(GCConnectionStatus gcConnectionStatus) {
        GCConnectionStatus prevStatus = this.connectionStatus;
        this.connectionStatus = gcConnectionStatus;

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
        logger.info(ready ? "ready" : "not ready");
    }

    private void processGcMessage(int id, ByteString contents) {
        IPacketGCMsg packetGCMsg = getPacketGCMsg(id, contents.toByteArray());
        handleGCMsg(packetGCMsg);
    }

    private static IPacketGCMsg getPacketGCMsg(int eMsg, byte[] data) {
        int realEMsg = MsgUtil.getGCMsg(eMsg);

        if (MsgUtil.isProtoBuf(eMsg)) {
            return new PacketClientGCMsgProtobuf(realEMsg, data);
        } else {
            return new PacketClientGCMsg(realEMsg, data);
        }
    }

    public void launch() {
        if (!logged) {
            return;
        }
        logger.info("Launching GC");
        try {
            user.gamePlayed(APP_ID);
            welcomeFunc.start();
        } catch (Exception e) {
            logger.error("!!launch: ", e);
            e.printStackTrace();
        }
    }

    public void exit() {
        welcomeFunc.stop();
        user.getCurrentGamesPlayed().remove(APP_ID);
        setConnectionStatus(GCConnectionStatus.GCConnectionStatus_NO_SESSION);
    }

    private void sayHello() {
        ClientGCMsgProtobuf<CMsgClientHello.Builder> hello = new ClientGCMsgProtobuf<>(CMsgClientHello.class,
                EGCBaseClientMsg.k_EMsgGCClientHello_VALUE);
        hello.getBody().setClientSessionNeed(EDOTAGCSessionNeed.k_EDOTAGCSessionNeed_UserInUINeverConnected_VALUE);
        hello.getBody().setClientLauncher(PartnerAccountType.PARTNER_NONE);
        hello.getBody().setEngine(ESourceEngine.k_ESE_Source2);
        hello.getBody().setSecretKey("");
        gameCoordinator.send(hello);
    }

    @Override
    protected void init() {
        super.init();
        addHandler(gameCoordinator = new Dota2SteamGameCoordinator());
        addHandler(user = new SteamUser());
        gameCoordinator.addDota2Handler(sharedObjectsHandler = new Dota2SharedObjects());
        gameCoordinator.addDota2Handler(chatHandler = new Dota2Chat());
        gameCoordinator.addDota2Handler(matchHandler = new Dota2Match());
        gameCoordinator.addDota2Handler(partyHandler = new Dota2Party());
        gameCoordinator.addDota2Handler(lobbyHandler = new Dota2Lobby());
        gameCoordinator.addDota2Handler(playerHandler = new Dota2Player());
        gameCoordinator.addHandler(this);
    }

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

    public Dota2SteamGameCoordinator getGameCoordinator() {
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

    public Dota2Player getPlayerHandler() {
        return playerHandler;
    }

    public boolean isReady() {
        return ready;
    }

}
