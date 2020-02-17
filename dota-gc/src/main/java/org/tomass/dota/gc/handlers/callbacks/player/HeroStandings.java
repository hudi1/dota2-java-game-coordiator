package org.tomass.dota.gc.handlers.callbacks.player;

import org.tomass.protobuf.dota.DotaGcmessagesClient.CMsgGCGetHeroStandingsResponse.Builder;

import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;

public class HeroStandings extends CallbackMsg {

    private Builder body;

    public HeroStandings(Builder body) {
        this.body = body;
    }

    public Builder getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "HeroStandings [body=" + body + "]";
    }

}
