package org.tomass.dota.gc.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.tomass.dota.gc.clients.impl.DotaClientImpl;
import org.tomass.dota.gc.config.AppConfig;
import org.tomass.dota.gc.config.LobbyMatch;
import org.tomass.dota.gc.config.ScheduledSeries;
import org.tomass.dota.gc.handlers.callbacks.league.LeagueNodeCallback;
import org.tomass.dota.gc.handlers.callbacks.lobby.LobbyNewCallback;
import org.tomass.dota.gc.util.DotaGlobalConstant;
import org.tomass.dota.gc.wrappers.SteamClientWrapper;

@Component
public class SeriesLobbyTask {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private AppConfig config;

    @Autowired
    private SteamClientWrapper steamClient;

    @Scheduled(cron = "${task.seriesLobbyTask}")
    public void scheduled() {
        logger.trace(">>seriesLobbyTask");
        for (ScheduledSeries scheduledLobby : config.getSeries()) {
            if (scheduledLobby.getState().equals(DotaGlobalConstant.LOBBY_SERIES_LIVE)
                    || scheduledLobby.getState().equals(DotaGlobalConstant.LOBBY_SERIES_RUNNING)) {
                DotaClientImpl client = steamClient.getClient();
                if (client != null && client.getLobbyHandler().getLobby() == null
                        && scheduledLobby.getLeagueId() != null && scheduledLobby.getNodeId() != null) {
                    LeagueNodeCallback leagueNode = client.getLeagueHandler()
                            .requestLeagueNode(scheduledLobby.getLeagueId(), scheduledLobby.getNodeId());
                    if (leagueNode != null && leagueNode.getBody().hasNode()) {
                        Integer team1Wins;
                        Integer team2Wins;

                        if (leagueNode.getBody().getNode().getTeam1Wins() == scheduledLobby.getTeamInfo1().getWins()) {
                            team1Wins = leagueNode.getBody().getNode().getTeam1Wins();
                            team2Wins = leagueNode.getBody().getNode().getTeam2Wins();
                        } else {
                            team1Wins = leagueNode.getBody().getNode().getTeam2Wins();
                            team2Wins = leagueNode.getBody().getNode().getTeam1Wins();
                        }

                        if (!team1Wins.equals(scheduledLobby.getTeamInfo1().getWins())
                                || !team2Wins.equals(scheduledLobby.getTeamInfo2().getWins())) {
                            scheduledLobby.getTeamInfo1().setWins(team1Wins);
                            scheduledLobby.getTeamInfo2().setWins(team2Wins);
                            scheduledLobby.setState(DotaGlobalConstant.LOBBY_SERIES_RUNNING);
                        } else {
                            logger.debug("==scheduledLobbyNewTask: Game is still live "
                                    + scheduledLobby.getTeamInfo1().getName() + " vs "
                                    + scheduledLobby.getTeamInfo2().getName());
                        }

                        if (leagueNode.getBody().getNode().getIsCompleted()) {
                            scheduledLobby.setState(DotaGlobalConstant.LOBBY_SERIES_COMPLETED);
                        }

                        if (scheduledLobby.getState() == DotaGlobalConstant.LOBBY_SERIES_RUNNING) {
                            LobbyNewCallback callback = client.requestNewScheduledTeamLobby(scheduledLobby);
                            if (callback != null) {
                                scheduledLobby.setState(DotaGlobalConstant.LOBBY_SERIES_CREATED);
                                scheduledLobby.getMatches().add(new LobbyMatch(callback.getLobby().getMatchId(),
                                        callback.getLobby().getLobbyId()));
                            }
                        }
                    }
                }
            }
        }
        logger.trace("<<seriesLobbyTask");
    }
}
