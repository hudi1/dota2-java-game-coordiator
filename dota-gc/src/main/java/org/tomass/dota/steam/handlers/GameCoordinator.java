package org.tomass.dota.steam.handlers;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tomass.dota.gc.handlers.ClientGCMsgHandler;

import com.google.protobuf.ByteString;

import in.dragonbra.javasteam.base.ClientMsgProtobuf;
import in.dragonbra.javasteam.base.IClientGCMsg;
import in.dragonbra.javasteam.base.IPacketMsg;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.handlers.ClientMsgHandler;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgGCClient;
import in.dragonbra.javasteam.steam.handlers.steamgamecoordinator.callback.MessageCallback;
import in.dragonbra.javasteam.types.JobID;
import in.dragonbra.javasteam.util.MsgUtil;
import in.dragonbra.javasteam.util.compat.Consumer;

public class GameCoordinator extends ClientMsgHandler {

    private final Integer appId;

    private Map<EMsg, Consumer<IPacketMsg>> dispatchMap;

    private Map<Class<? extends ClientGCMsgHandler>, ClientGCMsgHandler> handlers = new HashMap<>();

    private Map<JobID, CompletableFuture<Object>> subscribers = new HashMap<>();

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public GameCoordinator(Integer appId) {
        this.appId = appId;
        dispatchMap = new HashMap<>();
        dispatchMap.put(EMsg.ClientFromGC, packetMsg -> handleFromGC(packetMsg));
    }

    @Override
    public void handleMsg(IPacketMsg packetMsg) {
        if (packetMsg == null) {
            throw new IllegalArgumentException("packetMsg is null");
        }

        logger.trace(">>handleMsg for the client " + client + " msg: " + packetMsg.getMsgType());
        Consumer<IPacketMsg> dispatcher = dispatchMap.get(packetMsg.getMsgType());
        if (dispatcher != null) {
            dispatcher.accept(packetMsg);
        }
    }

    private void handleFromGC(IPacketMsg packetMsg) {
        ClientMsgProtobuf<CMsgGCClient.Builder> msg = new ClientMsgProtobuf<>(CMsgGCClient.class, packetMsg);
        MessageCallback callback = new MessageCallback(msg.getBody());
        logger.info(">>handleGCMsg for the client " + client + " GC msg: " + callback.geteMsg());
        if (callback.getAppID() != this.appId) {
            return;
        }

        // TODO ASYNC
        if (callback.getMessage().getTargetJobID().getValue() > 0) {
            submitResponse(packetMsg.getTargetJobID(), callback.getMessage());
        }
        for (Map.Entry<Class<? extends ClientGCMsgHandler>, ClientGCMsgHandler> entry : handlers.entrySet()) {
            try {
                entry.getValue().handleGCMsg(callback.getMessage());
            } catch (Exception e) {
                logger.warn("Unhandled exception from " + entry.getKey().getName() + " handlers", e);
            }
        }
    }

    public void submitResponse(long responseId, Object result) {
        logger.info(">>submitResponse for the client " + client + " jobid: " + responseId);
        if (this.subscribers.get(new JobID(responseId)) != null)
            this.subscribers.get(new JobID(responseId)).complete(result);
    }

    /**
     * Adds a new handler to the internal list of message handlers.
     *
     * @param handler
     *            The handler to add.
     */
    public void addHandler(ClientGCMsgHandler handler) {
        if (handlers.containsKey(handler.getClass())) {
            throw new IllegalArgumentException("A handler of type " + handler.getClass() + " is already registered.");
        }
        handlers.put(handler.getClass(), handler);
    }

    /**
     * Removes a registered handler by name.
     *
     * @param handler
     *            The handler name to remove.
     */
    public void removeHandler(Class<? extends ClientGCMsgHandler> handler) {
        handlers.remove(handler);
    }

    /**
     * Removes a registered handler.
     *
     * @param handler
     *            The handler name to remove.
     */
    public void removeHandler(ClientGCMsgHandler handler) {
        removeHandler(handler.getClass());
    }

    public void send(IClientGCMsg msg) {
        if (msg == null) {
            throw new IllegalArgumentException("msg is null");
        }

        ClientMsgProtobuf<CMsgGCClient.Builder> clientMsg = new ClientMsgProtobuf<>(CMsgGCClient.class,
                EMsg.ClientToGC);

        clientMsg.getProtoHeader().setRoutingAppid(appId);
        clientMsg.getBody().setMsgtype(MsgUtil.makeGCMsg(msg.getMsgType(), msg.isProto()));
        clientMsg.getBody().setAppid(appId);
        clientMsg.getBody().setPayload(ByteString.copyFrom(msg.serialize()));
        client.send(clientMsg);
    }

    public Map<EMsg, Consumer<IPacketMsg>> getDispatchMap() {
        return dispatchMap;
    }

    public Map<JobID, CompletableFuture<Object>> getSubscribers() {
        return subscribers;
    }

}
