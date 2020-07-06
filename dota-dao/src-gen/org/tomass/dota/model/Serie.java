package org.tomass.dota.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import org.sqlproc.engine.annotation.Pojo;
import org.tomass.dota.model.Team;

@Pojo
@SuppressWarnings("all")
public class Serie implements Serializable {
  private static final long serialVersionUID = 1L;
  
  public static final String ORDER_BY_ID = "ID";
  
  public static final String ORDER_BY_TEAM1 = "TEAM1";
  
  public static final String ORDER_BY_TEAM2 = "TEAM2";
  
  public Serie() {
  }
  
  public Serie(final Integer leagueId, final String password, final Team team1, final Integer team1Wins, final Team team2, final Integer team2Wins, final Integer state, final LocalDateTime scheduledTime, final Integer bestOf) {
    super();
    setLeagueId(leagueId);
    setPassword(password);
    setTeam1(team1);
    setTeam1Wins(team1Wins);
    setTeam2(team2);
    setTeam2Wins(team2Wins);
    setState(state);
    setScheduledTime(scheduledTime);
    setBestOf(bestOf);
  }
  
  private Integer id;
  
  public Integer getId() {
    return this.id;
  }
  
  public void setId(final Integer id) {
    this.id = id;
  }
  
  public Serie withId(final Integer id) {
    this.id = id;
    return this;
  }
  
  private Integer leagueId;
  
  public Integer getLeagueId() {
    return this.leagueId;
  }
  
  public void setLeagueId(final Integer leagueId) {
    this.leagueId = leagueId;
  }
  
  public Serie withLeagueId(final Integer leagueId) {
    this.leagueId = leagueId;
    return this;
  }
  
  private String password;
  
  public String getPassword() {
    return this.password;
  }
  
  public void setPassword(final String password) {
    this.password = password;
  }
  
  public Serie withPassword(final String password) {
    this.password = password;
    return this;
  }
  
  private Team team1;
  
  public Team getTeam1() {
    return this.team1;
  }
  
  public void setTeam1(final Team team1) {
    this.team1 = team1;
  }
  
  public Serie withTeam1(final Team team1) {
    this.team1 = team1;
    return this;
  }
  
  private Integer team1Wins;
  
  public Integer getTeam1Wins() {
    return this.team1Wins;
  }
  
  public void setTeam1Wins(final Integer team1Wins) {
    this.team1Wins = team1Wins;
  }
  
  public Serie withTeam1Wins(final Integer team1Wins) {
    this.team1Wins = team1Wins;
    return this;
  }
  
  private Team team2;
  
  public Team getTeam2() {
    return this.team2;
  }
  
  public void setTeam2(final Team team2) {
    this.team2 = team2;
  }
  
  public Serie withTeam2(final Team team2) {
    this.team2 = team2;
    return this;
  }
  
  private Integer team2Wins;
  
  public Integer getTeam2Wins() {
    return this.team2Wins;
  }
  
  public void setTeam2Wins(final Integer team2Wins) {
    this.team2Wins = team2Wins;
  }
  
  public Serie withTeam2Wins(final Integer team2Wins) {
    this.team2Wins = team2Wins;
    return this;
  }
  
  private Integer state;
  
  public Integer getState() {
    return this.state;
  }
  
  public void setState(final Integer state) {
    this.state = state;
  }
  
  public Serie withState(final Integer state) {
    this.state = state;
    return this;
  }
  
  private LocalDateTime scheduledTime;
  
  public LocalDateTime getScheduledTime() {
    return this.scheduledTime;
  }
  
  public void setScheduledTime(final LocalDateTime scheduledTime) {
    this.scheduledTime = scheduledTime;
  }
  
  public Serie withScheduledTime(final LocalDateTime scheduledTime) {
    this.scheduledTime = scheduledTime;
    return this;
  }
  
  private Integer bestOf;
  
  public Integer getBestOf() {
    return this.bestOf;
  }
  
  public void setBestOf(final Integer bestOf) {
    this.bestOf = bestOf;
  }
  
  public Serie withBestOf(final Integer bestOf) {
    this.bestOf = bestOf;
    return this;
  }
  
  private String leagueName;
  
  public String getLeagueName() {
    return this.leagueName;
  }
  
  public void setLeagueName(final String leagueName) {
    this.leagueName = leagueName;
  }
  
  public Serie withLeagueName(final String leagueName) {
    this.leagueName = leagueName;
    return this;
  }
  
  private String name;
  
  public String getName() {
    return this.name;
  }
  
  public void setName(final String name) {
    this.name = name;
  }
  
  public Serie withName(final String name) {
    this.name = name;
    return this;
  }
  
  private String nodeName;
  
  public String getNodeName() {
    return this.nodeName;
  }
  
  public void setNodeName(final String nodeName) {
    this.nodeName = nodeName;
  }
  
  public Serie withNodeName(final String nodeName) {
    this.nodeName = nodeName;
    return this;
  }
  
  private Integer nodeId;
  
  public Integer getNodeId() {
    return this.nodeId;
  }
  
  public void setNodeId(final Integer nodeId) {
    this.nodeId = nodeId;
  }
  
  public Serie withNodeId(final Integer nodeId) {
    this.nodeId = nodeId;
    return this;
  }
  
  private Integer seriesId;
  
  public Integer getSeriesId() {
    return this.seriesId;
  }
  
  public void setSeriesId(final Integer seriesId) {
    this.seriesId = seriesId;
  }
  
  public Serie withSeriesId(final Integer seriesId) {
    this.seriesId = seriesId;
    return this;
  }
  
  private Long actualLobbyId;
  
  public Long getActualLobbyId() {
    return this.actualLobbyId;
  }
  
  public void setActualLobbyId(final Long actualLobbyId) {
    this.actualLobbyId = actualLobbyId;
  }
  
  public Serie withActualLobbyId(final Long actualLobbyId) {
    this.actualLobbyId = actualLobbyId;
    return this;
  }
  
  private Long actualMatchId;
  
  public Long getActualMatchId() {
    return this.actualMatchId;
  }
  
  public void setActualMatchId(final Long actualMatchId) {
    this.actualMatchId = actualMatchId;
  }
  
  public Serie withActualMatchId(final Long actualMatchId) {
    this.actualMatchId = actualMatchId;
    return this;
  }
  
  private byte[] detail;
  
  public byte[] getDetail() {
    return this.detail;
  }
  
  public void setDetail(final byte[] detail) {
    this.detail = detail;
  }
  
  public Serie withDetail(final byte[] detail) {
    this.detail = detail;
    return this;
  }
  
  @Override
  public boolean equals(final Object obj) {
    if (this == obj)
    	return true;
    if (obj == null)
    	return false;
    if (getClass() != obj.getClass())
    	return false;
    Serie other = (Serie) obj;
    if (id == null || !id.equals(other.id))
    	return false;
    return true;
  }
  
  @Override
  public int hashCode() {
    return java.util.Objects.hash(id);
  }
  
  @Override
  public String toString() {
    return "Serie [id=" + id + ", leagueId=" + leagueId + ", password=" + password + ", team1Wins=" + team1Wins + ", team2Wins=" + team2Wins + ", state=" + state + ", scheduledTime=" + scheduledTime + ", bestOf=" + bestOf + ", leagueName=" + leagueName + ", name=" + name + ", nodeName=" + nodeName + ", nodeId=" + nodeId + ", seriesId=" + seriesId + ", actualLobbyId=" + actualLobbyId + ", actualMatchId=" + actualMatchId + ", detail=" + detail + "]";
  }
  
  public String toStringFull() {
    return "Serie [id=" + id + ", leagueId=" + leagueId + ", password=" + password + ", team1=" + team1 + ", team1Wins=" + team1Wins + ", team2=" + team2 + ", team2Wins=" + team2Wins + ", state=" + state + ", scheduledTime=" + scheduledTime + ", bestOf=" + bestOf + ", leagueName=" + leagueName + ", name=" + name + ", nodeName=" + nodeName + ", nodeId=" + nodeId + ", seriesId=" + seriesId + ", actualLobbyId=" + actualLobbyId + ", actualMatchId=" + actualMatchId + ", detail=" + detail + "]";
  }
  
  public enum Attribute {
    leagueName,
    
    name,
    
    nodeName,
    
    nodeId,
    
    seriesId,
    
    actualLobbyId,
    
    actualMatchId,
    
    detail;
  }
  
  private Set<String> nullValues_ =  new java.util.HashSet<String>();
  
  public void setNull_(final Serie.Attribute... attributes) {
    if (attributes == null)
    	throw new IllegalArgumentException();
    for (Attribute attribute : attributes)
    	nullValues_.add(attribute.name());
  }
  
  public Serie withNull_(final Serie.Attribute... attributes) {
    setNull_(attributes);
    return this;
  }
  
  public void clearNull_(final Serie.Attribute... attributes) {
    if (attributes == null)
    	throw new IllegalArgumentException();
    for (Attribute attribute : attributes)
    	nullValues_.remove(attribute.name());
  }
  
  public Serie _clearNull_(final Serie.Attribute... attributes) {
    clearNull_(attributes);
    return this;
  }
  
  public void setNull_(final String... attributes) {
    if (attributes == null)
    	throw new IllegalArgumentException();
    for (String attribute : attributes)
    	nullValues_.add(attribute);
  }
  
  public Serie withNull_(final String... attributes) {
    setNull_(attributes);
    return this;
  }
  
  public void clearNull_(final String... attributes) {
    if (attributes == null)
    	throw new IllegalArgumentException();
    for (String attribute : attributes)
    	nullValues_.remove(attribute);
  }
  
  public Serie _clearNull_(final String... attributes) {
    clearNull_(attributes);
    return this;
  }
  
  public Boolean isNull_(final Serie.Attribute attribute) {
    if (attribute == null)
    	throw new IllegalArgumentException();
    return nullValues_.contains(attribute.name());
  }
  
  public Boolean isNull_(final String attrName) {
    if (attrName == null)
    	throw new IllegalArgumentException();
    return nullValues_.contains(attrName);
  }
  
  public Boolean isDef_(final String attrName, final Boolean isAttrNotNull) {
    if (attrName == null)
    	throw new IllegalArgumentException();
    if (nullValues_.contains(attrName))
    	return true;
    if (isAttrNotNull != null)
    	return isAttrNotNull;
    return false;
  }
  
  public void clearAllNull_() {
    nullValues_ = new java.util.HashSet<String>();
  }
  
  public enum Association {
    team1,
    
    team2;
  }
  
  private Set<String> initAssociations_ =  new java.util.HashSet<String>();
  
  public Set<String> getInitAssociations_() {
    return this.initAssociations_;
  }
  
  public void setInitAssociations_(final Set<String> initAssociations_) {
    this.initAssociations_ = initAssociations_;
  }
  
  public void setInit_(final Serie.Association... associations) {
    if (associations == null)
    	throw new IllegalArgumentException();
    for (Association association : associations)
    	initAssociations_.add(association.name());
  }
  
  public Serie withInit_(final Serie.Association... associations) {
    setInit_(associations);
    return this;
  }
  
  public void clearInit_(final Serie.Association... associations) {
    if (associations == null)
    	throw new IllegalArgumentException();
    for (Association association : associations)
    	initAssociations_.remove(association.name());
  }
  
  public Serie _clearInit_(final Serie.Association... associations) {
    clearInit_(associations);
    return this;
  }
  
  public void setInit_(final String... associations) {
    if (associations == null)
    	throw new IllegalArgumentException();
    for (String association : associations)
    	initAssociations_.add(association);
  }
  
  public Serie withInit_(final String... associations) {
    setInit_(associations);
    return this;
  }
  
  public void clearInit_(final String... associations) {
    if (associations == null)
    	throw new IllegalArgumentException();
    for (String association : associations)
    	initAssociations_.remove(association);
  }
  
  public Serie _clearInit_(final String... associations) {
    clearInit_(associations);
    return this;
  }
  
  public Boolean toInit_(final Serie.Association association) {
    if (association == null)
    	throw new IllegalArgumentException();
    return initAssociations_.contains(association.name());
  }
  
  public Boolean toInit_(final String association) {
    if (association == null)
    	throw new IllegalArgumentException();
    return initAssociations_.contains(association);
  }
  
  public void clearAllInit_() {
    initAssociations_ = new java.util.HashSet<String>();
  }
  
  public enum OpAttribute {
    id,
    
    leagueId,
    
    password,
    
    team1,
    
    team1Wins,
    
    team2,
    
    team2Wins,
    
    state,
    
    scheduledTime,
    
    bestOf,
    
    leagueName,
    
    name,
    
    nodeName,
    
    nodeId,
    
    seriesId,
    
    actualLobbyId,
    
    actualMatchId,
    
    detail;
  }
  
  private Map<String, String> operators_ =  new java.util.HashMap<String, String>();
  
  public Map<String, String> getOperators_() {
    return operators_;
  }
  
  public String getOp_(final String attrName) {
    if (attrName == null)
    	throw new IllegalArgumentException();
    return operators_.get(attrName);
  }
  
  public void setOp_(final String operator, final Serie.OpAttribute... attributes) {
    if (attributes == null)
    	throw new IllegalArgumentException();
    for (OpAttribute attribute : attributes)
    	operators_.put(attribute.name(), operator);
  }
  
  public Serie withOp_(final String operator, final Serie.OpAttribute... attributes) {
    setOp_(operator, attributes);
    return this;
  }
  
  public void clearOp_(final Serie.OpAttribute... attributes) {
    if (attributes == null)
    	throw new IllegalArgumentException();
    for (OpAttribute attribute : attributes)
    	operators_.remove(attribute.name());
  }
  
  public Serie _clearOp_(final Serie.OpAttribute... attributes) {
    clearOp_(attributes);
    return this;
  }
  
  public void setOp_(final String operator, final String... attributes) {
    if (attributes == null)
    	throw new IllegalArgumentException();
    for (String attribute : attributes)
    	operators_.put(attribute, operator);
  }
  
  public Serie withOp_(final String operator, final String... attributes) {
    setOp_(operator, attributes);
    return this;
  }
  
  public void clearOp_(final String... attributes) {
    if (attributes == null)
    	throw new IllegalArgumentException();
    for (String attribute : attributes)
    	operators_.remove(attribute);
  }
  
  public Serie _clearOp_(final String... attributes) {
    clearOp_(attributes);
    return this;
  }
  
  public void setNullOp_(final Serie.OpAttribute... attributes) {
    if (attributes == null)
    	throw new IllegalArgumentException();
    for (OpAttribute attribute : attributes)
    	operators_.put(attribute.name(), "is null");
  }
  
  public Serie withNullOp_(final Serie.OpAttribute... attributes) {
    setNullOp_(attributes);
    return this;
  }
  
  public void setNullOp_(final String... attributes) {
    if (attributes == null)
    	throw new IllegalArgumentException();
    for (String attribute : attributes)
    	operators_.put(attribute, "is null");
  }
  
  public Serie withNullOp_(final String... attributes) {
    setNullOp_(attributes);
    return this;
  }
  
  public void clearAllOps_() {
    operators_ = new java.util.HashMap<String, String>();
  }
}
