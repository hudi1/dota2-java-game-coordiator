package org.tomass.dota.gc.handlers.callbacks.party;

import org.tomass.protobuf.dota.DotaGcmessagesCommonMatchManagement.CSODOTAPartyInvite;

import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;

public class PartyInviteCallback extends CallbackMsg {

    public CSODOTAPartyInvite partyInvite;

    public PartyInviteCallback(CSODOTAPartyInvite partyInvite) {
        this.partyInvite = partyInvite;
    }

    public CSODOTAPartyInvite getPartyInvite() {
        return partyInvite;
    }

}
