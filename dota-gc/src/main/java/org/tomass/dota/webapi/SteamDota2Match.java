package org.tomass.dota.webapi;

import java.util.LinkedHashMap;
import java.util.Map;

import org.tomass.dota.webapi.model.RealtimeStats;
import org.tomass.dota.webapi.model.TeamInfo;

import in.dragonbra.javasteam.steam.steamclient.configuration.SteamConfiguration;
import in.dragonbra.javasteam.steam.webapi.WebAPI;
import in.dragonbra.javasteam.types.KeyValue;

public class SteamDota2Match {

    public static TeamInfo getTeam(SteamConfiguration configuration, Integer teamId) {
        if (teamId == null || configuration == null) {
            return null;
        }
        TeamInfo teamInfo = new TeamInfo(teamId);
        try {
            if (teamId != null) {
                WebAPI api = configuration.getWebAPI("IDOTA2Match_570");
                Map<String, String> params = new LinkedHashMap<>();
                params.put("start_at_team_id", teamId + "");
                params.put("teams_requested", "1");
                KeyValue response = api.call("GetTeamInfoByTeamID", params);
                teamInfo = TeamInfo.parseFromTeamInfo(response);
                teamInfo.setId(teamId);
                if (teamInfo.getName() == null) {
                    teamInfo.setName(teamId.toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return teamInfo;
    }

    public static RealtimeStats getRealTimeStat(SteamConfiguration configuration, Long steamId) {
        try {
            WebAPI api = configuration.getWebAPI("IDOTA2MatchStats_570");
            Map<String, String> params = new LinkedHashMap<>();
            params.put("server_steam_id", steamId.toString());
            KeyValue response = api.call("GetRealtimeStats", params);
            return RealtimeStats.parseFrom(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
