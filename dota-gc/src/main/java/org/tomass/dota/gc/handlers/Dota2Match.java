package org.tomass.dota.gc.handlers;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tomass.dota.gc.clients.Dota2Client;
import org.tomass.dota.gc.handlers.callbacks.match.DatagramTicketCallback;
import org.tomass.dota.gc.handlers.callbacks.match.MatchHistoryCallback;
import org.tomass.dota.gc.handlers.callbacks.match.MatchMakingStatsCallback;
import org.tomass.dota.gc.handlers.callbacks.match.MatchesMinimalCallback;
import org.tomass.dota.gc.handlers.callbacks.match.RequestMatchesCallback;
import org.tomass.dota.gc.handlers.callbacks.match.TopSourceTvGamesCallback;
import org.tomass.dota.gc.handlers.callbacks.match.WatchGameCallback;
import org.tomass.protobuf.dota.DotaGcmessagesClient.CMsgDOTAGetPlayerMatchHistoryResponse;
import org.tomass.protobuf.dota.DotaGcmessagesClient.CMsgDOTAMatchmakingStatsResponse;
import org.tomass.protobuf.dota.DotaGcmessagesClient.CMsgDOTARequestMatchesResponse;
import org.tomass.protobuf.dota.DotaGcmessagesClientMatchManagement.CMsgClientToGCRequestSteamDatagramTicketResponse;
import org.tomass.protobuf.dota.DotaGcmessagesClientWatch.CMsgClientToGCFindTopSourceTVGames;
import org.tomass.protobuf.dota.DotaGcmessagesClientWatch.CMsgGCToClientFindTopSourceTVGamesResponse;
import org.tomass.protobuf.dota.DotaGcmessagesClientWatch.CMsgWatchGame;
import org.tomass.protobuf.dota.DotaGcmessagesClientWatch.CMsgWatchGameResponse;
import org.tomass.protobuf.dota.DotaGcmessagesCommon.CMsgDOTAMatchMinimal;
import org.tomass.protobuf.dota.DotaGcmessagesMsgid.EDOTAGCMsg;

import in.dragonbra.javasteam.base.ClientGCMsgProtobuf;
import in.dragonbra.javasteam.base.IPacketGCMsg;
import in.dragonbra.javasteam.util.compat.Consumer;

public class Dota2Match extends Dota2ClientGCMsgHandlerImpl {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Map<Integer, Consumer<IPacketGCMsg>> dispatchMap;

    public Dota2Match(Dota2Client client) {
        super(client);
        dispatchMap = new HashMap<>();
        dispatchMap.put(EDOTAGCMsg.k_EMsgGCMatchmakingStatsResponse_VALUE, packetMsg -> handleMmstats(packetMsg));
        dispatchMap.put(EDOTAGCMsg.k_EMsgGCToClientFindTopSourceTVGamesResponse_VALUE,
                packetMsg -> handleTopSourceTv(packetMsg));
        dispatchMap.put(EDOTAGCMsg.k_EMsgDOTAGetPlayerMatchHistoryResponse_VALUE,
                packetMsg -> handlePlayerMatchHistory(packetMsg));
        dispatchMap.put(EDOTAGCMsg.k_EMsgGCRequestMatches_VALUE, packetMsg -> handleMatches(packetMsg));
        dispatchMap.put(EDOTAGCMsg.k_EMsgClientToGCMatchesMinimalResponse_VALUE,
                packetMsg -> handleMatchesMinimal(packetMsg));
        dispatchMap.put(EDOTAGCMsg.k_EMsgGCWatchGameResponse_VALUE, packetMsg -> handleWatchGame(packetMsg));
        dispatchMap.put(EDOTAGCMsg.k_EMsgGCToClientSteamDatagramTicket_VALUE,
                packetMsg -> handleSteamDatagramTicket(packetMsg));

    }

    private void handleMmstats(IPacketGCMsg msg) {
        ClientGCMsgProtobuf<CMsgDOTAMatchmakingStatsResponse.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgDOTAMatchmakingStatsResponse.class, msg);
        logger.trace(">>handleMmstats for the client " + client + ": " + protobuf.getBody());
        client.postCallback(new MatchMakingStatsCallback(protobuf.getBody()));

    }

    private void handleTopSourceTv(IPacketGCMsg msg) {
        ClientGCMsgProtobuf<CMsgGCToClientFindTopSourceTVGamesResponse.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgGCToClientFindTopSourceTVGamesResponse.class, msg);
        logger.trace(">>handleTopSourceTv for the client " + client + ": " + protobuf.getBody());
        client.postCallback(new TopSourceTvGamesCallback(protobuf.getBody()));
    }

    private void handlePlayerMatchHistory(IPacketGCMsg msg) {
        ClientGCMsgProtobuf<CMsgDOTAGetPlayerMatchHistoryResponse.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgDOTAGetPlayerMatchHistoryResponse.class, msg);
        logger.trace(">>handlePlayerMatchHistory for the client " + client + ": " + protobuf.getBody());
        client.postCallback(new MatchHistoryCallback(protobuf.getBody()));
    }

    private void handleMatches(IPacketGCMsg data) {
        ClientGCMsgProtobuf<CMsgDOTARequestMatchesResponse.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgDOTARequestMatchesResponse.class, data);
        logger.trace(">>handleMatches for the client " + client + ": " + protobuf.getBody());
        client.postCallback(new RequestMatchesCallback(protobuf.getBody()));
    }

    private void handleMatchesMinimal(IPacketGCMsg data) {
        ClientGCMsgProtobuf<CMsgDOTAMatchMinimal.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgDOTAMatchMinimal.class, data);
        logger.trace(">>handleMatchesMinimal for the client " + client + ": " + protobuf.getBody());
        client.postCallback(new MatchesMinimalCallback(protobuf.getBody()));
    }

    private void handleWatchGame(IPacketGCMsg msg) {
        ClientGCMsgProtobuf<CMsgWatchGameResponse.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgWatchGameResponse.class, msg);
        logger.trace(">>handleWatchGame for the client " + client + ": " + protobuf.getBody());
        client.postCallback(new WatchGameCallback(protobuf.getBody()));
    }

    private void handleSteamDatagramTicket(IPacketGCMsg msg) {
        ClientGCMsgProtobuf<CMsgClientToGCRequestSteamDatagramTicketResponse.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgClientToGCRequestSteamDatagramTicketResponse.class, msg);
        logger.trace(">>handleSteamDatagramTicket for the client " + client + ": " + protobuf.getBody());
        client.postCallback(new DatagramTicketCallback(protobuf.getBody()));
    }

    // actions
    public void requestTopSourceTvGames() {
        ClientGCMsgProtobuf<CMsgClientToGCFindTopSourceTVGames.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgClientToGCFindTopSourceTVGames.class, EDOTAGCMsg.k_EMsgClientToGCFindTopSourceTVGames_VALUE);
        logger.trace(">>requestTopSourceTvGames for the client " + client + ": " + protobuf.getBody());
        send(protobuf);
    }

    public void requestWatchGame(Long lobbyId) {
        ClientGCMsgProtobuf<CMsgWatchGame.Builder> protobuf = new ClientGCMsgProtobuf<>(CMsgWatchGame.class,
                EDOTAGCMsg.k_EMsgGCWatchGame_VALUE);
        protobuf.getBody().setLobbyId(lobbyId);
        protobuf.getBody().setServerSteamid(client.getGameCoordinator().getClient().getSteamID().convertToUInt64());
        protobuf.getBody()
                .setWatchServerSteamid(client.getGameCoordinator().getClient().getSteamID().convertToUInt64());
        logger.trace(">>requestWatchGame for the client " + client + ": " + protobuf.getBody());
        send(protobuf);
    }

    @Override
    public void handleGCMsg(IPacketGCMsg packetGCMsg) {
        Consumer<IPacketGCMsg> dispatcher = dispatchMap.get(packetGCMsg.getMsgType());
        if (dispatcher != null) {
            logger.info(">>handleGCMsg for the client " + client + " match msg: " + packetGCMsg.getMsgType());
            dispatcher.accept(packetGCMsg);
        }
    }

}
