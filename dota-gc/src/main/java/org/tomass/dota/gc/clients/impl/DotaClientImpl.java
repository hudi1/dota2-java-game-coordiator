package org.tomass.dota.gc.clients.impl;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.sqlproc.engine.impl.SqlStandardControl;
import org.tomass.dota.dao.PlayerDao;
import org.tomass.dota.dao.SerieDao;
import org.tomass.dota.dao.TeamDao;
import org.tomass.dota.dao.TeamPlayerDao;
import org.tomass.dota.gc.clients.Dota2Client;
import org.tomass.dota.gc.clients.model.LobbyInvitation;
import org.tomass.dota.gc.clients.model.LobbyReadyInfo;
import org.tomass.dota.gc.config.SteamClientConfig;
import org.tomass.dota.gc.handlers.callbacks.ClientRichPresenceInfoCallback;
import org.tomass.dota.gc.handlers.callbacks.ReadyCallback;
import org.tomass.dota.gc.handlers.callbacks.chat.ChatMembersJoinUpdateCallback;
import org.tomass.dota.gc.handlers.callbacks.chat.ChatMessageCallback;
import org.tomass.dota.gc.handlers.callbacks.league.LeagueAvailableLobbyNodes;
import org.tomass.dota.gc.handlers.callbacks.league.LeagueInfoAdmin;
import org.tomass.dota.gc.handlers.callbacks.league.LeagueNodeCallback;
import org.tomass.dota.gc.handlers.callbacks.lobby.LobbyInvitationCreatedCallback;
import org.tomass.dota.gc.handlers.callbacks.lobby.LobbyInviteCallback;
import org.tomass.dota.gc.handlers.callbacks.lobby.LobbyNewCallback;
import org.tomass.dota.gc.handlers.callbacks.lobby.LobbyRemovedCallback;
import org.tomass.dota.gc.handlers.callbacks.lobby.LobbyUpdatedCallback;
import org.tomass.dota.gc.handlers.callbacks.lobby.PracticeLobbyCallback;
import org.tomass.dota.gc.handlers.callbacks.match.MatchDetailsCallback;
import org.tomass.dota.gc.handlers.callbacks.match.MatchSignedOutCallback;
import org.tomass.dota.gc.handlers.callbacks.match.TopSourceTvGamesCallback;
import org.tomass.dota.gc.handlers.callbacks.party.PartyInviteCallback;
import org.tomass.dota.gc.handlers.callbacks.party.PartyNewCallback;
import org.tomass.dota.gc.util.CSOTypes;
import org.tomass.dota.gc.util.DotaGlobalConstant;
import org.tomass.dota.gc.util.LobbyDetailsFactory;
import org.tomass.dota.model.DotaClientInfo;
import org.tomass.dota.model.Player;
import org.tomass.dota.model.Serie;
import org.tomass.dota.model.Team;
import org.tomass.dota.model.Team.Association;
import org.tomass.dota.model.TeamPlayer;
import org.tomass.dota.webapi.SteamDota2Match;
import org.tomass.dota.webapi.model.RealtimeStats;
import org.tomass.dota.webapi.model.TeamInfo;
import org.tomass.protobuf.dota.DotaGcmessagesClientMatchManagement.CMsgPracticeLobbySetDetails;
import org.tomass.protobuf.dota.DotaGcmessagesCommonLeague.CMsgDOTALeagueAvailableLobbyNodes.NodeInfo;
import org.tomass.protobuf.dota.DotaGcmessagesCommonLeague.CMsgDOTALeagueAvailableLobbyNodes.NodeInfo.Builder;
import org.tomass.protobuf.dota.DotaGcmessagesCommonLeague.CMsgDOTALeagueInfo;
import org.tomass.protobuf.dota.DotaGcmessagesCommonMatchManagement.CDOTALobbyMember;
import org.tomass.protobuf.dota.DotaGcmessagesCommonMatchManagement.CLobbyTeamDetails;
import org.tomass.protobuf.dota.DotaGcmessagesCommonMatchManagement.CSODOTALobby;
import org.tomass.protobuf.dota.DotaGcmessagesCommonMatchManagement.CSODOTALobby.State;
import org.tomass.protobuf.dota.DotaGcmessagesCommonMatchManagement.CSODOTAParty;
import org.tomass.protobuf.dota.DotaSharedEnums.DOTAJoinLobbyResult;
import org.tomass.protobuf.dota.DotaSharedEnums.DOTASelectionPriorityChoice;
import org.tomass.protobuf.dota.DotaSharedEnums.DOTASelectionPriorityRules;
import org.tomass.protobuf.dota.DotaSharedEnums.DOTA_GC_TEAM;
import org.tomass.protobuf.dota.DotaSharedEnums.EMatchOutcome;

import in.dragonbra.javasteam.enums.EAccountType;
import in.dragonbra.javasteam.enums.EChatEntryType;
import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.enums.EUniverse;
import in.dragonbra.javasteam.steam.handlers.steamfriends.callback.FriendMsgCallback;
import in.dragonbra.javasteam.steam.steamclient.configuration.SteamConfiguration;
import in.dragonbra.javasteam.types.SteamID;
import in.dragonbra.javasteam.util.event.ScheduledFunction;

public class DotaClientImpl extends Dota2Client {

    private ScheduledFunction launchCountdown;

    private ScheduledFunction readycheck;

    private Map<Long, LobbyInvitation> invitations = new ConcurrentHashMap<>();

    @Autowired
    private SerieDao serieDao;

    @Autowired
    private TeamDao teamDao;

    @Autowired
    private PlayerDao playerDao;

    @Autowired
    private TeamPlayerDao teamPlayerDao;

    public DotaClientImpl(SteamClientConfig config) {
        super(config);
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

        manager.subscribe(MatchSignedOutCallback.class, this::onMatchSignedOut);
        manager.subscribe(ChatMembersJoinUpdateCallback.class, this::onChatMembersJoin);
        manager.subscribe(LobbyInvitationCreatedCallback.class, this::onLobbyInvitationCreated);
    }

    private void onLobbyInvitationCreated(LobbyInvitationCreatedCallback callback) {
        invitations.compute(callback.getBuilder().getSteamId(),
                (k, v) -> v == null ? new LobbyInvitation() : v.incrementAndReturn());
    }

    private void onChatMembersJoin(ChatMembersJoinUpdateCallback callback) {
        if (getLobbyHandler().getLobbyChatChannel() != null) {
            if (callback.getChannel().getId() == getLobbyHandler().getLobbyChatChannel().getId()) {
                Serie serie = serieDao.get(new Serie().withActualLobbyId(getLobbyHandler().getLobby().getLobbyId()));
                Team team1 = null;
                Team team2 = null;
                if (serie != null) {
                    team1 = teamDao.get(new Team().withId(serie.getTeam1().getId()).withInit_(Association.players));
                    team2 = teamDao.get(new Team().withId(serie.getTeam2().getId()).withInit_(Association.players));
                }

                Optional<CDOTALobbyMember> newMember = getLobbyHandler().getLobby().getMembersList().stream()
                        .filter(m -> m.getId() == callback.getPlayerSteamId()).findFirst();
                if (newMember.isPresent()) {
                    SteamID steam = new SteamID(newMember.get().getId());
                    String team = null;
                    if (team1 != null && team1.getPlayers().stream().map(p -> p.getAccountId())
                            .anyMatch(p -> p.longValue() == steam.getAccountID())) {
                        team = "Radiant";
                    }
                    if (team2 != null && team2.getPlayers().stream().map(p -> p.getAccountId())
                            .anyMatch(p -> p.longValue() == steam.getAccountID())) {
                        team = "Dire";
                    }
                    if (team != null) {
                        getLobbyHandler().sendLobbyMessage(getString("helloNew", newMember.get().getName(), team));
                    }
                }
            }
        }
    }

    private void onLobbyUpdated(LobbyUpdatedCallback callback) {
        stopScheduledLobby();
        Serie serie = serieDao.get(new Serie().withActualLobbyId(callback.getLobby().getLobbyId()));
        Team team1 = null;
        Team team2 = null;
        if (serie != null) {
            team1 = teamDao.get(new Team().withId(serie.getTeam1().getId()).withInit_(Association.players));
            team2 = teamDao.get(new Team().withId(serie.getTeam2().getId()).withInit_(Association.players));
        }
        LobbyReadyInfo info = getInfo(callback.getLobby(), team1, team2);

        if (info.isCoinTossReady()) {
            lobbyHandler.launchPracticeLobby();
        }

        if (callback.getLobby().getState().equals(State.RUN)) {
            lobbyHandler.abandonCurrentGame();
            lobbyHandler.leaveLobby();
            if (serie != null) {
                serie.setState(DotaGlobalConstant.LIVE);
                serie.setActualMatchId(callback.getLobby().getMatchId());
                serieDao.update(serie);
            }
        }

        if (info.isReady()) {
            getLobbyHandler().sendLobbyMessage(getString("lobbyReady"));
            launchScheduledLobby();
        }

        for (Entry<Long, LobbyInvitation> entry : invitations.entrySet()) {
            if (callback.getLobby().getMembersList().stream().map(m -> (Long) m.getId()).collect(Collectors.toSet())
                    .contains(entry.getKey())) {
                entry.getValue().setAcepted(true);
            }
            if (!entry.getValue().isAcepted()
                    && !callback.getLobby().getPendingInvitesList().contains(entry.getKey())) {
                entry.getValue().setDeclined(true);
            }
        }
    }

    private void onReady(ReadyCallback callback) {
        // chatHandler.leaveChannels();
        // partyHandler.leaveParty();
        // lobbyHandler.leaveLobby();
    }

    private void onMatchSignedOut(MatchSignedOutCallback callback) {
        Long matchId = callback.getBuilder().getMatchId();
        MatchDetailsCallback details = getMatchHandler().requestMatchDetails(matchId);

        if (details != null && details.getBuilder().getResult() == EResult.OK.code()) {
            Serie serie = serieDao.get(new Serie().withActualMatchId(matchId));
            LeagueNodeCallback leagueNode = getLeagueHandler().requestLeagueNode(serie.getLeagueId(),
                    serie.getNodeId());

            if (serie != null && (leagueNode == null || !leagueNode.getBody().hasNode())) {
                int direTeamId = details.getBuilder().getMatch().getDireTeamId();
                int radiantTeamId = details.getBuilder().getMatch().getRadiantTeamId();
                EMatchOutcome outcome = details.getBuilder().getMatch().getMatchOutcome();
                if (outcome != null) {
                    switch (outcome) {
                    case k_EMatchOutcome_DireVictory:
                        if (serie.getTeam1().getId().intValue() == direTeamId) {
                            serie.setTeam1Wins(serie.getTeam1Wins() + 1);
                        }
                        if (serie.getTeam2().getId().intValue() == direTeamId) {
                            serie.setTeam2Wins(serie.getTeam2Wins() + 1);
                        }
                        break;
                    case k_EMatchOutcome_RadVictory:
                        if (serie.getTeam1().getId().intValue() == radiantTeamId) {
                            serie.setTeam1Wins(serie.getTeam1Wins() + 1);
                        }
                        if (serie.getTeam2().getId().intValue() == radiantTeamId) {
                            serie.setTeam2Wins(serie.getTeam2Wins() + 1);
                        }
                        break;
                    default:
                        break;
                    }
                }
                if (serie.getTeam1Wins() > serie.getBestOf() / 2 || serie.getTeam1Wins() > serie.getBestOf()) {
                    serie.setState(DotaGlobalConstant.COMPLETED);
                } else {
                    serie.setState(DotaGlobalConstant.RUNNING);
                }
                serieDao.update(serie);
            }
        }
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
            logger.error("!!onFriendMsg ", e);
            steamFriends.sendChatMessage(requestSteamId, EChatEntryType.ChatMsg, "Match was not found");
        }
    }

    private void onChatMessage(ChatMessageCallback callback) {
        logger.info("Accepting chat message: " + callback.getBuilder().getText());
        if (getLobbyHandler().getLobby() != null
                && callback.getChatChannel().getName().equals("Lobby_" + getLobbyHandler().getLobby().getLobbyId())) {
            if (callback.getBuilder().getText().equals("!launch")) {
                if (getLobbyHandler().getLobby() != null) {
                    LobbyReadyInfo info = getInfo(lobbyHandler.getLobby(), null, null);
                    if (info.isReady()) {
                        launchScheduledLobby();
                    } else {
                        for (String message : info.getMessages()) {
                            getLobbyHandler().sendLobbyMessage(message);
                        }
                    }
                }
            } else if (callback.getBuilder().getText().equals("!forcelaunch")) {
                // TODO popup error
                getLobbyHandler().launchPracticeLobby();
            } else if (callback.getBuilder().getText().equals("!stop")) {
                stopScheduledLobby();
            } else if (callback.getBuilder().getText().equals("!pass")) {
                getLobbyHandler()
                        .sendLobbyMessage(getString("lobbyPassword", getLobbyHandler().getLobby().getPassKey()));
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
                            getLobbyHandler().sendLobbyMessage("Missing action or value");
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
                        getLobbyHandler().sendLobbyMessage(command.getValue());
                    }
                }
            }
        }
    }

    private void onLobbyInvite(LobbyInviteCallback callback) {
        logger.info("Accepting lobby invite: " + callback.getLobbyInvite().getGroupId());
        // lobbyHandler.respondToLobbyInvite(callback.getLobbyInvite().getGroupId(), true);
    }

    private void onLobbyNew(LobbyNewCallback callback) {
        chatHandler.joinLobbyChannel(callback.getLobby());
        lobbyHandler.practiceLobbySetTeamSlot(DOTA_GC_TEAM.DOTA_GC_TEAM_PLAYER_POOL, 1);
        if (readycheck != null) {
            readycheck.stop();
        }
        readycheck = createLobbycheck();
        readycheck.start();
    }

    private void onLobbyRemoved(LobbyRemovedCallback callback) {
        Serie serie = serieDao.get(new Serie().withActualLobbyId(callback.getLobby().getLobbyId()));
        getChatHandler().leaveChannels();
        invitations.clear();
        if (serie != null && serie.getState().equals(DotaGlobalConstant.CREATED)) {
            serie.setState(DotaGlobalConstant.COMPLETED);
            serieDao.update(serie);
        }

        if (readycheck != null) {
            readycheck.stop();
        } else {
            logger.warn("!!onLobbyRemoved " + readycheck);
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

    public LobbyNewCallback requestSeriesLobby(Serie serie) {
        CMsgPracticeLobbySetDetails.Builder detail = LobbyDetailsFactory.createSeriesLobby(serie);
        serie.setDetail(detail.build().toByteArray());
        LobbyNewCallback callback = createPracticeLobby(detail);
        if (callback != null) {
            invitePlayers(serie.getTeam1());
            invitePlayers(serie.getTeam2());
            serie.setState(DotaGlobalConstant.CREATED);
            serie.setSeriesId(callback.getLobby().getSeriesId());
            serie.setBestOf(callback.getLobby().getSeriesType());
            serie.setActualLobbyId(callback.getLobby().getLobbyId());
            serie.setActualMatchId(null);
            serieDao.update(serie);
        }
        return callback;
    }

    public LobbyNewCallback requestLobby(Serie serie) {
        CMsgPracticeLobbySetDetails.Builder detail = LobbyDetailsFactory.createLobby(serie);
        serie.setDetail(detail.build().toByteArray());
        LobbyNewCallback callback = createPracticeLobby(detail);
        if (callback != null) {
            invitePlayers(serie.getTeam1());
            invitePlayers(serie.getTeam2());
            serie.setState(DotaGlobalConstant.CREATED);
            serie.setActualLobbyId(callback.getLobby().getLobbyId());
            serieDao.update(serie);
        }
        return callback;
    }

    public LobbyNewCallback requestSimpleLobby(String name, String password) {
        CMsgPracticeLobbySetDetails.Builder detail = LobbyDetailsFactory.createSimpleLobby(name, password);
        Serie serie = new Serie();
        serie.setName(name);
        serie.setPassword(password);
        serie.setDetail(detail.build().toByteArray());
        LobbyNewCallback callback = createPracticeLobby(detail);
        if (callback != null) {
            invitePlayers(serie.getTeam1());
            invitePlayers(serie.getTeam2());
            serie.setState(DotaGlobalConstant.CREATED);
            serie.setActualLobbyId(callback.getLobby().getLobbyId());
        }
        return callback;
    }

    private LobbyNewCallback createPracticeLobby(CMsgPracticeLobbySetDetails.Builder detail) {
        PracticeLobbyCallback result = lobbyHandler.createPracticeLobby(detail.build());
        if (result.getResult().getNumber() <= 1) {
            getSubscribers().remove(CSOTypes.LOBBY_VALUE);
            return registerAndWait(CSOTypes.LOBBY_VALUE, 60l);
        } else {
            throw new RuntimeException(DOTAJoinLobbyResult.forNumber(result.getResult().getNumber()).name());
        }
    }

    private void invitePlayers(Team team) {
        if (team != null) {
            team = teamDao.get(team.withInit_(org.tomass.dota.model.Team.Association.players));
            for (Player player : team.getPlayers()) {
                lobbyHandler.inviteToLobby(player.getAccountId());
            }
        }
    }

    public LobbyReadyInfo getInfo(CSODOTALobby lobby, Team team1, Team team2) {
        LobbyReadyInfo info = new LobbyReadyInfo();

        // team
        Integer teamCount = 0;
        for (CLobbyTeamDetails teamDetail : lobby.getTeamDetailsList()) {
            if (team1 != null && team2 != null) {
                if (teamDetail.getTeamId() != team1.getTeamId().intValue()
                        && teamDetail.getTeamId() != team2.getTeamId().intValue()) {
                    if (info.getMessages().isEmpty()) {
                        info.getMessages().add(getString("validTeam", team1.getName(), team2.getName()));
                    }
                }
            }
            if (teamDetail.hasTeamName()) {
                teamCount++;
            }
        }
        if (teamCount < 2) {
            info.getMessages().add(getString("setTeam"));
        }

        // members
        int goodGuysCount = 0;
        int badGuysCount = 0;
        for (CDOTALobbyMember member : lobby.getMembersList()) {
            if (team1 != null && team2 != null) {
                Integer steamId = (int) new SteamID(member.getId()).getAccountID();
                if (team1.getPlayers().stream().anyMatch(p -> p.getAccountId().equals(steamId))) {
                    if (member.getTeam().equals(DOTA_GC_TEAM.DOTA_GC_TEAM_BAD_GUYS)) {
                        info.getMessages().add(getString("playerWrongTeam", member.getName()));
                    }
                }
                if (team2.getPlayers().stream().anyMatch(p -> p.getAccountId().equals(steamId))) {
                    if (member.getTeam().equals(DOTA_GC_TEAM.DOTA_GC_TEAM_GOOD_GUYS)) {
                        info.getMessages().add(getString("playerWrongTeam", member.getName()));
                    }
                }
            }

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
            info.getMessages().add(getString("completeTeam"));
        }

        // coin toss
        if (info.getMessages().isEmpty()) {
            if (!lobby.getSeriesCurrentNonPriorityTeamChoice()
                    .equals(DOTASelectionPriorityChoice.k_DOTASelectionPriorityChoice_Invalid)
                    && !lobby.getSeriesCurrentPriorityTeamChoice()
                            .equals(DOTASelectionPriorityChoice.k_DOTASelectionPriorityChoice_Invalid)) {
                if (lobby.getState().equals(State.UI)) {
                    info.setCoinTossReady(true);
                }
            } else {
                if (!lobby.hasSeriesCurrentSelectionPriorityUsedCoinToss()) {
                    info.setReady(true);
                }
            }
        }
        return info;
    }

    public NodeInfo getNode(Integer leagueId, Integer team1Id, Integer team2Id) {
        LeagueAvailableLobbyNodes response = leagueHandler.requestLeagueAvailableNodes(leagueId);
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
        LeagueAvailableLobbyNodes result = leagueHandler.requestLeagueAvailableNodes(leagueId);
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
            Serie serie = serieDao.get(new Serie().withActualLobbyId(getLobbyHandler().getLobby().getLobbyId()));
            try {
                if (serie != null) {
                    CMsgPracticeLobbySetDetails.Builder details = CMsgPracticeLobbySetDetails
                            .parseFrom(serie.getDetail()).toBuilder();
                    switch (action.toLowerCase()) {
                    case "server":
                        details.setServerRegion(Integer.parseInt(value));
                        break;
                    case "mode":
                        details.setGameMode(Integer.parseInt(value));
                        break;
                    case "priority":
                        details.setSelectionPriorityRules(
                                DOTASelectionPriorityRules.forNumber(Integer.parseInt(value)));
                        break;
                    case "series":
                        details.setSeriesType(Integer.parseInt(value));
                        break;
                    case "pass":
                        details.setPassKey(value);
                        break;
                    case "penaltyRadiant":
                        details.setPenaltyLevelRadiant(Integer.parseInt(value));
                        break;
                    case "penaltyDire":
                        details.setPenaltyLevelDire(Integer.parseInt(value));
                        break;
                    default:
                    }
                    getLobbyHandler().configPracticeLobby(details.build());
                }
            } catch (Exception e) {
                logger.error("!! configureLobby ", e);
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
                getLobbyHandler().sendLobbyMessage(getString("launchingLobby", countdown--));
                if (countdown < 1) {
                    lobbyHandler.launchPracticeLobby();
                    launchCountdown.stop();
                    launchCountdown = null;
                }
            }
        }, 1000));
    }

    private ScheduledFunction createLobbycheck() {
        return (new ScheduledFunction(new Runnable() {

            private LocalDateTime created = LocalDateTime.now();
            private Serie serie;
            private Team team1;
            private Team team2;

            @Override
            public void run() {
                if (getLobbyHandler().getLobby() != null) {
                    if (serie == null) {
                        serie = serieDao.get(new Serie().withActualLobbyId(getLobbyHandler().getLobby().getLobbyId()));
                        if (serie != null) {
                            team1 = teamDao
                                    .get(new Team().withId(serie.getTeam1().getId()).withInit_(Association.players));
                            team2 = teamDao
                                    .get(new Team().withId(serie.getTeam2().getId()).withInit_(Association.players));
                        }
                    }

                    long seconds = Duration.between(created, LocalDateTime.now()).getSeconds();
                    if (seconds > 1800) {
                        getLobbyHandler().sendLobbyMessage(getString("timeIsOver"));
                        if (serie != null) {
                            serie.setState(DotaGlobalConstant.COMPLETED);
                            serieDao.update(serie);
                        }
                        getLobbyHandler().destroyLobby();
                    } else {
                        getLobbyHandler().sendLobbyMessage(getString("remainingMinutes", ((1800 - seconds) / 60)));
                    }

                    LobbyReadyInfo info = getInfo(getLobbyHandler().getLobby(), team1, team2);
                    for (String message : info.getMessages()) {
                        getLobbyHandler().sendLobbyMessage(message);
                    }

                    if (getLobbyHandler().getLobby().getLeagueNodeId() == 0) {
                        logger.warn("!!lobbycheck  " + getString("lobbyNode"));
                    }
                    repeatPlayerInvite();
                }
            }

        }, 60000));
    }

    private void repeatPlayerInvite() {
        for (Entry<Long, LobbyInvitation> entry : invitations.entrySet()) {
            if (entry.getValue().isDeclined() && entry.getValue().getCount() < 1) {
                getLobbyHandler().inviteToLobbyLong(entry.getKey());
            }
        }
    }

    private String getTournamentName(Integer leagueId) {
        LeagueInfoAdmin admins = leagueHandler.requestLeagueInfoAdmins();
        if (admins != null) {
            for (CMsgDOTALeagueInfo info : admins.getBody().getInfosList()) {
                if (info.getLeagueId() == leagueId.intValue()) {
                    return info.getName();
                }
            }
        }
        return null;
    }

    public void checkScheduledLobby(Serie serie) {
        if (serie.getLeagueName() == null) {
            serie.setLeagueName(getTournamentName(serie.getLeagueId()));
        }
        if (serie.getTeam1().getName() == null) {
            updateTeamInfo(serie.getTeam1().getTeamId());
        }
        if (serie.getTeam2().getName() == null) {
            updateTeamInfo(serie.getTeam2().getTeamId());
        }
        if (serie.getName() == null) {
            if (serie.getTeam1().getName() != null && serie.getTeam2().getName() != null) {
                serie.setName(serie.getTeam1().getName() + " vs " + serie.getTeam2().getName());
            }
        }
        if (serie.getNodeId() == null) {
            NodeInfo nodeInfo = getNode(serie.getLeagueId(), serie.getTeam1().getId(), serie.getTeam2().getId());
            if (nodeInfo != null) {
                serie.setNodeId(nodeInfo.getNodeId());
                serie.setNodeName(nodeInfo.getNodeName());
            } else {
                serie.setNodeId(0);
            }
        }
        if (serie.getName() != null) {
            serie.setState(DotaGlobalConstant.INIT);
        }
        serieDao.update(serie);
    }

    private void updateTeamInfo(Integer teamId) {
        TeamInfo teamInfo = SteamDota2Match
                .getTeam(SteamConfiguration.create(c -> c.withWebAPIKey(config.getSteamWebApi())), teamId);
        if (teamInfo != null) {
            Team teamDb = teamDao.get(new Team().withTeamId(teamId));
            teamDb.setName(teamInfo.getName());
            teamDao.update(teamDb);
            SqlStandardControl sqlControl = new SqlStandardControl();
            sqlControl.setSqlName("DELETE_TEAM_PLAYER_BY_TEAM");
            teamPlayerDao.delete(new TeamPlayer().withTeamId(teamDb.getId()), sqlControl);

            for (org.tomass.dota.webapi.model.Player player : teamInfo.getPlayers()) {
                Player playerDb = playerDao.get(new Player().withAccountId(player.getAccountId()));
                if (playerDb == null) {
                    playerDb = new Player();
                    playerDb.setAccountId(player.getAccountId());
                    playerDao.insert(playerDb);
                }

                TeamPlayer teamPlayer = new TeamPlayer();
                teamPlayer.setPlayerId(playerDb.getId());
                teamPlayer.setTeamId(teamDb.getId());
                teamPlayerDao.insert(teamPlayer);
            }
        }
    }

    public DotaClientInfo getInfo() {
        DotaClientInfo info = new DotaClientInfo();
        info.setName(config.getUser());
        info.setLogged(logged);
        info.setReady(ready);
        if (result != null) {
            info.setResult(result.name());
        }
        if (getLobbyHandler().getLobby() != null) {
            info.setLobbyId(getLobbyHandler().getLobby().getLobbyId());
        }
        if (getPartyHandler().getParty() != null) {
            info.setPartyId(getPartyHandler().getParty().getPartyId());
        }

        return info;
    }

}
