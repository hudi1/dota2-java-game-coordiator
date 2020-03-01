package org.tomass.dota.webapi.model;

import java.util.ArrayList;
import java.util.List;

import in.dragonbra.javasteam.types.KeyValue;

public class TeamInfo {

    private static final String TEAMS = "teams";

    private List<Player> players;

    private String name;

    private Integer id;

    private Integer wins;

    private Integer netWorth;

    public TeamInfo() {
        this.wins = 0;
    }

    public TeamInfo(Integer id) {
        this();
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Player> getPlayers() {
        if (players == null) {
            players = new ArrayList<>();
        }
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getWins() {
        return wins;
    }

    public void setWins(Integer wins) {
        this.wins = wins;
    }

    public Integer getNetWorth() {
        return netWorth;
    }

    public void setNetWorth(Integer netWorth) {
        this.netWorth = netWorth;
    }

    public static TeamInfo parseFromTeamInfo(KeyValue keyValue) {
        TeamInfo team = new TeamInfo();
        if (keyValue.get(TEAMS).getChildren().size() > 0) {
            KeyValue children = keyValue.get(TEAMS).getChildren().get(0);
            team.setName(children.get("name").asString());
            for (int i = 0; i < children.getChildren().size() - 9; i++) {
                Integer accountId = children.get("player_" + i + "_account_id").asInteger();
                Player player = new Player();
                player.setAccountId(accountId);
                team.getPlayers().add(player);
            }
        }

        return team;
    }

    public static TeamInfo parseFromRealTimeStat(KeyValue keyValue) {
        TeamInfo team = new TeamInfo();
        team.setName(keyValue.get("team_name").asString());
        team.setNetWorth(keyValue.get("net_worth").asInteger());
        team.getPlayers().add(Player.parseFrom(keyValue.get("players").get("0")));
        team.getPlayers().add(Player.parseFrom(keyValue.get("players").get("1")));
        team.getPlayers().add(Player.parseFrom(keyValue.get("players").get("2")));
        team.getPlayers().add(Player.parseFrom(keyValue.get("players").get("3")));
        team.getPlayers().add(Player.parseFrom(keyValue.get("players").get("4")));
        return team;
    }

    @Override
    public String toString() {
        return "TeamInfo [players=" + players + ", name=" + name + ", id=" + id + ", wins=" + wins + "]";
    }

}
