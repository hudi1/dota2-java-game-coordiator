package org.tomass.dota.gc.handlers.callbacks;

import org.tomass.protobuf.dota.GcsdkGcmessages.GCConnectionStatus;

import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;

public class ConnectionStatusCallback extends CallbackMsg {

    private GCConnectionStatus status;

    public ConnectionStatusCallback(GCConnectionStatus status) {
        this.status = status;
    }

    public GCConnectionStatus getStatus() {
        return status;
    }

}
