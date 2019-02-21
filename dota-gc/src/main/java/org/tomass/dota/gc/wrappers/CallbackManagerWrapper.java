package org.tomass.dota.gc.wrappers;

import java.io.Closeable;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackManager;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.ICallbackMsg;
import in.dragonbra.javasteam.util.compat.Consumer;

@Component
public class CallbackManagerWrapper {

    private final Logger log = LoggerFactory.getLogger(CallbackManagerWrapper.class);

    @Autowired
    private SteamClientWrapper steamClientWrapper;

    private CallbackManager callbackManager;

    private boolean isRunning;

    @PostConstruct
    public void init() {
        callbackManager = new CallbackManager(steamClientWrapper.getSteamClient());
    }

    @Async
    public void callCallbacks() {
        while (isRunning) {
            callbackManager.runWaitCallbacks(1000L);
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }

    public <TCallback extends ICallbackMsg> Closeable subscribe(Class<? extends TCallback> callbackType,
            Consumer<TCallback> callbackFunc) {
        return callbackManager.subscribe(callbackType, callbackFunc);
    }

}
