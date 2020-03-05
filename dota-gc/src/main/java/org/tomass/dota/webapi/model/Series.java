package org.tomass.dota.webapi.model;

import java.util.ArrayList;
import java.util.List;

public class Series {

    private List<Match> matches = new ArrayList<>();
    private Integer seriesId;
    private TeamInfo team1;
    private TeamInfo team2;

    public Series() {
    }

    public Series(Integer seriesId) {
        this.seriesId = seriesId;
    }

    public Series(TeamInfo team1, TeamInfo team2) {
        this.team1 = team1;
        this.team2 = team2;
    }

    public List<Match> getMatches() {
        return matches;
    }

    public void setMatches(List<Match> matches) {
        this.matches = matches;
    }

    public Integer getSeriesId() {
        return seriesId;
    }

    public void setSeriesId(Integer seriesId) {
        this.seriesId = seriesId;
    }

    public TeamInfo getTeam1() {
        return team1;
    }

    public void setTeam1(TeamInfo team1) {
        this.team1 = team1;
    }

    public TeamInfo getTeam2() {
        return team2;
    }

    public void setTeam2(TeamInfo team2) {
        this.team2 = team2;
    }

    public static void complete(Series series) {
        for (Match match : series.getMatches()) {
            if (series.getTeam1().getName() == null) {
                TeamInfo team = new TeamInfo(match.getRadiant().getId());
                team.setName(match.getRadiant().getName());
                series.setTeam1(team);
            }
            if (series.getTeam2().getName() == null) {
                TeamInfo team = new TeamInfo(match.getDire().getId());
                team.setName(match.getDire().getName());
                series.setTeam2(team);
            }
            TeamInfo winner = match.isRadiantWin() ? match.getRadiant() : match.getDire();
            if (series.getTeam1().equals(winner)) {
                series.getTeam1().setWins(series.getTeam1().getWins() + 1);
            } else if (series.getTeam2().equals(winner)) {
                series.getTeam2().setWins(series.getTeam2().getWins() + 1);
            }
        }
    }

    public TeamInfo getWinner() {
        if (team1 != null && team2 != null) {
            if (team1.getWins() > team2.getWins()) {
                return team1;
            } else if (team1.getWins() < team2.getWins()) {
                return team2;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "Series [matches=" + matches + ", seriesId=" + seriesId + ", team1=" + team1 + ", team2=" + team2 + "]";
    }

}
