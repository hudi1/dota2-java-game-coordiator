package org.tomass.dota.gc.handlers;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.tomass.dota.gc.util.Games;
import org.tomass.dota.gc.wrappers.SteamClientWrapper;
import org.tomass.protobuf.dota.BaseGcmessages.CMsgInviteToLobby;
import org.tomass.protobuf.dota.BaseGcmessages.CMsgInviteToParty;
import org.tomass.protobuf.dota.BaseGcmessages.CMsgKickFromParty;
import org.tomass.protobuf.dota.BaseGcmessages.CMsgLeaveParty;
import org.tomass.protobuf.dota.BaseGcmessages.CMsgLobbyInviteResponse;
import org.tomass.protobuf.dota.BaseGcmessages.CMsgPartyInviteResponse;
import org.tomass.protobuf.dota.BaseGcmessages.EGCBaseMsg;
import org.tomass.protobuf.dota.DotaGcmessagesClient.CMsgBalancedShuffleLobby;
import org.tomass.protobuf.dota.DotaGcmessagesClient.CMsgFlipLobbyTeams;
import org.tomass.protobuf.dota.DotaGcmessagesClient.CMsgGCMatchDetailsRequest;
import org.tomass.protobuf.dota.DotaGcmessagesClient.CMsgGCPlayerInfoRequest;
import org.tomass.protobuf.dota.DotaGcmessagesClient.CMsgGCPlayerInfoRequest.PlayerInfo;
import org.tomass.protobuf.dota.DotaGcmessagesClientChat.CMsgDOTAChatMessage;
import org.tomass.protobuf.dota.DotaGcmessagesClientChat.CMsgDOTAJoinChatChannel;
import org.tomass.protobuf.dota.DotaGcmessagesClientChat.CMsgDOTALeaveChatChannel;
import org.tomass.protobuf.dota.DotaGcmessagesClientChat.CMsgDOTARequestChatChannelList;
import org.tomass.protobuf.dota.DotaGcmessagesClientMatchManagement.CMsgAbandonCurrentGame;
import org.tomass.protobuf.dota.DotaGcmessagesClientMatchManagement.CMsgApplyTeamToPracticeLobby;
import org.tomass.protobuf.dota.DotaGcmessagesClientMatchManagement.CMsgDOTAPartyMemberSetCoach;
import org.tomass.protobuf.dota.DotaGcmessagesClientMatchManagement.CMsgPracticeLobbyCreate;
import org.tomass.protobuf.dota.DotaGcmessagesClientMatchManagement.CMsgPracticeLobbyJoin;
import org.tomass.protobuf.dota.DotaGcmessagesClientMatchManagement.CMsgPracticeLobbyJoinBroadcastChannel;
import org.tomass.protobuf.dota.DotaGcmessagesClientMatchManagement.CMsgPracticeLobbyKickFromTeam;
import org.tomass.protobuf.dota.DotaGcmessagesClientMatchManagement.CMsgPracticeLobbyLaunch;
import org.tomass.protobuf.dota.DotaGcmessagesClientMatchManagement.CMsgPracticeLobbyLeave;
import org.tomass.protobuf.dota.DotaGcmessagesClientMatchManagement.CMsgPracticeLobbyList;
import org.tomass.protobuf.dota.DotaGcmessagesClientMatchManagement.CMsgPracticeLobbySetCoach;
import org.tomass.protobuf.dota.DotaGcmessagesClientMatchManagement.CMsgPracticeLobbySetDetails;
import org.tomass.protobuf.dota.DotaGcmessagesClientMatchManagement.CMsgPracticeLobbySetTeamSlot;
import org.tomass.protobuf.dota.DotaGcmessagesClientMatchManagement.CMsgStopFindingMatch;
import org.tomass.protobuf.dota.DotaGcmessagesClientTeam.CMsgDOTAProTeamListRequest;
import org.tomass.protobuf.dota.DotaGcmessagesCommonMatchManagement.CMsgPartyReadyCheckRequest;
import org.tomass.protobuf.dota.DotaGcmessagesCommonMatchManagement.CSODOTALobby;
import org.tomass.protobuf.dota.DotaGcmessagesCommonMatchManagement.CSODOTAParty;
import org.tomass.protobuf.dota.DotaGcmessagesMsgid.EDOTAGCMsg;
import org.tomass.protobuf.dota.DotaSharedEnums.DOTABotDifficulty;
import org.tomass.protobuf.dota.DotaSharedEnums.DOTAChatChannelType_t;
import org.tomass.protobuf.dota.DotaSharedEnums.DOTA_GC_TEAM;
import org.tomass.protobuf.dota.GcsdkGcmessages.CMsgClientHello;
import org.tomass.protobuf.dota.GcsdkGcmessages.CMsgGCClientPing;
import org.tomass.protobuf.dota.GcsdkGcmessages.CMsgSOCacheSubscriptionRefresh;
import org.tomass.protobuf.dota.GcsdkGcmessages.ESourceEngine;
import org.tomass.protobuf.dota.GcsdkGcmessages.PartnerAccountType;
import org.tomass.protobuf.dota.Gcsystemmsgs.EGCBaseClientMsg;
import org.tomass.protobuf.dota.Gcsystemmsgs.ESOMsg;

import com.google.protobuf.ByteString;

import in.dragonbra.javasteam.base.ClientGCMsgProtobuf;
import in.dragonbra.javasteam.base.ClientMsg;
import in.dragonbra.javasteam.base.ClientMsgProtobuf;
import in.dragonbra.javasteam.base.IClientGCMsg;
import in.dragonbra.javasteam.base.IPacketMsg;
import in.dragonbra.javasteam.enums.EAppUsageEvent;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.generated.MsgClientAppUsageEvent;
import in.dragonbra.javasteam.handlers.ClientMsgHandler;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver.CMsgClientGamesPlayed;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgGCClient;
import in.dragonbra.javasteam.steam.handlers.steamgamecoordinator.callback.MessageCallback;
import in.dragonbra.javasteam.types.GameID;
import in.dragonbra.javasteam.util.MsgUtil;

@Controller
public class DotaGCHandler extends ClientMsgHandler {

    private Games game = Games.DOTA;

    private ESourceEngine engine = ESourceEngine.k_ESE_Source2;

    @Autowired
    private SteamClientWrapper client;

    private CSODOTALobby lobby;

    private CSODOTAParty party;

    boolean running = false;

    @PostConstruct
    public void init() {
        client.addHandler(this);
    }

    public void start() {
        running = true;

        ClientMsg<MsgClientAppUsageEvent> launchEvent = new ClientMsg<>(MsgClientAppUsageEvent.class);
        launchEvent.getBody().setGameID(new GameID(game.getGameId()));
        launchEvent.getBody().setAppUsageEvent(EAppUsageEvent.GameLaunch);
        client.send(launchEvent);

        // UploadRichPresence(RPType.Init);

        ClientMsgProtobuf<CMsgClientGamesPlayed.Builder> playGame = new ClientMsgProtobuf<>(CMsgClientGamesPlayed.class,
                EMsg.ClientGamesPlayed);

        playGame.getBody().addGamesPlayedBuilder().setGameId(game.getGameId());
        // .setGameExtraInfo("Dota 2")
        // .setStreamingProviderId(0).setGameFlags(engine.getNumber());
        // .setOwnerId((int) client.getSteamID().getAccountID());
        client.send(playGame);

        sayHello();
    }

    public void stop() {
        running = false;

        ClientMsgProtobuf<CMsgClientGamesPlayed.Builder> playGame = new ClientMsgProtobuf<>(CMsgClientGamesPlayed.class,
                EMsg.ClientGamesPlayed);
        // playGame.Body.games_played left empty
        client.send(playGame);

        // UploadRichPresence(RPType.None);
    }

    public void abandonGame() {
        ClientGCMsgProtobuf<CMsgAbandonCurrentGame.Builder> abandon = new ClientGCMsgProtobuf<>(
                CMsgAbandonCurrentGame.class, EDOTAGCMsg.k_EMsgGCAbandonCurrentGame_VALUE);
        send(abandon);
    }

    public void stopQueue() {
        ClientGCMsgProtobuf<CMsgAbandonCurrentGame.Builder> queue = new ClientGCMsgProtobuf<>(
                CMsgStopFindingMatch.class, EDOTAGCMsg.k_EMsgGCStopFindingMatch_VALUE);
        send(queue);
    }

    public void respondPartyInvite(long partyId, boolean accept) {
        ClientGCMsgProtobuf<CMsgPartyInviteResponse.Builder> invite = new ClientGCMsgProtobuf<>(
                CMsgPartyInviteResponse.class, EGCBaseMsg.k_EMsgGCPartyInviteResponse_VALUE);
        invite.getBody().setPartyId(partyId);
        invite.getBody().setAccept(accept);
        send(invite);
    }

    public void respondLobbyInvite(long lobbyId, boolean accept, long customGameCrc, int customGameTimestamp) {
        ClientGCMsgProtobuf<CMsgLobbyInviteResponse.Builder> invite = new ClientGCMsgProtobuf<>(
                CMsgLobbyInviteResponse.class, EGCBaseMsg.k_EMsgGCLobbyInviteResponse_VALUE);
        invite.getBody().setLobbyId(lobbyId);
        invite.getBody().setAccept(accept);
        if (customGameCrc != 0)
            invite.getBody().setCustomGameCrc(customGameCrc);
        if (customGameTimestamp != 0)
            invite.getBody().setCustomGameTimestamp(customGameTimestamp);
        send(invite);
    }

    public void joinLobby(long lobbyId, String passKey) {
        ClientGCMsgProtobuf<CMsgPracticeLobbyJoin.Builder> joinLobby = new ClientGCMsgProtobuf<>(
                CMsgPracticeLobbyJoin.class, EDOTAGCMsg.k_EMsgGCPracticeLobbyJoin_VALUE);
        joinLobby.getBody().setLobbyId(lobbyId);
        joinLobby.getBody().setPassKey(passKey);
        send(joinLobby);
    }

    public void leaveLobby() {
        ClientGCMsgProtobuf<CMsgPracticeLobbyLeave.Builder> leaveLobby = new ClientGCMsgProtobuf<>(
                CMsgPracticeLobbyLeave.class, EDOTAGCMsg.k_EMsgGCPracticeLobbyLeave_VALUE);
        send(leaveLobby);
    }

    public void pong() {
        ClientGCMsgProtobuf<CMsgGCClientPing.Builder> pingResponse = new ClientGCMsgProtobuf<>(CMsgGCClientPing.class,
                EGCBaseClientMsg.k_EMsgGCPingResponse_VALUE);
        send(pingResponse);
    }

    public void joinBroadcastChannel(int channel) {
        ClientGCMsgProtobuf<CMsgPracticeLobbyJoinBroadcastChannel.Builder> joinChannel = new ClientGCMsgProtobuf<>(
                CMsgPracticeLobbyJoinBroadcastChannel.class,
                EDOTAGCMsg.k_EMsgGCPracticeLobbyJoinBroadcastChannel_VALUE);
        joinChannel.getBody().setChannel(channel);
        send(joinChannel);
    }

    public void joinCoachSlot(DOTA_GC_TEAM team) {
        ClientGCMsgProtobuf<CMsgPracticeLobbySetCoach.Builder> joinChannel = new ClientGCMsgProtobuf<>(
                CMsgPracticeLobbySetCoach.class, EDOTAGCMsg.k_EMsgGCPracticeLobbySetCoach_VALUE);
        joinChannel.getBody().setTeam(team);
        send(joinChannel);
    }

    public void requestSubscriptionRefresh(int type, long id) {
        ClientGCMsgProtobuf<CMsgSOCacheSubscriptionRefresh.Builder> refresh = new ClientGCMsgProtobuf<>(
                CMsgSOCacheSubscriptionRefresh.class, ESOMsg.k_ESOMsg_CacheSubscriptionRefresh_VALUE);
        refresh.getBody().getOwnerSoidBuilder().setId(id).setType(type);
        send(refresh);
    }

    public void requestPlayerInfo(Iterable<? extends PlayerInfo> ids) {
        ClientGCMsgProtobuf<CMsgGCPlayerInfoRequest.Builder> req = new ClientGCMsgProtobuf<>(
                CMsgGCPlayerInfoRequest.class, EDOTAGCMsg.k_EMsgGCPlayerInfoRequest_VALUE);
        req.getBody().addAllPlayerInfos(ids);
        send(req);
    }

    public void requestProTeamList() {
        ClientGCMsgProtobuf<CMsgDOTAProTeamListRequest.Builder> req = new ClientGCMsgProtobuf<>(
                CMsgDOTAProTeamListRequest.class, EDOTAGCMsg.k_EMsgGCProTeamListRequest_VALUE);
        send(req);
    }

    public void joinTeam(DOTA_GC_TEAM team, int slot, DOTABotDifficulty botDifficulty) {
        ClientGCMsgProtobuf<CMsgPracticeLobbySetTeamSlot.Builder> joinSlot = new ClientGCMsgProtobuf<>(
                CMsgPracticeLobbySetTeamSlot.class, EDOTAGCMsg.k_EMsgGCPracticeLobbySetTeamSlot_VALUE);
        joinSlot.getBody().setTeam(team);
        joinSlot.getBody().setSlot(slot);
        if (botDifficulty != DOTABotDifficulty.BOT_DIFFICULTY_EXTRA3)
            joinSlot.getBody().setBotDifficulty(botDifficulty);
        send(joinSlot);
    }

    public void setBotSlotDifficulty(DOTA_GC_TEAM team, int slot, DOTABotDifficulty botDifficulty) {
        joinTeam(team, slot, botDifficulty);
    }

    public void launchLobby() {
        send(new ClientGCMsgProtobuf<>(CMsgPracticeLobbyLaunch.class, EDOTAGCMsg.k_EMsgGCPracticeLobbyLaunch_VALUE));
    }

    public void createLobby(String passKey, CMsgPracticeLobbySetDetails details) {
        ClientGCMsgProtobuf<CMsgPracticeLobbyCreate.Builder> create = new ClientGCMsgProtobuf<>(
                CMsgPracticeLobbyCreate.class, EDOTAGCMsg.k_EMsgGCPracticeLobbyCreate_VALUE);
        create.getBody().setPassKey(passKey);
        create.getBody().setLobbyDetails(details);
        send(create);
    }

    public void setLobbyDetails(CMsgPracticeLobbySetDetails details) {
        ClientGCMsgProtobuf<CMsgPracticeLobbyCreate.Builder> update = new ClientGCMsgProtobuf<>(
                CMsgPracticeLobbySetDetails.class, EDOTAGCMsg.k_EMsgGCPracticeLobbySetDetails_VALUE);
        // there's no way to pass a pre-allocated body, so copy the params
        // PropertyCopier.CopyProperties(update.Body, details);
        update.getBody().setLobbyDetails(details);
        send(update);
    }

    public void inviteToParty(long steamId) {

        ClientGCMsgProtobuf<CMsgInviteToParty.Builder> invite = new ClientGCMsgProtobuf<>(CMsgInviteToParty.class,
                EGCBaseMsg.k_EMsgGCInviteToParty_VALUE);
        invite.getBody().setSteamId(steamId);
        send(invite);

        if (party != null) {
            // TODO
            /*
             * ClientMsgProtobuf<CMsgClientUDSInviteToGame.Builder> udsInvite = new
             * ClientMsgProtobuf<>(CMsgClientUDSInviteToGame.class, EMsg.ClientUDSInviteToGame);
             * invite.getBoddy().connect_string = "+invite " + Party.party_id; if (Engine ==
             * ESourceEngine.k_ESE_Source2) invite.Body.connect_string += " -launchsource2"; invite.Body.steam_id_dest =
             * steam_id; invite.Body.steam_id_src = 0; client.Send(invite);
             */
        }
    }

    public void inviteToLobby(long steamId) {
        ClientGCMsgProtobuf<CMsgInviteToParty.Builder> invite = new ClientGCMsgProtobuf<>(CMsgInviteToLobby.class,
                EGCBaseMsg.k_EMsgGCInviteToLobby_VALUE);
        invite.getBody().setSteamId(steamId);
        send(invite);

        if (lobby != null) {
            /*
             * var invite = new ClientMsgProtobuf<CMsgClientUDSInviteToGame>(EMsg.ClientUDSInviteToGame);
             * invite.Body.steam_id_dest = steam_id; invite.Body.connect_string = "+invite " + Lobby.lobby_id; if
             * (Engine == ESourceEngine.k_ESE_Source2) invite.Body.connect_string += " -launchsource2";
             * Client.Send(invite);
             */
        }
    }

    public void applyTeamToLobby(int teamid) {
        ClientGCMsgProtobuf<CMsgApplyTeamToPracticeLobby.Builder> apply = new ClientGCMsgProtobuf<>(
                CMsgApplyTeamToPracticeLobby.class, EDOTAGCMsg.k_EMsgGCApplyTeamToPracticeLobby_VALUE);
        apply.getBody().setTeamId(teamid);
        send(apply);
    }

    public void setPartyCoach(boolean coach) {
        ClientGCMsgProtobuf<CMsgDOTAPartyMemberSetCoach.Builder> slot = new ClientGCMsgProtobuf<>(
                CMsgDOTAPartyMemberSetCoach.class, EDOTAGCMsg.k_EMsgGCPartyMemberSetCoach_VALUE);
        slot.getBody().setWantsCoach(coach);
        send(slot);
    }

    public void leaveParty() {
        send(new ClientGCMsgProtobuf<>(CMsgLeaveParty.class, EGCBaseMsg.k_EMsgGCLeaveParty_VALUE));
    }

    public void readyCheck() {
        send(new ClientGCMsgProtobuf<>(CMsgPartyReadyCheckRequest.class,
                EDOTAGCMsg.k_EMsgPartyReadyCheckRequest_VALUE));
    }

    public void kickPlayerFromParty(long steamId) {
        ClientGCMsgProtobuf<CMsgKickFromParty.Builder> kick = new ClientGCMsgProtobuf<>(CMsgKickFromParty.class,
                EGCBaseMsg.k_EMsgGCKickFromParty_VALUE);
        kick.getBody().setSteamId(steamId);
        send(kick);
    }

    public void kickPlayerFromLobbyTeam(int accountId) {
        ClientGCMsgProtobuf<CMsgPracticeLobbyKickFromTeam.Builder> kick = new ClientGCMsgProtobuf<>(
                CMsgPracticeLobbyKickFromTeam.class, EDOTAGCMsg.k_EMsgGCPracticeLobbyKickFromTeam_VALUE);
        kick.getBody().setAccountId(accountId);
        send(kick);
    }

    public void joinChatChannel(String name, DOTAChatChannelType_t type) {
        ClientGCMsgProtobuf<CMsgDOTAJoinChatChannel.Builder> joinChannel = new ClientGCMsgProtobuf<>(
                CMsgDOTAJoinChatChannel.class, EDOTAGCMsg.k_EMsgGCJoinChatChannel_VALUE);
        joinChannel.getBody().setChannelName(name);
        joinChannel.getBody().setChannelType(type);
        send(joinChannel);
    }

    public void requestChatChannelList() {
        send(new ClientGCMsgProtobuf<>(CMsgDOTARequestChatChannelList.class,
                EDOTAGCMsg.k_EMsgGCRequestChatChannelList_VALUE));
    }

    public void requestMatchResult(long matchId) {
        ClientGCMsgProtobuf<CMsgGCMatchDetailsRequest.Builder> requestMatch = new ClientGCMsgProtobuf<>(
                CMsgGCMatchDetailsRequest.class, EDOTAGCMsg.k_EMsgGCMatchDetailsRequest_VALUE);
        requestMatch.getBody().setMatchId(matchId);
        send(requestMatch);
    }

    public void sendChannelMessage(long channelId, String message) {
        ClientGCMsgProtobuf<CMsgDOTAChatMessage.Builder> chatMsg = new ClientGCMsgProtobuf<>(CMsgDOTAChatMessage.class,
                EDOTAGCMsg.k_EMsgGCChatMessage_VALUE);
        chatMsg.getBody().setChannelId(channelId);
        chatMsg.getBody().setText(message);
        send(chatMsg);
    }

    public void leaveChatChannel(long channelId) {
        ClientGCMsgProtobuf<CMsgDOTALeaveChatChannel.Builder> leaveChannel = new ClientGCMsgProtobuf<>(
                CMsgDOTALeaveChatChannel.class, EDOTAGCMsg.k_EMsgGCLeaveChatChannel_VALUE);
        leaveChannel.getBody().setChannelId(channelId);
        send(leaveChannel);
    }

    public void practiceLobbyList(String passKey) {
        ClientGCMsgProtobuf<CMsgPracticeLobbyList.Builder> list = new ClientGCMsgProtobuf<>(CMsgPracticeLobbyList.class,
                EDOTAGCMsg.k_EMsgGCPracticeLobbyList_VALUE);
        list.getBody().setPassKey(passKey);
        send(list);
    }

    public void practiceLobbyShuffle() {
        send(new ClientGCMsgProtobuf<>(CMsgBalancedShuffleLobby.class, EDOTAGCMsg.k_EMsgGCBalancedShuffleLobby_VALUE));
    }

    public void practiceLobbyFlip() {
        send(new ClientGCMsgProtobuf<>(CMsgFlipLobbyTeams.class, EDOTAGCMsg.k_EMsgGCFlipLobbyTeams_VALUE));
    }

    private void sayHello() {
        if (!running) {
            return;
        }
        ClientGCMsgProtobuf<CMsgClientHello.Builder> hello = new ClientGCMsgProtobuf<>(CMsgClientHello.class,
                EGCBaseClientMsg.k_EMsgGCClientHello_VALUE);
        hello.getBody().setClientSessionNeed(104);
        hello.getBody().setClientLauncher(PartnerAccountType.PARTNER_NONE);
        hello.getBody().setEngine(engine);
        hello.getBody().setSecretKey("");
        send(hello);
    }

    public void send(IClientGCMsg msg) {
        if (msg == null) {
            throw new IllegalArgumentException("msg is null");
        }

        ClientMsgProtobuf<CMsgGCClient.Builder> clientMsg = new ClientMsgProtobuf<>(CMsgGCClient.class,
                EMsg.ClientToGC);

        clientMsg.getProtoHeader().setRoutingAppid(game.getGameId());
        clientMsg.getBody().setMsgtype(MsgUtil.makeGCMsg(msg.getMsgType(), msg.isProto()));
        clientMsg.getBody().setAppid(game.getGameId());

        clientMsg.getBody().setPayload(ByteString.copyFrom(msg.serialize()));
        client.send(clientMsg);
    }

    @Override
    public void handleMsg(IPacketMsg packetMsg) {
        if (packetMsg == null) {
            throw new IllegalArgumentException("packetMsg is null");
        }

        if (packetMsg.getMsgType() == EMsg.ClientFromGC) {
            ClientMsgProtobuf<CMsgGCClient.Builder> msg = new ClientMsgProtobuf<>(CMsgGCClient.class, packetMsg);
            MessageCallback callback = new MessageCallback(msg.getBody());
            switch (callback.geteMsg()) {
            case EDOTAGCMsg.k_EMsgGCPracticeLobbyJoinResponse_VALUE:
                ClientMsgProtobuf<CMsgLobbyInviteResponse.Builder> persState = new ClientMsgProtobuf<>(
                        CMsgLobbyInviteResponse.class, packetMsg);
                // TODO callback to solve response
                // persState.getBody().getLobbyId();
                break;
            default:
                break;
            }
        }
    }

}
