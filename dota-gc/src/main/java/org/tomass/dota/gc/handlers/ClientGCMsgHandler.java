package org.tomass.dota.gc.handlers;

import in.dragonbra.javasteam.base.IClientGCMsg;
import in.dragonbra.javasteam.base.IPacketGCMsg;
import in.dragonbra.javasteam.types.JobID;

public interface ClientGCMsgHandler {

    public abstract void handleGCMsg(IPacketGCMsg packetGCMsg) throws Exception;

    public abstract void send(IClientGCMsg msg);

    // TODO
    public abstract JobID sendJob(IClientGCMsg msg);

    // TODO
    public abstract Object sendJobAndWait(IClientGCMsg msg, long timeout);

}
