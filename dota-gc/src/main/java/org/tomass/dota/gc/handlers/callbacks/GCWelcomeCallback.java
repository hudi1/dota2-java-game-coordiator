package org.tomass.dota.gc.handlers.callbacks;

import org.tomass.protobuf.dota.GcsdkGcmessages.CMsgClientWelcome.Builder;

import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;

public class GCWelcomeCallback extends CallbackMsg {

    private Builder body;

    public GCWelcomeCallback(Builder body) {
        this.body = body;
    }

    public Builder getBody() {
        return body;
    }

}
