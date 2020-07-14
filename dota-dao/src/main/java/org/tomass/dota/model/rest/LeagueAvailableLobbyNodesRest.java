package org.tomass.dota.model.rest;

import java.util.ArrayList;
import java.util.List;

public class LeagueAvailableLobbyNodesRest {

    private List<NodeInfoRest> nodeInfo;

    public List<NodeInfoRest> getNodeInfo() {
        if (nodeInfo == null) {
            nodeInfo = new ArrayList<>();
        }
        return nodeInfo;
    }

    public void setNodeInfo(List<NodeInfoRest> nodeInfo) {
        this.nodeInfo = nodeInfo;
    }

}
