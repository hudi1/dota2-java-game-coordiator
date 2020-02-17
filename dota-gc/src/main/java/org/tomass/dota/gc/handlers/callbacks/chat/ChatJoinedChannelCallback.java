package org.tomass.dota.gc.handlers.callbacks.chat;

import org.tomass.dota.gc.handlers.features.Dota2Chat.ChatChannel;

import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;

public class ChatJoinedChannelCallback extends CallbackMsg {

    private ChatChannel channel;

    public ChatJoinedChannelCallback(ChatChannel channel) {
        this.channel = channel;
    }

    public ChatChannel getChannel() {
        return channel;
    }

}
