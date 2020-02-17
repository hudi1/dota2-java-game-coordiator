package org.tomass.dota.gc.handlers.callbacks.chat;

import java.util.Set;

import org.tomass.dota.gc.handlers.features.Dota2Chat.ChatChannel;

import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;

public class ChatMembersLeftUpdate extends CallbackMsg {

    private ChatChannel channel;
    private Set<Long> left;

    public ChatMembersLeftUpdate(ChatChannel channel, Set<Long> left) {
        this.channel = channel;
        this.left = left;
    }

    public ChatChannel getChannel() {
        return channel;
    }

    public Set<Long> getLeft() {
        return left;
    }

}
