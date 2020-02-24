package org.tomass.dota.gc.handlers.callbacks.lobby;

import org.tomass.protobuf.dota.DotaSharedEnums.DOTAJoinLobbyResult;

import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.types.JobID;

public class PracticeLobbyCallback extends CallbackMsg {

    private DOTAJoinLobbyResult result;

    public PracticeLobbyCallback(DOTAJoinLobbyResult result) {
        this(JobID.INVALID, result);
    }

    public PracticeLobbyCallback(JobID jobID, DOTAJoinLobbyResult result) {
        setJobID(jobID);
        this.result = result;
    }

    public DOTAJoinLobbyResult getResult() {
        return result;
    }

    @Override
    public String toString() {
        return "PracticeLobbyCallback [result=" + result + "]";
    }

}
