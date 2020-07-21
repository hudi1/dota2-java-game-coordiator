package org.tomass.dota.gc.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tomass.dota.gc.clients.Dota2Client;
import org.tomass.dota.steam.handlers.Dota2SteamGameCoordinator;

import in.dragonbra.javasteam.base.IClientGCMsg;
import in.dragonbra.javasteam.types.JobID;

public abstract class Dota2ClientGCMsgHandler implements ClientGCMsgHandler {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

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
        if (client.isReady()) {
            gameCoordinator.send(msg);
        } else {
            logger.warn("GC is not ready");
        }
    }

    public JobID sendJob(IClientGCMsg msg) {
        JobID jobID = client.getNextJobID();
        msg.setSourceJobID(jobID);
        send(msg);
        return jobID;
    }

    public <T> T sendJobAndWait(IClientGCMsg msg) {
        sendJob(msg);
        return client.registerAndWait(msg.getSourceJobID());
    }

    public <T> T sendCustomAndWait(IClientGCMsg msg, Object key) {
        send(msg);
        return client.registerAndWait(key);
    }

    public <T> T sendJobAndWait(IClientGCMsg msg, Long timeout) {
        sendJob(msg);
        return client.registerAndWait(msg.getSourceJobID(), timeout);
    }

    public <T> T sendCustomAndWait(IClientGCMsg msg, Object key, Long timeout) {
        send(msg);
        return client.registerAndWait(key, timeout);
    }

}
