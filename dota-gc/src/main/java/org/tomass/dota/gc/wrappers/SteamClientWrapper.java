package org.tomass.dota.gc.wrappers;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import in.dragonbra.javasteam.base.IClientMsg;
import in.dragonbra.javasteam.handlers.ClientMsgHandler;
import in.dragonbra.javasteam.steam.steamclient.SteamClient;

@Component
public class SteamClientWrapper {

    private final Logger log = LoggerFactory.getLogger(SteamClientWrapper.class);

    private SteamClient steamClient;

    @PostConstruct
    public void init() {
        steamClient = new SteamClient();
    }

    public SteamClient getSteamClient() {
        return steamClient;
    }

    public <T extends ClientMsgHandler> T getHandler(Class<T> type) {
        return steamClient.getHandler(type);
    }

    public void disconnect() {
        steamClient.disconnect();
    }

    public void connect() {
        steamClient.connect();
    }

    public void addHandler(ClientMsgHandler handler) {
        steamClient.addHandler(handler);
    }

    public void send(IClientMsg msg) {
        steamClient.send(msg);
    }

}
