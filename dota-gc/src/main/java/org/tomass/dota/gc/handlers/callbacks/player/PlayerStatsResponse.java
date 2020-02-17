package org.tomass.dota.gc.handlers.callbacks.player;

import org.tomass.protobuf.dota.DotaGcmessagesClient.CMsgGCToClientPlayerStatsResponse.Builder;

import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.types.JobID;

public class PlayerStatsResponse extends CallbackMsg {

    private Builder body;

    public PlayerStatsResponse(JobID jobID, Builder body) {
        setJobID(jobID);
        this.body = body;
    }

    public Builder getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "PlayerStatsResponse [body=" + body + "]";
    }

}
