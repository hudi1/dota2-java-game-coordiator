package org.tomass.dota.gc.rest;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.tomass.dota.gc.clients.impl.DotaClientImpl;
import org.tomass.dota.gc.wrappers.SteamClientWrapper;

@RestController
public class AppController extends BaseCommonController {

    @Autowired
    private SteamClientWrapper steamClient;

    @RequestMapping(value = "/steam/client/info", method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<String> konfigurujApp(HttpServletRequest request) {
        StringBuilder info = new StringBuilder();
        for (DotaClientImpl client : steamClient.getClients()) {
            info.append(client.getInfo());
        }

        return new ResponseEntity<>(info.toString(), HttpStatus.OK);
    }

}
