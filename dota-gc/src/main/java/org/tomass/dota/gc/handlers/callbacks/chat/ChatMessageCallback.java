package org.tomass.dota.gc.handlers.callbacks.chat;

import org.tomass.dota.gc.handlers.Dota2Chat.ChatChannel;
import org.tomass.protobuf.dota.DotaGcmessagesClientChat.CMsgDOTAChatMessage.Builder;

import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;

public class ChatMessageCallback extends CallbackMsg {

    private Builder builder;
    private ChatChannel chatChannel;

    public ChatMessageCallback(Builder builder, ChatChannel chatChannel) {
        this.builder = builder;
        this.chatChannel = chatChannel;
    }

    public Builder getBuilder() {
        return builder;
    }

    public ChatChannel getChatChannel() {
        return chatChannel;
    }

}
