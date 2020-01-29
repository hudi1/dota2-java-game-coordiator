package org.tomass.dota.gc.handlers.callbacks.match;

import org.tomass.protobuf.dota.DotaGcmessagesCommon.CMsgDOTAMatchMinimal.Builder;

import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;

public class MatchesMinimalCallback extends CallbackMsg {

    private Builder builder;

    public MatchesMinimalCallback(Builder builder) {
        this.builder = builder;
    }

    public Builder getBuilder() {
        return builder;
    }

}
