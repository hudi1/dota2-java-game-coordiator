package org.tomass.dota.gc.handlers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tomass.dota.gc.clients.Dota2Client;
import org.tomass.dota.gc.handlers.callbacks.lobby.LobbyInviteCallback;
import org.tomass.dota.gc.handlers.callbacks.lobby.LobbyInviteRemovedCallback;
import org.tomass.dota.gc.handlers.callbacks.lobby.LobbyNewCallback;
import org.tomass.dota.gc.handlers.callbacks.lobby.LobbyRemovedCallback;
import org.tomass.dota.gc.handlers.callbacks.lobby.LobbyUpdatedCallback;
import org.tomass.dota.gc.handlers.callbacks.shared.SingleObjectNew;
import org.tomass.dota.gc.handlers.callbacks.shared.SingleObjectRemoved;
import org.tomass.dota.gc.handlers.callbacks.shared.SingleObjectUpdated;
import org.tomass.dota.gc.util.CSOTypes;
import org.tomass.dota.gc.util.ServerRegions;
import org.tomass.protobuf.dota.BaseGcmessages.CMsgInviteToLobby;
import org.tomass.protobuf.dota.BaseGcmessages.CMsgLobbyInviteResponse;
import org.tomass.protobuf.dota.BaseGcmessages.EGCBaseMsg;
import org.tomass.protobuf.dota.DotaGcmessagesClient.CMsgBalancedShuffleLobby;
import org.tomass.protobuf.dota.DotaGcmessagesClient.CMsgDOTADestroyLobbyRequest;
import org.tomass.protobuf.dota.DotaGcmessagesClient.CMsgFlipLobbyTeams;
import org.tomass.protobuf.dota.DotaGcmessagesClientMatchManagement.CMsgAbandonCurrentGame;
import org.tomass.protobuf.dota.DotaGcmessagesClientMatchManagement.CMsgFriendPracticeLobbyListRequest;
import org.tomass.protobuf.dota.DotaGcmessagesClientMatchManagement.CMsgLobbyList;
import org.tomass.protobuf.dota.DotaGcmessagesClientMatchManagement.CMsgPracticeLobbyCreate;
import org.tomass.protobuf.dota.DotaGcmessagesClientMatchManagement.CMsgPracticeLobbyJoin;
import org.tomass.protobuf.dota.DotaGcmessagesClientMatchManagement.CMsgPracticeLobbyJoinBroadcastChannel;
import org.tomass.protobuf.dota.DotaGcmessagesClientMatchManagement.CMsgPracticeLobbyKick;
import org.tomass.protobuf.dota.DotaGcmessagesClientMatchManagement.CMsgPracticeLobbyKickFromTeam;
import org.tomass.protobuf.dota.DotaGcmessagesClientMatchManagement.CMsgPracticeLobbyLaunch;
import org.tomass.protobuf.dota.DotaGcmessagesClientMatchManagement.CMsgPracticeLobbyLeave;
import org.tomass.protobuf.dota.DotaGcmessagesClientMatchManagement.CMsgPracticeLobbyList;
import org.tomass.protobuf.dota.DotaGcmessagesClientMatchManagement.CMsgPracticeLobbySetDetails;
import org.tomass.protobuf.dota.DotaGcmessagesClientMatchManagement.CMsgPracticeLobbySetTeamSlot;
import org.tomass.protobuf.dota.DotaGcmessagesCommonMatchManagement.CSODOTALobby;
import org.tomass.protobuf.dota.DotaGcmessagesCommonMatchManagement.CSODOTALobbyInvite;
import org.tomass.protobuf.dota.DotaGcmessagesCommonMatchManagement.LobbyDotaTVDelay;
import org.tomass.protobuf.dota.DotaGcmessagesMsgid.EDOTAGCMsg;
import org.tomass.protobuf.dota.DotaSharedEnums.DOTABotDifficulty;
import org.tomass.protobuf.dota.DotaSharedEnums.DOTALobbyVisibility;
import org.tomass.protobuf.dota.DotaSharedEnums.DOTA_GC_TEAM;
import org.tomass.protobuf.dota.DotaSharedEnums.DOTA_GameMode;

import com.google.protobuf.ByteString;

import in.dragonbra.javasteam.base.ClientGCMsgProtobuf;
import in.dragonbra.javasteam.base.IPacketGCMsg;
import in.dragonbra.javasteam.util.compat.Consumer;

public class Dota2Lobby extends Dota2ClientGCMsgHandlerImpl {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Map<Integer, Consumer<IPacketGCMsg>> dispatchMap;

    private CSODOTALobby lobby;

    public Dota2Lobby(Dota2Client client) {
        super(client);
        dispatchMap = new HashMap<>();

        client.getManager().subscribe(SingleObjectNew.class, this::onSingleObjectNew);
        client.getManager().subscribe(SingleObjectUpdated.class, this::onSingleObjectUpdated);
        client.getManager().subscribe(SingleObjectRemoved.class, this::onSingleObjectRemoved);
    }

    private void onSingleObjectNew(SingleObjectNew callback) {
        switch (callback.getTypeId()) {
        case CSOTypes.LOBBY_INVITE_VALUE:
            handleLobbyInvite(callback.getData());
            break;
        case CSOTypes.LOBBY_VALUE:
            handleLobbyNew(callback.getData());
            break;
        default:
            logger.info("!!onSingleObjectNew for the client " + client + " with type: " + callback.getTypeId());
            break;
        }
    }

    private void onSingleObjectUpdated(SingleObjectUpdated callback) {
        switch (callback.getTypeId()) {
        case CSOTypes.LOBBY_VALUE:
            handleLobbyUpdated(callback.getData());
            break;
        default:
            logger.info("!!onSingleObjectUpdated for the client " + client + " with type: " + callback.getTypeId());
            break;
        }
    }

    private void onSingleObjectRemoved(SingleObjectRemoved callback) {
        switch (callback.getTypeId()) {
        case CSOTypes.LOBBY_INVITE_VALUE:
            handleLobbyInviteRemoved(callback.getData());
            break;
        case CSOTypes.LOBBY_VALUE:
            handleLobbyRemoved(callback.getData());
            break;
        default:
            logger.info("!!onSingleObjectRemoved for the client " + client + " with type: " + callback.getTypeId());
            break;
        }
    }

    public void lobbyCleanup() {
        this.lobby = null;
    }

    public void handleLobbyInvite(ByteString data) {
        try {
            CSODOTALobbyInvite lobbyInvite = CSODOTALobbyInvite.parseFrom(data);
            logger.trace(">>handleLobbyInvite for the client " + client + ": " + lobbyInvite);
            client.postCallback(new LobbyInviteCallback(lobbyInvite));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleLobbyInviteRemoved(ByteString data) {
        try {
            CSODOTALobbyInvite lobbyInvite = CSODOTALobbyInvite.parseFrom(data);
            logger.trace(">>handleLobbyInviteRemoved for the client " + client + ": " + lobbyInvite);
            client.postCallback(new LobbyInviteRemovedCallback(lobbyInvite));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleLobbyNew(ByteString data) {
        try {
            CSODOTALobby lobby = CSODOTALobby.parseFrom(data);
            logger.trace(">>handleLobbyNew for the client " + client + ": " + lobby);
            this.lobby = lobby;
            client.postCallback(new LobbyNewCallback(lobby));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleLobbyUpdated(ByteString data) {
        try {
            CSODOTALobby lobby = CSODOTALobby.parseFrom(data);
            logger.trace(">>handleLobbyUpdated for the client " + client + ": " + lobby);
            this.lobby = lobby;
            client.postCallback(new LobbyUpdatedCallback(lobby));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleLobbyRemoved(ByteString data) {
        try {
            CSODOTALobby lobby = CSODOTALobby.parseFrom(data);
            logger.trace(">>handleLobbyUpdated for the client " + client + ": " + lobby);
            this.lobby = null;
            client.postCallback(new LobbyRemovedCallback(lobby));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // actions

    public void createPracticeLobby(String passKey) {
        ClientGCMsgProtobuf<CMsgPracticeLobbyCreate.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgPracticeLobbyCreate.class, EDOTAGCMsg.k_EMsgGCPracticeLobbyCreate_VALUE);
        protobuf.getBody().setPassKey(passKey);
        protobuf.getBody().getLobbyDetailsBuilder().setAllowCheats(false)
                .setDotaTvDelay(LobbyDotaTVDelay.LobbyDotaTV_120).setGameName(UUID.randomUUID().toString())
                .setVisibility(DOTALobbyVisibility.DOTALobbyVisibility_Unlisted);
        logger.trace(">>respondToPartyInvite for the client " + client + ": " + protobuf.getBody());
        send(protobuf);
    }

    public void configPracticeLobby(CMsgPracticeLobbySetDetails options) {
        ClientGCMsgProtobuf<CMsgPracticeLobbySetDetails.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgPracticeLobbySetDetails.class, EDOTAGCMsg.k_EMsgGCPracticeLobbySetDetails_VALUE);
        protobuf.getBody().mergeFrom(options);
        logger.trace(">>configPracticeLobby for the client " + client + ": " + protobuf.getBody());
        send(protobuf);
    }

    public void getLobbyList(ServerRegions serverRegion, DOTA_GameMode gameMode) {
        ClientGCMsgProtobuf<CMsgLobbyList.Builder> protobuf = new ClientGCMsgProtobuf<>(CMsgLobbyList.class,
                EDOTAGCMsg.k_EMsgGCLobbyList_VALUE);
        protobuf.getBody().setServerRegion(serverRegion.getNumber());
        protobuf.getBody().setGameMode(gameMode);
        logger.trace(">>getLobbyList for the client " + client + ": " + protobuf.getBody());
        send(protobuf);
    }

    public void getPracticeLobbyList(String password) {
        ClientGCMsgProtobuf<CMsgPracticeLobbyList.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgPracticeLobbyList.class, EDOTAGCMsg.k_EMsgGCPracticeLobbyList_VALUE);
        protobuf.getBody().setPassKey(password);
        logger.trace(">>getPracticeLobbyList for the client " + client + ": " + protobuf.getBody());
        send(protobuf);
    }

    public void getFriendPracticeLobbyList() {
        ClientGCMsgProtobuf<CMsgFriendPracticeLobbyListRequest.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgFriendPracticeLobbyListRequest.class, EDOTAGCMsg.k_EMsgGCFriendPracticeLobbyListRequest_VALUE);
        logger.trace(">>getFriendPracticeLobbyList for the client " + client + ": " + protobuf.getBody());
        send(protobuf);
    }

    public void balancedShuffleLobby() {
        if (lobby == null) {
            return;
        }
        ClientGCMsgProtobuf<CMsgBalancedShuffleLobby.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgBalancedShuffleLobby.class, EDOTAGCMsg.k_EMsgGCBalancedShuffleLobby_VALUE);
        logger.trace(">>balancedShuffleLobby for the client " + client + ": " + protobuf.getBody());
        send(protobuf);
    }

    public void flipLobbyTeams() {
        if (lobby == null) {
            return;
        }
        ClientGCMsgProtobuf<CMsgFlipLobbyTeams.Builder> protobuf = new ClientGCMsgProtobuf<>(CMsgFlipLobbyTeams.class,
                EDOTAGCMsg.k_EMsgGCFlipLobbyTeams_VALUE);
        logger.trace(">>flipLobbyTeams for the client " + client + ": " + protobuf.getBody());
        send(protobuf);
    }

    public void inviteToLobby(long steamId) {
        if (lobby == null) {
            return;
        }
        ClientGCMsgProtobuf<CMsgInviteToLobby.Builder> protobuf = new ClientGCMsgProtobuf<>(CMsgInviteToLobby.class,
                EGCBaseMsg.k_EMsgGCInviteToLobby_VALUE);
        protobuf.getBody().setSteamId(steamId);
        logger.trace(">>flipLobbyTeams for the client " + client + ": " + protobuf.getBody());
        send(protobuf);
    }

    public void practiceLobbyKick(int accountId) {
        if (lobby == null) {
            return;
        }
        ClientGCMsgProtobuf<CMsgPracticeLobbyKick.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgPracticeLobbyKick.class, EDOTAGCMsg.k_EMsgGCPracticeLobbyKick_VALUE);
        protobuf.getBody().setAccountId(accountId);
        logger.trace(">>practiceLobbyKick for the client " + client + ": " + protobuf.getBody());
        send(protobuf);
    }

    public void practiceLobbyKickFromTeam(int accountId) {
        if (lobby == null) {
            return;
        }
        ClientGCMsgProtobuf<CMsgPracticeLobbyKickFromTeam.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgPracticeLobbyKickFromTeam.class, EDOTAGCMsg.k_EMsgGCPracticeLobbyKickFromTeam_VALUE);
        protobuf.getBody().setAccountId(accountId);
        logger.trace(">>practiceLobbyKickFromTeam for the client " + client + ": " + protobuf.getBody());
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
        logger.trace(">>joinPracticeLobby for the client " + client + ": " + protobuf.getBody());
        send(protobuf);
    }

    public void leaveLobby() {
        // if (lobby == null) {
        // return;
        // }
        ClientGCMsgProtobuf<CMsgPracticeLobbyLeave.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgPracticeLobbyLeave.class, EDOTAGCMsg.k_EMsgGCPracticeLobbyLeave_VALUE);
        logger.trace(">>leaveLobby for the client " + client + ": " + protobuf.getBody());
        send(protobuf);
    }

    public void abandonCurrentGame() {
        if (lobby == null) {
            return;
        }
        ClientGCMsgProtobuf<CMsgAbandonCurrentGame.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgAbandonCurrentGame.class, EDOTAGCMsg.k_EMsgGCAbandonCurrentGame_VALUE);
        logger.trace(">>abandonCurrentGame for the client " + client + ": " + protobuf.getBody());
        send(protobuf);
    }

    public void launchPracticeLobby() {
        if (lobby == null) {
            return;
        }
        ClientGCMsgProtobuf<CMsgPracticeLobbyLaunch.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgPracticeLobbyLaunch.class, EDOTAGCMsg.k_EMsgGCPracticeLobbyLaunch_VALUE);
        logger.trace(">>launchPracticeLobby for the client " + client + ": " + protobuf.getBody());
        send(protobuf);
    }

    public void joinPracticeLobbyTeam(DOTA_GC_TEAM team, int slot) {
        if (lobby == null) {
            return;
        }
        ClientGCMsgProtobuf<CMsgPracticeLobbySetTeamSlot.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgPracticeLobbySetTeamSlot.class, EDOTAGCMsg.k_EMsgGCPracticeLobbySetTeamSlot_VALUE);
        protobuf.getBody().setTeam(team);
        protobuf.getBody().setSlot(slot);
        logger.trace(">>joinPracticeLobbyTeam for the client " + client + ": " + protobuf.getBody());
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
        logger.trace(">>joinPracticeLobbyBroadcastChannel for the client " + client + ": " + protobuf.getBody());
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
        logger.trace(">>addBotToPracticeLobby for the client " + client + ": " + protobuf.getBody());
        send(protobuf);
    }

    public void respondToLobbyInvite(long lobbyId, boolean accept) {
        ClientGCMsgProtobuf<CMsgLobbyInviteResponse.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgLobbyInviteResponse.class, EGCBaseMsg.k_EMsgGCLobbyInviteResponse_VALUE);
        protobuf.getBody().setLobbyId(lobbyId);
        protobuf.getBody().setAccept(accept);
        logger.trace(">>respondToLobbyInvite for the client " + client + ": " + protobuf.getBody());
        send(protobuf);
    }

    public void destroyLobby() {
        ClientGCMsgProtobuf<CMsgDOTADestroyLobbyRequest.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgDOTADestroyLobbyRequest.class, EDOTAGCMsg.k_EMsgDestroyLobbyRequest_VALUE);
        logger.trace(">>destroyLobby for the client " + client + ": " + protobuf.getBody());
        send(protobuf);
    }

    @Override
    public void handleGCMsg(IPacketGCMsg packetGCMsg) {
        Consumer<IPacketGCMsg> dispatcher = dispatchMap.get(packetGCMsg.getMsgType());
        if (dispatcher != null) {
            logger.info(">>handleGCMsg for the client " + client + " lobby msg: " + packetGCMsg.getMsgType());
            dispatcher.accept(packetGCMsg);
        }
    }

    public CSODOTALobby getLobby() {
        return lobby;
    }

}
