package org.tomass.dota.gc.handlers.features;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tomass.dota.gc.handlers.Dota2ClientGCMsgHandler;
import org.tomass.dota.gc.handlers.callbacks.lobby.LeagueAvailableLobbyNodes;
import org.tomass.dota.gc.handlers.callbacks.lobby.LeagueInfoAdmin;
import org.tomass.dota.gc.handlers.callbacks.lobby.LeagueNodeCallback;
import org.tomass.dota.gc.handlers.callbacks.lobby.LobbyCallback;
import org.tomass.dota.gc.handlers.callbacks.lobby.LobbyInviteCallback;
import org.tomass.dota.gc.handlers.callbacks.lobby.LobbyInviteRemovedCallback;
import org.tomass.dota.gc.handlers.callbacks.lobby.LobbyNewCallback;
import org.tomass.dota.gc.handlers.callbacks.lobby.LobbyRemovedCallback;
import org.tomass.dota.gc.handlers.callbacks.lobby.LobbyUpdatedCallback;
import org.tomass.dota.gc.handlers.callbacks.lobby.PracticeLobbyCallback;
import org.tomass.dota.gc.handlers.callbacks.shared.SingleObjectNewLobby;
import org.tomass.dota.gc.handlers.callbacks.shared.SingleObjectRemovedLobby;
import org.tomass.dota.gc.handlers.callbacks.shared.SingleObjectUpdatedLobby;
import org.tomass.dota.gc.handlers.features.Dota2Chat.ChatChannel;
import org.tomass.dota.gc.util.CSOTypes;
import org.tomass.dota.gc.util.ServerRegions;
import org.tomass.dota.steam.handlers.Dota2SteamGameCoordinator;
import org.tomass.protobuf.dota.BaseGcmessages.CMsgInviteToLobby;
import org.tomass.protobuf.dota.BaseGcmessages.CMsgLobbyInviteResponse;
import org.tomass.protobuf.dota.BaseGcmessages.EGCBaseMsg;
import org.tomass.protobuf.dota.DotaGcmessagesClient.CMsgBalancedShuffleLobby;
import org.tomass.protobuf.dota.DotaGcmessagesClient.CMsgDOTADestroyLobbyRequest;
import org.tomass.protobuf.dota.DotaGcmessagesClient.CMsgFlipLobbyTeams;
import org.tomass.protobuf.dota.DotaGcmessagesClientMatchManagement.CMsgAbandonCurrentGame;
import org.tomass.protobuf.dota.DotaGcmessagesClientMatchManagement.CMsgFriendPracticeLobbyListRequest;
import org.tomass.protobuf.dota.DotaGcmessagesClientMatchManagement.CMsgFriendPracticeLobbyListResponse;
import org.tomass.protobuf.dota.DotaGcmessagesClientMatchManagement.CMsgLobbyList;
import org.tomass.protobuf.dota.DotaGcmessagesClientMatchManagement.CMsgLobbyListResponse;
import org.tomass.protobuf.dota.DotaGcmessagesClientMatchManagement.CMsgPracticeLobbyCreate;
import org.tomass.protobuf.dota.DotaGcmessagesClientMatchManagement.CMsgPracticeLobbyJoin;
import org.tomass.protobuf.dota.DotaGcmessagesClientMatchManagement.CMsgPracticeLobbyJoinBroadcastChannel;
import org.tomass.protobuf.dota.DotaGcmessagesClientMatchManagement.CMsgPracticeLobbyJoinResponse;
import org.tomass.protobuf.dota.DotaGcmessagesClientMatchManagement.CMsgPracticeLobbyKick;
import org.tomass.protobuf.dota.DotaGcmessagesClientMatchManagement.CMsgPracticeLobbyKickFromTeam;
import org.tomass.protobuf.dota.DotaGcmessagesClientMatchManagement.CMsgPracticeLobbyLaunch;
import org.tomass.protobuf.dota.DotaGcmessagesClientMatchManagement.CMsgPracticeLobbyLeave;
import org.tomass.protobuf.dota.DotaGcmessagesClientMatchManagement.CMsgPracticeLobbyList;
import org.tomass.protobuf.dota.DotaGcmessagesClientMatchManagement.CMsgPracticeLobbyListResponse;
import org.tomass.protobuf.dota.DotaGcmessagesClientMatchManagement.CMsgPracticeLobbySetDetails;
import org.tomass.protobuf.dota.DotaGcmessagesClientMatchManagement.CMsgPracticeLobbySetTeamSlot;
import org.tomass.protobuf.dota.DotaGcmessagesClientWatch.CMsgClientToGCTopLeagueMatchesRequest;
import org.tomass.protobuf.dota.DotaGcmessagesClientWatch.CMsgGCToClientFindTopLeagueMatchesResponse;
import org.tomass.protobuf.dota.DotaGcmessagesCommonLeague.CMsgDOTALeagueAvailableLobbyNodes;
import org.tomass.protobuf.dota.DotaGcmessagesCommonLeague.CMsgDOTALeagueAvailableLobbyNodesRequest;
import org.tomass.protobuf.dota.DotaGcmessagesCommonLeague.CMsgDOTALeagueInfoList;
import org.tomass.protobuf.dota.DotaGcmessagesCommonLeague.CMsgDOTALeagueInfoListAdminsRequest;
import org.tomass.protobuf.dota.DotaGcmessagesCommonLeague.CMsgDOTALeagueLiveGames;
import org.tomass.protobuf.dota.DotaGcmessagesCommonLeague.CMsgDOTALeagueNodeRequest;
import org.tomass.protobuf.dota.DotaGcmessagesCommonLeague.CMsgDOTALeagueNodeResponse;
import org.tomass.protobuf.dota.DotaGcmessagesCommonMatchManagement.CSODOTALobby;
import org.tomass.protobuf.dota.DotaGcmessagesCommonMatchManagement.CSODOTALobbyInvite;
import org.tomass.protobuf.dota.DotaGcmessagesCommonMatchManagement.LobbyDotaTVDelay;
import org.tomass.protobuf.dota.DotaGcmessagesMsgid.EDOTAGCMsg;
import org.tomass.protobuf.dota.DotaSharedEnums.DOTABotDifficulty;
import org.tomass.protobuf.dota.DotaSharedEnums.DOTALobbyVisibility;
import org.tomass.protobuf.dota.DotaSharedEnums.DOTASelectionPriorityRules;
import org.tomass.protobuf.dota.DotaSharedEnums.DOTA_GC_TEAM;
import org.tomass.protobuf.dota.DotaSharedEnums.DOTA_GameMode;

import com.google.protobuf.ByteString;

import in.dragonbra.javasteam.base.ClientGCMsgProtobuf;
import in.dragonbra.javasteam.base.IPacketGCMsg;
import in.dragonbra.javasteam.enums.EAccountType;
import in.dragonbra.javasteam.enums.EUniverse;
import in.dragonbra.javasteam.types.SteamID;
import in.dragonbra.javasteam.util.compat.Consumer;

public class Dota2Lobby extends Dota2ClientGCMsgHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Map<Integer, Consumer<IPacketGCMsg>> dispatchMap;

    private CSODOTALobby lobby;

    public Dota2Lobby() {
        dispatchMap = new HashMap<>();
        dispatchMap.put(EDOTAGCMsg.k_EMsgGCPracticeLobbyListResponse_VALUE,
                packetMsg -> handlePracticeLobbyList(packetMsg));
        dispatchMap.put(EDOTAGCMsg.k_EMsgGCFriendPracticeLobbyListResponse_VALUE,
                packetMsg -> handleFriendPracticeLobby(packetMsg));
        dispatchMap.put(EDOTAGCMsg.k_EMsgGCLobbyListResponse_VALUE, packetMsg -> handleLobby(packetMsg));
        dispatchMap.put(EDOTAGCMsg.k_EMsgGCPracticeLobbyResponse_VALUE, packetMsg -> handlePracticeLobby(packetMsg));
        dispatchMap.put(EDOTAGCMsg.k_EMsgDOTALeagueInfoListAdminsReponse_VALUE,
                packetMsg -> handleLeagueInfoAdmins(packetMsg));
        dispatchMap.put(EDOTAGCMsg.k_EMsgDOTALeagueAvailableLobbyNodes_VALUE,
                packetMsg -> handleLeagueAvaiableNodes(packetMsg));
        dispatchMap.put(EDOTAGCMsg.k_EMsgDOTALeagueNodeResponse_VALUE, packetMsg -> handleLeagueNode(packetMsg));
        dispatchMap.put(EDOTAGCMsg.k_EMsgGCToGCGetLiveLeagueMatchesResponse_VALUE,
                packetMsg -> handleLiveLeagueMatches(packetMsg));
        dispatchMap.put(EDOTAGCMsg.k_EMsgGCToClientTopLeagueMatchesResponse_VALUE,
                packetMsg -> handleTopLeagueMatches(packetMsg));

    }

    @Override
    public void setup(Dota2SteamGameCoordinator gameCoordinator) {
        super.setup(gameCoordinator);
        client.getManager().subscribe(SingleObjectNewLobby.class, this::onSingleObjectNew);
        client.getManager().subscribe(SingleObjectUpdatedLobby.class, this::onSingleObjectUpdated);
        client.getManager().subscribe(SingleObjectRemovedLobby.class, this::onSingleObjectRemoved);
    }

    private void handlePracticeLobby(IPacketGCMsg data) {
        ClientGCMsgProtobuf<CMsgPracticeLobbyJoinResponse.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgPracticeLobbyJoinResponse.class, data);
        logger.trace(">>handlePracticeLobby: " + protobuf.getBody() + " /" + data.getTargetJobID());
        client.postCallback(new PracticeLobbyCallback(data.getTargetJobID(), protobuf.getBody().getResult()));
    }

    private void handlePracticeLobbyList(IPacketGCMsg data) {
        ClientGCMsgProtobuf<CMsgPracticeLobbyListResponse.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgPracticeLobbyListResponse.class, data);
        logger.trace(">>handlePracticeLobbyList: " + protobuf.getBody() + " /" + data.getTargetJobID());
        client.postCallback(new LobbyCallback(data.getTargetJobID(), protobuf.getBody().getLobbiesList()));
    }

    private void handleFriendPracticeLobby(IPacketGCMsg data) {
        ClientGCMsgProtobuf<CMsgFriendPracticeLobbyListResponse.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgFriendPracticeLobbyListResponse.class, data);
        logger.trace(">>handleFriendPracticeLobby: " + protobuf.getBody());
        client.postCallback(data.getMsgType(), new LobbyCallback(protobuf.getBody().getLobbiesList()));
    }

    private void handleLobby(IPacketGCMsg data) {
        ClientGCMsgProtobuf<CMsgLobbyListResponse.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgLobbyListResponse.class, data);
        logger.trace(">>handleLobby: " + protobuf.getBody());
        client.postCallback(data.getMsgType(), new LobbyCallback(protobuf.getBody().getLobbiesList()));
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

    private void handleLiveLeagueMatches(IPacketGCMsg data) {
        ClientGCMsgProtobuf<CMsgDOTALeagueLiveGames.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgDOTALeagueLiveGames.class, data);
        logger.trace(">>handleLiveLeagueMatches: " + protobuf.getBody());
        // client.postCallback(new LeagueNodeCallback(data.getTargetJobID(), protobuf.getBody()));
    }

    private void handleTopLeagueMatches(IPacketGCMsg data) {
        ClientGCMsgProtobuf<CMsgGCToClientFindTopLeagueMatchesResponse.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgGCToClientFindTopLeagueMatchesResponse.class, data);
        logger.trace(">>handleTopLeagueMatches: " + protobuf.getBody());
        // client.postCallback(new LeagueNodeCallback(data.getTargetJobID(), protobuf.getBody()));
    }

    private void onSingleObjectNew(SingleObjectNewLobby callback) {
        switch (callback.getTypeId()) {
        case CSOTypes.LOBBY_INVITE_VALUE:
            handleLobbyInvite(callback.getData());
            break;
        case CSOTypes.LOBBY_VALUE:
            handleLobbyNew(callback.getData());
            break;
        default:
            logger.debug("!!onSingleObjectNew with type: " + callback.getTypeId());
            break;
        }
    }

    private void onSingleObjectUpdated(SingleObjectUpdatedLobby callback) {
        switch (callback.getTypeId()) {
        case CSOTypes.LOBBY_VALUE:
            handleLobbyUpdated(callback.getData());
            break;
        default:
            logger.debug("!!onSingleObjectUpdated with type: " + callback.getTypeId());
            break;
        }
    }

    private void onSingleObjectRemoved(SingleObjectRemovedLobby callback) {
        logger.debug("!!onSingleObjectRemoved with type: " + callback.getTypeId());
        switch (callback.getTypeId()) {
        case CSOTypes.LOBBY_INVITE_VALUE:
            handleLobbyInviteRemoved(callback.getData());
            break;
        case CSOTypes.LOBBY_VALUE:
            handleLobbyRemoved(callback.getData());
            break;
        default:
            logger.debug("!!onSingleObjectRemoved with type: " + callback.getTypeId());
            break;
        }
    }

    public void handleLobbyInvite(ByteString data) {
        try {
            CSODOTALobbyInvite lobbyInvite = CSODOTALobbyInvite.parseFrom(data);
            logger.trace(">>handleLobbyInvite: " + lobbyInvite);
            client.postCallback(new LobbyInviteCallback(lobbyInvite));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleLobbyInviteRemoved(ByteString data) {
        try {
            CSODOTALobbyInvite lobbyInvite = CSODOTALobbyInvite.parseFrom(data);
            logger.trace(">>handleLobbyInviteRemoved: " + lobbyInvite);
            client.postCallback(new LobbyInviteRemovedCallback(lobbyInvite));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleLobbyNew(ByteString data) {
        try {
            CSODOTALobby lobby = CSODOTALobby.parseFrom(data);
            logger.trace(">>handleLobbyNew: " + lobby);
            this.lobby = lobby;
            client.postCallback(CSOTypes.LOBBY_VALUE, new LobbyNewCallback(lobby));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleLobbyUpdated(ByteString data) {
        try {
            CSODOTALobby lobby = CSODOTALobby.parseFrom(data);
            logger.trace(">>handleLobbyUpdated: " + lobby);
            this.lobby = lobby;
            client.postCallback(new LobbyUpdatedCallback(lobby));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleLobbyRemoved(ByteString data) {
        try {
            CSODOTALobby lobby = CSODOTALobby.parseFrom(data);
            logger.trace(">>handleLobbyRemoved: " + lobby);
            this.lobby = null;
            client.postCallback(new LobbyRemovedCallback(lobby));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // actions
    public PracticeLobbyCallback createPracticeLobby(String passKey, String name) {
        ClientGCMsgProtobuf<CMsgPracticeLobbyCreate.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgPracticeLobbyCreate.class, EDOTAGCMsg.k_EMsgGCPracticeLobbyCreate_VALUE);
        protobuf.getBody().getLobbyDetailsBuilder().setAllowCheats(false)
                .setDotaTvDelay(LobbyDotaTVDelay.LobbyDotaTV_120)
                .setGameName(name == null ? UUID.randomUUID().toString() : name)
                .setVisibility(DOTALobbyVisibility.DOTALobbyVisibility_Public)
                .setServerRegion(ServerRegions.AUSTRIA.getNumber()).setPassKey(passKey).setAllowSpectating(true);
        logger.trace(">>createPracticeLobby: " + protobuf.getBody());
        return sendJobAndWait(protobuf);
    }

    public PracticeLobbyCallback createTournamentLobby(String passKey, String name) {
        ClientGCMsgProtobuf<CMsgPracticeLobbyCreate.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgPracticeLobbyCreate.class, EDOTAGCMsg.k_EMsgGCPracticeLobbyCreate_VALUE);
        protobuf.getBody().getLobbyDetailsBuilder().setAllowCheats(false)
                .setDotaTvDelay(LobbyDotaTVDelay.LobbyDotaTV_120)
                .setGameName(name == null ? UUID.randomUUID().toString() : name)
                .setVisibility(DOTALobbyVisibility.DOTALobbyVisibility_Public)
                .setServerRegion(ServerRegions.AUSTRIA.getNumber()).setPassKey(passKey).setAllowSpectating(true)
                .setGameMode(DOTA_GameMode.DOTA_GAMEMODE_CM_VALUE)
                .setSelectionPriorityRules(DOTASelectionPriorityRules.k_DOTASelectionPriorityRules_Automatic)
                .setSeriesType(1);
        logger.trace(">>createTournamentLobby: " + protobuf.getBody());
        return sendJobAndWait(protobuf);
    }

    public PracticeLobbyCallback createTournamentLobby(String passKey, String name, Integer leagueId, Integer nodeId) {
        ClientGCMsgProtobuf<CMsgPracticeLobbyCreate.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgPracticeLobbyCreate.class, EDOTAGCMsg.k_EMsgGCPracticeLobbyCreate_VALUE);
        protobuf.getBody().getLobbyDetailsBuilder().setAllowCheats(false)
                .setDotaTvDelay(LobbyDotaTVDelay.LobbyDotaTV_120)
                .setGameName(name == null ? UUID.randomUUID().toString() : name)
                .setVisibility(DOTALobbyVisibility.DOTALobbyVisibility_Public)
                .setServerRegion(ServerRegions.EUROPE.getNumber()).setPassKey(passKey).setAllowSpectating(true)
                .setGameMode(DOTA_GameMode.DOTA_GAMEMODE_CM_VALUE)
                .setSelectionPriorityRules(DOTASelectionPriorityRules.k_DOTASelectionPriorityRules_Automatic)
                .setSeriesType(1).setLeagueid(leagueId).setLeagueNodeId(nodeId);
        logger.trace(">>createTournamentLobby: " + protobuf.getBody());
        return sendJobAndWait(protobuf);
    }

    public void configPracticeLobby(CMsgPracticeLobbySetDetails options) {
        ClientGCMsgProtobuf<CMsgPracticeLobbySetDetails.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgPracticeLobbySetDetails.class, EDOTAGCMsg.k_EMsgGCPracticeLobbySetDetails_VALUE);
        protobuf.getBody().mergeFrom(options);
        logger.trace(">>configPracticeLobby: " + protobuf.getBody());
        send(protobuf);
    }

    public LobbyCallback getLobbyList(Integer serverRegion, Integer gameMode) {
        ClientGCMsgProtobuf<CMsgLobbyList.Builder> protobuf = new ClientGCMsgProtobuf<>(CMsgLobbyList.class,
                EDOTAGCMsg.k_EMsgGCLobbyList_VALUE);
        protobuf.getBody().setServerRegion(serverRegion == null ? ServerRegions.UNSPECIFIED.getNumber() : serverRegion);
        protobuf.getBody()
                .setGameMode(gameMode == null ? DOTA_GameMode.DOTA_GAMEMODE_NONE : DOTA_GameMode.forNumber(gameMode));
        logger.trace(">>getLobbyList: " + protobuf.getBody());
        return sendCustomAndWait(protobuf, EDOTAGCMsg.k_EMsgGCLobbyListResponse_VALUE);
    }

    public LobbyCallback getPracticeLobbyList(String password) {
        ClientGCMsgProtobuf<CMsgPracticeLobbyList.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgPracticeLobbyList.class, EDOTAGCMsg.k_EMsgGCPracticeLobbyList_VALUE);
        protobuf.getBody().setPassKey(password);
        logger.trace(">>getPracticeLobbyList: " + protobuf.getBody());
        return sendJobAndWait(protobuf);
    }

    public LobbyCallback getFriendPracticeLobbyList() {
        ClientGCMsgProtobuf<CMsgFriendPracticeLobbyListRequest.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgFriendPracticeLobbyListRequest.class, EDOTAGCMsg.k_EMsgGCFriendPracticeLobbyListRequest_VALUE);
        logger.trace(">>getFriendPracticeLobbyList: " + protobuf.getBody());
        return sendCustomAndWait(protobuf, EDOTAGCMsg.k_EMsgGCFriendPracticeLobbyListResponse_VALUE);
    }

    public void balancedShuffleLobby() {
        if (lobby == null) {
            return;
        }
        ClientGCMsgProtobuf<CMsgBalancedShuffleLobby.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgBalancedShuffleLobby.class, EDOTAGCMsg.k_EMsgGCBalancedShuffleLobby_VALUE);
        logger.trace(">>balancedShuffleLobby: " + protobuf.getBody());
        send(protobuf);
    }

    public void flipLobbyTeams() {
        if (lobby == null) {
            return;
        }
        ClientGCMsgProtobuf<CMsgFlipLobbyTeams.Builder> protobuf = new ClientGCMsgProtobuf<>(CMsgFlipLobbyTeams.class,
                EDOTAGCMsg.k_EMsgGCFlipLobbyTeams_VALUE);
        logger.trace(">>flipLobbyTeams: " + protobuf.getBody());
        send(protobuf);
    }

    public void inviteToLobby(Integer accountId) {
        inviteToLobby(new SteamID(accountId, EUniverse.Public, EAccountType.Individual).convertToUInt64());
    }

    public void inviteToLobby(long steamId) {
        if (lobby == null) {
            return;
        }
        ClientGCMsgProtobuf<CMsgInviteToLobby.Builder> protobuf = new ClientGCMsgProtobuf<>(CMsgInviteToLobby.class,
                EGCBaseMsg.k_EMsgGCInviteToLobby_VALUE);
        protobuf.getBody().setSteamId(steamId);
        logger.trace(">>inviteToLobby: " + protobuf.getBody());
        send(protobuf);
    }

    public void practiceLobbyKick(int accountId) {
        if (lobby == null) {
            return;
        }
        ClientGCMsgProtobuf<CMsgPracticeLobbyKick.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgPracticeLobbyKick.class, EDOTAGCMsg.k_EMsgGCPracticeLobbyKick_VALUE);
        protobuf.getBody().setAccountId(accountId);
        logger.trace(">>practiceLobbyKick: " + protobuf.getBody());
        send(protobuf);
    }

    public void practiceLobbyKickFromTeam(int accountId) {
        if (lobby == null) {
            return;
        }
        ClientGCMsgProtobuf<CMsgPracticeLobbyKickFromTeam.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgPracticeLobbyKickFromTeam.class, EDOTAGCMsg.k_EMsgGCPracticeLobbyKickFromTeam_VALUE);
        protobuf.getBody().setAccountId(accountId);
        logger.trace(">>practiceLobbyKickFromTeam: " + protobuf.getBody());
        send(protobuf);
    }

    public void joinPracticeLobby(long id, String password) {
        if (lobby == null) {
            return;
        }
        ClientGCMsgProtobuf<CMsgPracticeLobbyJoin.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgPracticeLobbyJoin.class, EDOTAGCMsg.k_EMsgGCPracticeLobbyJoin_VALUE);
        protobuf.getBody().setLobbyId(id);
        protobuf.getBody().setPassKey(password);
        logger.trace(">>joinPracticeLobby: " + protobuf.getBody());
        send(protobuf);
    }

    public void leaveLobby() {
        if (lobby == null) {
            return;
        }
        ClientGCMsgProtobuf<CMsgPracticeLobbyLeave.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgPracticeLobbyLeave.class, EDOTAGCMsg.k_EMsgGCPracticeLobbyLeave_VALUE);
        logger.trace(">>leaveLobby: " + protobuf.getBody());
        send(protobuf);
    }

    public void abandonCurrentGame() {
        if (lobby == null) {
            return;
        }
        ClientGCMsgProtobuf<CMsgAbandonCurrentGame.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgAbandonCurrentGame.class, EDOTAGCMsg.k_EMsgGCAbandonCurrentGame_VALUE);
        logger.trace(">>abandonCurrentGame: " + protobuf.getBody());
        send(protobuf);
    }

    public void launchPracticeLobby() {
        if (lobby == null) {
            return;
        }
        ClientGCMsgProtobuf<CMsgPracticeLobbyLaunch.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgPracticeLobbyLaunch.class, EDOTAGCMsg.k_EMsgGCPracticeLobbyLaunch_VALUE);
        logger.trace(">>launchPracticeLobby: " + protobuf.getBody());
        send(protobuf);
    }

    public void practiceLobbySetTeamSlot(DOTA_GC_TEAM team, int slot) {
        if (lobby == null) {
            return;
        }
        ClientGCMsgProtobuf<CMsgPracticeLobbySetTeamSlot.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgPracticeLobbySetTeamSlot.class, EDOTAGCMsg.k_EMsgGCPracticeLobbySetTeamSlot_VALUE);
        protobuf.getBody().setTeam(team);
        protobuf.getBody().setSlot(slot);
        logger.trace(">>practiceLobbySetTeamSlot: " + protobuf.getBody());
        send(protobuf);
    }

    public void joinPracticeLobbyBroadcastChannel(int channel) {
        if (lobby == null) {
            return;
        }
        ClientGCMsgProtobuf<CMsgPracticeLobbyJoinBroadcastChannel.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgPracticeLobbyJoinBroadcastChannel.class,
                EDOTAGCMsg.k_EMsgGCPracticeLobbyJoinBroadcastChannel_VALUE);
        protobuf.getBody().setChannel(channel);
        logger.trace(">>joinPracticeLobbyBroadcastChannel: " + protobuf.getBody());
        send(protobuf);
    }

    public void addBotToPracticeLobby(DOTA_GC_TEAM team, int slot, DOTABotDifficulty botDifficulty) {
        if (lobby == null) {
            return;
        }
        ClientGCMsgProtobuf<CMsgPracticeLobbySetTeamSlot.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgPracticeLobbyLeave.class, EDOTAGCMsg.k_EMsgGCPracticeLobbySetTeamSlot_VALUE);
        protobuf.getBody().setTeam(team);
        protobuf.getBody().setSlot(slot);
        protobuf.getBody().setBotDifficulty(botDifficulty);
        logger.trace(">>addBotToPracticeLobby: " + protobuf.getBody());
        send(protobuf);
    }

    public void respondToLobbyInvite(long lobbyId, boolean accept) {
        ClientGCMsgProtobuf<CMsgLobbyInviteResponse.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgLobbyInviteResponse.class, EGCBaseMsg.k_EMsgGCLobbyInviteResponse_VALUE);
        protobuf.getBody().setLobbyId(lobbyId);
        protobuf.getBody().setAccept(accept);
        logger.trace(">>respondToLobbyInvite: " + protobuf.getBody());
        send(protobuf);
    }

    public void destroyLobby() {
        ClientGCMsgProtobuf<CMsgDOTADestroyLobbyRequest.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgDOTADestroyLobbyRequest.class, EDOTAGCMsg.k_EMsgDestroyLobbyRequest_VALUE);
        logger.trace(">>destroyLobby: " + protobuf.getBody());
        send(protobuf);
    }

    public LeagueInfoAdmin requestLeagueInfoAdmins() {
        ClientGCMsgProtobuf<CMsgDOTALeagueInfoListAdminsRequest.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgDOTALeagueInfoListAdminsRequest.class, EDOTAGCMsg.k_EMsgDOTALeagueInfoListAdminsRequest_VALUE);
        logger.trace(">>destroyLobby: " + protobuf.getBody());
        return sendJobAndWait(protobuf);
    }

    public LeagueAvailableLobbyNodes requestLeagueAvaiableNodes(Integer leagueId) {
        ClientGCMsgProtobuf<CMsgDOTALeagueAvailableLobbyNodesRequest.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgDOTALeagueAvailableLobbyNodesRequest.class,
                EDOTAGCMsg.k_EMsgDOTALeagueAvailableLobbyNodesRequest_VALUE);
        protobuf.getBody().setLeagueId(leagueId);
        logger.trace(">>requestLeagueAvaiableNodes: " + protobuf.getBody());
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

    public LeagueNodeCallback requestTopLeagueMatches() {
        ClientGCMsgProtobuf<CMsgClientToGCTopLeagueMatchesRequest.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgClientToGCTopLeagueMatchesRequest.class, EDOTAGCMsg.k_EMsgClientToGCTopLeagueMatchesRequest_VALUE);
        logger.trace(">>requestTopLeagueMatches: " + protobuf.getBody());
        return sendJobAndWait(protobuf);
    }

    public void sendLobbyMessage(String text) {
        if (lobby != null) {
            ChatChannel channel = client.getChatHandler().getLobbyChatChannel(lobby.getLobbyId() + "");
            if (channel != null) {
                channel.send(text);
            }
        }
    }

    @Override
    public void handleGCMsg(IPacketGCMsg packetGCMsg) {
        Consumer<IPacketGCMsg> dispatcher = dispatchMap.get(packetGCMsg.getMsgType());
        if (dispatcher != null) {
            logger.trace(">>handleGCMsg lobby msg: " + packetGCMsg.getMsgType());
            dispatcher.accept(packetGCMsg);
        }
    }

    public CSODOTALobby getLobby() {
        return lobby;
    }

}
