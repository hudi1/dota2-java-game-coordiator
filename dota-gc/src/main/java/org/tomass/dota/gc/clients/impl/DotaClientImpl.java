package org.tomass.dota.gc.clients.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.util.StringUtils;
import org.tomass.dota.gc.clients.Dota2Client;
import org.tomass.dota.gc.config.AppConfig;
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

import in.dragonbra.javasteam.enums.EAccountType;
import in.dragonbra.javasteam.enums.EChatEntryType;
import in.dragonbra.javasteam.enums.EUniverse;
import in.dragonbra.javasteam.steam.handlers.steamfriends.callback.FriendMsgCallback;
import in.dragonbra.javasteam.steam.webapi.WebAPI;
import in.dragonbra.javasteam.types.KeyValue;
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
            } else {
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
        // TODO wait all welcoming messages
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        chatHandler.leaveChannels();
        partyHandler.leaveParty();
        lobbyHandler.leaveLobby();
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
                if (info.getWatchingServer() != null) {
                    WebAPI api = getConfiguration().getWebAPI("IDOTA2MatchStats_570");
                    Map<String, String> params = new LinkedHashMap<>();
                    params.put("server_steam_id", info.getWatchingServer().convertToUInt64() + "");
                    params.put("key", appConfig.getSteamWebApi());
                    KeyValue response = api.call("GetRealtimeStats", params);
                    RealtimeStats stats = RealtimeStats.parseFrom(response);
                    steamFriends.sendChatMessage(requestSteamId, EChatEntryType.ChatMsg, stats.getRadiant().toString());
                    steamFriends.sendChatMessage(requestSteamId, EChatEntryType.ChatMsg, stats.getDire().toString());
                } else {
                    steamFriends.sendChatMessage(requestSteamId, EChatEntryType.ChatMsg, "Match was not found");
                }
            } else if (message.startsWith("!lobby")) {
                if (appConfig.getSteamIdAdmins().contains(steamId)) {
                    String action = null;
                    if (message.split(" ").length > 1) {
                        action = message.split(" ")[1];
                    } else {
                        steamFriends.sendChatMessage(requestSteamId, EChatEntryType.ChatMsg, "Action was not set");
                        return;
                    }
                    if (action.equals("leave")) {
                        lobbyHandler.leaveLobby();
                        return;
                    }
                    if (lobbyHandler.getLobby() == null) {
                        if (action.equals("new")) {
                            if (message.split(" ").length > 2) {
                                String password = message.split(" ")[2];
                                lobbyHandler.createPracticeLobby(password, null);
                                lobbyHandler.inviteToLobby(steamId);
                            } else {
                                steamFriends.sendChatMessage(requestSteamId, EChatEntryType.ChatMsg,
                                        "Password was not set");
                            }
                        }

                        if (action.equals("tournament")) {
                            if (message.split(" ").length > 2) {
                                String password = message.split(" ")[2];
                                lobbyHandler.createTournamentLobby(password, null);
                                lobbyHandler.inviteToLobby(steamId);
                            } else {
                                steamFriends.sendChatMessage(requestSteamId, EChatEntryType.ChatMsg,
                                        "Password was not set");
                            }
                        }

                        if (action.equals("team")) {
                            if (message.split(" ").length > 4) {
                                String password = message.split(" ")[2];
                                Integer team1Id = Integer.parseInt(message.split(" ")[3]);
                                Integer team2Id = Integer.parseInt(message.split(" ")[4]);
                                requestNewTeamLobby(team1Id, team2Id, password);
                            } else {
                                steamFriends.sendChatMessage(requestSteamId, EChatEntryType.ChatMsg,
                                        "Password and teams was not set");
                            }
                        }
                    } else {
                        steamFriends.sendChatMessage(requestSteamId, EChatEntryType.ChatMsg, "Lobby already created");
                    }
                } else {
                    steamFriends.sendChatMessage(requestSteamId, EChatEntryType.ChatMsg,
                            "Sorry you have no permission to call this");
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
            } else if (callback.getBuilder().getText().equals("!stop")) {
                stopScheduledLobby();
            } else if (callback.getBuilder().getText().equals("!pass")) {
                SteamID sender = new SteamID(callback.getBuilder().getAccountId(), EUniverse.Public,
                        EAccountType.Individual);
                steamFriends.sendChatMessage(sender, EChatEntryType.ChatMsg,
                        "Lobby password: " + lobbyHandler.getLobby().getPassKey());
            }
        }
    }

    public boolean isReadyToTournamentLaunch(CSODOTALobby lobby) {
        if (lobby != null) {
            if (!lobby.getSeriesCurrentSelectionPriorityUsedCoinToss()) {
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
                                && badGuysCount > config.getLobbyCountMembers()) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
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
        readycheck.stop();
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

    public void requestNewTeamLobby(Integer team1Id, Integer team2Id, String password) {
        try {
            TeamInfo teamInfo1 = getTeam(team1Id);
            TeamInfo teamInfo2 = getTeam(team2Id);
            PracticeLobbyCallback result = null;

            if (teamInfo1 != null && teamInfo2 != null) {
                if (config.getLeagueId() != null) {
                    LeagueAvailableLobbyNodes response = lobbyHandler.requestLeagueAvaiableNodes(config.getLeagueId());
                    for (Builder nodeInfo : response.getBody().getNodeInfosBuilderList()) {
                        if ((nodeInfo.getTeamId1() == team1Id.intValue() || nodeInfo.getTeamId1() == team2Id.intValue())
                                && (nodeInfo.getTeamId2() == team1Id.intValue()
                                        || nodeInfo.getTeamId2() == team2Id.intValue())) {
                            int nodeId = nodeInfo.getNodeId();
                            result = lobbyHandler.createTournamentLobby(password,
                                    teamInfo1.getName() + " vs " + teamInfo2.getName(), config.getLeagueId(), nodeId);
                            break;
                        }
                    }
                }
                if (result == null) {
                    result = lobbyHandler.createTournamentLobby(password,
                            teamInfo1.getName() + " vs " + teamInfo2.getName());
                }
                if (result.getResult().getNumber() > 1) {
                    return;
                }
                registerAndWait(CSOTypes.LOBBY_VALUE, 10l);

                for (Player player : teamInfo1.getPlayers()) {
                    // lobbyHandler.inviteToLobby(player.getAccountId());
                }
                for (Player player : teamInfo2.getPlayers()) {
                    // lobbyHandler.inviteToLobby(player.getAccountId());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private TeamInfo getTeam(Integer teamId) {
        if (teamId == null) {
            return null;
        }
        TeamInfo teamInfo = null;
        try {
            if (teamId != null) {
                WebAPI api = getConfiguration().getWebAPI("IDOTA2Match_570");
                Map<String, String> params = new LinkedHashMap<>();
                params.put("start_at_team_id", teamId + "");
                params.put("teams_requested", "1");
                params.put("key", appConfig.getSteamWebApi());
                KeyValue response = api.call("GetTeamInfoByTeamID", params);
                teamInfo = TeamInfo.parseFrom(response);
                if (teamInfo.getName() == null) {
                    teamInfo.setName(teamId.toString());
                }
            }
        } catch (Exception e) {
            teamInfo = new TeamInfo();
            teamInfo.setName(teamId + "");
        }
        return teamInfo;
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

}
