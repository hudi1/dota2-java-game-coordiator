package org.tomass.dota.gc.handlers.callbacks.lobby;

import org.tomass.protobuf.dota.DotaGcmessagesCommonLeague.CMsgDOTALeagueInfoList.Builder;

import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.types.JobID;

public class LeagueInfoAdmin extends CallbackMsg {

    private Builder body;

    public LeagueInfoAdmin(JobID jobID, Builder builder) {
        setJobID(jobID);
        this.body = builder;
    }

    public Builder getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "LeagueInfoAdmin [body=" + body + "]";
    }

}
