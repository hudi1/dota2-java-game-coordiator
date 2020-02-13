package org.tomass.dota.gc.handlers.callbacks;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgClientRichPresenceInfo.Builder;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgClientRichPresenceInfo.RichPresence;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.types.JobID;
import in.dragonbra.javasteam.types.SteamID;

public class ClientRichPresenceInfoCallback extends CallbackMsg {

    private Long watchableGameID;
    private SteamID watchingServer;
    private Long steamId;
    private String all;

    public ClientRichPresenceInfoCallback(JobID jobID, Builder body) {
        setJobID(jobID);
        parse(body);
    }

    public void parse(Builder body) {
        StringBuilder all = new StringBuilder();
        for (RichPresence presence : body.getRichPresenceList()) {
            if (presence.hasRichPresenceKv()) {
                setSteamId(presence.getSteamidUser());
                byte[] data = presence.getRichPresenceKv().toByteArray();
                for (int i = 1; i < data.length; i++) {
                    String value = (char) data[i] + "";
                    if (value.trim().length() > 0) {
                        all.append(value);
                    } else {
                        all.append(" ");
                    }
                }
            }
        }

        Map<String, String> kvMap = Arrays.asList(all.toString().split("  ")).stream().map(s -> s.trim().split(" "))
                .collect(Collectors.toMap(a -> a[0], a -> a.length > 1 ? a[1] : ""));
        if (kvMap.get("WatchableGameID") != null) {
            watchableGameID = Long.parseLong(kvMap.get("WatchableGameID"));
        }

        if (kvMap.get("watching_server") != null) {
            watchingServer = new SteamID();
            watchingServer.setFromSteam3String(kvMap.get("watching_server"));
        }
        this.all = all.toString();
    }

    public Long getWatchableGameID() {
        return watchableGameID;
    }

    public void setWatchableGameID(Long watchableGameID) {
        this.watchableGameID = watchableGameID;
    }

    public SteamID getWatchingServer() {
        return watchingServer;
    }

    public void setWatchingServer(SteamID watchingServer) {
        this.watchingServer = watchingServer;
    }

    public Long getSteamId() {
        return steamId;
    }

    public void setSteamId(Long steamId) {
        this.steamId = steamId;
    }

    public String getAll() {
        return all;
    }

    public void setAll(String all) {
        this.all = all;
    }

    @Override
    public String toString() {
        return "ClientRichPresenceInfoCallback [watchableGameID=" + watchableGameID + ", watchingServer="
                + watchingServer + ", steamId=" + steamId + ", all=" + all + "]";
    }

}
