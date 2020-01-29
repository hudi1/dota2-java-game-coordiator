package org.tomass.dota.gc.clients.impl;

import org.tomass.dota.gc.clients.Dota2Client;
import org.tomass.dota.gc.config.SteamClientConfig;
import org.tomass.dota.gc.handlers.callbacks.chat.ChatMessageCallback;
import org.tomass.dota.gc.handlers.callbacks.lobby.LobbyInviteCallback;
import org.tomass.dota.gc.handlers.callbacks.lobby.LobbyNewCallback;
import org.tomass.dota.gc.handlers.callbacks.party.PartyInviteCallback;
import org.tomass.dota.gc.handlers.callbacks.party.PartyNewCallback;
import org.tomass.protobuf.dota.DotaGcmessagesCommonMatchManagement.CSODOTAParty;
import org.tomass.protobuf.dota.DotaSharedEnums.DOTA_GC_TEAM;

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

        partyHandler.leaveParty();
        lobbyHandler.leaveLobby();
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
