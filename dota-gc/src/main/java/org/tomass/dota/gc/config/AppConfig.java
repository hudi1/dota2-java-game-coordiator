package org.tomass.dota.gc.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.Assert;

public class AppConfig {

    private Map<String, String> uzivateleHesla = new ConcurrentHashMap<>();
    private Map<String, String> uzivateleRole = new ConcurrentHashMap<>();

    private Map<String, SteamClientConfig> clients;
    private Map<Integer, String> heroes;
    private Map<Integer, String> notableplayers;
    private List<Long> steamIdAdmins;
    private String steamWebApi;

    public long refreshDuration;

    public AppConfig() {
        clients = new ConcurrentHashMap<String, SteamClientConfig>();
        steamIdAdmins = new ArrayList<>();
    }

    public Map<String, String> getUzivateleHesla() {
        return uzivateleHesla;
    }

    public void setUzivateleHesla(Map<String, String> uzivateleHesla) {
        this.uzivateleHesla = uzivateleHesla;
    }

    public Map<String, String> getUzivateleRole() {
        return uzivateleRole;
    }

    public void setUzivateleRole(Map<String, String> uzivateleRole) {
        this.uzivateleRole = uzivateleRole;
    }

    public void pridejUzivatel(String uzivatel, String heslo) {
        Assert.notNull(uzivatel, "uzivatel je pozadovan");
        Assert.notNull(heslo, "heslo je pozadovano");
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String hashedPassword = passwordEncoder.encode(heslo);
        uzivateleHesla.put(uzivatel, hashedPassword);
    }

    public void vymazUzivatel(String uzivatel) {
        Assert.notNull(uzivatel, "uzivatel je pozadovan");
        uzivateleHesla.remove(uzivatel);
        uzivateleRole.remove(uzivatel);
    }

    public void pridejUzivatelRole(String uzivatel, String... role) {
        Assert.notNull(uzivatel, "uzivatel je pozadovan");
        String[] _role = uzivateleRole.containsKey(uzivatel) ? uzivateleRole.get(uzivatel).split(",") : new String[] {};
        Set<String> roleSet = new HashSet<>();
        roleSet.addAll(Arrays.asList(_role));
        roleSet.addAll(Arrays.asList(role));
        uzivateleRole.put(uzivatel, roleSet.stream().collect(Collectors.joining(",")));
    }

    public void vymazUzivatelRole(String uzivatel, String... role) {
        Assert.notNull(uzivatel, "uzivatel je pozadovan");
        String[] _role = uzivateleRole.containsKey(uzivatel) ? uzivateleRole.get(uzivatel).split(",") : new String[] {};
        Set<String> roleSet = new HashSet<>();
        roleSet.addAll(Arrays.asList(_role));
        roleSet.removeAll(Arrays.asList(role));
        uzivateleRole.put(uzivatel, roleSet.stream().collect(Collectors.joining(",")));
    }

    public Map<String, SteamClientConfig> getClients() {
        return clients;
    }

    public void setClients(Map<String, SteamClientConfig> clients) {
        this.clients = clients;
    }

    public List<Long> getSteamIdAdmins() {
        return steamIdAdmins;
    }

    public void setSteamIdAdmins(List<Long> steamIdAdmins) {
        this.steamIdAdmins = steamIdAdmins;
    }

    public long getRefreshDuration() {
        return refreshDuration;
    }

    public void setRefreshDuration(long refreshDuration) {
        this.refreshDuration = refreshDuration;
    }

    public String getSteamWebApi() {
        return steamWebApi;
    }

    public void setSteamWebApi(String steamWebApi) {
        this.steamWebApi = steamWebApi;
    }

    public Map<Integer, String> getHeroes() {
        return heroes;
    }

    public void setHeroes(Map<Integer, String> heroes) {
        this.heroes = heroes;
    }

    public Map<Integer, String> getNotableplayers() {
        return notableplayers;
    }

    public void setNotableplayers(Map<Integer, String> notableplayers) {
        this.notableplayers = notableplayers;
    }

}
