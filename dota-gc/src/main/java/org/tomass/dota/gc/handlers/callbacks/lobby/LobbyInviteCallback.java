package org.tomass.dota.gc.handlers.callbacks.lobby;

import org.tomass.protobuf.dota.DotaGcmessagesCommonMatchManagement.CSODOTALobbyInvite;

import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;

public class LobbyInviteCallback extends CallbackMsg {

    public CSODOTALobbyInvite lobbyInvite;

    public LobbyInviteCallback(CSODOTALobbyInvite lobbyInvite) {
        this.lobbyInvite = lobbyInvite;
    }

    public CSODOTALobbyInvite getLobbyInvite() {
        return lobbyInvite;
    }

}
