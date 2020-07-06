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
import org.tomass.dota.dao.SerieDao;
import org.tomass.dota.dao.TeamDao;
import org.tomass.dota.gc.config.AppConfig;
import org.tomass.dota.gc.util.DotaGlobalConstant;
import org.tomass.dota.gc.wrappers.SteamClientWrapper;
import org.tomass.dota.model.Serie;
import org.tomass.dota.model.SerieList;
import org.tomass.dota.model.Team;

@RestController
public class ConfigSeriesController extends BaseCommonController {

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private SteamClientWrapper steamClient;

    @Autowired
    private TeamDao teamDao;

    @Autowired
    private SerieDao serieDao;

    @RequestMapping(value = "/schedule/series/all", method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<SerieList> listSeries() {
        return new ResponseEntity<SerieList>(new SerieList(serieDao.list(new Serie())), HttpStatus.OK);
    }

    @RequestMapping(value = "/schedule/series", method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<Serie> konfigurujApp(@RequestParam(required = false) Integer leagueId,
            @RequestParam(required = false) Integer team1Id, @RequestParam(required = false) Integer team2Id,
            @RequestParam String password,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime scheduledTime,
            HttpServletRequest request) {

        Team team1Db = teamDao.get(new Team().withTeamId(team1Id));
        if (team1Db == null) {
            team1Db = new Team();
            team1Db.setTeamId(team1Id);
            teamDao.insert(team1Db);
        }

        Team team2Db = teamDao.get(new Team().withTeamId(team2Id));
        if (team2Db == null) {
            team2Db = new Team();
            team2Db.setTeamId(team2Id);
            teamDao.insert(team2Db);
        }

        Serie serie = new Serie(leagueId, password, team1Db, 0, team2Db, 0, DotaGlobalConstant.SCHEDULED, scheduledTime,
                0);
        serieDao.insert(serie);
        return new ResponseEntity<>(serie, HttpStatus.OK);
    }

}
