package org.tomass.dota.gc.handlers.callbacks.chat;

import org.tomass.dota.gc.handlers.features.Dota2Chat.ChatChannel;

import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;

public class ChatMembersJoinUpdateCallback extends CallbackMsg {

    private ChatChannel channel;
    private Long playerSteamId;

    public ChatMembersJoinUpdateCallback(ChatChannel channel, Long playerSteamId) {
        this.channel = channel;
        this.playerSteamId = playerSteamId;
    }

    public ChatChannel getChannel() {
        return channel;
    }

    public Long getPlayerSteamId() {
        return playerSteamId;
    }

}
