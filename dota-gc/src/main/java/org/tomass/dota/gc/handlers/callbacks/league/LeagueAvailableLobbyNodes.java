package org.tomass.dota.gc.handlers.callbacks.league;

import org.tomass.dota.model.rest.LeagueAvailableLobbyNodesRest;
import org.tomass.dota.model.rest.NodeInfoRest;
import org.tomass.dota.webapi.SteamDota2Match;
import org.tomass.dota.webapi.model.TeamInfo;
import org.tomass.protobuf.dota.DotaGcmessagesCommonLeague.CMsgDOTALeagueAvailableLobbyNodes.Builder;
import org.tomass.protobuf.dota.DotaGcmessagesCommonLeague.CMsgDOTALeagueAvailableLobbyNodes.NodeInfo;

import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.steam.steamclient.configuration.SteamConfiguration;
import in.dragonbra.javasteam.types.JobID;

public class LeagueAvailableLobbyNodes extends CallbackMsg {

    private Builder body;

    public LeagueAvailableLobbyNodes(JobID jobID, Builder body) {
        setJobID(jobID);
        this.body = body;
    }

    public Builder getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "LeagueAvailableLobbyNodes [body=" + body + "]";
    }

    public LeagueAvailableLobbyNodesRest getRest(String steamWebApi) {
        LeagueAvailableLobbyNodesRest rest = new LeagueAvailableLobbyNodesRest();
        for (NodeInfo nodeInfo : body.getNodeInfosList()) {
            NodeInfoRest restInfo = new NodeInfoRest();
            restInfo.setNodeId(nodeInfo.getNodeId());
            restInfo.setNodeName(nodeInfo.getNodeName());
            restInfo.setTeam1(nodeInfo.getTeamId1());
            restInfo.setTeam2(nodeInfo.getTeamId2());
            if (steamWebApi != null) {
                TeamInfo teamInfo1 = SteamDota2Match
                        .getTeam(SteamConfiguration.create(c -> c.withWebAPIKey(steamWebApi)), nodeInfo.getTeamId1());
                restInfo.setTeamName1(teamInfo1.getName());
                TeamInfo teamInfo2 = SteamDota2Match
                        .getTeam(SteamConfiguration.create(c -> c.withWebAPIKey(steamWebApi)), nodeInfo.getTeamId2());
                restInfo.setTeamName2(teamInfo2.getName());
            }
            rest.getNodeInfo().add(restInfo);
        }

        return rest;
    }

}
