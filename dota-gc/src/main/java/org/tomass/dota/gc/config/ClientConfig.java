package org.tomass.dota.gc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.tomass.dota.gc.clients.impl.DotaClientImpl;

@Configuration
public class ClientConfig {

    @Bean
    @Scope(value = "prototype")
    public DotaClientImpl client(SteamClientConfig clientConfig) {
        return new DotaClientImpl(clientConfig);
    }

}
