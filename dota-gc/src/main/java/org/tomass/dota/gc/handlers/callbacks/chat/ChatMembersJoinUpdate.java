package org.tomass.dota.gc.handlers.callbacks.chat;

import java.util.Set;

import org.tomass.dota.gc.handlers.Dota2Chat.ChatChannel;

import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;

public class ChatMembersJoinUpdate extends CallbackMsg {

    private ChatChannel channel;
    private Set<Long> join;

    public ChatMembersJoinUpdate(ChatChannel channel, Set<Long> join) {
        this.channel = channel;
        this.join = join;
    }

    public ChatChannel getChannel() {
        return channel;
    }

    public Set<Long> getJoin() {
        return join;
    }

}
