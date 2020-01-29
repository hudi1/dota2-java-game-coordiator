package org.tomass.dota.gc.handlers;

import org.tomass.dota.gc.clients.Dota2Client;

import in.dragonbra.javasteam.base.IClientGCMsg;
import in.dragonbra.javasteam.types.JobID;

public abstract class Dota2ClientGCMsgHandlerImpl implements ClientGCMsgHandler {

    protected Dota2Client client;

    public Dota2ClientGCMsgHandlerImpl(Dota2Client client) {
        this.client = client;
    }

    @Override
    public void send(IClientGCMsg msg) {
        client.send(msg);
    }

    @Override
    public JobID sendJob(IClientGCMsg msg) {
        return client.sendJob(msg);
    }

    @Override
    public Object sendJobAndWait(IClientGCMsg msg, long timeout) {
        return client.sendJobAndWait(msg, timeout);
    }

}
