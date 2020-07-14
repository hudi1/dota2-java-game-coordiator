package org.tomass.dota.gc.handlers.features;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tomass.dota.gc.handlers.Dota2ClientGCMsgHandler;
import org.tomass.dota.gc.handlers.callbacks.league.LeagueAvailableLobbyNodes;
import org.tomass.dota.gc.handlers.callbacks.league.LeagueInfoAdmin;
import org.tomass.dota.gc.handlers.callbacks.league.LeagueNodeCallback;
import org.tomass.dota.steam.handlers.Dota2SteamGameCoordinator;
import org.tomass.protobuf.dota.DotaGcmessagesCommonLeague.CMsgDOTALeagueAvailableLobbyNodes;
import org.tomass.protobuf.dota.DotaGcmessagesCommonLeague.CMsgDOTALeagueAvailableLobbyNodesRequest;
import org.tomass.protobuf.dota.DotaGcmessagesCommonLeague.CMsgDOTALeagueInfoList;
import org.tomass.protobuf.dota.DotaGcmessagesCommonLeague.CMsgDOTALeagueInfoListAdminsRequest;
import org.tomass.protobuf.dota.DotaGcmessagesCommonLeague.CMsgDOTALeagueNodeRequest;
import org.tomass.protobuf.dota.DotaGcmessagesCommonLeague.CMsgDOTALeagueNodeResponse;
import org.tomass.protobuf.dota.DotaGcmessagesMsgid.EDOTAGCMsg;

import in.dragonbra.javasteam.base.ClientGCMsgProtobuf;
import in.dragonbra.javasteam.base.IPacketGCMsg;
import in.dragonbra.javasteam.util.compat.Consumer;

public class Dota2League extends Dota2ClientGCMsgHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Map<Integer, Consumer<IPacketGCMsg>> dispatchMap;

    public Dota2League() {
        dispatchMap = new HashMap<>();
        dispatchMap.put(EDOTAGCMsg.k_EMsgDOTALeagueInfoListAdminsReponse_VALUE,
                packetMsg -> handleLeagueInfoAdmins(packetMsg));
        dispatchMap.put(EDOTAGCMsg.k_EMsgDOTALeagueAvailableLobbyNodes_VALUE,
                packetMsg -> handleLeagueAvaiableNodes(packetMsg));
        dispatchMap.put(EDOTAGCMsg.k_EMsgDOTALeagueNodeResponse_VALUE, packetMsg -> handleLeagueNode(packetMsg));
    }

    private void handleLeagueInfoAdmins(IPacketGCMsg data) {
        ClientGCMsgProtobuf<CMsgDOTALeagueInfoList.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgDOTALeagueInfoList.class, data);
        logger.trace(">>handleLeagueInfoAdmins: " + protobuf.getBody());
        client.postCallback(new LeagueInfoAdmin(data.getTargetJobID(), protobuf.getBody()));
    }

    private void handleLeagueAvaiableNodes(IPacketGCMsg data) {
        ClientGCMsgProtobuf<CMsgDOTALeagueAvailableLobbyNodes.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgDOTALeagueAvailableLobbyNodes.class, data);
        logger.trace(">>handleLeagueAvaiableNodes: " + protobuf.getBody());
        client.postCallback(new LeagueAvailableLobbyNodes(data.getTargetJobID(), protobuf.getBody()));
    }

    private void handleLeagueNode(IPacketGCMsg data) {
        ClientGCMsgProtobuf<CMsgDOTALeagueNodeResponse.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgDOTALeagueNodeResponse.class, data);
        logger.trace(">>handleLeagueNodeRequest: " + protobuf.getBody());
        client.postCallback(new LeagueNodeCallback(data.getTargetJobID(), protobuf.getBody()));
    }

    // actions

    public LeagueInfoAdmin requestLeagueInfoAdmins() {
        ClientGCMsgProtobuf<CMsgDOTALeagueInfoListAdminsRequest.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgDOTALeagueInfoListAdminsRequest.class, EDOTAGCMsg.k_EMsgDOTALeagueInfoListAdminsRequest_VALUE);
        logger.trace(">>requestLeagueInfoAdmins: " + protobuf.getBody());
        return sendJobAndWait(protobuf);
    }

    public LeagueAvailableLobbyNodes requestLeagueAvailableNodes(Integer leagueId) {
        ClientGCMsgProtobuf<CMsgDOTALeagueAvailableLobbyNodesRequest.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgDOTALeagueAvailableLobbyNodesRequest.class,
                EDOTAGCMsg.k_EMsgDOTALeagueAvailableLobbyNodesRequest_VALUE);
        protobuf.getBody().setLeagueId(leagueId);
        logger.trace(">>requestLeagueAvailableNodes: " + protobuf.getBody());
        return sendJobAndWait(protobuf);
    }

    public LeagueNodeCallback requestLeagueNode(Integer leagueId, Integer nodeId) {
        ClientGCMsgProtobuf<CMsgDOTALeagueNodeRequest.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgDOTALeagueNodeRequest.class, EDOTAGCMsg.k_EMsgDOTALeagueNodeRequest_VALUE);
        protobuf.getBody().setLeagueId(leagueId);
        protobuf.getBody().setNodeId(nodeId);
        logger.trace(">>requestLeagueNodeRequest: " + protobuf.getBody());
        return sendJobAndWait(protobuf);
    }

    @Override
    public void setup(Dota2SteamGameCoordinator gameCoordinator) {
        super.setup(gameCoordinator);

    }

    @Override
    public void handleGCMsg(IPacketGCMsg packetGCMsg) {
        Consumer<IPacketGCMsg> dispatcher = dispatchMap.get(packetGCMsg.getMsgType());
        if (dispatcher != null) {
            logger.trace(">>handleGCMsg team msg: " + packetGCMsg.getMsgType());
            dispatcher.accept(packetGCMsg);
        }
    }

}