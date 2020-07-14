package org.tomass.dota.model.rest;

import java.io.Serializable;

public class LeagueInfoAdminRest implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer leagueId;
    private String name;

    public Integer getLeagueId() {
        return leagueId;
    }

    public void setLeagueId(Integer leagueId) {
        this.leagueId = leagueId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "LeagueInfoAdminRest [leagueId=" + leagueId + ", name=" + name + "]";
    }

}
