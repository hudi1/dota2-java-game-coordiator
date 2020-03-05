package org.tomass.dota.webapi.model;

import in.dragonbra.javasteam.types.KeyValue;

public class Match {

    private boolean radiantWin;
    private Long matchId;
    private Integer leagueId;
    private TeamInfo radiant;
    private TeamInfo dire;

    public Match() {
    }

    public Match(Long matchId) {
        this.matchId = matchId;
    }

    public boolean isRadiantWin() {
        return radiantWin;
    }

    public void setRadiantWin(boolean radiantWin) {
        this.radiantWin = radiantWin;
    }

    public Long getMatchId() {
        return matchId;
    }

    public void setMatchId(Long matchId) {
        this.matchId = matchId;
    }

    public Integer getLeagueId() {
        return leagueId;
    }

    public void setLeagueId(Integer leagueId) {
        this.leagueId = leagueId;
    }

    public TeamInfo getRadiant() {
        return radiant;
    }

    public void setRadiant(TeamInfo radiant) {
        this.radiant = radiant;
    }

    public TeamInfo getDire() {
        return dire;
    }

    public void setDire(TeamInfo dire) {
        this.dire = dire;
    }

    public static Match parseFromMatchDetails(Match match, KeyValue keyValue) {
        if (match == null) {
            match = new Match();
        }

        match.setRadiantWin(keyValue.get("radiant_win").asBoolean());
        match.setMatchId(keyValue.get("match_id").asLong());
        match.setLeagueId(keyValue.get("leagueid").asInteger());
        TeamInfo radiant = new TeamInfo();
        radiant.setId(keyValue.get("radiant_team_id").asInteger());
        radiant.setName(keyValue.get("radiant_name").asString());
        match.setRadiant(radiant);
        TeamInfo dire = new TeamInfo();
        dire.setId(keyValue.get("dire_team_id").asInteger());
        dire.setName(keyValue.get("dire_name").asString());
        match.setDire(dire);

        return match;
    }

    @Override
    public String toString() {
        return "Match [radiantWin=" + radiantWin + ", matchId=" + matchId + ", leagueId=" + leagueId + ", radiant="
                + radiant + ", dire=" + dire + "]";
    }

}