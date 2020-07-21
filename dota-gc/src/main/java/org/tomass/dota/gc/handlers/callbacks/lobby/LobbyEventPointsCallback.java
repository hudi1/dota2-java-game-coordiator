package org.tomass.dota.gc.handlers.callbacks.lobby;

import org.tomass.protobuf.dota.DotaGcmessagesCommon.CMsgLobbyEventPoints.Builder;

import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;

public class LobbyEventPointsCallback extends CallbackMsg {

    private Builder builder;

    public LobbyEventPointsCallback(Builder builder) {
        this.builder = builder;
    }

    public Builder getBuilder() {
        return builder;
    }

}
