package org.tomass.dota.gc.handlers.callbacks.chat;

import org.tomass.dota.gc.handlers.Dota2Chat.ChatChannel;

import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;

public class ChatChannelLeft extends CallbackMsg {

    private ChatChannel channel;

    public ChatChannelLeft(ChatChannel channel) {
        this.channel = channel;
    }

    public ChatChannel getChannel() {
        return channel;
    }

}
