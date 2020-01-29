package org.tomass.dota.steam.handlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import in.dragonbra.javasteam.base.ClientMsgProtobuf;
import in.dragonbra.javasteam.base.IPacketMsg;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.handlers.ClientMsgHandler;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver.CMsgClientGamesPlayed;
import in.dragonbra.javasteam.util.compat.Consumer;

public class SteamUser extends ClientMsgHandler {

    private Map<EMsg, Consumer<IPacketMsg>> dispatchMap;

    private List<Integer> currentGamesPlayed;

    public SteamUser() {
        dispatchMap = new HashMap<>();
        currentGamesPlayed = new ArrayList<>();
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

    public void gamesPlayed(List<Integer> appIds) {
        currentGamesPlayed.addAll(appIds);
        ClientMsgProtobuf<CMsgClientGamesPlayed.Builder> playGame = new ClientMsgProtobuf<>(CMsgClientGamesPlayed.class,
                EMsg.ClientGamesPlayed);

        for (Integer appId : appIds) {
            playGame.getBody().addGamesPlayedBuilder().setGameId(appId);
        }
        client.send(playGame);
    }

    public List<Integer> getCurrentGamesPlayed() {
        return currentGamesPlayed;
    }

}
