package org.tomass.dota.gc.handlers.callbacks.match;

import org.tomass.protobuf.dota.DotaGcmessagesClient.CMsgGCMatchDetailsResponse.Builder;

import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.types.JobID;

public class MatchDetailsCallback extends CallbackMsg {

    private Integer result;

    public MatchDetailsCallback(JobID jobID, Builder body) {
        setJobID(jobID);
        this.setResult(body.getResult());
    }

    public Integer getResult() {
        return result;
    }

    public void setResult(Integer result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "MatchDetailsCallback [result=" + result + "]";
    }

}
