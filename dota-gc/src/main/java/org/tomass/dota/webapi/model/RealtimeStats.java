package org.tomass.dota.webapi.model;

import in.dragonbra.javasteam.types.KeyValue;

public class RealtimeStats {

    private static final String TEAMS = "teams";

    private Team radiant;
    private Team dire;

    public RealtimeStats() {
    }

    public static RealtimeStats parseFrom(KeyValue keyValue) {
        RealtimeStats stats = new RealtimeStats();
        KeyValue teams = keyValue.get(TEAMS);
        stats.setRadiant(Team.parseFrom(teams.get("0")));
        stats.setDire(Team.parseFrom(teams.get("1")));
        return stats;
    }

    public Team getRadiant() {
        return radiant;
    }

    public void setRadiant(Team radiant) {
        this.radiant = radiant;
    }

    public Team getDire() {
        return dire;
    }

    public void setDire(Team dire) {
        this.dire = dire;
    }

    @Override
    public String toString() {
        return "RealtimeStats [radiant=" + radiant + ", dire=" + dire + "]";
    }

}
