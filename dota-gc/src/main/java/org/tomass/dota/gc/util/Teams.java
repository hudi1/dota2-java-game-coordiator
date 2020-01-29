package org.tomass.dota.gc.util;

public enum Teams {

    DIRE(1), RADIANT(2);

    private int teamId;

    private Teams(int teamId) {
        this.teamId = teamId;
    }

    public int getTeamId() {
        return teamId;
    }

    public static Teams forNumber(int value) {
        switch (value) {
        case 1:
            return DIRE;
        case 2:
            return RADIANT;
        default:
            return null;
        }
    }

}
