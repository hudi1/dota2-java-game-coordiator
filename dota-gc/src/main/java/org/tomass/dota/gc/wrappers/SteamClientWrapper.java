package org.tomass.dota.gc.wrappers;

import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tomass.dota.gc.clients.impl.DotaClientImpl;
import org.tomass.dota.gc.config.AppConfig;
import org.tomass.dota.gc.config.SteamClientConfig;
import org.tomass.dota.gc.handlers.callbacks.lobby.LobbyNewCallback;
import org.tomass.dota.webapi.model.TeamInfo;

@Component
public class SteamClientWrapper {

    @Autowired
    private AppConfig config;

    private Queue<DotaClientImpl> clients = new LinkedList<>();

    private final Object clientLock = new Object();

    public SteamClientWrapper() {
    }

    @PostConstruct
    public void init() {
        for (Map.Entry<String, SteamClientConfig> entry : config.getClients().entrySet()) {
            DotaClientImpl klient = new DotaClientImpl(entry.getValue(), config);
            if (klient.getConfig().isConnectOnStart()) {
                klient.connect();
            }
            clients.add(klient);
        }
    }

    public DotaClientImpl getSteamClient(String name) {
        Optional<DotaClientImpl> client = clients.stream().filter(c -> c.getConfig().getUser().equals(name))
                .findFirst();
        if (client.isPresent()) {
            return client.get();
        }
        return null;
    }

    public DotaClientImpl getClient() {
        return clients.peek();
    }

    public LobbyNewCallback requestNewTeamLobby(Integer leagueId, TeamInfo teamInfo1, TeamInfo teamInfo2,
            String password) {
        synchronized (clientLock) {
            for (DotaClientImpl client : clients) {
                if (client.getLobbyHandler().getLobby() == null) {
                    return client.requestNewTeamLobby(leagueId, teamInfo1, teamInfo2, password);
                }
            }
        }
        return null;
    }

}
