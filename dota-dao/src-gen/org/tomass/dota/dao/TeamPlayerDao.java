package org.tomass.dota.dao;

import java.util.List;
import org.slf4j.Logger;
import org.sqlproc.engine.SqlControl;
import org.sqlproc.engine.SqlEngineFactory;
import org.sqlproc.engine.SqlRowProcessor;
import org.sqlproc.engine.SqlSession;
import org.sqlproc.engine.SqlSessionFactory;
import org.tomass.dota.model.TeamPlayer;

@SuppressWarnings("all")
public class TeamPlayerDao {
  protected final Logger logger = org.slf4j.LoggerFactory.getLogger(getClass());
  
  public TeamPlayerDao() {
  }
  
  public TeamPlayerDao(final SqlEngineFactory sqlEngineFactory) {
    this.sqlEngineFactory = sqlEngineFactory;
  }
  
  public TeamPlayerDao(final SqlEngineFactory sqlEngineFactory, final SqlSessionFactory sqlSessionFactory) {
    this.sqlEngineFactory = sqlEngineFactory;
    this.sqlSessionFactory = sqlSessionFactory;
  }
  
  protected SqlEngineFactory sqlEngineFactory;
  
  protected SqlSessionFactory sqlSessionFactory;
  
  public TeamPlayer insert(final SqlSession sqlSession, final TeamPlayer teamPlayer, SqlControl sqlControl) {
    if (logger.isTraceEnabled()) {
    	logger.trace("sql insert teamPlayer: " + teamPlayer + " " + sqlControl);
    }
    String sqlName = (sqlControl != null && sqlControl.getSqlName() != null) ? sqlControl.getSqlName() : "INSERT_TEAM_PLAYER";
    org.sqlproc.engine.SqlCrudEngine sqlInsertTeamPlayer = sqlEngineFactory.getCheckedCrudEngine(sqlName);
    int count = sqlInsertTeamPlayer.insert(sqlSession, teamPlayer, sqlControl);
    if (logger.isTraceEnabled()) {
    	logger.trace("sql insert teamPlayer result: " + count + " " + teamPlayer);
    }
    return (count > 0) ? teamPlayer : null;
  }
  
  public TeamPlayer insert(final TeamPlayer teamPlayer, SqlControl sqlControl) {
    return insert(sqlSessionFactory.getSqlSession(), teamPlayer, sqlControl);
  }
  
  public TeamPlayer insert(final SqlSession sqlSession, final TeamPlayer teamPlayer) {
    return insert(sqlSession, teamPlayer, null);
  }
  
  public TeamPlayer insert(final TeamPlayer teamPlayer) {
    return insert(teamPlayer, null);
  }
  
  public TeamPlayer get(final SqlSession sqlSession, final TeamPlayer teamPlayer, SqlControl sqlControl) {
    if (logger.isTraceEnabled()) {
    	logger.trace("sql get: " + teamPlayer + " " + sqlControl);
    }
    String sqlName = (sqlControl != null && sqlControl.getSqlName() != null) ? sqlControl.getSqlName() : "GET_TEAM_PLAYER";
    org.sqlproc.engine.SqlCrudEngine sqlGetEngineTeamPlayer = sqlEngineFactory.getCheckedCrudEngine(sqlName);
    //sqlControl = getMoreResultClasses(teamPlayer, sqlControl);
    TeamPlayer teamPlayerGot = sqlGetEngineTeamPlayer.get(sqlSession, TeamPlayer.class, teamPlayer, sqlControl);
    if (logger.isTraceEnabled()) {
    	logger.trace("sql get teamPlayer result: " + teamPlayerGot);
    }
    return teamPlayerGot;
  }
  
  public TeamPlayer get(final TeamPlayer teamPlayer, SqlControl sqlControl) {
    return get(sqlSessionFactory.getSqlSession(), teamPlayer, sqlControl);
  }
  
  public TeamPlayer get(final SqlSession sqlSession, final TeamPlayer teamPlayer) {
    return get(sqlSession, teamPlayer, null);
  }
  
  public TeamPlayer get(final TeamPlayer teamPlayer) {
    return get(teamPlayer, null);
  }
  
  public int update(final SqlSession sqlSession, final TeamPlayer teamPlayer, SqlControl sqlControl) {
    if (logger.isTraceEnabled()) {
    	logger.trace("sql update teamPlayer: " + teamPlayer + " " + sqlControl);
    }
    String sqlName = (sqlControl != null && sqlControl.getSqlName() != null) ? sqlControl.getSqlName() : "UPDATE_TEAM_PLAYER";
    org.sqlproc.engine.SqlCrudEngine sqlUpdateEngineTeamPlayer = sqlEngineFactory.getCheckedCrudEngine(sqlName);
    int count = sqlUpdateEngineTeamPlayer.update(sqlSession, teamPlayer, sqlControl);
    if (logger.isTraceEnabled()) {
    	logger.trace("sql update teamPlayer result count: " + count);
    }
    return count;
  }
  
  public int update(final TeamPlayer teamPlayer, SqlControl sqlControl) {
    return update(sqlSessionFactory.getSqlSession(), teamPlayer, sqlControl);
  }
  
  public int update(final SqlSession sqlSession, final TeamPlayer teamPlayer) {
    return update(sqlSession, teamPlayer, null);
  }
  
  public int update(final TeamPlayer teamPlayer) {
    return update(teamPlayer, null);
  }
  
  public int delete(final SqlSession sqlSession, final TeamPlayer teamPlayer, SqlControl sqlControl) {
    if (logger.isTraceEnabled()) {
    	logger.trace("sql delete teamPlayer: " + teamPlayer + " " + sqlControl);
    }
    String sqlName = (sqlControl != null && sqlControl.getSqlName() != null) ? sqlControl.getSqlName() : "DELETE_TEAM_PLAYER";
    org.sqlproc.engine.SqlCrudEngine sqlDeleteEngineTeamPlayer = sqlEngineFactory.getCheckedCrudEngine(sqlName);
    int count = sqlDeleteEngineTeamPlayer.delete(sqlSession, teamPlayer, sqlControl);
    if (logger.isTraceEnabled()) {
    	logger.trace("sql delete teamPlayer result count: " + count);
    }
    return count;
  }
  
  public int delete(final TeamPlayer teamPlayer, SqlControl sqlControl) {
    return delete(sqlSessionFactory.getSqlSession(), teamPlayer, sqlControl);
  }
  
  public int delete(final SqlSession sqlSession, final TeamPlayer teamPlayer) {
    return delete(sqlSession, teamPlayer, null);
  }
  
  public int delete(final TeamPlayer teamPlayer) {
    return delete(teamPlayer, null);
  }
  
  public List<TeamPlayer> list(final SqlSession sqlSession, final TeamPlayer teamPlayer, SqlControl sqlControl) {
    if (logger.isTraceEnabled()) {
    	logger.trace("sql list teamPlayer: " + teamPlayer + " " + sqlControl);
    }
    String sqlName = (sqlControl != null && sqlControl.getSqlName() != null) ? sqlControl.getSqlName() : "SELECT_TEAM_PLAYER";
    org.sqlproc.engine.SqlQueryEngine sqlEngineTeamPlayer = sqlEngineFactory.getCheckedQueryEngine(sqlName);
    //sqlControl = getMoreResultClasses(teamPlayer, sqlControl);
    List<TeamPlayer> teamPlayerList = sqlEngineTeamPlayer.query(sqlSession, TeamPlayer.class, teamPlayer, sqlControl);
    if (logger.isTraceEnabled()) {
    	logger.trace("sql list teamPlayer size: " + ((teamPlayerList != null) ? teamPlayerList.size() : "null"));
    }
    return teamPlayerList;
  }
  
  public List<TeamPlayer> list(final TeamPlayer teamPlayer, SqlControl sqlControl) {
    return list(sqlSessionFactory.getSqlSession(), teamPlayer, sqlControl);
  }
  
  public List<TeamPlayer> list(final SqlSession sqlSession, final TeamPlayer teamPlayer) {
    return list(sqlSession, teamPlayer, null);
  }
  
  public List<TeamPlayer> list(final TeamPlayer teamPlayer) {
    return list(teamPlayer, null);
  }
  
  public int query(final SqlSession sqlSession, final TeamPlayer teamPlayer, SqlControl sqlControl, final SqlRowProcessor<TeamPlayer> sqlRowProcessor) {
    if (logger.isTraceEnabled()) {
    	logger.trace("sql query teamPlayer: " + teamPlayer + " " + sqlControl);
    }
    String sqlName = (sqlControl != null && sqlControl.getSqlName() != null) ? sqlControl.getSqlName() : "SELECT_TEAM_PLAYER";
    org.sqlproc.engine.SqlQueryEngine sqlEngineTeamPlayer = sqlEngineFactory.getCheckedQueryEngine(sqlName);
    //sqlControl = getMoreResultClasses(teamPlayer, sqlControl);
    int rownums = sqlEngineTeamPlayer.query(sqlSession, TeamPlayer.class, teamPlayer, sqlControl, sqlRowProcessor);
    if (logger.isTraceEnabled()) {
    	logger.trace("sql query teamPlayer size: " + rownums);
    }
    return rownums;
  }
  
  public int query(final TeamPlayer teamPlayer, SqlControl sqlControl, final SqlRowProcessor<TeamPlayer> sqlRowProcessor) {
    return query(sqlSessionFactory.getSqlSession(), teamPlayer, sqlControl, sqlRowProcessor);
  }
  
  public int query(final SqlSession sqlSession, final TeamPlayer teamPlayer, final SqlRowProcessor<TeamPlayer> sqlRowProcessor) {
    return query(sqlSession, teamPlayer, null, sqlRowProcessor);
  }
  
  public int query(final TeamPlayer teamPlayer, final SqlRowProcessor<TeamPlayer> sqlRowProcessor) {
    return query(teamPlayer, null, sqlRowProcessor);
  }
  
  public int count(final SqlSession sqlSession, final TeamPlayer teamPlayer, SqlControl sqlControl) {
    if (logger.isTraceEnabled()) {
    	logger.trace("count teamPlayer: " + teamPlayer + " " + sqlControl);
    }
    String sqlName = (sqlControl != null && sqlControl.getSqlName() != null) ? sqlControl.getSqlName() : "SELECT_TEAM_PLAYER";
    org.sqlproc.engine.SqlQueryEngine sqlEngineTeamPlayer = sqlEngineFactory.getCheckedQueryEngine(sqlName);
    //sqlControl = getMoreResultClasses(teamPlayer, sqlControl);
    int count = sqlEngineTeamPlayer.queryCount(sqlSession, teamPlayer, sqlControl);
    if (logger.isTraceEnabled()) {
    	logger.trace("count: " + count);
    }
    return count;
  }
  
  public int count(final TeamPlayer teamPlayer, SqlControl sqlControl) {
    return count(sqlSessionFactory.getSqlSession(), teamPlayer, sqlControl);
  }
  
  public int count(final SqlSession sqlSession, final TeamPlayer teamPlayer) {
    return count(sqlSession, teamPlayer, null);
  }
  
  public int count(final TeamPlayer teamPlayer) {
    return count(teamPlayer, null);
  }
}
