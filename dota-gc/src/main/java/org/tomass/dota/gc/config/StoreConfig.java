package org.tomass.dota.gc.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StoreConfig {

    @Bean
    @ConfigurationProperties(prefix = "config")
    public AppConfig appConfig() {
        AppConfig config = new AppConfig();
        return config;
    }

}
