package org.tomass.dota.gc.handlers.callbacks.lobby;

import org.tomass.protobuf.dota.DotaGcmessagesCommonMatchManagement.CSODOTALobby;

import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;

public class LobbyNewCallback extends CallbackMsg {

    public CSODOTALobby lobby;

    public LobbyNewCallback(CSODOTALobby lobby) {
        this.lobby = lobby;
    }

    public CSODOTALobby getLobby() {
        return lobby;
    }

    @Override
    public String toString() {
        return "LobbyNewCallback [lobby=" + lobby + "]";
    }

}
