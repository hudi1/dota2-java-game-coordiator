package org.tomass.dota.gc.util;

import java.util.UUID;

import org.tomass.dota.model.Serie;
import org.tomass.protobuf.dota.DotaGcmessagesClientMatchManagement.CMsgPracticeLobbySetDetails;
import org.tomass.protobuf.dota.DotaGcmessagesCommonMatchManagement.LobbyDotaTVDelay;
import org.tomass.protobuf.dota.DotaSharedEnums.DOTALobbyVisibility;
import org.tomass.protobuf.dota.DotaSharedEnums.DOTASelectionPriorityRules;
import org.tomass.protobuf.dota.DotaSharedEnums.DOTA_GameMode;

public class LobbyDetailsFactory {

    public static CMsgPracticeLobbySetDetails.Builder createSimpleLobby(String passKey, String name) {
        return createFullLobby(passKey, name, 0, 0, 0);
    }

    public static CMsgPracticeLobbySetDetails.Builder createFullLobby(String passKey, String name, Integer leagueId,
            Integer nodeId, Integer seriesType) {

        CMsgPracticeLobbySetDetails.Builder details = CMsgPracticeLobbySetDetails.newBuilder();
        details.setAllowCheats(false).setDotaTvDelay(LobbyDotaTVDelay.LobbyDotaTV_120)
                .setGameName(name == null ? UUID.randomUUID().toString() : name)
                .setVisibility(DOTALobbyVisibility.DOTALobbyVisibility_Public)
                .setServerRegion(ServerRegions.EUROPE.getNumber()).setPassKey(passKey).setAllowSpectating(true)
                .setGameMode(DOTA_GameMode.DOTA_GAMEMODE_CM_VALUE)
                .setSelectionPriorityRules(DOTASelectionPriorityRules.k_DOTASelectionPriorityRules_Automatic)
                .setSeriesType(seriesType).setLeagueid(leagueId).setLeagueNodeId(nodeId);
        return details;
    }

    public static CMsgPracticeLobbySetDetails.Builder createSeriesLobby(Serie serie) {
        return createFullLobby(serie.getPassword(), serie.getName(), serie.getLeagueId(), serie.getNodeId(),
                serie.getBestOf());
    }

    public static CMsgPracticeLobbySetDetails.Builder createLobby(Serie lobby) {
        return createSimpleLobby(lobby.getPassword(), lobby.getName());
    }

}
