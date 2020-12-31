package org.tomass.dota.webapi;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tomass.dota.webapi.model.League;
import org.tomass.dota.webapi.model.Match;
import org.tomass.dota.webapi.model.RealtimeStats;
import org.tomass.dota.webapi.model.Series;
import org.tomass.dota.webapi.model.TeamInfo;

import in.dragonbra.javasteam.steam.steamclient.configuration.SteamConfiguration;
import in.dragonbra.javasteam.steam.webapi.WebAPI;
import in.dragonbra.javasteam.types.KeyValue;

public class SteamDota2Match {

    public static final Logger logger = LoggerFactory.getLogger(SteamDota2Match.class);

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
            }
        } catch (Exception e) {
            logger.error("!!getTeam ", e);
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
            logger.error("!!getRealTimeStat ", e);
        }
        return null;
    }

    public static League getLeagueMatchHistory(SteamConfiguration configuration, Integer leagueId) {
        try {
            WebAPI api = configuration.getWebAPI("IDOTA2Match_570");
            Map<String, String> params = new LinkedHashMap<>();
            params.put("league_id", leagueId.toString());
            KeyValue response = api.call("GetMatchHistory", params);
            League league = League.parseFromMatchHistory(response);

            for (Series series : league.getSeries()) {
                for (Match match : series.getMatches()) {
                    params = new LinkedHashMap<>();
                    params.put("match_id", match.getMatchId().toString());
                    response = api.call("GetMatchDetails", params);
                    Match.parseFromMatchDetails(match, response);
                }
                Series.complete(series);
                TeamInfo info1 = series.getTeam1();
                TeamInfo team1 = null;

                if (info1 != null) {
                    team1 = league.getTeams().get(info1.getId());
                    if (team1 == null) {
                        team1 = new TeamInfo(info1.getId());
                        team1.setName(info1.getName());
                        league.getTeams().put(info1.getId(), team1);
                    }
                }
                TeamInfo info2 = series.getTeam2();
                TeamInfo team2 = null;
                if (info2 != null) {
                    team2 = league.getTeams().get(info2.getId());
                    if (team2 == null) {
                        team2 = new TeamInfo(info2.getId());
                        team2.setName(info2.getName());
                        league.getTeams().put(info2.getId(), team2);
                    }
                }

                if (series.getWinner() != null) {
                    if (series.getWinner().equals(team1)) {
                        team1.setWins(team1.getWins() + 1);
                    } else if (series.getWinner().equals(team2)) {
                        team2.setWins(team2.getWins() + 1);
                    }
                }
            }
            return league;
        } catch (Exception e) {
            logger.error("!!getLeagueMatchHistory ", e);
        }
        return null;
    }
}
