package org.tomass.dota.gc.task;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.tomass.dota.gc.clients.impl.DotaClientImpl;
import org.tomass.dota.gc.config.AppConfig;
import org.tomass.dota.gc.config.LobbyMatch;
import org.tomass.dota.gc.config.ScheduledSeries;
import org.tomass.dota.gc.handlers.callbacks.lobby.LobbyNewCallback;
import org.tomass.dota.gc.util.DotaGlobalConstant;
import org.tomass.dota.gc.wrappers.SteamClientWrapper;
import org.tomass.dota.webapi.SteamDota2Match;

@Component
public class ScheduledLobbyCreatorTask {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private AppConfig config;

    @Autowired
    private SteamClientWrapper steamClient;

    @Scheduled(cron = "5 * * * * *")
    public void scheduled() {
        logger.trace(">>scheduledLobbyCreatorTask");
        for (ScheduledSeries scheduledLobby : config.getSeries()) {
            DotaClientImpl client = steamClient.getClient();
            if (client != null) {
                if (scheduledLobby.getTeamInfo1().getName() == null) {
                    scheduledLobby.setTeamInfo1(
                            SteamDota2Match.getTeam(client.getConfiguration(), scheduledLobby.getTeamInfo1().getId()));
                }
                if (scheduledLobby.getTeamInfo1().getName() == null) {
                    scheduledLobby.setTeamInfo2(
                            SteamDota2Match.getTeam(client.getConfiguration(), scheduledLobby.getTeamInfo2().getId()));
                }

                if (scheduledLobby.getScheduledTime().isBefore(LocalDateTime.now().plusMinutes(5l))) {
                    if (scheduledLobby.getState() == DotaGlobalConstant.LOBBY_SERIES_SCHEDULED) {
                        LobbyNewCallback callback = steamClient.requestNewTeamLobby(scheduledLobby.getLeagueId(),
                                scheduledLobby.getTeamInfo1(), scheduledLobby.getTeamInfo2(),
                                scheduledLobby.getPassword());
                        if (callback != null) {
                            scheduledLobby.setState(DotaGlobalConstant.LOBBY_SERIES_CREATED);
                            scheduledLobby.setSeriesId(callback.getLobby().getSeriesId());
                            scheduledLobby.setSeriesType(callback.getLobby().getSeriesType());
                            scheduledLobby.setNodeId(callback.getLobby().getLeagueNodeId());
                            scheduledLobby.getMatches().add(
                                    new LobbyMatch(callback.getLobby().getMatchId(), callback.getLobby().getLobbyId()));
                        }
                    }
                }
            }
        }
        logger.trace("<<scheduledLobbyCreatorTask");
    }
}
