package org.tomass.dota.gc.handlers.callbacks.lobby;

import org.tomass.protobuf.dota.DotaGcmessagesCommonMatchManagement.CSODOTALobby;

import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;

public class LobbyRemovedCallback extends CallbackMsg {

    public CSODOTALobby lobby;

    public LobbyRemovedCallback(CSODOTALobby lobby) {
        this.lobby = lobby;
    }

    public CSODOTALobby getLobby() {
        return lobby;
    }

}
