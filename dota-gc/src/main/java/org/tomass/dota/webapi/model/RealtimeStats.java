package org.tomass.dota.webapi.model;

import in.dragonbra.javasteam.types.KeyValue;

public class RealtimeStats {

    private static final String TEAMS = "teams";

    private TeamInfo radiant;
    private TeamInfo dire;

    public RealtimeStats() {
    }

    public static RealtimeStats parseFrom(KeyValue keyValue) {
        RealtimeStats stats = new RealtimeStats();
        KeyValue teams = keyValue.get(TEAMS);
        stats.setRadiant(TeamInfo.parseFromRealTimeStat(teams.get("0")));
        stats.setDire(TeamInfo.parseFromRealTimeStat(teams.get("1")));
        return stats;
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

    @Override
    public String toString() {
        return "RealtimeStats [radiant=" + radiant + ", dire=" + dire + "]";
    }

}
