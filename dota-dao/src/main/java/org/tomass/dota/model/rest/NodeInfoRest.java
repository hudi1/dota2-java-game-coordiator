package org.tomass.dota.model.rest;

import java.io.Serializable;

public class NodeInfoRest implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer nodeId;
    private String nodeName;
    private Integer team1;
    private Integer team2;
    private String teamName1;
    private String teamName2;

    public Integer getNodeId() {
        return nodeId;
    }

    public void setNodeId(Integer nodeId) {
        this.nodeId = nodeId;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public Integer getTeam1() {
        return team1;
    }

    public void setTeam1(Integer team1) {
        this.team1 = team1;
    }

    public Integer getTeam2() {
        return team2;
    }

    public void setTeam2(Integer team2) {
        this.team2 = team2;
    }

    public String getTeamName1() {
        return teamName1;
    }

    public void setTeamName1(String teamName1) {
        this.teamName1 = teamName1;
    }

    public String getTeamName2() {
        return teamName2;
    }

    public void setTeamName2(String teamName2) {
        this.teamName2 = teamName2;
    }

}
