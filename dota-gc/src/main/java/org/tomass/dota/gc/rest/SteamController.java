package org.tomass.dota.gc.rest;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.tomass.dota.gc.wrappers.SteamClientWrapper;

@RestController
public class SteamController extends BaseCommonController {

    private final Logger log = LoggerFactory.getLogger(SteamController.class);

    @Autowired
    private SteamClientWrapper steamClient;

    @RequestMapping(value = "/steam/client", method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    @Secured({ Role.CONFIG })
    public ResponseEntity<String> konfigurujSteam(@RequestParam String funkce, @RequestParam String klient,
            @RequestParam(required = false) boolean bezKonverze, @RequestParam(required = false) String args,
            @RequestParam(required = false) Integer batch, @RequestParam(required = false) String oddelovace,
            HttpServletRequest request) {

        log.debug(">>steamApp '{}' '{}' '{}' '{}'", funkce, args, batch, oddelovace);
        String result = konfigurujBatch(funkce, bezKonverze, args, batch, steamClient.getSteamClient(klient),
                oddelovace);
        log.debug("<<steamApp '{}'", result);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/steam/client/config", method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    @Secured({ Role.CONFIG })
    public ResponseEntity<String> konfigurujClient(@RequestParam String funkce, @RequestParam String klient,
            @RequestParam(required = false) boolean bezKonverze, @RequestParam(required = false) String args,
            @RequestParam(required = false) Integer batch, @RequestParam(required = false) String oddelovace,
            HttpServletRequest request) {

        log.debug(">>konfigurujClient '{}' '{}' '{}' '{}'", funkce, args, batch, oddelovace);
        String result = konfigurujBatch(funkce, bezKonverze, args, batch,
                steamClient.getSteamClient(klient).getConfig(), oddelovace);
        log.debug("<<konfigurujClient '{}'", result);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/steam/client/handler/match", method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    @Secured({ Role.CONFIG })
    public ResponseEntity<String> konfigurujClientGc(@RequestParam String funkce, @RequestParam String klient,
            @RequestParam(required = false) boolean bezKonverze, @RequestParam(required = false) String args,
            @RequestParam(required = false) Integer batch, @RequestParam(required = false) String oddelovace,
            HttpServletRequest request) {

        log.debug(">>konfigurujClientGc '{}' '{}' '{}' '{}'", funkce, args, batch, oddelovace);
        String result = konfigurujBatch(funkce, bezKonverze, args, batch,
                steamClient.getSteamClient(klient).getMatchHandler(), oddelovace);
        log.debug("<<konfigurujClientGc '{}'", result);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/steam/client/handler/lobby", method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    @Secured({ Role.CONFIG })
    public ResponseEntity<String> konfigurujClientGcLobby(@RequestParam String funkce, @RequestParam String klient,
            @RequestParam(required = false) boolean bezKonverze, @RequestParam(required = false) String args,
            @RequestParam(required = false) Integer batch, @RequestParam(required = false) String oddelovace,
            HttpServletRequest request) {

        log.debug(">>konfigurujClientGc '{}' '{}' '{}' '{}'", funkce, args, batch, oddelovace);
        String result = konfigurujBatch(funkce, bezKonverze, args, batch,
                steamClient.getSteamClient(klient).getLobbyHandler(), oddelovace);
        log.debug("<<konfigurujClientGc '{}'", result);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/steam/client/handler/party", method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    @Secured({ Role.CONFIG })
    public ResponseEntity<String> konfigurujClientGcParty(@RequestParam String funkce, @RequestParam String klient,
            @RequestParam(required = false) boolean bezKonverze, @RequestParam(required = false) String args,
            @RequestParam(required = false) Integer batch, @RequestParam(required = false) String oddelovace,
            HttpServletRequest request) {

        log.debug(">>konfigurujClientGc '{}' '{}' '{}' '{}'", funkce, args, batch, oddelovace);
        String result = konfigurujBatch(funkce, bezKonverze, args, batch,
                steamClient.getSteamClient(klient).getPartyHandler(), oddelovace);
        log.debug("<<konfigurujClientGc '{}'", result);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/steam/client/handler/chat", method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    @Secured({ Role.CONFIG })
    public ResponseEntity<String> konfigurujClientGcChat(@RequestParam String funkce, @RequestParam String klient,
            @RequestParam(required = false) boolean bezKonverze, @RequestParam(required = false) String args,
            @RequestParam(required = false) Integer batch, @RequestParam(required = false) String oddelovace,
            HttpServletRequest request) {

        log.debug(">>konfigurujClientGc '{}' '{}' '{}' '{}'", funkce, args, batch, oddelovace);
        String result = konfigurujBatch(funkce, bezKonverze, args, batch,
                steamClient.getSteamClient(klient).getChatHandler(), oddelovace);
        log.debug("<<konfigurujClientGc '{}'", result);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/steam/client/handler/game", method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    @Secured({ Role.CONFIG })
    public ResponseEntity<String> konfigurujClientGcGame(@RequestParam String funkce, @RequestParam String klient,
            @RequestParam(required = false) boolean bezKonverze, @RequestParam(required = false) String args,
            @RequestParam(required = false) Integer batch, @RequestParam(required = false) String oddelovace,
            HttpServletRequest request) {

        log.debug(">>konfigurujClientGc '{}' '{}' '{}' '{}'", funkce, args, batch, oddelovace);
        String result = konfigurujBatch(funkce, bezKonverze, args, batch,
                steamClient.getSteamClient(klient).getGameCoordinator(), oddelovace);
        log.debug("<<konfigurujClientGc '{}'", result);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

}
