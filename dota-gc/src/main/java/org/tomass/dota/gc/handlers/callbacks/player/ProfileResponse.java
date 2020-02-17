package org.tomass.dota.gc.handlers.callbacks.player;

import org.tomass.protobuf.dota.DotaGcmessagesClient.CMsgProfileResponse.Builder;

import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.types.JobID;

public class ProfileResponse extends CallbackMsg {

    private Builder body;

    public ProfileResponse(JobID jobID, Builder body) {
        setJobID(jobID);
        this.body = body;
    }

    public Builder getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "ProfileResponse [body=" + body + "]";
    }

}
