package org.tomass.dota.gc.rest;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.tomass.dota.gc.clients.impl.DotaClientImpl;
import org.tomass.dota.gc.config.AppConfig;
import org.tomass.dota.gc.wrappers.SteamClientWrapper;
import org.tomass.dota.model.DotaClientInfo;
import org.tomass.dota.model.DotaClientInfoList;

@RestController
public class ClientsController extends BaseCommonController {

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private SteamClientWrapper steamClient;

    @RequestMapping(value = "/steam/client/info/all", method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<DotaClientInfoList> clientInfoAll(HttpServletRequest request) {

        DotaClientInfoList result = new DotaClientInfoList();
        for (DotaClientImpl client : steamClient.getClients()) {
            result.getClientInfo().add(client.getInfo());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/steam/client/info", method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<DotaClientInfo> clientInfo(@RequestParam String klient, HttpServletRequest request) {

        DotaClientInfo result = null;
        if (steamClient.getSteamClient(klient) != null) {
            result = steamClient.getSteamClient(klient).getInfo();
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

}
