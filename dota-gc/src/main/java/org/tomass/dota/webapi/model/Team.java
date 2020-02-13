package org.tomass.dota.webapi.model;

import java.util.ArrayList;
import java.util.List;

import in.dragonbra.javasteam.types.KeyValue;

public class Team {

    private String name;
    private Integer netWorth;
    private List<Player> players = new ArrayList<>();

    public Team() {
    }

    public static Team parseFrom(KeyValue keyValue) {
        Team team = new Team();
        team.setName(keyValue.get("team_name").asString());
        team.setNetWorth(keyValue.get("net_worth").asInteger());
        team.getPlayers().add(Player.parseFrom(keyValue.get("players").get("0")));
        team.getPlayers().add(Player.parseFrom(keyValue.get("players").get("1")));
        team.getPlayers().add(Player.parseFrom(keyValue.get("players").get("2")));
        team.getPlayers().add(Player.parseFrom(keyValue.get("players").get("3")));
        team.getPlayers().add(Player.parseFrom(keyValue.get("players").get("4")));
        return team;
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
