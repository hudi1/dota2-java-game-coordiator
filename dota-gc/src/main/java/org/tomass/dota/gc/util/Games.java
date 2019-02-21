package org.tomass.dota.gc.util;

public enum Games {

    DOTA(570), DOTA_TEST(205790);

    private int gameId;

    private Games(int gameId) {
        this.gameId = gameId;
    }

    public int getGameId() {
        return gameId;
    }

}
