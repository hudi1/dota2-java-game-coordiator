package org.tomass.dota.gc.handlers.callbacks.player;

import org.tomass.protobuf.dota.DotaGcmessagesCommon.CMsgDOTAProfileCard.Builder;

import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.types.JobID;

public class ProfileCardResponse extends CallbackMsg {

    private Builder body;

    public ProfileCardResponse(JobID jobID, Builder builder) {
        setJobID(jobID);
        this.body = builder;
    }

    public Builder getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "ProfileCardResponse [body=" + body + "]";
    }

}
