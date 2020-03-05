package org.tomass.dota.gc.handlers.callbacks.match;

import org.tomass.protobuf.dota.DotaGcmessagesClientWatch.CMsgGCToClientFindTopSourceTVGamesResponse.Builder;

import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;

public class TopSourceTvGamesCallback extends CallbackMsg {

    private Builder builder;

    public TopSourceTvGamesCallback(Builder builder) {
        this.builder = builder;
    }

    public Builder getBuilder() {
        return builder;
    }
}
