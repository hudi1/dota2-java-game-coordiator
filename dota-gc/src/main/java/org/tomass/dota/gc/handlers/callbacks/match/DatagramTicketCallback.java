package org.tomass.dota.gc.handlers.callbacks.match;

import org.tomass.protobuf.dota.DotaGcmessagesClientMatchManagement.CMsgClientToGCRequestSteamDatagramTicketResponse.Builder;

import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;

public class DatagramTicketCallback extends CallbackMsg {

    private Builder builder;

    public DatagramTicketCallback(Builder builder) {
        this.builder = builder;
    }

    public Builder getBuilder() {
        return builder;
    }

}
