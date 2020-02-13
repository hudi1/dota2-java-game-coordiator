package org.tomass.dota.gc.handlers;

import in.dragonbra.javasteam.base.IPacketGCMsg;

public interface ClientGCMsgHandler {

    public abstract void handleGCMsg(IPacketGCMsg packetGCMsg);

}
