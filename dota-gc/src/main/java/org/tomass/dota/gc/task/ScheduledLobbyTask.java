package org.tomass.dota.gc.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tomass.dota.gc.config.AppConfig;
import org.tomass.dota.gc.util.DotaGlobalConstant;
import org.tomass.dota.gc.wrappers.SteamClientWrapper;

@Component
public class ScheduledLobbyTask implements DotaGlobalConstant {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private AppConfig config;

    @Autowired
    private SteamClientWrapper steamClient;

    // @Scheduled(cron = "${task.scheduledLobbyTask}")
    // public void scheduled() {
    // logger.trace(">>scheduledLobbyTask");
    // for (ScheduledLobby lobby : config.getLobbies()) {
    // if (lobby.getState() == SCHEDULED) {
    // if (lobby.getScheduledTime().isBefore(LocalDateTime.now().plusMinutes(5l))) {
    // steamClient.requestLobby(lobby);
    // }
    // }
    // }
    // logger.trace("<<scheduledLobbyTask");
    // }

}
