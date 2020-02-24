package org.tomass.dota.steam.handlers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import in.dragonbra.javasteam.base.ClientMsgProtobuf;
import in.dragonbra.javasteam.base.IPacketMsg;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.handlers.ClientMsgHandler;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver.CMsgClientGamesPlayed;
import in.dragonbra.javasteam.util.compat.Consumer;

public class SteamUser extends ClientMsgHandler {

    private Map<EMsg, Consumer<IPacketMsg>> dispatchMap;

    private Set<Integer> currentGamesPlayed;

    public SteamUser() {
        dispatchMap = new HashMap<>();
        currentGamesPlayed = new HashSet<>();
    }

    @Override
    public void handleMsg(IPacketMsg packetMsg) {
        if (packetMsg == null) {
            throw new IllegalArgumentException("packetMsg is null");
        }

        Consumer<IPacketMsg> dispatcher = dispatchMap.get(packetMsg.getMsgType());
        if (dispatcher != null) {
            dispatcher.accept(packetMsg);
        }
    }

    public void gamePlayed(Integer appId) {
        ClientMsgProtobuf<CMsgClientGamesPlayed.Builder> playGame = new ClientMsgProtobuf<>(CMsgClientGamesPlayed.class,
                EMsg.ClientGamesPlayed);

        playGame.getBody().addGamesPlayedBuilder().setGameId(appId);
        client.send(playGame);
        currentGamesPlayed.add(appId);
    }

    public Set<Integer> getCurrentGamesPlayed() {
        return currentGamesPlayed;
    }

}
