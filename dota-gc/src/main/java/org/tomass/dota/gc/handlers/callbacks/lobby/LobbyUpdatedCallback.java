package org.tomass.dota.gc.handlers.callbacks.lobby;

import org.tomass.protobuf.dota.DotaGcmessagesCommonMatchManagement.CSODOTALobby;

import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;

public class LobbyUpdatedCallback extends CallbackMsg {

    public CSODOTALobby lobby;

    public LobbyUpdatedCallback(CSODOTALobby lobby) {
        this.lobby = lobby;
    }

    public CSODOTALobby getLobby() {
        return lobby;
    }

}
