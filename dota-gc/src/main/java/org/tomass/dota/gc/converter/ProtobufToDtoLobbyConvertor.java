package org.tomass.dota.gc.converter;

import java.util.List;

import org.tomass.dota.gc.dto.DotaLobby;
import org.tomass.dota.gc.dto.DotaLobbyMember;
import org.tomass.protobuf.dota.DotaGcmessagesCommonMatchManagement.CDOTALobbyMember;
import org.tomass.protobuf.dota.DotaGcmessagesCommonMatchManagement.CSODOTALobby;

public class ProtobufToDtoLobbyConvertor {

    public static DotaLobby convert(CSODOTALobby csoLobby, DotaLobby oldLobby) {
        DotaLobby lobby = new DotaLobby();
        lobby.setLobbyId(csoLobby.getLobbyId());
        if (oldLobby != null) {
            lobby.getExpectedBadGuys().addAll(oldLobby.getExpectedBadGuys());
            lobby.getExpectedGoodGuys().addAll(oldLobby.getExpectedGoodGuys());
        }
        List<CDOTALobbyMember> members = csoLobby.getMembersList();
        for (CDOTALobbyMember cdotaLobbyMember : members) {
            DotaLobbyMember member = new DotaLobbyMember();
            member.setId(cdotaLobbyMember.getId());
            member.setName(cdotaLobbyMember.getName());
            member.setTeam(cdotaLobbyMember.getTeam());
            member.setChannelId(cdotaLobbyMember.getChannel());
            lobby.getMembers().add(member);
        }

        return lobby;
    }
}
