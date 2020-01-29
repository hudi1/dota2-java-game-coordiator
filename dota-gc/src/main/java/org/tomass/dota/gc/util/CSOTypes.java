package org.tomass.dota.gc.util;

public enum CSOTypes {

    /// <summary>
    /// An economy item.
    /// </summary>
    ECON_ITEM(1),

    /// <summary>
    /// An econ item recipe.
    /// </summary>
    ITEM_RECIPE(5),

    /// <summary>
    /// Game account client for Econ.
    /// </summary>
    ECON_GAME_ACCOUNT_CLIENT(7),

    /// <summary>
    /// Selected item preset.
    /// </summary>
    SELECTED_ITEM_PRESET(35),

    /// <summary>
    /// Item preset instance.
    /// </summary>
    ITEM_PRESET_INSTANCE(36),

    /// <summary>
    /// Active drop rate bonus.
    /// </summary>
    DROP_RATE_BONUS(38),

    /// <summary>
    /// Pass to view a league.
    /// </summary>
    LEAGUE_VIEW_PASS(39),

    /// <summary>
    /// Event ticket.
    /// </summary>
    EVENT_TICKET(40),

    /// <summary>
    /// Item tournament passport.
    /// </summary>
    ITEM_TOURNAMENT_PASSPORT(42),

    /// <summary>
    /// DOTA 2 game account client.
    /// </summary>
    GAME_ACCOUNT_CLIENT(2002),

    /// <summary>
    /// A Dota 2 party.
    /// </summary>
    PARTY(2003),

    /// <summary>
    /// A Dota 2 lobby.
    /// </summary>
    LOBBY(2004),

    /// <summary>
    /// A party invite.
    /// </summary>
    PARTY_INVITE(2006),

    /// <summary>
    /// Game hero favorites.
    /// </summary>
    GAME_HERO_FAVORITES(2007),

    /// <summary>
    /// Ping map location state.
    /// </summary>
    MAP_LOCATION_STATE(2008),

    /// <summary>
    /// Tournament.
    /// </summary>
    TOURNAMENT(2009),

    /// <summary>
    /// A player challenge.
    /// </summary>
    PLAYER_CHALLENGE(2010),

    /// <summary>
    /// A lobby invite), introduced in Reborn.
    /// </summary>
    LOBBY_INVITE(2011),

    /// <summary>
    /// A game count plus
    /// </summary>
    GAME_ACCOUNT_PLUS(2012);

    public static final int ECON_ITEM_VALUE = 1;

    public static final int ITEM_RECIPE_VALUE = 5;

    public static final int ECON_GAME_ACCOUNT_CLIENT_VALUE = 7;

    public static final int SELECTED_ITEM_PRESET_VALUE = 35;

    public static final int ITEM_PRESET_INSTANCE_VALUE = 36;

    public static final int DROP_RATE_BONUS_VALUE = 38;

    public static final int LEAGUE_VIEW_PASS_VALUE = 39;

    public static final int EVENT_TICKET_VALUE = 40;

    public static final int ITEM_TOURNAMENT_PASSPORT_VALUE = 42;

    public static final int GAME_ACCOUNT_CLIENT_VALUE = 2002;

    public static final int PARTY_VALUE = 2003;

    public static final int LOBBY_VALUE = 2004;

    public static final int PARTY_INVITE_VALUE = 2006;

    public static final int GAME_HERO_FAVORITES_VALUE = 2007;

    public static final int MAP_LOCATION_STATE_VALUE = 2008;

    public static final int TOURNAMENT_VALUE = 2009;

    public static final int PLAYER_CHALLENGE_VALUE = 2010;

    public static final int LOBBY_INVITE_VALUE = 2011;

    public static final int GAME_ACCOUNT_PLUS_VALUE = 2012;

    private final int value;

    private CSOTypes(int value) {
        this.value = value;
    }

    public final int getNumber() {
        return value;
    }

}
