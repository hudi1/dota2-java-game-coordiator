package org.tomass.dota.model.rest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class LeagueInfoAdminListRest implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<LeagueInfoAdminRest> info;

    public List<LeagueInfoAdminRest> getInfo() {
        if (info == null) {
            info = new ArrayList<LeagueInfoAdminRest>();
        }
        return info;
    }

    public void setInfo(List<LeagueInfoAdminRest> info) {
        this.info = info;
    }
}
