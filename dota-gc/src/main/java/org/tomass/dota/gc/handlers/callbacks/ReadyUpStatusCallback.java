package org.tomass.dota.gc.handlers.callbacks;

import org.tomass.protobuf.dota.DotaGcmessagesClientMatchManagement.CMsgReadyUpStatus.Builder;

import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;

public class ReadyUpStatusCallback extends CallbackMsg {

    private Builder body;

    public ReadyUpStatusCallback(Builder body) {
        this.body = body;
    }

    public Builder getBody() {
        return body;
    }

}
