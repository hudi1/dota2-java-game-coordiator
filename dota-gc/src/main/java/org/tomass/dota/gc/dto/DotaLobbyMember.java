package org.tomass.dota.gc.dto;

import java.io.Serializable;

import org.tomass.protobuf.dota.DotaSharedEnums.DOTA_GC_TEAM;

public class DotaLobbyMember implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;

    private Long id;

    private DOTA_GC_TEAM team;

    private Integer channelId;

    public DotaLobbyMember() {
    }

    public DotaLobbyMember(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DOTA_GC_TEAM getTeam() {
        return team;
    }

    public void setTeam(DOTA_GC_TEAM team) {
        this.team = team;
    }

    public Integer getChannelId() {
        return channelId;
    }

    public void setChannelId(Integer channelId) {
        this.channelId = channelId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DotaLobbyMember other = (DotaLobbyMember) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "DotaLobbyMember [name=" + name + ", id=" + id + ", team=" + team + ", channelId=" + channelId + "]";
    }

}
