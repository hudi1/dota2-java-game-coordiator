package org.tomass.dota.gc.rest;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.tomass.dota.gc.handlers.callbacks.league.LeagueAvailableLobbyNodes;
import org.tomass.dota.gc.handlers.callbacks.league.LeagueInfoAdmin;
import org.tomass.dota.gc.handlers.features.Dota2League;
import org.tomass.dota.gc.rest.util.Role;
import org.tomass.dota.gc.wrappers.SteamClientWrapper;
import org.tomass.dota.model.rest.LeagueAvailableLobbyNodesRest;
import org.tomass.dota.model.rest.LeagueInfoAdminListRest;

@RestController
public class HandlerController extends BaseCommonController {

    @Autowired
    private SteamClientWrapper steamClient;

    @RequestMapping(value = "/steam/client/handler/league/admin", method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    @Secured({ Role.CONFIG })
    public ResponseEntity<LeagueInfoAdminListRest> leagueAdmin(HttpServletRequest request) {

        Dota2League handler = steamClient.getClient().getLeagueHandler();
        LeagueInfoAdminListRest rest = null;

        LeagueInfoAdmin leagueInfo = handler.requestLeagueInfoAdmins();
        if (leagueInfo != null) {
            rest = leagueInfo.getRest();
        }

        return new ResponseEntity<>(rest, HttpStatus.OK);
    }

    @RequestMapping(value = "/steam/client/handler/league/nodes", method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    @Secured({ Role.CONFIG })
    public ResponseEntity<LeagueAvailableLobbyNodesRest> availableNodes(@RequestParam Integer leagueId,
            HttpServletRequest request) {

        Dota2League handler = steamClient.getClient().getLeagueHandler();
        LeagueAvailableLobbyNodesRest rest = null;

        LeagueAvailableLobbyNodes availableNodes = handler.requestLeagueAvailableNodes(leagueId);
        if (availableNodes != null) {
            rest = availableNodes.getRest(steamClient.getClient().getConfig().getSteamWebApi());
        }

        return new ResponseEntity<>(rest, HttpStatus.OK);
    }

}
