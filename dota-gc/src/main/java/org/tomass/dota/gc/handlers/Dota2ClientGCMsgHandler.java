package org.tomass.dota.gc.handlers;

import org.tomass.dota.gc.clients.Dota2Client;
import org.tomass.dota.steam.handlers.Dota2SteamGameCoordinator;

import in.dragonbra.javasteam.base.IClientGCMsg;
import in.dragonbra.javasteam.types.JobID;

public abstract class Dota2ClientGCMsgHandler implements ClientGCMsgHandler {

    protected Dota2SteamGameCoordinator gameCoordinator;
    protected Dota2Client client;

    public void setup(Dota2SteamGameCoordinator gameCoordinator) {
        this.gameCoordinator = gameCoordinator;
        this.client = gameCoordinator.getClient();
    }

    public Dota2SteamGameCoordinator getGameCoordinator() {
        return gameCoordinator;
    }

    public void send(IClientGCMsg msg) {
        gameCoordinator.send(msg);
    }

    public JobID sendJob(IClientGCMsg msg) {
        return gameCoordinator.sendJob(msg);
    }

    public <T> T sendJobAndWait(IClientGCMsg msg, Long timeout) {
        return gameCoordinator.sendJobAndWait(msg, timeout);
    }

    public <T> T sendCustomAndWait(IClientGCMsg msg, Long id, Long timeout) {
        return gameCoordinator.sendCustomAndWait(msg, id, timeout);
    }

}
