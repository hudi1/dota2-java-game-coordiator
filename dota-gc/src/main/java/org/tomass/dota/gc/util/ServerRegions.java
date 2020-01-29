package org.tomass.dota.gc.util;

public enum ServerRegions {

    UNSPECIFIED(0),

    US_WEST(1),

    US_EAST(2),

    EUROPE(3),

    KOREA(4),

    SINGAPORE(5),

    DUBAI(6),

    AUSTRALIA(7),

    STOCKHOLM(8),

    AUSTRIA(9),

    BRAZIL(10),

    SOUTH_AFRICA(11),

    PERFECT_WORLD_TELECOM(12),

    PERFECT_WORLD_UNICOM(13),

    CHILE(14),

    PERU(15), INDIA(16),

    PERFECT_WORLD_TELECOM_GUANGDONG(17),

    PERFECT_WORLD_TELECOM_ZHEJIANG(18),

    JAPAN(19),

    PERFECT_WORLD_TELECOM_WUHAN(20),

    PERFECT_WORLD_TELECOM_TIANJIN(25);

    private final int value;

    private ServerRegions(int value) {
        this.value = value;
    }

    public final int getNumber() {
        return value;
    }

}
