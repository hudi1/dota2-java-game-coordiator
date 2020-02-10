package org.tomass.dota.gc.clients.impl;

import org.tomass.dota.gc.clients.Dota2Client;
import org.tomass.dota.gc.config.SteamClientConfig;
import org.tomass.dota.gc.handlers.callbacks.ClientRichPresenceInfoCallback;
import org.tomass.dota.gc.handlers.callbacks.ReadyCallback;
import org.tomass.dota.gc.handlers.callbacks.chat.ChatMessageCallback;
import org.tomass.dota.gc.handlers.callbacks.lobby.LobbyInviteCallback;
import org.tomass.dota.gc.handlers.callbacks.lobby.LobbyNewCallback;
import org.tomass.dota.gc.handlers.callbacks.match.TopSourceTvGamesCallback.Game;
import org.tomass.dota.gc.handlers.callbacks.party.PartyInviteCallback;
import org.tomass.dota.gc.handlers.callbacks.party.PartyNewCallback;
import org.tomass.protobuf.dota.DotaGcmessagesCommonMatchManagement.CSODOTAParty;
import org.tomass.protobuf.dota.DotaSharedEnums.DOTA_GC_TEAM;

import in.dragonbra.javasteam.enums.EChatEntryType;
import in.dragonbra.javasteam.steam.handlers.steamfriends.callback.FriendMsgCallback;
import in.dragonbra.javasteam.types.SteamID;

public class DotaClientImpl extends Dota2Client {

    public DotaClientImpl(SteamClientConfig config) {
        super(config);
    }

    @Override
    protected void init() {
        super.init();

        manager.subscribe(ChatMessageCallback.class, this::onChatMessage);
        manager.subscribe(LobbyInviteCallback.class, this::onLobbyInvite);
        manager.subscribe(LobbyNewCallback.class, this::onLobbyNew);

        manager.subscribe(PartyInviteCallback.class, this::onPartyInvite);
        manager.subscribe(PartyNewCallback.class, this::onParty);
        manager.subscribe(ReadyCallback.class, this::onReady);

        manager.subscribe(FriendMsgCallback.class, this::onFriendMsg);
    }

    private void onReady(ReadyCallback callback) {
        partyHandler.leaveParty();
        lobbyHandler.leaveLobby();
    }

    private void onFriendMsg(FriendMsgCallback callback) {
        logger.info("Accepting chat message: " + callback.getMessage());
        String message = callback.getMessage();
        long steamId = callback.getSender().convertToUInt64();
        SteamID requestSteamId = callback.getSender();
        if (message.startsWith("!info")) {
            if (message.contains(" ")) {
                steamId = Long.parseLong(message.split(" ")[1]);
            }
            ClientRichPresenceInfoCallback info = (ClientRichPresenceInfoCallback) gameCoordinator
                    .requestClientRichPresence(steamId);
            if (info.getWatchableGameID() != null) {
                Game response = matchHandler.requestTopSourceTvGames(info.getWatchableGameID());
                steamFriends.sendChatMessage(requestSteamId, EChatEntryType.ChatMsg, response + "");
            } else {
                steamFriends.sendChatMessage(requestSteamId, EChatEntryType.ChatMsg, "Match was not found");
            }
        }
    }

    private void onChatMessage(ChatMessageCallback callback) {
        logger.info("Accepting chat message: " + callback.getBuilder().getText());
    }

    private void onLobbyInvite(LobbyInviteCallback callback) {
        logger.info("Accepting lobby invite: " + callback.getLobbyInvite().getGroupId());
        lobbyHandler.respondToLobbyInvite(callback.getLobbyInvite().getGroupId(), true);
    }

    private void onLobbyNew(LobbyNewCallback callback) {
        chatHandler.joinLobbyChannel();
        lobbyHandler.joinPracticeLobbyTeam(DOTA_GC_TEAM.DOTA_GC_TEAM_PLAYER_POOL, 1);
    }

    private void onPartyInvite(PartyInviteCallback callback) {
        logger.info("Accepting party invite: " + callback.getPartyInvite().getGroupId());
        partyHandler.respondToPartyInvite(callback.getPartyInvite().getGroupId(), true);
    }

    private void onParty(PartyNewCallback callback) {
        this.partyHandler.setPartyCoach(true);
        this.chatHandler.joinPartyChannel();
        CSODOTAParty party = callback.getParty();

        if (party.getMembersCount() > config.getPartyCountMembers()) {
            logger.debug("Party is complete, readyCheck");
            this.partyHandler.readyCheck();
        }

        if (party.hasReadyCheck()) {
            if (party.getReadyCheck().hasStartTimestamp() && !party.getReadyCheck().hasFinishTimestamp()) {
                logger.debug("Ready to play");
                this.partyHandler.readyCheckAcknowledgeReady();
            }

            if (party.getReadyCheck().getReadyMembersCount() > config.getPartyCountMembers()) {
                logger.debug("Party is ready, leaving");
                this.partyHandler.leaveParty();
            }
        }
    }

}
