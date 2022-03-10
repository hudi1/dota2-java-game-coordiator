package org.tomass.dota.gc.handlers.features;

import java.util.HashMap;
import java.util.Map;

import org.tomass.dota.gc.handlers.Dota2ClientGCMsgHandler;
import org.tomass.dota.gc.handlers.callbacks.player.ConductScorecardCallback;
import org.tomass.dota.gc.handlers.callbacks.player.HeroStandings;
import org.tomass.dota.gc.handlers.callbacks.player.PlayerInfoCallback;
import org.tomass.dota.gc.handlers.callbacks.player.PlayerStatsResponse;
import org.tomass.dota.gc.handlers.callbacks.player.ProfileCardResponse;
import org.tomass.dota.gc.handlers.callbacks.player.ProfileResponse;
import org.tomass.dota.steam.handlers.Dota2SteamGameCoordinator;
import org.tomass.protobuf.dota.DotaGcmessagesClient.CMsgClientToGCGetProfileCard;
import org.tomass.protobuf.dota.DotaGcmessagesClient.CMsgClientToGCPlayerStatsRequest;
import org.tomass.protobuf.dota.DotaGcmessagesClient.CMsgGCGetHeroStandings;
import org.tomass.protobuf.dota.DotaGcmessagesClient.CMsgGCGetHeroStandingsResponse;
import org.tomass.protobuf.dota.DotaGcmessagesClient.CMsgGCPlayerInfoRequest;
import org.tomass.protobuf.dota.DotaGcmessagesClient.CMsgGCToClientPlayerStatsResponse;
import org.tomass.protobuf.dota.DotaGcmessagesClient.CMsgPlayerConductScorecard;
import org.tomass.protobuf.dota.DotaGcmessagesClient.CMsgPlayerConductScorecardRequest;
import org.tomass.protobuf.dota.DotaGcmessagesClient.CMsgProfileRequest;
import org.tomass.protobuf.dota.DotaGcmessagesClient.CMsgProfileResponse;
import org.tomass.protobuf.dota.DotaGcmessagesClientFantasy.CMsgDOTAPlayerInfo;
import org.tomass.protobuf.dota.DotaGcmessagesCommon.CMsgDOTAProfileCard;
import org.tomass.protobuf.dota.DotaGcmessagesMsgid.EDOTAGCMsg;

import in.dragonbra.javasteam.base.ClientGCMsgProtobuf;
import in.dragonbra.javasteam.base.IPacketGCMsg;
import in.dragonbra.javasteam.util.compat.Consumer;

public class Dota2Player extends Dota2ClientGCMsgHandler {

    private Map<Integer, Consumer<IPacketGCMsg>> dispatchMap;

    public Dota2Player() {
        dispatchMap = new HashMap<>();
        dispatchMap.put(EDOTAGCMsg.k_EMsgGCPlayerInfo_VALUE, packetMsg -> handlePlayerInfo(packetMsg));
        dispatchMap.put(EDOTAGCMsg.k_EMsgClientToGCLatestConductScorecard_VALUE,
                packetMsg -> handleConductScorecard(packetMsg));
        dispatchMap.put(EDOTAGCMsg.k_EMsgGCGetHeroStandingsResponse_VALUE, packetMsg -> handleHeroStandings(packetMsg));

        dispatchMap.put(EDOTAGCMsg.k_EMsgProfileResponse_VALUE, packetMsg -> handleProfileResponse(packetMsg));
        dispatchMap.put(EDOTAGCMsg.k_EMsgClientToGCGetProfileCardResponse_VALUE,
                packetMsg -> handleProfileCardResponse(packetMsg));
        dispatchMap.put(EDOTAGCMsg.k_EMsgGCToClientPlayerStatsResponse_VALUE,
                packetMsg -> handlePlayerStats(packetMsg));
    }

    private void handlePlayerInfo(IPacketGCMsg msg) {
        ClientGCMsgProtobuf<CMsgDOTAPlayerInfo.Builder> protobuf = new ClientGCMsgProtobuf<>(CMsgDOTAPlayerInfo.class,
                msg);
        getLogger().trace(">>handlePlayerInfo: " + protobuf.getBody());
        client.postCallback(msg.getMsgType(), new PlayerInfoCallback(protobuf.getBody()));
    }

    private void handleConductScorecard(IPacketGCMsg msg) {
        ClientGCMsgProtobuf<CMsgPlayerConductScorecard.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgPlayerConductScorecard.class, msg);
        getLogger().trace(">>handleConductScorecard: " + protobuf.getBody());
        client.postCallback(msg.getMsgType(), new ConductScorecardCallback(protobuf.getBody()));
    }

    private void handleHeroStandings(IPacketGCMsg msg) {
        ClientGCMsgProtobuf<CMsgGCGetHeroStandingsResponse.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgGCGetHeroStandingsResponse.class, msg);
        getLogger().trace(">>handleHeroStandings: " + protobuf.getBody());
        client.postCallback(msg.getMsgType(), new HeroStandings(protobuf.getBody()));
    }

    private void handleProfileResponse(IPacketGCMsg msg) {
        ClientGCMsgProtobuf<CMsgProfileResponse.Builder> protobuf = new ClientGCMsgProtobuf<>(CMsgProfileResponse.class,
                msg);
        getLogger().trace(">>handleProfileResponse: " + protobuf.getBody());
        client.postCallback(new ProfileResponse(msg.getTargetJobID(), protobuf.getBody()));
    }

    private void handleProfileCardResponse(IPacketGCMsg msg) {
        ClientGCMsgProtobuf<CMsgDOTAProfileCard.Builder> protobuf = new ClientGCMsgProtobuf<>(CMsgDOTAProfileCard.class,
                msg);
        getLogger().trace(">>handleProfileCardResponse: " + protobuf.getBody());
        client.postCallback(new ProfileCardResponse(msg.getTargetJobID(), protobuf.getBody()));
    }

    private void handlePlayerStats(IPacketGCMsg msg) {
        ClientGCMsgProtobuf<CMsgGCToClientPlayerStatsResponse.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgGCToClientPlayerStatsResponse.class, msg);
        getLogger().trace(">>handlePlayerStats: " + protobuf.getBody());
        client.postCallback(new PlayerStatsResponse(msg.getTargetJobID(), protobuf.getBody()));
    }

    // actions

    public ProfileResponse requestProfile(Integer accountId) {
        ClientGCMsgProtobuf<CMsgProfileRequest.Builder> protobuf = new ClientGCMsgProtobuf<>(CMsgProfileRequest.class,
                EDOTAGCMsg.k_EMsgProfileRequest_VALUE);
        protobuf.getBody().setAccountId(accountId);
        getLogger().trace(">>requestProfile: " + protobuf.getBody());
        return sendJobAndWait(protobuf);
    }

    public ProfileCardResponse requestProfileCard(Integer accountId) {
        ClientGCMsgProtobuf<CMsgClientToGCGetProfileCard.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgClientToGCGetProfileCard.class, EDOTAGCMsg.k_EMsgClientToGCGetProfileCard_VALUE);
        protobuf.getBody().setAccountId(accountId);
        getLogger().trace(">>requestProfileCard: " + protobuf.getBody());
        return sendJobAndWait(protobuf);
    }

    public PlayerStatsResponse requestPlayerStats(Integer accountId) {
        ClientGCMsgProtobuf<CMsgClientToGCPlayerStatsRequest.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgClientToGCPlayerStatsRequest.class, EDOTAGCMsg.k_EMsgClientToGCPlayerStatsRequest_VALUE);
        protobuf.getBody().setAccountId(accountId);
        getLogger().trace(">>requestPlayerStats: " + protobuf.getBody());
        return sendJobAndWait(protobuf);
    }

    // Disabled by Valve
    @Deprecated
    public PlayerInfoCallback requestPlayerInfo(Integer accountId) {
        ClientGCMsgProtobuf<CMsgGCPlayerInfoRequest.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgGCPlayerInfoRequest.class, EDOTAGCMsg.k_EMsgGCPlayerInfoRequest_VALUE);
        protobuf.getBody().addPlayerInfosBuilder().setAccountId(accountId).build();
        getLogger().trace(">>requestPlayerInfo: " + protobuf.getBody());
        return sendCustomAndWait(protobuf, EDOTAGCMsg.k_EMsgGCPlayerInfo_VALUE);
    }

    public ConductScorecardCallback requestConductScorecard() {
        ClientGCMsgProtobuf<CMsgPlayerConductScorecardRequest.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgPlayerConductScorecardRequest.class,
                EDOTAGCMsg.k_EMsgClientToGCLatestConductScorecardRequest_VALUE);
        getLogger().trace(">>requestConductScorecard: " + protobuf.getBody());
        return sendCustomAndWait(protobuf, EDOTAGCMsg.k_EMsgClientToGCLatestConductScorecard_VALUE);
    }

    public HeroStandings requestHeroStanding() {
        ClientGCMsgProtobuf<CMsgGCGetHeroStandings.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgGCGetHeroStandings.class, EDOTAGCMsg.k_EMsgGCGetHeroStandings_VALUE);
        getLogger().trace(">>requestHeroStanding: " + protobuf.getBody());
        return sendCustomAndWait(protobuf, EDOTAGCMsg.k_EMsgGCGetHeroStandingsResponse_VALUE);
    }

    @Override
    public void setup(Dota2SteamGameCoordinator gameCoordinator) {
        super.setup(gameCoordinator);

    }

    @Override
    public void handleGCMsg(IPacketGCMsg packetGCMsg) {
        Consumer<IPacketGCMsg> dispatcher = dispatchMap.get(packetGCMsg.getMsgType());
        if (dispatcher != null) {
            getLogger().trace(">>handleGCMsg player msg: " + packetGCMsg.getMsgType());
            dispatcher.accept(packetGCMsg);
        }
    }

}
