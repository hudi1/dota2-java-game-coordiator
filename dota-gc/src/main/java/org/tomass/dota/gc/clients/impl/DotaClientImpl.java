package org.tomass.dota.gc.clients.impl;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections4.map.HashedMap;
import org.springframework.util.StringUtils;
import org.tomass.dota.gc.clients.Dota2Client;
import org.tomass.dota.gc.config.AppConfig;
import org.tomass.dota.gc.config.ScheduledSeries;
import org.tomass.dota.gc.config.SteamClientConfig;
import org.tomass.dota.gc.handlers.callbacks.ClientRichPresenceInfoCallback;
import org.tomass.dota.gc.handlers.callbacks.ReadyCallback;
import org.tomass.dota.gc.handlers.callbacks.chat.ChatMessageCallback;
import org.tomass.dota.gc.handlers.callbacks.league.LeagueAvailableLobbyNodes;
import org.tomass.dota.gc.handlers.callbacks.league.LeagueInfoAdmin;
import org.tomass.dota.gc.handlers.callbacks.lobby.LobbyInviteCallback;
import org.tomass.dota.gc.handlers.callbacks.lobby.LobbyNewCallback;
import org.tomass.dota.gc.handlers.callbacks.lobby.LobbyRemovedCallback;
import org.tomass.dota.gc.handlers.callbacks.lobby.LobbyUpdatedCallback;
import org.tomass.dota.gc.handlers.callbacks.lobby.PracticeLobbyCallback;
import org.tomass.dota.gc.handlers.callbacks.match.TopSourceTvGamesCallback;
import org.tomass.dota.gc.handlers.callbacks.party.PartyInviteCallback;
import org.tomass.dota.gc.handlers.callbacks.party.PartyNewCallback;
import org.tomass.dota.gc.util.CSOTypes;
import org.tomass.dota.gc.util.DotaGlobalConstant;
import org.tomass.dota.gc.util.LobbyDetailsFactory;
import org.tomass.dota.webapi.SteamDota2Match;
import org.tomass.dota.webapi.model.Player;
import org.tomass.dota.webapi.model.RealtimeStats;
import org.tomass.dota.webapi.model.TeamInfo;
import org.tomass.protobuf.dota.DotaGcmessagesClientMatchManagement.CMsgPracticeLobbySetDetails;
import org.tomass.protobuf.dota.DotaGcmessagesCommonLeague.CMsgDOTALeagueAvailableLobbyNodes.NodeInfo;
import org.tomass.protobuf.dota.DotaGcmessagesCommonLeague.CMsgDOTALeagueAvailableLobbyNodes.NodeInfo.Builder;
import org.tomass.protobuf.dota.DotaGcmessagesCommonLeague.CMsgDOTALeagueInfo;
import org.tomass.protobuf.dota.DotaGcmessagesCommonMatchManagement.CDOTALobbyMember;
import org.tomass.protobuf.dota.DotaGcmessagesCommonMatchManagement.CSODOTALobby;
import org.tomass.protobuf.dota.DotaGcmessagesCommonMatchManagement.CSODOTALobby.State;
import org.tomass.protobuf.dota.DotaGcmessagesCommonMatchManagement.CSODOTAParty;
import org.tomass.protobuf.dota.DotaSharedEnums.DOTASelectionPriorityChoice;
import org.tomass.protobuf.dota.DotaSharedEnums.DOTASelectionPriorityRules;
import org.tomass.protobuf.dota.DotaSharedEnums.DOTA_GC_TEAM;

import in.dragonbra.javasteam.enums.EAccountType;
import in.dragonbra.javasteam.enums.EChatEntryType;
import in.dragonbra.javasteam.enums.EUniverse;
import in.dragonbra.javasteam.steam.handlers.steamfriends.callback.FriendMsgCallback;
import in.dragonbra.javasteam.steam.steamclient.configuration.SteamConfiguration;
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
    }

    private void onLobbyUpdated(LobbyUpdatedCallback callback) {
        stopScheduledLobby();
        if (!callback.getLobby().getSeriesCurrentNonPriorityTeamChoice()
                .equals(DOTASelectionPriorityChoice.k_DOTASelectionPriorityChoice_Invalid)
                && !callback.getLobby().getSeriesCurrentPriorityTeamChoice()
                        .equals(DOTASelectionPriorityChoice.k_DOTASelectionPriorityChoice_Invalid)) {
            if (callback.getLobby().getState().equals(State.UI)) {
                lobbyHandler.launchPracticeLobby();
            }
        }
        if (callback.getLobby().getState().equals(State.RUN)) {
            lobbyHandler.abandonCurrentGame();
            lobbyHandler.leaveLobby();
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
                    TopSourceTvGamesCallback response = matchHandler.requestTopSourceTvGames(info.getWatchableGameID());
                    steamFriends.sendChatMessage(requestSteamId, EChatEntryType.ChatMsg,
                            response.getBuilder().getGameList(0) + "");
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
            } else if (callback.getBuilder().getText().equals("!forcelaunch")) {
                lobbyHandler.launchPracticeLobby();
            } else if (callback.getBuilder().getText().equals("!stop")) {
                stopScheduledLobby();
            } else if (callback.getBuilder().getText().equals("!pass")) {
                lobbyHandler.sendLobbyMessage("Lobby password: " + lobbyHandler.getLobby().getPassKey());
            } else {
                Long playerId = new SteamID(callback.getBuilder().getAccountId(), EUniverse.Public,
                        EAccountType.Individual).convertToUInt64();
                if (appConfig.getSteamIdAdmins().contains(playerId)) {
                    if (callback.getBuilder().getText().startsWith("!config")) {
                        if (callback.getBuilder().getText().split(" ").length > 2) {
                            String action = callback.getBuilder().getText().split(" ")[1];
                            String value = callback.getBuilder().getText().split(" ")[2];
                            configureLobby(action, value);
                        } else {
                            lobbyHandler.sendLobbyMessage("Missing action or value");
                        }
                    }
                    if (callback.getBuilder().getText().startsWith("!kick")) {
                        if (callback.getBuilder().getText().split(" ").length > 1) {
                            SteamID kick = new SteamID(Long.parseLong(callback.getBuilder().getText().split(" ")[1]));
                            getLobbyHandler().practiceLobbyKick((int) kick.getAccountID());
                        }
                    }
                }
                for (Entry<String, String> command : appConfig.getChatCommands().entrySet()) {
                    if (callback.getBuilder().getText().equals("!" + command.getKey())) {
                        lobbyHandler.sendLobbyMessage(command.getValue());
                    }
                }
            }
        }
    }

    private void onLobbyInvite(LobbyInviteCallback callback) {
        logger.info("Accepting lobby invite: " + callback.getLobbyInvite().getGroupId());
        lobbyHandler.respondToLobbyInvite(callback.getLobbyInvite().getGroupId(), true);
    }

    private void onLobbyNew(LobbyNewCallback callback) {
        chatHandler.joinLobbyChannel();
        lobbyHandler.practiceLobbySetTeamSlot(DOTA_GC_TEAM.DOTA_GC_TEAM_PLAYER_POOL, 1);
        Optional<ScheduledSeries> lobby = appConfig.getSeries().stream().filter(l -> l.getMatches().stream()
                .map(m -> m.getLobbyId()).collect(Collectors.toList()).contains(callback.getLobby().getLobbyId()))
                .findFirst();
        if (lobby.isPresent()) {
            readycheck = createLobbycheck(lobby.get());
            readycheck.start();
        }
    }

    private void onLobbyRemoved(LobbyRemovedCallback callback) {
        Optional<ScheduledSeries> lobby = appConfig.getSeries().stream().filter(l -> l.getMatches().stream()
                .map(m -> m.getLobbyId()).collect(Collectors.toList()).contains(callback.getLobby().getLobbyId()))
                .findFirst();
        if (lobby.isPresent()) {
            lobby.get().setState(DotaGlobalConstant.LOBBY_SERIES_LIVE);
        }
        if (readycheck != null) {
            readycheck.stop();
        }
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

    public LobbyNewCallback requestNewScheduledTeamLobby(ScheduledSeries serie) {
        LobbyNewCallback callback = null;
        CMsgPracticeLobbySetDetails.Builder detail = LobbyDetailsFactory.createTournamentLobby(serie.getPassword(),
                serie.getSeriesName(), serie.getLeagueId(), serie.getNodeId());
        PracticeLobbyCallback result = lobbyHandler.createPracticeLobby(detail.build());
        if (result.getResult().getNumber() <= 1) {
            callback = registerAndWait(CSOTypes.LOBBY_VALUE);
            serie.setDetail(detail);

            for (Player player : serie.getTeamInfo1().getPlayers()) {
                lobbyHandler.inviteToLobby(player.getAccountId());
            }
            for (Player player : serie.getTeamInfo2().getPlayers()) {
                lobbyHandler.inviteToLobby(player.getAccountId());
            }
        }
        return callback;
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

    public NodeInfo getNode(Integer leagueId, Integer team1Id, Integer team2Id) {
        LeagueAvailableLobbyNodes response = leagueHandler.requestLeagueAvaiableNodes(leagueId);
        if (response != null) {
            for (NodeInfo nodeInfo : response.getBody().getNodeInfosList()) {
                if ((nodeInfo.getTeamId1() == team1Id.intValue() || nodeInfo.getTeamId1() == team2Id.intValue())
                        && (nodeInfo.getTeamId2() == team1Id.intValue()
                                || nodeInfo.getTeamId2() == team2Id.intValue())) {
                    return nodeInfo;
                }
            }
        }
        return null;
    }

    public String getScheduledLeagueMatches(Integer leagueId) {
        StringBuilder response = new StringBuilder();
        LeagueAvailableLobbyNodes result = leagueHandler.requestLeagueAvaiableNodes(leagueId);
        for (Builder nodeInfo : result.getBody().getNodeInfosBuilderList()) {
            TeamInfo teamInfo1 = SteamDota2Match.getTeam(getConfiguration(), nodeInfo.getTeamId1());
            TeamInfo teamInfo2 = SteamDota2Match.getTeam(getConfiguration(), nodeInfo.getTeamId2());
            response.append(nodeInfo.getNodeGroupName() + ": " + nodeInfo.getNodeId() + System.lineSeparator());
            response.append(teamInfo1.getName() + " vs " + teamInfo2.getName() + System.lineSeparator());
        }
        return response.toString();
    }

    private void configureLobby(String action, String value) {
        if (getLobbyHandler().getLobby() != null) {
            Optional<ScheduledSeries> serie = appConfig
                    .getSeries().stream().filter(l -> l.getMatches().stream().map(m -> m.getLobbyId())
                            .collect(Collectors.toList()).contains(getLobbyHandler().getLobby().getLobbyId()))
                    .findFirst();
            if (serie.isPresent()) {
                CMsgPracticeLobbySetDetails.Builder details = serie.get().getDetail();
                switch (action.toLowerCase()) {
                case "server":
                    details.setServerRegion(Integer.parseInt(value));
                    break;
                case "mode":
                    details.setGameMode(Integer.parseInt(value));
                    break;
                case "priority":
                    details.setSelectionPriorityRules(DOTASelectionPriorityRules.forNumber(Integer.parseInt(value)));
                    break;
                case "series":
                    details.setSeriesType(Integer.parseInt(value));
                    break;
                case "pass":
                    details.setPassKey(value);
                    break;
                default:
                }

                getLobbyHandler().configPracticeLobby(details.build());
            }
        }

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

    private ScheduledFunction createLobbycheck(ScheduledSeries series) {
        return (new ScheduledFunction(new Runnable() {

            private ScheduledSeries series;
            private Map<Long, Integer> invitations = new HashedMap<>();

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

                if (series != null) {
                    invitePlayer(series.getTeamInfo1(), invitations);
                    invitePlayer(series.getTeamInfo2(), invitations);
                }
            }

            public Runnable withSeries(ScheduledSeries series) {
                this.series = series;
                return this;
            }

        }.withSeries(series), 60000));
    }

    private void invitePlayer(TeamInfo team, Map<Long, Integer> invitations) {
        for (Player player : team.getPlayers()) {
            Long playerId = new SteamID(player.getAccountId(), EUniverse.Public, EAccountType.Individual)
                    .convertToUInt64();
            if (!getLobbyHandler().getLobby().getMembersList().stream().map(m -> m.getId()).collect(Collectors.toList())
                    .contains(playerId)) {
                if (!getLobbyHandler().getLobby().getPendingInvitesList().contains(playerId)) {
                    if (!invitations.containsKey(playerId)) {
                        invitations.put(playerId, 0);
                        getLobbyHandler().inviteToLobby(playerId);
                    } else {
                        if (invitations.get(playerId) < 3) {
                            getLobbyHandler().inviteToLobby(playerId);
                            invitations.put(playerId, invitations.get(playerId) + 1);
                        }
                    }
                }
            }
        }
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

    public String getTournamentName(Integer leagueId) {
        LeagueInfoAdmin admins = leagueHandler.requestLeagueInfoAdmins();
        for (CMsgDOTALeagueInfo info : admins.getBody().getInfosList()) {
            if (info.getLeagueId() == leagueId.intValue()) {
                return info.getName();
            }
        }

        return null;
    }

    public void checkScheduledLobby(ScheduledSeries serie) {
        if (serie.getLeagueName() == null) {
            serie.setLeagueName(getTournamentName(serie.getLeagueId()));
        }
        if (serie.getTeamInfo1() == null || serie.getTeamInfo1().getName() == null) {
            serie.setTeamInfo1(
                    SteamDota2Match.getTeam(SteamConfiguration.create(c -> c.withWebAPIKey(appConfig.getSteamWebApi())),
                            serie.getTeamInfo1().getId()));
        }
        if (serie.getTeamInfo2() == null || serie.getTeamInfo2().getName() == null) {
            serie.setTeamInfo2(
                    SteamDota2Match.getTeam(SteamConfiguration.create(c -> c.withWebAPIKey(appConfig.getSteamWebApi())),
                            serie.getTeamInfo2().getId()));
        }
        if (serie.getSeriesName() == null) {
            if (serie.getTeamInfo1().getName() != null && serie.getTeamInfo2().getName() != null) {
                serie.setSeriesName(serie.getTeamInfo1().getName() + " vs " + serie.getTeamInfo2().getName());
            }
        }
        if (serie.getNodeId() == null) {
            NodeInfo nodeInfo = getNode(serie.getLeagueId(), serie.getTeamInfo1().getId(),
                    serie.getTeamInfo2().getId());
            if (nodeInfo != null) {
                serie.setNodeId(nodeInfo.getNodeId());
                serie.setNodeName(nodeInfo.getNodeName());
            }
        }
    }

}
