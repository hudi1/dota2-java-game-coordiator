package org.tomass.dota.gc.converter;

import org.tomass.dota.gc.dto.DotaParty;
import org.tomass.protobuf.dota.DotaGcmessagesCommonMatchManagement.CSODOTAParty;

public class ProtobufToDtoPartyConvertor {

    public static DotaParty convert(CSODOTAParty csoParty) {
        DotaParty party = new DotaParty();

        party.setReadyCheck(csoParty.hasReadyCheck());
        party.setPartyId(csoParty.getPartyId());
        party.setReadyCountPlayer(csoParty.getReadyCheck().getReadyMembersCount());
        party.setMembersCount(csoParty.getMemberIdsCount());
        //
        // csoParty.getMemberIdsList();
        // csoParty.hasRe;
        // csoParty.getSentInvitesList();
        // csoParty.getRecvInvitesList();
        // csoParty.

        return party;
    }
}
