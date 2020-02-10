package org.tomass.dota.gc.handlers;

import in.dragonbra.javasteam.base.IClientGCMsg;
import in.dragonbra.javasteam.base.IPacketGCMsg;
import in.dragonbra.javasteam.types.JobID;

public interface ClientGCMsgHandler {

    public abstract void handleGCMsg(IPacketGCMsg packetGCMsg) throws Exception;

    public abstract void send(IClientGCMsg msg);

    public abstract JobID sendJob(IClientGCMsg msg);

    public abstract <T> T sendJobAndWait(IClientGCMsg msg, Long timeout);

    public abstract <T> T sendCustomAndWait(IClientGCMsg msg, Long id, Long timeout);

}
