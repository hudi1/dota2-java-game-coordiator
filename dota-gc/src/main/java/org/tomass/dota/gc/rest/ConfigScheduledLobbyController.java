package org.tomass.dota.gc.rest;

import java.time.LocalDateTime;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.tomass.dota.gc.config.AppConfig;
import org.tomass.dota.gc.config.ScheduledSeries;
import org.tomass.dota.gc.wrappers.SteamClientWrapper;
import org.tomass.dota.webapi.SteamDota2Match;
import org.tomass.dota.webapi.model.TeamInfo;

import in.dragonbra.javasteam.steam.steamclient.configuration.SteamConfiguration;

@RestController
public class ConfigScheduledLobbyController extends BaseCommonController {

    private final Logger log = LoggerFactory.getLogger(ConfigController.class);

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private SteamClientWrapper steamClient;

    @RequestMapping(value = "/schedule/lobby", method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<ScheduledSeries> konfigurujApp(@RequestParam Integer leagueId, @RequestParam Integer team1Id,
            @RequestParam Integer team2Id, @RequestParam String password,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime scheduledTime,
            HttpServletRequest request) {

        ScheduledSeries lobby = new ScheduledSeries(leagueId, new TeamInfo(team1Id), new TeamInfo(team2Id), password,
                scheduledTime);
        lobby.setTeamInfo1(SteamDota2Match
                .getTeam(SteamConfiguration.create(c -> c.withWebAPIKey(appConfig.getSteamWebApi())), team1Id));
        lobby.setTeamInfo2(SteamDota2Match
                .getTeam(SteamConfiguration.create(c -> c.withWebAPIKey(appConfig.getSteamWebApi())), team2Id));
        appConfig.getSeries().add(lobby);
        return new ResponseEntity<>(lobby, HttpStatus.OK);
    }

}
