package org.tomass.dota.gc.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

public class RestAuthenticationProvider extends DaoAuthenticationProvider {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        try {
            return super.authenticate(authentication);
        } catch (BadCredentialsException e) {
            logger.error("Uživatel zadal špatné přihlasovací údaje");
            throw e;
        }
    }

}
