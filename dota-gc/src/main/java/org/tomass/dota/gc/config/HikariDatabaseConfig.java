package org.tomass.dota.gc.config;

import javax.sql.DataSource;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
public class HikariDatabaseConfig {

    @Bean
    @Primary
    @ConfigurationProperties(prefix = "hikari.db")
    public HikariConfig refConfig() {
        return new HikariConfig();
    }

    @Bean
    @Primary
    public DataSource dataSource() {
        HikariConfig config = refConfig();
        return new HikariDataSource(config);
    }

    @Bean
    public JdbcTemplate jdbcTemplate() {
        return new JdbcTemplate(dataSource());
    }

}
