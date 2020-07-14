package org.tomass.dota.gc.handlers.callbacks.league;

import org.tomass.dota.model.rest.LeagueInfoAdminListRest;
import org.tomass.dota.model.rest.LeagueInfoAdminRest;
import org.tomass.protobuf.dota.DotaGcmessagesCommonLeague.CMsgDOTALeagueInfo;
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

    public LeagueInfoAdminListRest getRest() {
        LeagueInfoAdminListRest rest = new LeagueInfoAdminListRest();
        for (CMsgDOTALeagueInfo info : body.getInfosList()) {
            LeagueInfoAdminRest restInfo = new LeagueInfoAdminRest();
            restInfo.setLeagueId(info.getLeagueId());
            restInfo.setName(info.getName());
            rest.getInfo().add(restInfo);
        }
        return rest;
    }

}
