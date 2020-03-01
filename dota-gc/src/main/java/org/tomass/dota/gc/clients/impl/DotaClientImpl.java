package org.tomass.dota.gc.clients.impl;

import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.util.StringUtils;
import org.tomass.dota.gc.clients.Dota2Client;
import org.tomass.dota.gc.config.AppConfig;
import org.tomass.dota.gc.config.ScheduledSeries;
import org.tomass.dota.gc.config.SteamClientConfig;
import org.tomass.dota.gc.handlers.callbacks.ClientRichPresenceInfoCallback;
import org.tomass.dota.gc.handlers.callbacks.ReadyCallback;
import org.tomass.dota.gc.handlers.callbacks.chat.ChatMessageCallback;
import org.tomass.dota.gc.handlers.callbacks.lobby.LeagueAvailableLobbyNodes;
import org.tomass.dota.gc.handlers.callbacks.lobby.LobbyInviteCallback;
import org.tomass.dota.gc.handlers.callbacks.lobby.LobbyNewCallback;
import org.tomass.dota.gc.handlers.callbacks.lobby.LobbyRemovedCallback;
import org.tomass.dota.gc.handlers.callbacks.lobby.LobbyUpdatedCallback;
import org.tomass.dota.gc.handlers.callbacks.lobby.PracticeLobbyCallback;
import org.tomass.dota.gc.handlers.callbacks.match.TopSourceTvGamesCallback.Game;
import org.tomass.dota.gc.handlers.callbacks.party.PartyInviteCallback;
import org.tomass.dota.gc.handlers.callbacks.party.PartyNewCallback;
import org.tomass.dota.gc.util.CSOTypes;
import org.tomass.dota.gc.util.DotaGlobalConstant;
import org.tomass.dota.webapi.SteamDota2Match;
import org.tomass.dota.webapi.model.Player;
import org.tomass.dota.webapi.model.RealtimeStats;
import org.tomass.dota.webapi.model.TeamInfo;
import org.tomass.protobuf.dota.DotaGcmessagesCommonLeague.CMsgDOTALeagueAvailableLobbyNodes.NodeInfo.Builder;
import org.tomass.protobuf.dota.DotaGcmessagesCommonMatchManagement.CDOTALobbyMember;
import org.tomass.protobuf.dota.DotaGcmessagesCommonMatchManagement.CSODOTALobby;
import org.tomass.protobuf.dota.DotaGcmessagesCommonMatchManagement.CSODOTALobby.State;
import org.tomass.protobuf.dota.DotaGcmessagesCommonMatchManagement.CSODOTAParty;
import org.tomass.protobuf.dota.DotaSharedEnums.DOTASelectionPriorityChoice;
import org.tomass.protobuf.dota.DotaSharedEnums.DOTA_GC_TEAM;

import in.dragonbra.javasteam.enums.EChatEntryType;
import in.dragonbra.javasteam.steam.handlers.steamfriends.callback.FriendMsgCallback;
import in.dragonbra.javasteam.types.SteamID;
import in.dragonbra.javasteam.util.event.ScheduledFunction;

public class DotaClientImpl extends Dota2Client {

    private ScheduledFunction launchCountdown;

    private ScheduledFunction readycheck;

    public DotaClientImpl(SteamClientConfig config, AppConfig appConfig) {
        super(config, appConfig);
    }

    @Override
    protected void init() {
        super.init();

        manager.subscribe(ReadyCallback.class, this::onReady);
        manager.subscribe(ChatMessageCallback.class, this::onChatMessage);
        manager.subscribe(LobbyInviteCallback.class, this::onLobbyInvite);
        manager.subscribe(LobbyNewCallback.class, this::onLobbyNew);
        manager.subscribe(LobbyRemovedCallback.class, this::onLobbyRemoved);

        manager.subscribe(PartyInviteCallback.class, this::onPartyInvite);
        manager.subscribe(PartyNewCallback.class, this::onParty);

        manager.subscribe(FriendMsgCallback.class, this::onFriendMsg);
        manager.subscribe(LobbyUpdatedCallback.class, this::onLobbyUpdated);

        readycheck = createReadycheck();
    }

    private void onLobbyUpdated(LobbyUpdatedCallback callback) {
        stopScheduledLobby();
        if (!callback.getLobby().getSeriesCurrentNonPriorityTeamChoice()
                .equals(DOTASelectionPriorityChoice.k_DOTASelectionPriorityChoice_Invalid)
                && !callback.getLobby().getSeriesCurrentPriorityTeamChoice()
                        .equals(DOTASelectionPriorityChoice.k_DOTASelectionPriorityChoice_Invalid)) {
            if (callback.getLobby().getState().equals(State.UI)) {
                lobbyHandler.launchPracticeLobby();
            } else if (callback.getLobby().getState().equals(State.RUN)) {
                lobbyHandler.abandonCurrentGame();
                lobbyHandler.leaveLobby();
            }
        }
        if (isReadyToTournamentLaunch(callback.getLobby())) {
            getLobbyHandler().sendLobbyMessage("Lobby is ready to launch");
            launchScheduledLobby();
        }
    }

    private void onReady(ReadyCallback callback) {
        // chatHandler.leaveChannels();
        // partyHandler.leaveParty();
        // lobbyHandler.leaveLobby();
    }

    private void onFriendMsg(FriendMsgCallback callback) {
        logger.info("Accepting chat message: " + callback.getMessage());
        String message = callback.getMessage();
        long steamId = callback.getSender().convertToUInt64();
        SteamID requestSteamId = callback.getSender();
        try {
            if (message.startsWith("!info")) {
                if (message.contains(" ")) {
                    steamId = Long.parseLong(message.split(" ")[1]);
                }
                ClientRichPresenceInfoCallback info = (ClientRichPresenceInfoCallback) gameCoordinator
                        .requestClientRichPresence(steamId);
                logger.debug(info.getAll());
                if (info.getWatchableGameID() != null && info.getWatchableGameID() > 0) {
                    Game response = matchHandler.requestTopSourceTvGames(info.getWatchableGameID());
                    steamFriends.sendChatMessage(requestSteamId, EChatEntryType.ChatMsg,
                            response.toWellPrintedString());
                } else {
                    steamFriends.sendChatMessage(requestSteamId, EChatEntryType.ChatMsg, "Match was not found");
                }
            } else if (message.startsWith("!watch")) {
                if (message.contains(" ")) {
                    steamId = Long.parseLong(message.split(" ")[1]);
                }
                ClientRichPresenceInfoCallback info = (ClientRichPresenceInfoCallback) gameCoordinator
                        .requestClientRichPresence(steamId);
                if (info.getWatchingServer() != null && info.getWatchingServer().getAccountID() > 0) {
                    RealtimeStats stats = SteamDota2Match.getRealTimeStat(getConfiguration(),
                            info.getWatchingServer().getAccountID());
                    if (stats != null) {
                        steamFriends.sendChatMessage(requestSteamId, EChatEntryType.ChatMsg,
                                stats.getRadiant().toString());
                        steamFriends.sendChatMessage(requestSteamId, EChatEntryType.ChatMsg,
                                stats.getDire().toString());
                    } else {
                        steamFriends.sendChatMessage(requestSteamId, EChatEntryType.ChatMsg, "Match was not found");
                    }
                } else {
                    steamFriends.sendChatMessage(requestSteamId, EChatEntryType.ChatMsg, "Match was not found");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            steamFriends.sendChatMessage(requestSteamId, EChatEntryType.ChatMsg, "Match was not found");
        }
    }

    private void onChatMessage(ChatMessageCallback callback) {
        logger.info("Accepting chat message: " + callback.getBuilder().getText());
        if (callback.getChatChannel().getName().equals("Lobby_" + lobbyHandler.getLobby().getLobbyId())) {
            if (callback.getBuilder().getText().equals("!launch")) {
                if (lobbyHandler.getLobby() != null) {
                    if (isReadyToTournamentLaunch(lobbyHandler.getLobby())) {
                        launchScheduledLobby();
                    } else {
                        lobbyHandler.sendLobbyMessage(getNotReadyText(lobbyHandler.getLobby()));
                    }
                }
            } else if (callback.getBuilder().getText().equals("!launchnow")) {
                if (lobbyHandler.getLobby() != null) {
                    if (isReadyToTournamentLaunch(lobbyHandler.getLobby())) {
                        lobbyHandler.launchPracticeLobby();
                    } else {
                        lobbyHandler.sendLobbyMessage(getNotReadyText(lobbyHandler.getLobby()));
                    }
                }
            } else if (callback.getBuilder().getText().equals("!forcelaunch")) {
                lobbyHandler.launchPracticeLobby();
            } else if (callback.getBuilder().getText().equals("!stop")) {
                stopScheduledLobby();
            } else if (callback.getBuilder().getText().equals("!pass")) {
                lobbyHandler.sendLobbyMessage("Lobby password: " + lobbyHandler.getLobby().getPassKey());
            }
            for (Entry<String, String> command : appConfig.getChatCommands().entrySet()) {
                if (callback.getBuilder().getText().equals("!" + command.getKey())) {
                    lobbyHandler.sendLobbyMessage(command.getValue());
                }
            }
        }
    }

    public boolean isReadyToTournamentLaunch(CSODOTALobby lobby) {
        if (lobby != null) {
            if (!lobby.hasSeriesCurrentSelectionPriorityUsedCoinToss()) {
                if (lobby.getTeamDetailsCount() == 2 && !StringUtils.isEmpty(lobby.getTeamDetails(0).getTeamName())
                        && !StringUtils.isEmpty(lobby.getTeamDetails(1).getTeamName())) {
                    int goodGuysCount = 0;
                    int badGuysCount = 0;
                    for (CDOTALobbyMember member : lobby.getMembersList()) {
                        switch (member.getTeam()) {
                        case DOTA_GC_TEAM_BAD_GUYS:
                            badGuysCount++;
                            break;
                        case DOTA_GC_TEAM_GOOD_GUYS:
                            goodGuysCount++;
                            break;
                        default:
                            break;
                        }

                        if (goodGuysCount >= config.getLobbyCountMembers()
                                && badGuysCount >= config.getLobbyCountMembers()) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private void onLobbyInvite(LobbyInviteCallback callback) {
        logger.info("Accepting lobby invite: " + callback.getLobbyInvite().getGroupId());
        lobbyHandler.respondToLobbyInvite(callback.getLobbyInvite().getGroupId(), true);
    }

    private void onLobbyNew(LobbyNewCallback callback) {
        chatHandler.joinLobbyChannel();
        lobbyHandler.practiceLobbySetTeamSlot(DOTA_GC_TEAM.DOTA_GC_TEAM_PLAYER_POOL, 1);
        readycheck.start();
    }

    private void onLobbyRemoved(LobbyRemovedCallback callback) {
        Optional<ScheduledSeries> lobby = appConfig.getSeries().stream().filter(l -> l.getMatches().stream()
                .map(m -> m.getLobbyId()).collect(Collectors.toList()).contains(callback.getLobby().getLobbyId()))
                .findFirst();
        if (lobby.isPresent()) {
            lobby.get().setState(DotaGlobalConstant.LOBBY_SERIES_LIVE);
        }
        readycheck.stop();
    }

    private void onPartyInvite(PartyInviteCallback callback) {
        logger.info("Party invite: " + callback.getPartyInvite().getGroupId());
        partyHandler.respondToPartyInvite(callback.getPartyInvite().getGroupId(), false);
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

    public LobbyNewCallback requestNewTeamIdLobby(Integer leagueId, Integer team1Id, Integer team2Id, String password) {
        try {
            TeamInfo teamInfo1 = SteamDota2Match.getTeam(getConfiguration(), team1Id);
            TeamInfo teamInfo2 = SteamDota2Match.getTeam(getConfiguration(), team2Id);
            return requestNewTeamLobby(leagueId, teamInfo1, teamInfo2, password);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public LobbyNewCallback requestNewTeamLobby(Integer leagueId, TeamInfo teamInfo1, TeamInfo teamInfo2,
            String password) {
        PracticeLobbyCallback result = null;
        LobbyNewCallback callback = null;

        if (teamInfo1 != null && teamInfo2 != null) {
            if (leagueId != null) {
                LeagueAvailableLobbyNodes response = lobbyHandler.requestLeagueAvaiableNodes(leagueId);
                for (Builder nodeInfo : response.getBody().getNodeInfosBuilderList()) {
                    if ((nodeInfo.getTeamId1() == teamInfo1.getId().intValue()
                            || nodeInfo.getTeamId1() == teamInfo2.getId().intValue())
                            && (nodeInfo.getTeamId2() == teamInfo1.getId().intValue()
                                    || nodeInfo.getTeamId2() == teamInfo2.getId().intValue())) {
                        int nodeId = nodeInfo.getNodeId();
                        result = lobbyHandler.createTournamentLobby(password,
                                teamInfo1.getName() + " vs " + teamInfo2.getName(), leagueId, nodeId);
                        break;
                    }
                }
                if (result == null) {
                    result = lobbyHandler.createTournamentLobby(password,
                            teamInfo1.getName() + " vs " + teamInfo2.getName(), leagueId, 0);
                }
            }
            if (result == null) {
                result = lobbyHandler.createTournamentLobby(password,
                        teamInfo1.getName() + " vs " + teamInfo2.getName());
            }
            if (result.getResult().getNumber() <= 1) {
                callback = registerAndWait(CSOTypes.LOBBY_VALUE);

                for (Player player : teamInfo1.getPlayers()) {
                    lobbyHandler.inviteToLobby(player.getAccountId());
                }
                for (Player player : teamInfo2.getPlayers()) {
                    lobbyHandler.inviteToLobby(player.getAccountId());
                }
            }
        }
        return callback;
    }

    public String scheduledLeagueMatches(Integer leagueId) {
        StringBuilder response = new StringBuilder();

        LeagueAvailableLobbyNodes result = lobbyHandler.requestLeagueAvaiableNodes(leagueId);
        for (Builder nodeInfo : result.getBody().getNodeInfosBuilderList()) {
            TeamInfo teamInfo1 = SteamDota2Match.getTeam(getConfiguration(), nodeInfo.getTeamId1());
            TeamInfo teamInfo2 = SteamDota2Match.getTeam(getConfiguration(), nodeInfo.getTeamId2());
            response.append(nodeInfo.getNodeGroupName() + ": " + nodeInfo.getNodeId() + System.lineSeparator());
            response.append(teamInfo1.getName() + " vs " + teamInfo2.getName() + System.lineSeparator());
        }
        return response.toString();
    }

    public void launchScheduledLobby() {
        if (launchCountdown == null) {
            launchCountdown = createLobbyLaunchCountdown();
            launchCountdown.start();
        }
    }

    public void stopScheduledLobby() {
        if (launchCountdown != null) {
            launchCountdown.stop();
            launchCountdown = null;
        }
    }

    private ScheduledFunction createLobbyLaunchCountdown() {
        return (new ScheduledFunction(new Runnable() {

            private Integer countdown = 10;

            @Override
            public void run() {
                getLobbyHandler().sendLobbyMessage("Launching lobby in: " + countdown--);
                if (countdown < 1) {
                    lobbyHandler.launchPracticeLobby();
                    launchCountdown.stop();
                    launchCountdown = null;
                }
            }
        }, 1000));
    }

    private ScheduledFunction createReadycheck() {
        return (new ScheduledFunction(new Runnable() {

            @Override
            public void run() {
                String text = getNotReadyText(getLobbyHandler().getLobby());
                if (text != null) {
                    getLobbyHandler().sendLobbyMessage(text);
                }
                if (getLobbyHandler().getLobby().getLeaderId() == 0) {
                    getLobbyHandler().sendLobbyMessage("The league is not set");
                }
                if (getLobbyHandler().getLobby().getLeagueNodeId() == 0) {
                    getLobbyHandler().sendLobbyMessage("The series is not set");
                }
            }
        }, 60000));
    }

    public String getNotReadyText(CSODOTALobby lobby) {
        if (lobby.getTeamDetailsCount() != 2 || StringUtils.isEmpty(lobby.getTeamDetails(0).getTeamName())
                || StringUtils.isEmpty(lobby.getTeamDetails(1).getTeamName())) {
            return "Teams are not set";
        }
        int goodGuysCount = 0;
        int badGuysCount = 0;
        for (CDOTALobbyMember member : lobby.getMembersList()) {
            switch (member.getTeam()) {
            case DOTA_GC_TEAM_BAD_GUYS:
                badGuysCount++;
                break;
            case DOTA_GC_TEAM_GOOD_GUYS:
                goodGuysCount++;
                break;
            default:
                break;
            }
        }
        if (goodGuysCount < config.getLobbyCountMembers() || badGuysCount < config.getLobbyCountMembers()) {
            return "Teams are not complete";
        }

        return null;
    }

}
