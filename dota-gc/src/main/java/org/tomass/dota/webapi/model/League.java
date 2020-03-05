package org.tomass.dota.webapi.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import in.dragonbra.javasteam.types.KeyValue;

public class League {

    private List<Series> series = new ArrayList<>();

    private Map<Integer, TeamInfo> teams = new HashMap<>();

    public League() {
    }

    public static League parseFromMatchHistory(KeyValue keyValue) {
        League league = new League();
        for (int i = 0; i < keyValue.get("matches").getChildren().size(); i++) {
            Long matchId = keyValue.get("matches").get(i + "").get("match_id").asLong();
            Integer seriesId = keyValue.get("matches").get(i + "").get("series_id").asInteger();
            Integer radiantId = keyValue.get("matches").get(i + "").get("radiant_team_id").asInteger();
            Integer direId = keyValue.get("matches").get(i + "").get("dire_team_id").asInteger();
            Series serie = null;

            for (Series series : league.getSeries()) {
                if ((series.getTeam1().getId().equals(radiantId) || series.getTeam2().getId().equals(radiantId))
                        && (series.getTeam1().getId().equals(direId) || series.getTeam2().getId().equals(direId))) {
                    serie = series;
                    break;
                }
            }

            if (serie == null) {
                serie = new Series(new TeamInfo(radiantId), new TeamInfo(direId));
                serie.setSeriesId(seriesId);
                league.getSeries().add(serie);
            }
            serie.getMatches().add(new Match(matchId));
        }

        return league;
    }

    public List<Series> getSeries() {
        return series;
    }

    public void setSeries(List<Series> series) {
        this.series = series;
    }

    public Map<Integer, TeamInfo> getTeams() {
        return teams;
    }

    public void setTeams(Map<Integer, TeamInfo> teams) {
        this.teams = teams;
    }

    @Override
    public String toString() {
        return "League [series=" + series + "]";
    }

}
