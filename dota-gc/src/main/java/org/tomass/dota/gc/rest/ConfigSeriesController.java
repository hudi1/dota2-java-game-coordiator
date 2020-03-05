package org.tomass.dota.gc.rest;

import java.time.LocalDateTime;

import javax.servlet.http.HttpServletRequest;

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
import org.tomass.dota.webapi.model.TeamInfo;

@RestController
public class ConfigSeriesController extends BaseCommonController {

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private SteamClientWrapper steamClient;

    @RequestMapping(value = "/schedule/series", method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<ScheduledSeries> konfigurujApp(@RequestParam(required = false) Integer leagueId,
            @RequestParam(required = false) Integer team1Id, @RequestParam(required = false) Integer team2Id,
            @RequestParam String password,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime scheduledTime,
            HttpServletRequest request) {

        ScheduledSeries serie = new ScheduledSeries(leagueId, new TeamInfo(team1Id), new TeamInfo(team2Id), password,
                scheduledTime);
        steamClient.getClient().checkScheduledLobby(serie);
        serie.setAdmin(steamClient.getLeagueAdmin(leagueId));
        appConfig.getSeries().add(serie);
        return new ResponseEntity<>(serie, HttpStatus.OK);
    }

}
