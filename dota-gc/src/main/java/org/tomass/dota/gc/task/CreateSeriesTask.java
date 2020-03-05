package org.tomass.dota.gc.task;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.tomass.dota.gc.config.AppConfig;
import org.tomass.dota.gc.config.LobbyMatch;
import org.tomass.dota.gc.config.ScheduledSeries;
import org.tomass.dota.gc.handlers.callbacks.lobby.LobbyNewCallback;
import org.tomass.dota.gc.util.DotaGlobalConstant;
import org.tomass.dota.gc.wrappers.SteamClientWrapper;

@Component
public class CreateSeriesTask {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private AppConfig config;

    @Autowired
    private SteamClientWrapper steamClient;

    @Scheduled(cron = "${task.createSeriesTask}")
    public void scheduled() {
        logger.trace(">>createSeriesTask");
        for (ScheduledSeries scheduledLobby : config.getSeries()) {
            steamClient.getClient().checkScheduledLobby(scheduledLobby);
            if (scheduledLobby.getScheduledTime().isBefore(LocalDateTime.now().plusMinutes(5l))) {
                if (scheduledLobby.getState() == DotaGlobalConstant.LOBBY_SERIES_SCHEDULED) {
                    LobbyNewCallback callback = steamClient.requestNewTeamLobby(scheduledLobby);
                    if (callback != null) {
                        scheduledLobby.setState(DotaGlobalConstant.LOBBY_SERIES_CREATED);
                        scheduledLobby.setSeriesId(callback.getLobby().getSeriesId());
                        scheduledLobby.setSeriesType(callback.getLobby().getSeriesType());
                        scheduledLobby.getMatches().add(
                                new LobbyMatch(callback.getLobby().getMatchId(), callback.getLobby().getLobbyId()));
                    }
                }
            }
        }
        logger.trace("<<createSeriesTask");
    }
}
