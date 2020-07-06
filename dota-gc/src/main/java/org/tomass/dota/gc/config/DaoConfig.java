package org.tomass.dota.gc.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.sqlproc.engine.SqlFeature;
import org.sqlproc.engine.plugin.SimpleSqlPluginFactory;
import org.sqlproc.engine.spring.SpringEngineFactory;
import org.sqlproc.engine.spring.SpringSessionFactory;
import org.tomass.dota.dao.PlayerDao;
import org.tomass.dota.dao.SerieDao;
import org.tomass.dota.dao.TeamDao;
import org.tomass.dota.dao.TeamPlayerDao;

@Configuration
public class DaoConfig {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Bean
    public SpringEngineFactory engineFactory() {
        SpringEngineFactory sqlFactory = new SpringEngineFactory();
        sqlFactory.setMetaFilesNames("statements.meta", "custom-statements.meta");
        sqlFactory.setFilter(SqlFeature.MYSQL);
        SimpleSqlPluginFactory sqlPluginFactory = SimpleSqlPluginFactory.getInstance();
        sqlFactory.setPluginFactory(sqlPluginFactory);
        return sqlFactory;
    }

    @Bean
    public SpringSessionFactory springSessionFactory() {
        SpringSessionFactory sqlSessionFactory = new SpringSessionFactory(jdbcTemplate);
        return sqlSessionFactory;
    }

    @Bean
    public PlayerDao playerDao() {
        return new PlayerDao(engineFactory(), springSessionFactory());
    }

    @Bean
    public SerieDao serieDao() {
        return new SerieDao(engineFactory(), springSessionFactory());
    }

    @Bean
    public TeamDao teamDao() {
        return new TeamDao(engineFactory(), springSessionFactory());
    }

    @Bean
    public TeamPlayerDao teamPlayerDao() {
        return new TeamPlayerDao(engineFactory(), springSessionFactory());
    }

}
