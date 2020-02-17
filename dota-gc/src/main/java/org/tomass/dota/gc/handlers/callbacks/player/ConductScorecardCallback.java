package org.tomass.dota.gc.handlers.callbacks.player;

import org.tomass.protobuf.dota.DotaGcmessagesClient.CMsgPlayerConductScorecard.Builder;

import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;

public class ConductScorecardCallback extends CallbackMsg {

    private Builder body;

    public ConductScorecardCallback(Builder body) {
        this.body = body;
    }

    public Builder getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "ConductScorecardCallback [body=" + body + "]";
    }

}
