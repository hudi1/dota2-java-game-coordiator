package org.tomass.dota.gc.handlers.callbacks.shared;

import com.google.protobuf.ByteString;

import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;

public class SingleObjectRemovedLobby extends CallbackMsg {

    private int typeId;
    private ByteString data;

    public SingleObjectRemovedLobby(int typeId, ByteString data) {
        this.typeId = typeId;
        this.data = data;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public ByteString getData() {
        return data;
    }

    public void setData(ByteString data) {
        this.data = data;
    }

}
