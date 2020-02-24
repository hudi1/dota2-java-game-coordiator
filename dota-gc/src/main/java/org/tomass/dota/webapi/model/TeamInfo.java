package org.tomass.dota.webapi.model;

import java.util.ArrayList;
import java.util.List;

import in.dragonbra.javasteam.types.KeyValue;

public class TeamInfo {

    private static final String TEAMS = "teams";

    private List<Player> players;
    private String name;

    public TeamInfo() {
    }

    public static TeamInfo parseFrom(KeyValue keyValue) {
        TeamInfo info = new TeamInfo();
        KeyValue teams = keyValue.get(TEAMS);
        if (teams.getChildren().size() > 0) {
            KeyValue team = teams.getChildren().get(0);
            info.setName(team.get("name").asString());
            for (int i = 0; i < team.getChildren().size() - 9; i++) {
                Integer accountId = team.get("player_" + i + "_account_id").asInteger();
                Player player = new Player();
                player.setAccountId(accountId);
                info.getPlayers().add(player);
            }
        }

        return info;
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

    @Override
    public String toString() {
        return "TeamInfo [players=" + players + ", name=" + name + "]";
    }

}
