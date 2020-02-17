package org.tomass.dota.gc.handlers.callbacks.player;

import org.tomass.protobuf.dota.DotaGcmessagesClient.CMsgClientToGCGetProfileCard.Builder;

import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.types.JobID;

public class ProfileCardResponse extends CallbackMsg {

    private Builder body;

    public ProfileCardResponse(JobID jobID, Builder body) {
        setJobID(jobID);
        this.body = body;
    }

    public Builder getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "ProfileCardResponse [body=" + body + "]";
    }

}
