package org.tomass.dota.gc.handlers.callbacks.party;

import org.tomass.protobuf.dota.BaseGcmessages.CMsgInvitationCreated.Builder;

import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;

public class PartyInvitationCreatedCallback extends CallbackMsg {

    private Builder builder;

    public PartyInvitationCreatedCallback(Builder builder) {
        this.builder = builder;
    }

    public Builder getBuilder() {
        return builder;
    }

}
