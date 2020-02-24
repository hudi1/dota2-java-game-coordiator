package org.tomass.dota.gc.handlers.callbacks;

import org.tomass.protobuf.dota.DotaGcmessagesClient.CMsgDOTAPopup.Builder;

import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.types.JobID;

public class PopupCallback extends CallbackMsg {

    private Builder body;

    public PopupCallback(JobID jobID, Builder body) {
        setJobID(jobID);
        this.body = body;
    }

    public Builder getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "PopupCallback [body=" + body + "]";
    }

}
