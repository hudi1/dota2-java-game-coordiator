package org.tomass.dota.steam.handlers;

import java.util.HashMap;
import java.util.Map;

import org.tomass.dota.gc.clients.Dota2Client;

import in.dragonbra.javasteam.base.ClientMsgProtobuf;
import in.dragonbra.javasteam.base.IPacketMsg;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.handlers.ClientMsgHandler;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverUfs.CMsgClientUFSGetUGCDetails;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverUfs.CMsgClientUFSGetUGCDetailsResponse;
import in.dragonbra.javasteam.steam.handlers.steamcloud.callback.UGCDetailsCallback;
import in.dragonbra.javasteam.types.JobID;
import in.dragonbra.javasteam.types.UGCHandle;
import in.dragonbra.javasteam.util.compat.Consumer;

public class SteamCloud extends ClientMsgHandler {

    private Map<EMsg, Consumer<IPacketMsg>> dispatchMap;

    public SteamCloud() {
        dispatchMap = new HashMap<>();
        dispatchMap.put(EMsg.ClientUFSGetUGCDetailsResponse, new Consumer<IPacketMsg>() {
            @Override
            public void accept(IPacketMsg packetMsg) {
                handleUGCDetailsResponse(packetMsg);
            }
        });
    }

    /**
     * Requests details for a specific item of user generated content from the Steam servers. Results are returned in a
     * {@link UGCDetailsCallback}.
     *
     * @param ugcId
     *            The unique user generated content id.
     * @return The Job ID of the request. This can be used to find the appropriate {@link UGCDetailsCallback}.
     */
    public UGCDetailsCallback requestUGCDetails(UGCHandle ugcId) {
        if (ugcId == null) {
            throw new IllegalArgumentException("ugcId is null");
        }

        ClientMsgProtobuf<CMsgClientUFSGetUGCDetails.Builder> protobuf = new ClientMsgProtobuf<>(
                CMsgClientUFSGetUGCDetails.class, EMsg.ClientUFSGetUGCDetails);
        JobID jobID = client.getNextJobID();
        protobuf.setSourceJobID(jobID);

        protobuf.getBody().setHcontent(ugcId.getValue());
        return ((Dota2Client) client).sendJobAndWait(protobuf, 10);
    }

    @Override
    public void handleMsg(IPacketMsg packetMsg) {
        if (packetMsg == null) {
            throw new IllegalArgumentException("packetMsg is null");
        }

        Consumer<IPacketMsg> dispatcher = dispatchMap.get(packetMsg.getMsgType());
        if (dispatcher != null) {
            dispatcher.accept(packetMsg);
        }
    }

    private void handleUGCDetailsResponse(IPacketMsg packetMsg) {
        ClientMsgProtobuf<CMsgClientUFSGetUGCDetailsResponse.Builder> infoResponse = new ClientMsgProtobuf<>(
                CMsgClientUFSGetUGCDetailsResponse.class, packetMsg);

        client.postCallback(new UGCDetailsCallback(infoResponse.getTargetJobID(), infoResponse.getBody()));
    }

}
