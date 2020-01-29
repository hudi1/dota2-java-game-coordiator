package org.tomass.dota.gc.wrappers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tomass.dota.gc.clients.Dota2Client;
import org.tomass.dota.gc.config.AppConfig;
import org.tomass.dota.gc.config.SteamClientConfig;

@Component
public class SteamClientWrapper {

    @Autowired
    private AppConfig config;

    private Map<String, Dota2Client> clients;

    public SteamClientWrapper() {
        clients = new ConcurrentHashMap<>();
    }

    @PostConstruct
    public void init() {
        for (Map.Entry<String, SteamClientConfig> entry : config.getClients().entrySet()) {
            Dota2Client klient = new Dota2Client(entry.getValue());
            if (klient.getConfig().isConnectOnStart()) {
                klient.connect();
            }
            clients.put(entry.getKey(), klient);
        }
    }

    public Dota2Client getSteamClient(String name) {
        return clients.get(name);
    }

}
