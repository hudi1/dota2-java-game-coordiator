package org.tomass.dota.gc.handlers.callbacks.match;

import org.tomass.protobuf.dota.DotaGcmessagesClient.CMsgGCToClientMatchSignedOut.Builder;

import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;

public class MatchSignedOutCallback extends CallbackMsg {

    private Builder builder;

    public MatchSignedOutCallback(Builder builder) {
        this.builder = builder;
    }

    public Builder getBuilder() {
        return builder;
    }

}
