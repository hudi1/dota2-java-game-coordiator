package org.tomass.dota.dao;

import java.util.List;
import org.slf4j.Logger;
import org.sqlproc.engine.SqlControl;
import org.sqlproc.engine.SqlEngineFactory;
import org.sqlproc.engine.SqlRowProcessor;
import org.sqlproc.engine.SqlSession;
import org.sqlproc.engine.SqlSessionFactory;
import org.tomass.dota.model.Team;

@SuppressWarnings("all")
public class TeamDao {
  protected final Logger logger = org.slf4j.LoggerFactory.getLogger(getClass());
  
  public TeamDao() {
  }
  
  public TeamDao(final SqlEngineFactory sqlEngineFactory) {
    this.sqlEngineFactory = sqlEngineFactory;
  }
  
  public TeamDao(final SqlEngineFactory sqlEngineFactory, final SqlSessionFactory sqlSessionFactory) {
    this.sqlEngineFactory = sqlEngineFactory;
    this.sqlSessionFactory = sqlSessionFactory;
  }
  
  protected SqlEngineFactory sqlEngineFactory;
  
  protected SqlSessionFactory sqlSessionFactory;
  
  public Team insert(final SqlSession sqlSession, final Team team, SqlControl sqlControl) {
    if (logger.isTraceEnabled()) {
    	logger.trace("sql insert team: " + team + " " + sqlControl);
    }
    String sqlName = (sqlControl != null && sqlControl.getSqlName() != null) ? sqlControl.getSqlName() : "INSERT_TEAM";
    org.sqlproc.engine.SqlCrudEngine sqlInsertTeam = sqlEngineFactory.getCheckedCrudEngine(sqlName);
    int count = sqlInsertTeam.insert(sqlSession, team, sqlControl);
    if (logger.isTraceEnabled()) {
    	logger.trace("sql insert team result: " + count + " " + team);
    }
    return (count > 0) ? team : null;
  }
  
  public Team insert(final Team team, SqlControl sqlControl) {
    return insert(sqlSessionFactory.getSqlSession(), team, sqlControl);
  }
  
  public Team insert(final SqlSession sqlSession, final Team team) {
    return insert(sqlSession, team, null);
  }
  
  public Team insert(final Team team) {
    return insert(team, null);
  }
  
  public Team get(final SqlSession sqlSession, final Team team, SqlControl sqlControl) {
    if (logger.isTraceEnabled()) {
    	logger.trace("sql get: " + team + " " + sqlControl);
    }
    String sqlName = (sqlControl != null && sqlControl.getSqlName() != null) ? sqlControl.getSqlName() : "GET_TEAM";
    org.sqlproc.engine.SqlCrudEngine sqlGetEngineTeam = sqlEngineFactory.getCheckedCrudEngine(sqlName);
    //sqlControl = getMoreResultClasses(team, sqlControl);
    Team teamGot = sqlGetEngineTeam.get(sqlSession, Team.class, team, sqlControl);
    if (logger.isTraceEnabled()) {
    	logger.trace("sql get team result: " + teamGot);
    }
    return teamGot;
  }
  
  public Team get(final Team team, SqlControl sqlControl) {
    return get(sqlSessionFactory.getSqlSession(), team, sqlControl);
  }
  
  public Team get(final SqlSession sqlSession, final Team team) {
    return get(sqlSession, team, null);
  }
  
  public Team get(final Team team) {
    return get(team, null);
  }
  
  public int update(final SqlSession sqlSession, final Team team, SqlControl sqlControl) {
    if (logger.isTraceEnabled()) {
    	logger.trace("sql update team: " + team + " " + sqlControl);
    }
    String sqlName = (sqlControl != null && sqlControl.getSqlName() != null) ? sqlControl.getSqlName() : "UPDATE_TEAM";
    org.sqlproc.engine.SqlCrudEngine sqlUpdateEngineTeam = sqlEngineFactory.getCheckedCrudEngine(sqlName);
    int count = sqlUpdateEngineTeam.update(sqlSession, team, sqlControl);
    if (logger.isTraceEnabled()) {
    	logger.trace("sql update team result count: " + count);
    }
    return count;
  }
  
  public int update(final Team team, SqlControl sqlControl) {
    return update(sqlSessionFactory.getSqlSession(), team, sqlControl);
  }
  
  public int update(final SqlSession sqlSession, final Team team) {
    return update(sqlSession, team, null);
  }
  
  public int update(final Team team) {
    return update(team, null);
  }
  
  public int delete(final SqlSession sqlSession, final Team team, SqlControl sqlControl) {
    if (logger.isTraceEnabled()) {
    	logger.trace("sql delete team: " + team + " " + sqlControl);
    }
    String sqlName = (sqlControl != null && sqlControl.getSqlName() != null) ? sqlControl.getSqlName() : "DELETE_TEAM";
    org.sqlproc.engine.SqlCrudEngine sqlDeleteEngineTeam = sqlEngineFactory.getCheckedCrudEngine(sqlName);
    int count = sqlDeleteEngineTeam.delete(sqlSession, team, sqlControl);
    if (logger.isTraceEnabled()) {
    	logger.trace("sql delete team result count: " + count);
    }
    return count;
  }
  
  public int delete(final Team team, SqlControl sqlControl) {
    return delete(sqlSessionFactory.getSqlSession(), team, sqlControl);
  }
  
  public int delete(final SqlSession sqlSession, final Team team) {
    return delete(sqlSession, team, null);
  }
  
  public int delete(final Team team) {
    return delete(team, null);
  }
  
  public List<Team> list(final SqlSession sqlSession, final Team team, SqlControl sqlControl) {
    if (logger.isTraceEnabled()) {
    	logger.trace("sql list team: " + team + " " + sqlControl);
    }
    String sqlName = (sqlControl != null && sqlControl.getSqlName() != null) ? sqlControl.getSqlName() : "SELECT_TEAM";
    org.sqlproc.engine.SqlQueryEngine sqlEngineTeam = sqlEngineFactory.getCheckedQueryEngine(sqlName);
    //sqlControl = getMoreResultClasses(team, sqlControl);
    List<Team> teamList = sqlEngineTeam.query(sqlSession, Team.class, team, sqlControl);
    if (logger.isTraceEnabled()) {
    	logger.trace("sql list team size: " + ((teamList != null) ? teamList.size() : "null"));
    }
    return teamList;
  }
  
  public List<Team> list(final Team team, SqlControl sqlControl) {
    return list(sqlSessionFactory.getSqlSession(), team, sqlControl);
  }
  
  public List<Team> list(final SqlSession sqlSession, final Team team) {
    return list(sqlSession, team, null);
  }
  
  public List<Team> list(final Team team) {
    return list(team, null);
  }
  
  public int query(final SqlSession sqlSession, final Team team, SqlControl sqlControl, final SqlRowProcessor<Team> sqlRowProcessor) {
    if (logger.isTraceEnabled()) {
    	logger.trace("sql query team: " + team + " " + sqlControl);
    }
    String sqlName = (sqlControl != null && sqlControl.getSqlName() != null) ? sqlControl.getSqlName() : "SELECT_TEAM";
    org.sqlproc.engine.SqlQueryEngine sqlEngineTeam = sqlEngineFactory.getCheckedQueryEngine(sqlName);
    //sqlControl = getMoreResultClasses(team, sqlControl);
    int rownums = sqlEngineTeam.query(sqlSession, Team.class, team, sqlControl, sqlRowProcessor);
    if (logger.isTraceEnabled()) {
    	logger.trace("sql query team size: " + rownums);
    }
    return rownums;
  }
  
  public int query(final Team team, SqlControl sqlControl, final SqlRowProcessor<Team> sqlRowProcessor) {
    return query(sqlSessionFactory.getSqlSession(), team, sqlControl, sqlRowProcessor);
  }
  
  public int query(final SqlSession sqlSession, final Team team, final SqlRowProcessor<Team> sqlRowProcessor) {
    return query(sqlSession, team, null, sqlRowProcessor);
  }
  
  public int query(final Team team, final SqlRowProcessor<Team> sqlRowProcessor) {
    return query(team, null, sqlRowProcessor);
  }
  
  public int count(final SqlSession sqlSession, final Team team, SqlControl sqlControl) {
    if (logger.isTraceEnabled()) {
    	logger.trace("count team: " + team + " " + sqlControl);
    }
    String sqlName = (sqlControl != null && sqlControl.getSqlName() != null) ? sqlControl.getSqlName() : "SELECT_TEAM";
    org.sqlproc.engine.SqlQueryEngine sqlEngineTeam = sqlEngineFactory.getCheckedQueryEngine(sqlName);
    //sqlControl = getMoreResultClasses(team, sqlControl);
    int count = sqlEngineTeam.queryCount(sqlSession, team, sqlControl);
    if (logger.isTraceEnabled()) {
    	logger.trace("count: " + count);
    }
    return count;
  }
  
  public int count(final Team team, SqlControl sqlControl) {
    return count(sqlSessionFactory.getSqlSession(), team, sqlControl);
  }
  
  public int count(final SqlSession sqlSession, final Team team) {
    return count(sqlSession, team, null);
  }
  
  public int count(final Team team) {
    return count(team, null);
  }
}
