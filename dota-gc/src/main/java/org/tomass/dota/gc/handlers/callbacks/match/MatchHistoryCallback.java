package org.tomass.dota.gc.handlers.callbacks.match;

import org.tomass.protobuf.dota.DotaGcmessagesClient.CMsgDOTAGetPlayerMatchHistoryResponse.Builder;

import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.types.JobID;

public class MatchHistoryCallback extends CallbackMsg {

    private Builder builder;

    public MatchHistoryCallback(JobID jobID, Builder builder) {
        setJobID(jobID);
        this.builder = builder;
    }

    public Builder getBuilder() {
        return builder;
    }

}
