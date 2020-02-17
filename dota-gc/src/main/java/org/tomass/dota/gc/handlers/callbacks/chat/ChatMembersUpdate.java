package org.tomass.dota.gc.handlers.callbacks.chat;

import java.util.Set;

import org.tomass.dota.gc.handlers.features.Dota2Chat.ChatChannel;

import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;

public class ChatMembersUpdate extends CallbackMsg {

    private ChatChannel channel;
    private Set<Long> join;
    private Set<Long> left;

    public ChatMembersUpdate(ChatChannel channel, Set<Long> join, Set<Long> left) {
        this.channel = channel;
        this.join = join;
        this.left = left;
    }

    public ChatChannel getChannel() {
        return channel;
    }

    public Set<Long> getJoin() {
        return join;
    }

    public Set<Long> getLeft() {
        return left;
    }

}
