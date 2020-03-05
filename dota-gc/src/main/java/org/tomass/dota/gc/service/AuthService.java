package org.tomass.dota.gc.service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.tomass.dota.gc.config.AppConfig;

@Component
public class AuthService implements UserDetailsService {

    private Logger log = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private AppConfig config;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            if (StringUtils.isEmpty(username))
                throw new UsernameNotFoundException("User is empty");

            String heslo = config.getUsersPass().get(username);
            if (StringUtils.isEmpty(heslo))
                throw new UsernameNotFoundException("Password is empty");

            String[] _role = config.getUsersRole().containsKey(username)
                    ? config.getUsersRole().get(username).split(",")
                    : new String[] {};
            List<SimpleGrantedAuthority> role = Arrays.asList(_role).stream()
                    .map(r -> new SimpleGrantedAuthority("ROLE_" + r)).collect(Collectors.toList());
            log.debug("User " + username + " got role '" + role + "'");

            return new User(username, heslo, role);
        } catch (UsernameNotFoundException e) {
            log.error("User " + username + " not found");
            throw e;
        }
    }

}
