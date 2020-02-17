package org.tomass.dota.gc.handlers.callbacks.lobby;

import java.util.List;

import org.tomass.protobuf.dota.DotaGcmessagesClientMatchManagement.CMsgPracticeLobbyListResponseEntry;

import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.types.JobID;

public class LobbyCallback extends CallbackMsg {

    private List<CMsgPracticeLobbyListResponseEntry> lobbies;

    public LobbyCallback(List<CMsgPracticeLobbyListResponseEntry> lobbies) {
        this(JobID.INVALID, lobbies);
    }

    public LobbyCallback(JobID jobID, List<CMsgPracticeLobbyListResponseEntry> lobbies) {
        setJobID(jobID);
        this.lobbies = lobbies;
    }

    public List<CMsgPracticeLobbyListResponseEntry> getLobbies() {
        return lobbies;
    }

    @Override
    public String toString() {
        return "LobbyCallback [lobbies=" + lobbies + "]";
    }

}
