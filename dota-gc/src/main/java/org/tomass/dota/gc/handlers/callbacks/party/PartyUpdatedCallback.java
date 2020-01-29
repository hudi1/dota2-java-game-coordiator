package org.tomass.dota.gc.handlers.callbacks.party;

import org.tomass.protobuf.dota.DotaGcmessagesCommonMatchManagement.CSODOTAParty;

import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;

public class PartyUpdatedCallback extends CallbackMsg {

    public CSODOTAParty party;

    public PartyUpdatedCallback(CSODOTAParty party) {
        this.party = party;
    }

    public CSODOTAParty getParty() {
        return party;
    }

}
