package org.tomass.dota.gc.task;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.tomass.dota.dao.SerieDao;
import org.tomass.dota.gc.clients.impl.DotaClientImpl;
import org.tomass.dota.gc.config.AppConfig;
import org.tomass.dota.gc.handlers.callbacks.league.LeagueNodeCallback;
import org.tomass.dota.gc.util.DotaGlobalConstant;
import org.tomass.dota.gc.wrappers.SteamClientWrapper;
import org.tomass.dota.model.Serie;
import org.tomass.dota.model.Serie.Association;

@Component
public class SeriesTask implements DotaGlobalConstant {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private AppConfig config;

    @Autowired
    private SteamClientWrapper steamClient;

    @Autowired
    private SerieDao serieDao;

    @Scheduled(cron = "${task.seriesTask}")
    public void scheduled() {
        logger.trace(">>seriesTask");
        for (Serie serie : serieDao.list(new Serie().withInit_(Association.team1).withInit_(Association.team2))) {
            if (serie.getState().equals(SCHEDULED)) {
                steamClient.getClient().checkScheduledLobby(serie);
            }

            if (serie.getState().equals(INIT)) {
                if (serie.getScheduledTime().isBefore(LocalDateTime.now().plusMinutes(5l))) {
                    steamClient.requestSeriesLobby(serie);
                }
            } else if (serie.getState().equals(LIVE) || serie.getState().equals(RUNNING)) {
                if (serie.getLeagueId() != null && serie.getNodeId() != null) {
                    DotaClientImpl client = steamClient.getClient();
                    LeagueNodeCallback leagueNode = client.getLeagueHandler().requestLeagueNode(serie.getLeagueId(),
                            serie.getNodeId());
                    if (leagueNode != null && leagueNode.getBody().hasNode()) {
                        if (leagueNode.getBody().getNode().getIsCompleted()) {
                            serie.setState(DotaGlobalConstant.COMPLETED);
                        }

                        if (serie.getState().equals(LIVE)) {
                            Integer team1Wins = leagueNode.getBody().getNode().getTeam1Wins();
                            Integer team2Wins = leagueNode.getBody().getNode().getTeam2Wins();
                            if (team1Wins.equals(serie.getTeam1Wins()) && team2Wins.equals(serie.getTeam2Wins())) {
                                logger.debug("==scheduledLobbyNewTask: Game is still live " + serie.getName());
                            } else {
                                serie.setTeam1Wins(team1Wins);
                                serie.setTeam2Wins(team2Wins);
                                serie.setState(RUNNING);
                            }
                        }
                    }
                }
                if (serie.getState().equals(RUNNING)) {
                    steamClient.requestSeriesLobby(serie);
                }
            }
        }
        logger.trace("<<seriesTask");
    }

}
