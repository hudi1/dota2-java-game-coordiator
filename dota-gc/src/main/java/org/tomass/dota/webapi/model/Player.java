package org.tomass.dota.webapi.model;

import in.dragonbra.javasteam.types.KeyValue;

public class Player {

    private String name;
    private Integer netWorth;

    public Player() {
    }

    public static Player parseFrom(KeyValue keyValue) {
        Player player = new Player();
        player.setName(keyValue.get("name").asString());
        player.setNetWorth(keyValue.get("net_worth").asInteger());
        return player;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getNetWorth() {
        return netWorth;
    }

    public void setNetWorth(Integer netWorth) {
        this.netWorth = netWorth;
    }

    @Override
    public String toString() {
        return "Player [name=" + name + ", netWorth=" + netWorth + "]";
    }

}
