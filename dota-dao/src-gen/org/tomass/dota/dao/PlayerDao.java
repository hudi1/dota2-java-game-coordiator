package org.tomass.dota.dao;

import java.util.List;
import org.slf4j.Logger;
import org.sqlproc.engine.SqlControl;
import org.sqlproc.engine.SqlEngineFactory;
import org.sqlproc.engine.SqlRowProcessor;
import org.sqlproc.engine.SqlSession;
import org.sqlproc.engine.SqlSessionFactory;
import org.tomass.dota.model.Player;

@SuppressWarnings("all")
public class PlayerDao {
  protected final Logger logger = org.slf4j.LoggerFactory.getLogger(getClass());
  
  public PlayerDao() {
  }
  
  public PlayerDao(final SqlEngineFactory sqlEngineFactory) {
    this.sqlEngineFactory = sqlEngineFactory;
  }
  
  public PlayerDao(final SqlEngineFactory sqlEngineFactory, final SqlSessionFactory sqlSessionFactory) {
    this.sqlEngineFactory = sqlEngineFactory;
    this.sqlSessionFactory = sqlSessionFactory;
  }
  
  protected SqlEngineFactory sqlEngineFactory;
  
  protected SqlSessionFactory sqlSessionFactory;
  
  public Player insert(final SqlSession sqlSession, final Player player, SqlControl sqlControl) {
    if (logger.isTraceEnabled()) {
    	logger.trace("sql insert player: " + player + " " + sqlControl);
    }
    String sqlName = (sqlControl != null && sqlControl.getSqlName() != null) ? sqlControl.getSqlName() : "INSERT_PLAYER";
    org.sqlproc.engine.SqlCrudEngine sqlInsertPlayer = sqlEngineFactory.getCheckedCrudEngine(sqlName);
    int count = sqlInsertPlayer.insert(sqlSession, player, sqlControl);
    if (logger.isTraceEnabled()) {
    	logger.trace("sql insert player result: " + count + " " + player);
    }
    return (count > 0) ? player : null;
  }
  
  public Player insert(final Player player, SqlControl sqlControl) {
    return insert(sqlSessionFactory.getSqlSession(), player, sqlControl);
  }
  
  public Player insert(final SqlSession sqlSession, final Player player) {
    return insert(sqlSession, player, null);
  }
  
  public Player insert(final Player player) {
    return insert(player, null);
  }
  
  public Player get(final SqlSession sqlSession, final Player player, SqlControl sqlControl) {
    if (logger.isTraceEnabled()) {
    	logger.trace("sql get: " + player + " " + sqlControl);
    }
    String sqlName = (sqlControl != null && sqlControl.getSqlName() != null) ? sqlControl.getSqlName() : "GET_PLAYER";
    org.sqlproc.engine.SqlCrudEngine sqlGetEnginePlayer = sqlEngineFactory.getCheckedCrudEngine(sqlName);
    //sqlControl = getMoreResultClasses(player, sqlControl);
    Player playerGot = sqlGetEnginePlayer.get(sqlSession, Player.class, player, sqlControl);
    if (logger.isTraceEnabled()) {
    	logger.trace("sql get player result: " + playerGot);
    }
    return playerGot;
  }
  
  public Player get(final Player player, SqlControl sqlControl) {
    return get(sqlSessionFactory.getSqlSession(), player, sqlControl);
  }
  
  public Player get(final SqlSession sqlSession, final Player player) {
    return get(sqlSession, player, null);
  }
  
  public Player get(final Player player) {
    return get(player, null);
  }
  
  public int update(final SqlSession sqlSession, final Player player, SqlControl sqlControl) {
    if (logger.isTraceEnabled()) {
    	logger.trace("sql update player: " + player + " " + sqlControl);
    }
    String sqlName = (sqlControl != null && sqlControl.getSqlName() != null) ? sqlControl.getSqlName() : "UPDATE_PLAYER";
    org.sqlproc.engine.SqlCrudEngine sqlUpdateEnginePlayer = sqlEngineFactory.getCheckedCrudEngine(sqlName);
    int count = sqlUpdateEnginePlayer.update(sqlSession, player, sqlControl);
    if (logger.isTraceEnabled()) {
    	logger.trace("sql update player result count: " + count);
    }
    return count;
  }
  
  public int update(final Player player, SqlControl sqlControl) {
    return update(sqlSessionFactory.getSqlSession(), player, sqlControl);
  }
  
  public int update(final SqlSession sqlSession, final Player player) {
    return update(sqlSession, player, null);
  }
  
  public int update(final Player player) {
    return update(player, null);
  }
  
  public int delete(final SqlSession sqlSession, final Player player, SqlControl sqlControl) {
    if (logger.isTraceEnabled()) {
    	logger.trace("sql delete player: " + player + " " + sqlControl);
    }
    String sqlName = (sqlControl != null && sqlControl.getSqlName() != null) ? sqlControl.getSqlName() : "DELETE_PLAYER";
    org.sqlproc.engine.SqlCrudEngine sqlDeleteEnginePlayer = sqlEngineFactory.getCheckedCrudEngine(sqlName);
    int count = sqlDeleteEnginePlayer.delete(sqlSession, player, sqlControl);
    if (logger.isTraceEnabled()) {
    	logger.trace("sql delete player result count: " + count);
    }
    return count;
  }
  
  public int delete(final Player player, SqlControl sqlControl) {
    return delete(sqlSessionFactory.getSqlSession(), player, sqlControl);
  }
  
  public int delete(final SqlSession sqlSession, final Player player) {
    return delete(sqlSession, player, null);
  }
  
  public int delete(final Player player) {
    return delete(player, null);
  }
  
  public List<Player> list(final SqlSession sqlSession, final Player player, SqlControl sqlControl) {
    if (logger.isTraceEnabled()) {
    	logger.trace("sql list player: " + player + " " + sqlControl);
    }
    String sqlName = (sqlControl != null && sqlControl.getSqlName() != null) ? sqlControl.getSqlName() : "SELECT_PLAYER";
    org.sqlproc.engine.SqlQueryEngine sqlEnginePlayer = sqlEngineFactory.getCheckedQueryEngine(sqlName);
    //sqlControl = getMoreResultClasses(player, sqlControl);
    List<Player> playerList = sqlEnginePlayer.query(sqlSession, Player.class, player, sqlControl);
    if (logger.isTraceEnabled()) {
    	logger.trace("sql list player size: " + ((playerList != null) ? playerList.size() : "null"));
    }
    return playerList;
  }
  
  public List<Player> list(final Player player, SqlControl sqlControl) {
    return list(sqlSessionFactory.getSqlSession(), player, sqlControl);
  }
  
  public List<Player> list(final SqlSession sqlSession, final Player player) {
    return list(sqlSession, player, null);
  }
  
  public List<Player> list(final Player player) {
    return list(player, null);
  }
  
  public int query(final SqlSession sqlSession, final Player player, SqlControl sqlControl, final SqlRowProcessor<Player> sqlRowProcessor) {
    if (logger.isTraceEnabled()) {
    	logger.trace("sql query player: " + player + " " + sqlControl);
    }
    String sqlName = (sqlControl != null && sqlControl.getSqlName() != null) ? sqlControl.getSqlName() : "SELECT_PLAYER";
    org.sqlproc.engine.SqlQueryEngine sqlEnginePlayer = sqlEngineFactory.getCheckedQueryEngine(sqlName);
    //sqlControl = getMoreResultClasses(player, sqlControl);
    int rownums = sqlEnginePlayer.query(sqlSession, Player.class, player, sqlControl, sqlRowProcessor);
    if (logger.isTraceEnabled()) {
    	logger.trace("sql query player size: " + rownums);
    }
    return rownums;
  }
  
  public int query(final Player player, SqlControl sqlControl, final SqlRowProcessor<Player> sqlRowProcessor) {
    return query(sqlSessionFactory.getSqlSession(), player, sqlControl, sqlRowProcessor);
  }
  
  public int query(final SqlSession sqlSession, final Player player, final SqlRowProcessor<Player> sqlRowProcessor) {
    return query(sqlSession, player, null, sqlRowProcessor);
  }
  
  public int query(final Player player, final SqlRowProcessor<Player> sqlRowProcessor) {
    return query(player, null, sqlRowProcessor);
  }
  
  public int count(final SqlSession sqlSession, final Player player, SqlControl sqlControl) {
    if (logger.isTraceEnabled()) {
    	logger.trace("count player: " + player + " " + sqlControl);
    }
    String sqlName = (sqlControl != null && sqlControl.getSqlName() != null) ? sqlControl.getSqlName() : "SELECT_PLAYER";
    org.sqlproc.engine.SqlQueryEngine sqlEnginePlayer = sqlEngineFactory.getCheckedQueryEngine(sqlName);
    //sqlControl = getMoreResultClasses(player, sqlControl);
    int count = sqlEnginePlayer.queryCount(sqlSession, player, sqlControl);
    if (logger.isTraceEnabled()) {
    	logger.trace("count: " + count);
    }
    return count;
  }
  
  public int count(final Player player, SqlControl sqlControl) {
    return count(sqlSessionFactory.getSqlSession(), player, sqlControl);
  }
  
  public int count(final SqlSession sqlSession, final Player player) {
    return count(sqlSession, player, null);
  }
  
  public int count(final Player player) {
    return count(player, null);
  }
}
