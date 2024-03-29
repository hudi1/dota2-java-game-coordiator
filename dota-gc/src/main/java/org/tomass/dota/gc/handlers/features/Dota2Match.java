package org.tomass.dota.gc.handlers.features;

import java.util.HashMap;
import java.util.Map;

import org.tomass.dota.gc.handlers.Dota2ClientGCMsgHandler;
import org.tomass.dota.gc.handlers.callbacks.match.DatagramTicketCallback;
import org.tomass.dota.gc.handlers.callbacks.match.MatchDetailsCallback;
import org.tomass.dota.gc.handlers.callbacks.match.MatchHistoryCallback;
import org.tomass.dota.gc.handlers.callbacks.match.MatchMakingStatsCallback;
import org.tomass.dota.gc.handlers.callbacks.match.MatchSignedOutCallback;
import org.tomass.dota.gc.handlers.callbacks.match.MatchesMinimalCallback;
import org.tomass.dota.gc.handlers.callbacks.match.RequestMatchesCallback;
import org.tomass.dota.gc.handlers.callbacks.match.TopSourceTvGamesCallback;
import org.tomass.dota.gc.handlers.callbacks.match.WatchGameCallback;
import org.tomass.protobuf.dota.DotaGcmessagesClient.CMsgDOTAGetPlayerMatchHistory;
import org.tomass.protobuf.dota.DotaGcmessagesClient.CMsgDOTAGetPlayerMatchHistoryResponse;
import org.tomass.protobuf.dota.DotaGcmessagesClient.CMsgDOTAMatchmakingStatsRequest;
import org.tomass.protobuf.dota.DotaGcmessagesClient.CMsgDOTAMatchmakingStatsResponse;
import org.tomass.protobuf.dota.DotaGcmessagesClient.CMsgDOTARequestMatchesResponse;
import org.tomass.protobuf.dota.DotaGcmessagesClient.CMsgGCMatchDetailsRequest;
import org.tomass.protobuf.dota.DotaGcmessagesClient.CMsgGCMatchDetailsResponse;
import org.tomass.protobuf.dota.DotaGcmessagesClient.CMsgGCToClientMatchSignedOut;
import org.tomass.protobuf.dota.DotaGcmessagesClientMatchManagement.CMsgClientToGCRequestSteamDatagramTicketResponse;
import org.tomass.protobuf.dota.DotaGcmessagesClientWatch.CMsgClientToGCFindTopSourceTVGames;
import org.tomass.protobuf.dota.DotaGcmessagesClientWatch.CMsgClientToGCTopFriendMatchesRequest;
import org.tomass.protobuf.dota.DotaGcmessagesClientWatch.CMsgGCToClientFindTopSourceTVGamesResponse;
import org.tomass.protobuf.dota.DotaGcmessagesClientWatch.CMsgGCToClientTopFriendMatchesResponse;
import org.tomass.protobuf.dota.DotaGcmessagesClientWatch.CMsgWatchGame;
import org.tomass.protobuf.dota.DotaGcmessagesClientWatch.CMsgWatchGameResponse;
import org.tomass.protobuf.dota.DotaGcmessagesCommon.CMsgDOTAMatchMinimal;
import org.tomass.protobuf.dota.DotaGcmessagesMsgid.EDOTAGCMsg;

import in.dragonbra.javasteam.base.ClientGCMsgProtobuf;
import in.dragonbra.javasteam.base.IPacketGCMsg;
import in.dragonbra.javasteam.util.compat.Consumer;

public class Dota2Match extends Dota2ClientGCMsgHandler {

    private Map<Integer, Consumer<IPacketGCMsg>> dispatchMap;

    public Dota2Match() {
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
        dispatchMap.put(EDOTAGCMsg.k_EMsgGCToClientTopFriendMatchesResponse_VALUE,
                packetMsg -> handleTopFriendMatches(packetMsg));
        dispatchMap.put(EDOTAGCMsg.k_EMsgGCMatchmakingStatsResponse_VALUE,
                packetMsg -> handleMatchMakingStats(packetMsg));
        dispatchMap.put(EDOTAGCMsg.k_EMsgGCMatchDetailsResponse_VALUE, packetMsg -> handleMatchDetails(packetMsg));
        dispatchMap.put(EDOTAGCMsg.k_EMsgGCToClientMatchSignedOut_VALUE, packetMsg -> handleMatchSignedOut(packetMsg));
    }

    private void handleMmstats(IPacketGCMsg msg) {
        ClientGCMsgProtobuf<CMsgDOTAMatchmakingStatsResponse.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgDOTAMatchmakingStatsResponse.class, msg);
        getLogger().trace(">>handleMmstats: " + protobuf.getBody());
        client.postCallback(new MatchMakingStatsCallback(protobuf.getBody()));
    }

    private void handleTopSourceTv(IPacketGCMsg msg) {
        ClientGCMsgProtobuf<CMsgGCToClientFindTopSourceTVGamesResponse.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgGCToClientFindTopSourceTVGamesResponse.class, msg);
        getLogger().trace(">>handleTopSourceTv: " + protobuf.getBody());
        TopSourceTvGamesCallback callback = new TopSourceTvGamesCallback(protobuf.getBody());
        if (protobuf.getBody().getSpecificGames()) {
            client.postCallback(msg.getMsgType(), callback);
        } else {
            client.postCallback(callback);
        }
    }

    private void handlePlayerMatchHistory(IPacketGCMsg msg) {
        ClientGCMsgProtobuf<CMsgDOTAGetPlayerMatchHistoryResponse.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgDOTAGetPlayerMatchHistoryResponse.class, msg);
        getLogger().trace(">>handlePlayerMatchHistory: " + protobuf.getBody());
        client.postCallback(new MatchHistoryCallback(protobuf.getTargetJobID(), protobuf.getBody()));
    }

    private void handleMatches(IPacketGCMsg data) {
        ClientGCMsgProtobuf<CMsgDOTARequestMatchesResponse.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgDOTARequestMatchesResponse.class, data);
        getLogger().trace(">>handleMatches: " + protobuf.getBody());
        client.postCallback(new RequestMatchesCallback(protobuf.getBody()));
    }

    private void handleMatchesMinimal(IPacketGCMsg data) {
        ClientGCMsgProtobuf<CMsgDOTAMatchMinimal.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgDOTAMatchMinimal.class, data);
        getLogger().trace(">>handleMatchesMinimal: " + protobuf.getBody());
        client.postCallback(new MatchesMinimalCallback(protobuf.getBody()));
    }

    private void handleWatchGame(IPacketGCMsg msg) {
        ClientGCMsgProtobuf<CMsgWatchGameResponse.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgWatchGameResponse.class, msg);
        getLogger().trace(">>handleWatchGame: " + protobuf.getBody());
        client.postCallback(new WatchGameCallback(protobuf.getBody()));
    }

    private void handleSteamDatagramTicket(IPacketGCMsg msg) {
        ClientGCMsgProtobuf<CMsgClientToGCRequestSteamDatagramTicketResponse.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgClientToGCRequestSteamDatagramTicketResponse.class, msg);
        getLogger().trace(">>handleSteamDatagramTicket: " + protobuf.getBody());
        client.postCallback(new DatagramTicketCallback(protobuf.getBody()));
    }

    private void handleTopFriendMatches(IPacketGCMsg msg) {
        ClientGCMsgProtobuf<CMsgGCToClientTopFriendMatchesResponse.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgGCToClientTopFriendMatchesResponse.class, msg);
        getLogger().trace(">>handleTopFriendMatches: " + protobuf.getBody());
    }

    private void handleMatchMakingStats(IPacketGCMsg msg) {
        ClientGCMsgProtobuf<CMsgDOTAMatchmakingStatsResponse.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgDOTAMatchmakingStatsResponse.class, msg);
        getLogger().trace(">>handleMatchMakingStats: " + protobuf.getBody());
    }

    private void handleMatchDetails(IPacketGCMsg msg) {
        ClientGCMsgProtobuf<CMsgGCMatchDetailsResponse.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgGCMatchDetailsResponse.class, msg);
        getLogger().trace(">>handleMatchDetails: " + protobuf.getBody());
        client.postCallback(new MatchDetailsCallback(protobuf.getTargetJobID(), protobuf.getBody()));
    }

    private void handleMatchSignedOut(IPacketGCMsg msg) {
        ClientGCMsgProtobuf<CMsgGCToClientMatchSignedOut.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgGCToClientMatchSignedOut.class, msg);
        getLogger().trace(">>handleMatchSignedOut: " + protobuf.getBody());
        client.postCallback(new MatchSignedOutCallback(protobuf.getBody()));
    }

    // actions

    public TopSourceTvGamesCallback requestTopSourceTvGames(long lobbyId) {
        ClientGCMsgProtobuf<CMsgClientToGCFindTopSourceTVGames.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgClientToGCFindTopSourceTVGames.class, EDOTAGCMsg.k_EMsgClientToGCFindTopSourceTVGames_VALUE);
        getLogger().trace(">>requestTopSourceTvGames: " + protobuf.getBody());
        protobuf.getBody().addLobbyIds(lobbyId);
        return sendCustomAndWait(protobuf, EDOTAGCMsg.k_EMsgGCToClientFindTopSourceTVGamesResponse_VALUE);
    }

    public void requestTopFriendMatches() {
        ClientGCMsgProtobuf<CMsgClientToGCTopFriendMatchesRequest.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgClientToGCTopFriendMatchesRequest.class, EDOTAGCMsg.k_EMsgClientToGCTopFriendMatchesRequest_VALUE);
        getLogger().trace(">>requestTopSourceTvGames: " + protobuf.getBody());
        send(protobuf);
    }

    public void requestWatchGame(Long lobbyId) {
        ClientGCMsgProtobuf<CMsgWatchGame.Builder> protobuf = new ClientGCMsgProtobuf<>(CMsgWatchGame.class,
                EDOTAGCMsg.k_EMsgGCWatchGame_VALUE);
        protobuf.getBody().setLobbyId(lobbyId);
        protobuf.getBody().setServerSteamid(client.getGameCoordinator().getClient().getSteamID().convertToUInt64());
        protobuf.getBody()
                .setWatchServerSteamid(client.getGameCoordinator().getClient().getSteamID().convertToUInt64());
        getLogger().trace(">>requestWatchGame: " + protobuf.getBody());
        send(protobuf);
    }

    public void requestMatchMakingStat() {
        ClientGCMsgProtobuf<CMsgDOTAMatchmakingStatsRequest.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgDOTAMatchmakingStatsRequest.class, EDOTAGCMsg.k_EMsgGCMatchmakingStatsRequest_VALUE);
        getLogger().trace(">>requestMatchMakingStat: " + protobuf.getBody());
        send(protobuf);
    }

    public MatchDetailsCallback requestMatchDetails(Long matchId) {
        ClientGCMsgProtobuf<CMsgGCMatchDetailsRequest.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgGCMatchDetailsRequest.class, EDOTAGCMsg.k_EMsgGCMatchDetailsRequest_VALUE);
        protobuf.getBody().setMatchId(matchId);
        getLogger().trace(">>requestMatchDetails: " + protobuf.getBody());
        return sendJobAndWait(protobuf);
    }

    public MatchHistoryCallback requestPlayerMatch(Integer accountId, Long startMatchId) {
        ClientGCMsgProtobuf<CMsgDOTAGetPlayerMatchHistory.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgDOTAGetPlayerMatchHistoryResponse.class, EDOTAGCMsg.k_EMsgDOTAGetPlayerMatchHistory_VALUE);
        protobuf.getBody().setAccountId(accountId);
        if (startMatchId != null) {
            protobuf.getBody().setStartAtMatchId(startMatchId);
            protobuf.getBody().setMatchesRequested(10);
        } else {
            protobuf.getBody().setMatchesRequested(1); 
        }
        getLogger().trace(">>requestPlayerMatch: " + protobuf.getBody());
        return sendJobAndWait(protobuf);
    }

    @Override
    public void handleGCMsg(IPacketGCMsg packetGCMsg) {
        Consumer<IPacketGCMsg> dispatcher = dispatchMap.get(packetGCMsg.getMsgType());
        if (dispatcher != null) {
            getLogger().trace(">>handleGCMsg match msg: " + packetGCMsg.getMsgType());
            dispatcher.accept(packetGCMsg);
        }
    }

}
