package org.tomass.dota.gc.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class SteamClientLogger {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private Marker marker = null;

    public SteamClientLogger(String client, boolean useMarker) {
        if (useMarker) {
            this.marker = MarkerFactory.getMarker(client);
        }
    }

    public void trace(String msg) {
        logger.trace(marker, msg);
    }

    public void debug(String msg) {
        logger.debug(marker, msg);
    }

    public void info(String msg) {
        logger.info(marker, msg);
    }

    public void warn(String msg) {
        logger.warn(marker, msg);
    }

    public void warn(String msg, Throwable e) {
        logger.warn(marker, msg, e);
    }

    public void error(String msg) {
        logger.error(marker, msg);
    }

    public void error(String msg, Throwable e) {
        logger.error(marker, msg, e);
    }

}
