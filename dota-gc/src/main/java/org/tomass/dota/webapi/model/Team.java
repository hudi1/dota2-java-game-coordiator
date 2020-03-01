package org.tomass.dota.webapi.model;

import java.util.ArrayList;
import java.util.List;

public class Team {

    private String name;
    private Integer netWorth;
    private List<Player> players = new ArrayList<>();

    public Team() {
    }

    public Integer getNetWorth() {
        return netWorth;
    }

    public void setNetWorth(Integer netWorth) {
        this.netWorth = netWorth;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Player> getPlayers() {
        return players;
    }

    @Override
    public String toString() {
        return "Team [name=" + name + ", netWorth=" + netWorth + ", players=" + players + "]";
    }

}
