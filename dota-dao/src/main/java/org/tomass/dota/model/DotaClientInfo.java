package org.tomass.dota.model;

import java.io.Serializable;

public class DotaClientInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long lobbyId;
    private Long partyId;
    private String name;
    private boolean logged;
    private boolean ready;
    private String result;

    public Long getLobbyId() {
        return lobbyId;
    }

    public void setLobbyId(Long lobbyId) {
        this.lobbyId = lobbyId;
    }

    public Long getPartyId() {
        return partyId;
    }

    public void setPartyId(Long partyId) {
        this.partyId = partyId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isLogged() {
        return logged;
    }

    public void setLogged(boolean logged) {
        this.logged = logged;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "DotaClientInfo [lobbyId=" + lobbyId + ", partyId=" + partyId + ", name=" + name + ", logged=" + logged
                + ", ready=" + ready + ", result=" + result + "]";
    }

}
