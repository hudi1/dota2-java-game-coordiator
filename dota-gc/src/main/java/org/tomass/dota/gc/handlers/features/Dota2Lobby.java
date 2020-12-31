package org.tomass.dota.gc.handlers.features;

import java.util.HashMap;
import java.util.Map;

import org.tomass.dota.gc.handlers.Dota2ClientGCMsgHandler;
import org.tomass.dota.gc.handlers.callbacks.lobby.LobbyCallback;
import org.tomass.dota.gc.handlers.callbacks.lobby.LobbyEventPointsCallback;
import org.tomass.dota.gc.handlers.callbacks.lobby.LobbyInvitationCreatedCallback;
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
import org.tomass.protobuf.dota.BaseGcmessages.CMsgInvitationCreated;
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
import org.tomass.protobuf.dota.DotaGcmessagesCommon.CMsgLobbyEventPoints;
import org.tomass.protobuf.dota.DotaGcmessagesCommonMatchManagement.CSODOTALobby;
import org.tomass.protobuf.dota.DotaGcmessagesCommonMatchManagement.CSODOTALobby.CExtraMsg;
import org.tomass.protobuf.dota.DotaGcmessagesCommonMatchManagement.CSODOTALobbyInvite;
import org.tomass.protobuf.dota.DotaGcmessagesMsgid.EDOTAGCMsg;
import org.tomass.protobuf.dota.DotaSharedEnums.DOTABotDifficulty;
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
        dispatchMap.put(EGCBaseMsg.k_EMsgGCInvitationCreated_VALUE, packetMsg -> handleInvitationCreated(packetMsg));
        dispatchMap.put(EDOTAGCMsg.k_EMsgLobbyEventPoints_VALUE, packetMsg -> handleEventPoints(packetMsg));
    }

    @Override
    public void setup(Dota2SteamGameCoordinator gameCoordinator) {
        super.setup(gameCoordinator);
        client.getManager().subscribe(SingleObjectNewLobby.class, this::onSingleObjectNew);
        client.getManager().subscribe(SingleObjectUpdatedLobby.class, this::onSingleObjectUpdated);
        client.getManager().subscribe(SingleObjectRemovedLobby.class, this::onSingleObjectRemoved);
    }

    private void handleEventPoints(IPacketGCMsg data) {
        ClientGCMsgProtobuf<CMsgLobbyEventPoints.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgLobbyEventPoints.class, data);
        getLogger().trace(">>handleEventPoints: " + protobuf.getBody());
        client.postCallback(new LobbyEventPointsCallback(protobuf.getBody()));
    }

    private void handlePracticeLobby(IPacketGCMsg data) {
        ClientGCMsgProtobuf<CMsgPracticeLobbyJoinResponse.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgPracticeLobbyJoinResponse.class, data);
        getLogger().trace(">>handlePracticeLobby: " + protobuf.getBody() + "/" + data.getTargetJobID());
        client.postCallback(new PracticeLobbyCallback(data.getTargetJobID(), protobuf.getBody().getResult()));
    }

    private void handlePracticeLobbyList(IPacketGCMsg data) {
        ClientGCMsgProtobuf<CMsgPracticeLobbyListResponse.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgPracticeLobbyListResponse.class, data);
        getLogger().trace(">>handlePracticeLobbyList: " + protobuf.getBody() + "/" + data.getTargetJobID());
        client.postCallback(new LobbyCallback(data.getTargetJobID(), protobuf.getBody().getLobbiesList()));
    }

    private void handleFriendPracticeLobby(IPacketGCMsg data) {
        ClientGCMsgProtobuf<CMsgFriendPracticeLobbyListResponse.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgFriendPracticeLobbyListResponse.class, data);
        getLogger().trace(">>handleFriendPracticeLobby: " + protobuf.getBody());
        client.postCallback(data.getMsgType(), new LobbyCallback(protobuf.getBody().getLobbiesList()));
    }

    private void handleLobby(IPacketGCMsg data) {
        ClientGCMsgProtobuf<CMsgLobbyListResponse.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgLobbyListResponse.class, data);
        getLogger().trace(">>handleLobby: " + protobuf.getBody());
        client.postCallback(data.getMsgType(), new LobbyCallback(protobuf.getBody().getLobbiesList()));
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
            getLogger().debug("!!onSingleObjectNew with type: " + callback.getTypeId());
            break;
        }
    }

    private void onSingleObjectUpdated(SingleObjectUpdatedLobby callback) {
        switch (callback.getTypeId()) {
        case CSOTypes.LOBBY_VALUE:
            handleLobbyUpdated(callback.getData());
            break;
        default:
            getLogger().debug("!!onSingleObjectUpdated with type: " + callback.getTypeId());
            break;
        }
    }

    private void onSingleObjectRemoved(SingleObjectRemovedLobby callback) {
        getLogger().debug("!!onSingleObjectRemoved with type: " + callback.getTypeId());
        switch (callback.getTypeId()) {
        case CSOTypes.LOBBY_INVITE_VALUE:
            handleLobbyInviteRemoved(callback.getData());
            break;
        case CSOTypes.LOBBY_VALUE:
            handleLobbyRemoved(callback.getData());
            break;
        default:
            getLogger().debug("!!onSingleObjectRemoved with type: " + callback.getTypeId());
            break;
        }
    }

    private void handleLobbyInvite(ByteString data) {
        try {
            CSODOTALobbyInvite lobbyInvite = CSODOTALobbyInvite.parseFrom(data);
            getLogger().trace(">>handleLobbyInvite: " + lobbyInvite);
            client.postCallback(new LobbyInviteCallback(lobbyInvite));
        } catch (Exception e) {
            getLogger().error("!!handleLobbyInvite: ", e);
        }
    }

    private void handleLobbyInviteRemoved(ByteString data) {
        try {
            CSODOTALobbyInvite lobbyInvite = CSODOTALobbyInvite.parseFrom(data);
            getLogger().trace(">>handleLobbyInviteRemoved: " + lobbyInvite);
            client.postCallback(new LobbyInviteRemovedCallback(lobbyInvite));
        } catch (Exception e) {
            getLogger().error("!!handleLobbyInviteRemoved: ", e);
        }
    }

    private void handleLobbyNew(ByteString data) {
        try {
            CSODOTALobby lobby = CSODOTALobby.parseFrom(data);
            getLogger().trace(">>handleLobbyNew: " + lobby);
            if (this.lobby != null) {
                if (lobby.getLobbyId() != this.lobby.getLobbyId()) {
                    getLogger().warn("!!handleLobbyNew: " + lobby + " vs " + this.lobby);
                }
            }
            this.lobby = lobby;
            client.postCallback(CSOTypes.LOBBY_VALUE, new LobbyNewCallback(lobby));
        } catch (Exception e) {
            getLogger().error("!!handleLobbyNew: ", e);
        }
    }

    private void handleLobbyUpdated(ByteString data) {
        try {
            CSODOTALobby lobby = CSODOTALobby.parseFrom(data);
            getLogger().trace(">>handleLobbyUpdated: " + lobby);
            if (this.lobby != null) {
                if (lobby.getLobbyId() != this.lobby.getLobbyId()) {
                    getLogger().warn("!!handleLobbyUpdated: " + lobby + " vs " + this.lobby);
                }
            }
            this.lobby = lobby;
            client.postCallback(new LobbyUpdatedCallback(lobby));
            for (CExtraMsg extraMessage : lobby.getExtraMessagesList()) {
                client.processGcMessage(extraMessage.getId(), extraMessage.getContents());
            }
        } catch (Exception e) {
            getLogger().error("!!handleLobbyUpdated: ", e);
        }
    }

    private void handleLobbyRemoved(ByteString data) {
        try {
            CSODOTALobby lobby = CSODOTALobby.parseFrom(data);
            getLogger().trace(">>handleLobbyRemoved: " + lobby);
            if (lobby.hasLobbyId()) {
                client.postCallback(new LobbyRemovedCallback(lobby));
            } else {
                client.postCallback(new LobbyRemovedCallback(this.lobby));
            }
            this.lobby = null;
        } catch (Exception e) {
            getLogger().error("!!handleLobbyRemoved: ", e);
        }
    }

    private void handleInvitationCreated(IPacketGCMsg msg) {
        ClientGCMsgProtobuf<CMsgInvitationCreated.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgInvitationCreated.class, msg);
        if (lobby != null && lobby.getLobbyId() == protobuf.getBody().getGroupId()) {
            getLogger().trace(">>handleInvitationCreated: " + protobuf.getBody());
            client.postCallback(new LobbyInvitationCreatedCallback(protobuf.getBody()));
        }
    }

    // actions

    public PracticeLobbyCallback createPracticeLobby(CMsgPracticeLobbySetDetails detail) {
        ClientGCMsgProtobuf<CMsgPracticeLobbyCreate.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgPracticeLobbyCreate.class, EDOTAGCMsg.k_EMsgGCPracticeLobbyCreate_VALUE);
        protobuf.getBody().setLobbyDetails(detail);

        try {
            getLogger().trace(">>createPracticeLobby: " + protobuf.getBody());
            return sendJobAndWait(protobuf, 60l);
        } finally {
            getLogger().trace("<<createPracticeLobby: " + protobuf.getBody());
        }
    }

    public void configPracticeLobby(CMsgPracticeLobbySetDetails options) {
        ClientGCMsgProtobuf<CMsgPracticeLobbySetDetails.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgPracticeLobbySetDetails.class, EDOTAGCMsg.k_EMsgGCPracticeLobbySetDetails_VALUE);
        if (lobby == null) {
            return;
        }
        protobuf.getBody().mergeFrom(options);
        protobuf.getBody().setLobbyId(lobby.getLobbyId());
        getLogger().trace(">>configPracticeLobby: " + protobuf.getBody());
        send(protobuf);
    }

    public LobbyCallback getLobbyList(Integer serverRegion, Integer gameMode) {
        ClientGCMsgProtobuf<CMsgLobbyList.Builder> protobuf = new ClientGCMsgProtobuf<>(CMsgLobbyList.class,
                EDOTAGCMsg.k_EMsgGCLobbyList_VALUE);
        protobuf.getBody().setServerRegion(serverRegion == null ? ServerRegions.UNSPECIFIED.getNumber() : serverRegion);
        protobuf.getBody()
                .setGameMode(gameMode == null ? DOTA_GameMode.DOTA_GAMEMODE_NONE : DOTA_GameMode.forNumber(gameMode));
        getLogger().trace(">>getLobbyList: " + protobuf.getBody());
        return sendCustomAndWait(protobuf, EDOTAGCMsg.k_EMsgGCLobbyListResponse_VALUE);
    }

    public LobbyCallback requestPracticeLobbyList() {
        ClientGCMsgProtobuf<CMsgPracticeLobbyList.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgPracticeLobbyList.class, EDOTAGCMsg.k_EMsgGCPracticeLobbyList_VALUE);
        getLogger().trace(">>requestPracticeLobbyList: " + protobuf.getBody());
        return sendJobAndWait(protobuf);
    }

    public LobbyCallback requestFriendPracticeLobbyList() {
        ClientGCMsgProtobuf<CMsgFriendPracticeLobbyListRequest.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgFriendPracticeLobbyListRequest.class, EDOTAGCMsg.k_EMsgGCFriendPracticeLobbyListRequest_VALUE);
        getLogger().trace(">>requestFriendPracticeLobbyList: " + protobuf.getBody());
        return sendCustomAndWait(protobuf, EDOTAGCMsg.k_EMsgGCFriendPracticeLobbyListResponse_VALUE);
    }

    public void balancedShuffleLobby() {
        if (lobby == null) {
            return;
        }
        ClientGCMsgProtobuf<CMsgBalancedShuffleLobby.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgBalancedShuffleLobby.class, EDOTAGCMsg.k_EMsgGCBalancedShuffleLobby_VALUE);
        getLogger().trace(">>balancedShuffleLobby: " + protobuf.getBody());
        send(protobuf);
    }

    public void flipLobbyTeams() {
        if (lobby == null) {
            return;
        }
        ClientGCMsgProtobuf<CMsgFlipLobbyTeams.Builder> protobuf = new ClientGCMsgProtobuf<>(CMsgFlipLobbyTeams.class,
                EDOTAGCMsg.k_EMsgGCFlipLobbyTeams_VALUE);
        getLogger().trace(">>flipLobbyTeams: " + protobuf.getBody());
        send(protobuf);
    }

    public void inviteToLobby(Integer accountId) {
        inviteToLobbySteam(new SteamID(accountId, EUniverse.Public, EAccountType.Individual).convertToUInt64());
    }

    public void inviteToLobbySteam(long steamId) {
        if (lobby == null) {
            return;
        }
        ClientGCMsgProtobuf<CMsgInviteToLobby.Builder> protobuf = new ClientGCMsgProtobuf<>(CMsgInviteToLobby.class,
                EGCBaseMsg.k_EMsgGCInviteToLobby_VALUE);
        protobuf.getBody().setSteamId(steamId);
        getLogger().trace(">>inviteToLobby: " + protobuf.getBody());
        send(protobuf);
    }

    public void practiceLobbyKick(int accountId) {
        if (lobby == null) {
            return;
        }
        ClientGCMsgProtobuf<CMsgPracticeLobbyKick.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgPracticeLobbyKick.class, EDOTAGCMsg.k_EMsgGCPracticeLobbyKick_VALUE);
        protobuf.getBody().setAccountId(accountId);
        getLogger().trace(">>practiceLobbyKick: " + protobuf.getBody());
        send(protobuf);
    }

    public void practiceLobbyKickFromTeam(int accountId) {
        if (lobby == null) {
            return;
        }
        ClientGCMsgProtobuf<CMsgPracticeLobbyKickFromTeam.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgPracticeLobbyKickFromTeam.class, EDOTAGCMsg.k_EMsgGCPracticeLobbyKickFromTeam_VALUE);
        protobuf.getBody().setAccountId(accountId);
        getLogger().trace(">>practiceLobbyKickFromTeam: " + protobuf.getBody());
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
        getLogger().trace(">>joinPracticeLobby: " + protobuf.getBody());
        send(protobuf);
    }

    public void leaveLobby() {
        if (lobby == null) {
            return;
        }
        ClientGCMsgProtobuf<CMsgPracticeLobbyLeave.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgPracticeLobbyLeave.class, EDOTAGCMsg.k_EMsgGCPracticeLobbyLeave_VALUE);
        getLogger().trace(">>leaveLobby: " + protobuf.getBody());
        send(protobuf);
    }

    public void abandonCurrentGame() {
        if (lobby == null) {
            return;
        }
        ClientGCMsgProtobuf<CMsgAbandonCurrentGame.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgAbandonCurrentGame.class, EDOTAGCMsg.k_EMsgGCAbandonCurrentGame_VALUE);
        getLogger().trace(">>abandonCurrentGame: " + protobuf.getBody());
        send(protobuf);
    }

    public void launchPracticeLobby() {
        if (lobby == null) {
            return;
        }
        ClientGCMsgProtobuf<CMsgPracticeLobbyLaunch.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgPracticeLobbyLaunch.class, EDOTAGCMsg.k_EMsgGCPracticeLobbyLaunch_VALUE);
        getLogger().trace(">>launchPracticeLobby: " + protobuf.getBody());
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
        getLogger().trace(">>practiceLobbySetTeamSlot: " + protobuf.getBody());
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
        getLogger().trace(">>joinPracticeLobbyBroadcastChannel: " + protobuf.getBody());
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
        getLogger().trace(">>addBotToPracticeLobby: " + protobuf.getBody());
        send(protobuf);
    }

    public void respondToLobbyInvite(long lobbyId, boolean accept) {
        ClientGCMsgProtobuf<CMsgLobbyInviteResponse.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgLobbyInviteResponse.class, EGCBaseMsg.k_EMsgGCLobbyInviteResponse_VALUE);
        protobuf.getBody().setLobbyId(lobbyId);
        protobuf.getBody().setAccept(accept);
        getLogger().trace(">>respondToLobbyInvite: " + protobuf.getBody());
        send(protobuf);
    }

    public void destroyLobby() {
        ClientGCMsgProtobuf<CMsgDOTADestroyLobbyRequest.Builder> protobuf = new ClientGCMsgProtobuf<>(
                CMsgDOTADestroyLobbyRequest.class, EDOTAGCMsg.k_EMsgDestroyLobbyRequest_VALUE);
        getLogger().trace(">>destroyLobby: " + protobuf.getBody());
        send(protobuf);
    }

    public void sendLobbyMessage(String text) {
        if (lobby != null) {
            ChatChannel channel = client.getChatHandler().getLobbyChatChannel(lobby.getLobbyId() + "");
            if (channel != null) {
                channel.send(text);
            }
        }
    }

    public ChatChannel getLobbyChatChannel() {
        ChatChannel channel = null;
        if (lobby != null) {
            channel = client.getChatHandler().getLobbyChatChannel(lobby.getLobbyId() + "");
        }
        return channel;
    }

    @Override
    public void handleGCMsg(IPacketGCMsg packetGCMsg) {
        Consumer<IPacketGCMsg> dispatcher = dispatchMap.get(packetGCMsg.getMsgType());
        if (dispatcher != null) {
            getLogger().trace(">>handleGCMsg lobby msg: " + packetGCMsg.getMsgType());
            dispatcher.accept(packetGCMsg);
        }
    }

    public CSODOTALobby getLobby() {
        return lobby;
    }

}
