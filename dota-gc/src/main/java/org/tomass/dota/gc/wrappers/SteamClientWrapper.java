package org.tomass.dota.gc.wrappers;

import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tomass.dota.gc.clients.Dota2Client;
import org.tomass.dota.gc.clients.impl.DotaClientImpl;
import org.tomass.dota.gc.config.AppConfig;
import org.tomass.dota.gc.config.ScheduledSeries;
import org.tomass.dota.gc.config.SteamClientConfig;
import org.tomass.dota.gc.handlers.callbacks.league.LeagueInfoAdmin;
import org.tomass.dota.gc.handlers.callbacks.lobby.LobbyNewCallback;

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

    public LobbyNewCallback requestNewTeamLobby(ScheduledSeries serie) {
        synchronized (clientLock) {
            for (DotaClientImpl client : clients) {
                if (client.getLobbyHandler().getLobby() == null) {
                    return client.requestNewScheduledTeamLobby(serie);
                }
            }
        }
        return null;
    }

    public boolean getLeagueAdmin(Integer leagueId) {
        boolean admin = true;
        for (Dota2Client client : clients) {
            LeagueInfoAdmin admins = client.getLeagueHandler().requestLeagueInfoAdmins();
            if (admins != null) {
                if (!admins.getBody().getInfosList().stream().map(i -> i.getLeagueId()).collect(Collectors.toList())
                        .contains(leagueId)) {
                    admin = false;
                }
            } else {
                admin = false;
            }
        }
        return admin;
    }

    public DotaClientImpl getClient() {
        return clients.peek();
    }

    public Queue<DotaClientImpl> getClients() {
        return clients;
    }

}
