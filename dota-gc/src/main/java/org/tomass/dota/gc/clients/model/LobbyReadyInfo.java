package org.tomass.dota.gc.clients.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class LobbyReadyInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean ready;
    private boolean coinTossReady;
    private List<String> messages;

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public boolean isCoinTossReady() {
        return coinTossReady;
    }

    public void setCoinTossReady(boolean coinTossReady) {
        this.coinTossReady = coinTossReady;
    }

    public List<String> getMessages() {
        if (messages == null) {
            messages = new ArrayList<>();
        }
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }

}
