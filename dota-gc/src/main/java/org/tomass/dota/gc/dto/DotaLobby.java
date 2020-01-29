package org.tomass.dota.gc.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DotaLobby implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer pocetPozvanych = 0;

    private Integer pocetZucastnenych = 0;

    private Long lobbyId;

    private String status;

    private Set<Long> expectedGoodGuys = new HashSet<>();

    private Set<Long> expectedBadGuys = new HashSet<>();

    private List<DotaLobbyMember> members = new ArrayList<>();

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

    public Long getLobbyId() {
        return lobbyId;
    }

    public void setLobbyId(Long lobbyId) {
        this.lobbyId = lobbyId;
    }

    public Set<Long> getExpectedGoodGuys() {
        return expectedGoodGuys;
    }

    public void setExpectedGoodGuys(Set<Long> expectedGoodGuys) {
        this.expectedGoodGuys = expectedGoodGuys;
    }

    public Set<Long> getExpectedBadGuys() {
        return expectedBadGuys;
    }

    public void setExpectedBadGuys(Set<Long> expectedBadGuys) {
        this.expectedBadGuys = expectedBadGuys;
    }

    public List<DotaLobbyMember> getMembers() {
        return members;
    }

    public void setMembers(List<DotaLobbyMember> members) {
        this.members = members;
    }

}
