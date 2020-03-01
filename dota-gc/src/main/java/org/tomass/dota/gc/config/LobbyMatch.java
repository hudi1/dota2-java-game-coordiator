package org.tomass.dota.gc.config;

public class LobbyMatch {

    private Long matchId;

    private Long lobbyId;

    public LobbyMatch() {
    }

    public LobbyMatch(Long matchId, Long lobbyId) {
        this.matchId = matchId;
        this.lobbyId = lobbyId;
    }

    public Long getMatchId() {
        return matchId;
    }

    public void setMatchId(Long matchId) {
        this.matchId = matchId;
    }

    public Long getLobbyId() {
        return lobbyId;
    }

    public void setLobbyId(Long lobbyId) {
        this.lobbyId = lobbyId;
    }

}
