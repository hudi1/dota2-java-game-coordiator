package org.tomass.dota.gc.clients.model;

public class LobbyInvitation {

    private boolean declined;
    private boolean acepted;
    private Integer count = 0;

    public boolean isDeclined() {
        return declined;
    }

    public void setDeclined(boolean declined) {
        this.declined = declined;
    }

    public boolean isAcepted() {
        return acepted;
    }

    public void setAcepted(boolean acepted) {
        this.acepted = acepted;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public LobbyInvitation incrementAndReturn() {
        count++;
        return this;
    }

    @Override
    public String toString() {
        return "LobbyInvitation [declined=" + declined + ", acepted=" + acepted + ", count=" + count + "]";
    }

}
