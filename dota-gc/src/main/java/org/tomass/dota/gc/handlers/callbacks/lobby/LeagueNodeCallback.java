package org.tomass.dota.gc.handlers.callbacks.lobby;

import org.tomass.protobuf.dota.DotaGcmessagesCommonLeague.CMsgDOTALeagueNodeResponse.Builder;

import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.types.JobID;

public class LeagueNodeCallback extends CallbackMsg {

    private Builder body;

    public LeagueNodeCallback(JobID jobID, Builder body) {
        setJobID(jobID);
        this.body = body;
    }

    public Builder getBody() {
        return body;
    }

    public void setBody(Builder body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "LeagueNodeCallback [body=" + body + "]";
    }

}
