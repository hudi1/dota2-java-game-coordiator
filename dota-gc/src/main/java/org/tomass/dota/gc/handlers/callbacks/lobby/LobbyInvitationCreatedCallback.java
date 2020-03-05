package org.tomass.dota.gc.handlers.callbacks.lobby;

import org.tomass.protobuf.dota.BaseGcmessages.CMsgInvitationCreated.Builder;

import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;

public class LobbyInvitationCreatedCallback extends CallbackMsg {

    private Builder builder;

    public LobbyInvitationCreatedCallback(Builder builder) {
        this.builder = builder;
    }

    public Builder getBuilder() {
        return builder;
    }

}
