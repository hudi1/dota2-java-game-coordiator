package org.tomass.dota.gc.handlers.callbacks.player;

import org.tomass.protobuf.dota.DotaGcmessagesClientFantasy.CMsgDOTAPlayerInfo.Builder;

import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;

public class PlayerInfoCallback extends CallbackMsg {

    private Builder body;

    public PlayerInfoCallback(Builder body) {
        this.body = body;
    }

    public Builder getBody() {
        return body;
    }

}
