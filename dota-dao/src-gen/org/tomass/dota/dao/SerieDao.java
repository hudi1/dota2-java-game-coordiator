package org.tomass.dota.dao;

import java.util.List;
import org.slf4j.Logger;
import org.sqlproc.engine.SqlControl;
import org.sqlproc.engine.SqlEngineFactory;
import org.sqlproc.engine.SqlRowProcessor;
import org.sqlproc.engine.SqlSession;
import org.sqlproc.engine.SqlSessionFactory;
import org.tomass.dota.model.Serie;

@SuppressWarnings("all")
public class SerieDao {
  protected final Logger logger = org.slf4j.LoggerFactory.getLogger(getClass());
  
  public SerieDao() {
  }
  
  public SerieDao(final SqlEngineFactory sqlEngineFactory) {
    this.sqlEngineFactory = sqlEngineFactory;
  }
  
  public SerieDao(final SqlEngineFactory sqlEngineFactory, final SqlSessionFactory sqlSessionFactory) {
    this.sqlEngineFactory = sqlEngineFactory;
    this.sqlSessionFactory = sqlSessionFactory;
  }
  
  protected SqlEngineFactory sqlEngineFactory;
  
  protected SqlSessionFactory sqlSessionFactory;
  
  public Serie insert(final SqlSession sqlSession, final Serie serie, SqlControl sqlControl) {
    if (logger.isTraceEnabled()) {
    	logger.trace("sql insert serie: " + serie + " " + sqlControl);
    }
    String sqlName = (sqlControl != null && sqlControl.getSqlName() != null) ? sqlControl.getSqlName() : "INSERT_SERIE";
    org.sqlproc.engine.SqlCrudEngine sqlInsertSerie = sqlEngineFactory.getCheckedCrudEngine(sqlName);
    int count = sqlInsertSerie.insert(sqlSession, serie, sqlControl);
    if (logger.isTraceEnabled()) {
    	logger.trace("sql insert serie result: " + count + " " + serie);
    }
    return (count > 0) ? serie : null;
  }
  
  public Serie insert(final Serie serie, SqlControl sqlControl) {
    return insert(sqlSessionFactory.getSqlSession(), serie, sqlControl);
  }
  
  public Serie insert(final SqlSession sqlSession, final Serie serie) {
    return insert(sqlSession, serie, null);
  }
  
  public Serie insert(final Serie serie) {
    return insert(serie, null);
  }
  
  public Serie get(final SqlSession sqlSession, final Serie serie, SqlControl sqlControl) {
    if (logger.isTraceEnabled()) {
    	logger.trace("sql get: " + serie + " " + sqlControl);
    }
    String sqlName = (sqlControl != null && sqlControl.getSqlName() != null) ? sqlControl.getSqlName() : "GET_SERIE";
    org.sqlproc.engine.SqlCrudEngine sqlGetEngineSerie = sqlEngineFactory.getCheckedCrudEngine(sqlName);
    //sqlControl = getMoreResultClasses(serie, sqlControl);
    Serie serieGot = sqlGetEngineSerie.get(sqlSession, Serie.class, serie, sqlControl);
    if (logger.isTraceEnabled()) {
    	logger.trace("sql get serie result: " + serieGot);
    }
    return serieGot;
  }
  
  public Serie get(final Serie serie, SqlControl sqlControl) {
    return get(sqlSessionFactory.getSqlSession(), serie, sqlControl);
  }
  
  public Serie get(final SqlSession sqlSession, final Serie serie) {
    return get(sqlSession, serie, null);
  }
  
  public Serie get(final Serie serie) {
    return get(serie, null);
  }
  
  public int update(final SqlSession sqlSession, final Serie serie, SqlControl sqlControl) {
    if (logger.isTraceEnabled()) {
    	logger.trace("sql update serie: " + serie + " " + sqlControl);
    }
    String sqlName = (sqlControl != null && sqlControl.getSqlName() != null) ? sqlControl.getSqlName() : "UPDATE_SERIE";
    org.sqlproc.engine.SqlCrudEngine sqlUpdateEngineSerie = sqlEngineFactory.getCheckedCrudEngine(sqlName);
    int count = sqlUpdateEngineSerie.update(sqlSession, serie, sqlControl);
    if (logger.isTraceEnabled()) {
    	logger.trace("sql update serie result count: " + count);
    }
    return count;
  }
  
  public int update(final Serie serie, SqlControl sqlControl) {
    return update(sqlSessionFactory.getSqlSession(), serie, sqlControl);
  }
  
  public int update(final SqlSession sqlSession, final Serie serie) {
    return update(sqlSession, serie, null);
  }
  
  public int update(final Serie serie) {
    return update(serie, null);
  }
  
  public int delete(final SqlSession sqlSession, final Serie serie, SqlControl sqlControl) {
    if (logger.isTraceEnabled()) {
    	logger.trace("sql delete serie: " + serie + " " + sqlControl);
    }
    String sqlName = (sqlControl != null && sqlControl.getSqlName() != null) ? sqlControl.getSqlName() : "DELETE_SERIE";
    org.sqlproc.engine.SqlCrudEngine sqlDeleteEngineSerie = sqlEngineFactory.getCheckedCrudEngine(sqlName);
    int count = sqlDeleteEngineSerie.delete(sqlSession, serie, sqlControl);
    if (logger.isTraceEnabled()) {
    	logger.trace("sql delete serie result count: " + count);
    }
    return count;
  }
  
  public int delete(final Serie serie, SqlControl sqlControl) {
    return delete(sqlSessionFactory.getSqlSession(), serie, sqlControl);
  }
  
  public int delete(final SqlSession sqlSession, final Serie serie) {
    return delete(sqlSession, serie, null);
  }
  
  public int delete(final Serie serie) {
    return delete(serie, null);
  }
  
  public List<Serie> list(final SqlSession sqlSession, final Serie serie, SqlControl sqlControl) {
    if (logger.isTraceEnabled()) {
    	logger.trace("sql list serie: " + serie + " " + sqlControl);
    }
    String sqlName = (sqlControl != null && sqlControl.getSqlName() != null) ? sqlControl.getSqlName() : "SELECT_SERIE";
    org.sqlproc.engine.SqlQueryEngine sqlEngineSerie = sqlEngineFactory.getCheckedQueryEngine(sqlName);
    //sqlControl = getMoreResultClasses(serie, sqlControl);
    List<Serie> serieList = sqlEngineSerie.query(sqlSession, Serie.class, serie, sqlControl);
    if (logger.isTraceEnabled()) {
    	logger.trace("sql list serie size: " + ((serieList != null) ? serieList.size() : "null"));
    }
    return serieList;
  }
  
  public List<Serie> list(final Serie serie, SqlControl sqlControl) {
    return list(sqlSessionFactory.getSqlSession(), serie, sqlControl);
  }
  
  public List<Serie> list(final SqlSession sqlSession, final Serie serie) {
    return list(sqlSession, serie, null);
  }
  
  public List<Serie> list(final Serie serie) {
    return list(serie, null);
  }
  
  public int query(final SqlSession sqlSession, final Serie serie, SqlControl sqlControl, final SqlRowProcessor<Serie> sqlRowProcessor) {
    if (logger.isTraceEnabled()) {
    	logger.trace("sql query serie: " + serie + " " + sqlControl);
    }
    String sqlName = (sqlControl != null && sqlControl.getSqlName() != null) ? sqlControl.getSqlName() : "SELECT_SERIE";
    org.sqlproc.engine.SqlQueryEngine sqlEngineSerie = sqlEngineFactory.getCheckedQueryEngine(sqlName);
    //sqlControl = getMoreResultClasses(serie, sqlControl);
    int rownums = sqlEngineSerie.query(sqlSession, Serie.class, serie, sqlControl, sqlRowProcessor);
    if (logger.isTraceEnabled()) {
    	logger.trace("sql query serie size: " + rownums);
    }
    return rownums;
  }
  
  public int query(final Serie serie, SqlControl sqlControl, final SqlRowProcessor<Serie> sqlRowProcessor) {
    return query(sqlSessionFactory.getSqlSession(), serie, sqlControl, sqlRowProcessor);
  }
  
  public int query(final SqlSession sqlSession, final Serie serie, final SqlRowProcessor<Serie> sqlRowProcessor) {
    return query(sqlSession, serie, null, sqlRowProcessor);
  }
  
  public int query(final Serie serie, final SqlRowProcessor<Serie> sqlRowProcessor) {
    return query(serie, null, sqlRowProcessor);
  }
  
  public int count(final SqlSession sqlSession, final Serie serie, SqlControl sqlControl) {
    if (logger.isTraceEnabled()) {
    	logger.trace("count serie: " + serie + " " + sqlControl);
    }
    String sqlName = (sqlControl != null && sqlControl.getSqlName() != null) ? sqlControl.getSqlName() : "SELECT_SERIE";
    org.sqlproc.engine.SqlQueryEngine sqlEngineSerie = sqlEngineFactory.getCheckedQueryEngine(sqlName);
    //sqlControl = getMoreResultClasses(serie, sqlControl);
    int count = sqlEngineSerie.queryCount(sqlSession, serie, sqlControl);
    if (logger.isTraceEnabled()) {
    	logger.trace("count: " + count);
    }
    return count;
  }
  
  public int count(final Serie serie, SqlControl sqlControl) {
    return count(sqlSessionFactory.getSqlSession(), serie, sqlControl);
  }
  
  public int count(final SqlSession sqlSession, final Serie serie) {
    return count(sqlSession, serie, null);
  }
  
  public int count(final Serie serie) {
    return count(serie, null);
  }
}
