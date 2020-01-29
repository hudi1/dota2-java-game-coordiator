package org.tomass.dota.gc.dto;

import java.io.Serializable;

public class DotaParty implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean readyCheck;

    private Integer membersCount;

    private Integer readyCountPlayer;

    private Integer pocetPozvanych = 0;

    private Integer pocetZucastnenych = 0;

    private Long partyId;

    private String status;

    public Integer getPocetPozvanych() {
        return pocetPozvanych;
    }

    public void setPocetPozvanych(Integer pocetPozvanych) {
        this.pocetPozvanych = pocetPozvanych;
    }

    public Integer getPocetZucastnenych() {
        return pocetZucastnenych;
    }

    public void setPocetZucastnenych(Integer pocetZucastnenych) {
        this.pocetZucastnenych = pocetZucastnenych;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isReadyCheck() {
        return readyCheck;
    }

    public void setReadyCheck(boolean readyCheck) {
        this.readyCheck = readyCheck;
    }

    public Integer getMembersCount() {
        return membersCount;
    }

    public void setMembersCount(Integer membersCount) {
        this.membersCount = membersCount;
    }

    public Integer getReadyCountPlayer() {
        return readyCountPlayer;
    }

    public void setReadyCountPlayer(Integer readyCountPlayer) {
        this.readyCountPlayer = readyCountPlayer;
    }

    public Long getPartyId() {
        return partyId;
    }

    public void setPartyId(Long partyId) {
        this.partyId = partyId;
    }

}
