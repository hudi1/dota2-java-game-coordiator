package org.tomass.dota.gc.util;

import java.util.UUID;

import org.tomass.protobuf.dota.DotaGcmessagesClientMatchManagement.CMsgPracticeLobbySetDetails;
import org.tomass.protobuf.dota.DotaGcmessagesCommonMatchManagement.LobbyDotaTVDelay;
import org.tomass.protobuf.dota.DotaSharedEnums.DOTALobbyVisibility;
import org.tomass.protobuf.dota.DotaSharedEnums.DOTASelectionPriorityRules;
import org.tomass.protobuf.dota.DotaSharedEnums.DOTA_GameMode;

public class LobbyDetailsFactory {

    public static CMsgPracticeLobbySetDetails.Builder createSimpleLobby(String passKey, String name) {
        return createLobby(passKey, name, null, null, null, null, null, null);
    }

    public static CMsgPracticeLobbySetDetails.Builder createLobbyWithPreviousMatch(String passKey, String name,
            long matchId, Integer radiantWins, Integer direWins) {
        return createLobby(passKey, name, null, null, null, matchId, radiantWins, direWins);
    }

    public static CMsgPracticeLobbySetDetails.Builder createLeagueNodeLobby(String passKey, String name,
            Integer leagueId, Integer nodeId, Integer seriesType) {
        return createLobby(passKey, name, leagueId, nodeId, seriesType, null, null, null);
    }

    public static CMsgPracticeLobbySetDetails.Builder createLobby(String passKey, String name, Integer leagueId,
            Integer nodeId, Integer seriesType, Long matchId, Integer radiantWins, Integer direWins) {
        CMsgPracticeLobbySetDetails.Builder details = CMsgPracticeLobbySetDetails.newBuilder();
        details.setAllowCheats(false).setDotaTvDelay(LobbyDotaTVDelay.LobbyDotaTV_120)
                .setGameName(name == null ? UUID.randomUUID().toString() : name)
                .setVisibility(DOTALobbyVisibility.DOTALobbyVisibility_Public)
                .setServerRegion(ServerRegions.EUROPE.getNumber()).setPassKey(passKey).setAllowSpectating(true)
                .setGameMode(DOTA_GameMode.DOTA_GAMEMODE_CM_VALUE)
                .setSelectionPriorityRules(DOTASelectionPriorityRules.k_DOTASelectionPriorityRules_Automatic);
        if (leagueId != null) {
            details.setLeagueid(leagueId);
        }
        if (nodeId != null) {
            details.setLeagueNodeId(nodeId);
        }
        if (seriesType != null) {
            details.setSeriesType(seriesType);
        }
        if (matchId != null) {
            details.setPreviousMatchOverride(matchId);
        }
        if (radiantWins != null) {
            details.setRadiantSeriesWins(radiantWins);
        }
        if (direWins != null) {
            details.setDireSeriesWins(direWins);
        }
        return details;
    }

}
