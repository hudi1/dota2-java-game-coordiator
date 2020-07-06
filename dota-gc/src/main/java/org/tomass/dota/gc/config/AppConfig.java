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

    private Map<String, String> usersPass = new ConcurrentHashMap<>();
    private Map<String, String> usersRole = new ConcurrentHashMap<>();

    private Map<String, SteamClientConfig> clients;
    private Map<String, String> chatCommands;
    private Map<Integer, String> heroes;
    private Map<Integer, String> notableplayers;
    private List<Long> steamIdAdmins;

    public long refreshDuration;

    public AppConfig() {
        clients = new ConcurrentHashMap<String, SteamClientConfig>();
        setChatCommands(new ConcurrentHashMap<String, String>());
        steamIdAdmins = new ArrayList<>();
    }

    public Map<String, String> getUsersPass() {
        return usersPass;
    }

    public void setUsersPass(Map<String, String> usersPass) {
        this.usersPass = usersPass;
    }

    public Map<String, String> getUsersRole() {
        return usersRole;
    }

    public void setUsersRole(Map<String, String> usersRole) {
        this.usersRole = usersRole;
    }

    public void addUser(String user, String password) {
        Assert.notNull(user, "User is required");
        Assert.notNull(password, "Password is required");
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String hashedPassword = passwordEncoder.encode(password);
        usersPass.put(user, hashedPassword);
    }

    public void removeUser(String uzivatel) {
        Assert.notNull(uzivatel, "User is required");
        usersPass.remove(uzivatel);
        usersRole.remove(uzivatel);
    }

    public void addRoles(String uzivatel, String... role) {
        Assert.notNull(uzivatel, "User is required");
        String[] _role = usersRole.containsKey(uzivatel) ? usersRole.get(uzivatel).split(",") : new String[] {};
        Set<String> roleSet = new HashSet<>();
        roleSet.addAll(Arrays.asList(_role));
        roleSet.addAll(Arrays.asList(role));
        usersRole.put(uzivatel, roleSet.stream().collect(Collectors.joining(",")));
    }

    public void removeRoles(String uzivatel, String... role) {
        Assert.notNull(uzivatel, "User is required");
        String[] _role = usersRole.containsKey(uzivatel) ? usersRole.get(uzivatel).split(",") : new String[] {};
        Set<String> roleSet = new HashSet<>();
        roleSet.addAll(Arrays.asList(_role));
        roleSet.removeAll(Arrays.asList(role));
        usersRole.put(uzivatel, roleSet.stream().collect(Collectors.joining(",")));
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

    public Map<String, String> getChatCommands() {
        return chatCommands;
    }

    public void setChatCommands(Map<String, String> chatCommands) {
        this.chatCommands = chatCommands;
    }

    @Override
    public String toString() {
        return "AppConfig [usersPass=" + usersPass + ", usersRole=" + usersRole + ", clients=" + clients
                + ", chatCommands=" + chatCommands + ", heroes=" + heroes + ", notableplayers=" + notableplayers
                + ", steamIdAdmins=" + steamIdAdmins + ", refreshDuration=" + refreshDuration + "]";
    }

}
