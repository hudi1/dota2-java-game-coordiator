package org.tomass.dota.gc.config;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.tomass.dota.gc.util.DotaGlobalConstant;
import org.tomass.dota.webapi.model.TeamInfo;
import org.tomass.protobuf.dota.DotaGcmessagesClientMatchManagement.CMsgPracticeLobbySetDetails;

public class ScheduledSeries {

    private LocalDateTime scheduledTime;

    private String password;

    private TeamInfo teamInfo1;

    private TeamInfo teamInfo2;

    // bo1/bo3
    private Integer seriesType;

    private Integer seriesId;

    private String seriesName;

    private Integer nodeId;

    private String nodeName;

    private boolean nodeFound;

    private Integer leagueId;

    private String leagueName;

    private boolean admin;

    private Integer state;

    private List<LobbyMatch> matches;

    private CMsgPracticeLobbySetDetails.Builder detail;

    public ScheduledSeries() {
        this.matches = new ArrayList<>();
    }

    public ScheduledSeries(Integer leagueId, TeamInfo teamInfo1, TeamInfo teamInfo2, String password,
            LocalDateTime scheduledTime) {
        this.leagueId = leagueId;
        this.password = password;
        this.scheduledTime = scheduledTime;
        this.state = DotaGlobalConstant.LOBBY_SERIES_SCHEDULED;
        this.matches = new ArrayList<>();
        this.teamInfo1 = teamInfo1;
        this.teamInfo2 = teamInfo2;
    }

    public LocalDateTime getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(LocalDateTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public TeamInfo getTeamInfo1() {
        return teamInfo1;
    }

    public void setTeamInfo1(TeamInfo teamInfo1) {
        this.teamInfo1 = teamInfo1;
    }

    public TeamInfo getTeamInfo2() {
        return teamInfo2;
    }

    public void setTeamInfo2(TeamInfo teamInfo2) {
        this.teamInfo2 = teamInfo2;
    }

    public Integer getSeriesType() {
        return seriesType;
    }

    public void setSeriesType(Integer seriesType) {
        this.seriesType = seriesType;
    }

    public Integer getSeriesId() {
        return seriesId;
    }

    public void setSeriesId(Integer seriesId) {
        this.seriesId = seriesId;
    }

    public String getSeriesName() {
        return seriesName;
    }

    public void setSeriesName(String seriesName) {
        this.seriesName = seriesName;
    }

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

    public boolean isNodeFound() {
        return nodeFound;
    }

    public void setNodeFound(boolean nodeFound) {
        this.nodeFound = nodeFound;
    }

    public Integer getLeagueId() {
        return leagueId;
    }

    public void setLeagueId(Integer leagueId) {
        this.leagueId = leagueId;
    }

    public String getLeagueName() {
        return leagueName;
    }

    public void setLeagueName(String leagueName) {
        this.leagueName = leagueName;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public List<LobbyMatch> getMatches() {
        return matches;
    }

    public void setMatches(List<LobbyMatch> matches) {
        this.matches = matches;
    }

    public CMsgPracticeLobbySetDetails.Builder getDetail() {
        return detail;
    }

    public void setDetail(CMsgPracticeLobbySetDetails.Builder detail) {
        this.detail = detail;
    }

    @Override
    public String toString() {
        return "ScheduledSeries [scheduledTime=" + scheduledTime + ", password=" + password + ", teamInfo1=" + teamInfo1
                + ", teamInfo2=" + teamInfo2 + ", seriesType=" + seriesType + ", seriesId=" + seriesId + ", seriesName="
                + seriesName + ", nodeId=" + nodeId + ", nodeName=" + nodeName + ", nodeFound=" + nodeFound
                + ", leagueId=" + leagueId + ", leagueName=" + leagueName + ", admin=" + admin + ", state=" + state
                + ", matches=" + matches + ", detail=" + detail + "]";
    }

}
