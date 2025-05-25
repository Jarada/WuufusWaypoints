package com.github.jarada.waypoints.data;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import org.bukkit.Location;

import com.github.jarada.waypoints.util.Util;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public class PlayerData implements Serializable {

    private final UUID        playerUUID;
    private List<Waypoint>    homeWaypoints;
    private List<UUID>        discovered;
    private GridLocation      spawnPoint;
    private boolean           silentWaypoints;

    public PlayerData(UUID playerUUID) {
        this.playerUUID = playerUUID;
        homeWaypoints = new ArrayList<Waypoint>();
        discovered = new ArrayList<UUID>();
    }

    public PlayerData(YamlConfiguration config, String prefix) {
        playerUUID = Serializer.getUUID(config, prefix, "uuid");

        homeWaypoints = new ArrayList<Waypoint>();
        if (Serializer.getConfigurationSection(config, prefix, "waypoints") != null) {
            for (String key : Serializer.getConfigurationSection(config, prefix, "waypoints").getKeys(false)) {
                homeWaypoints.add(new Waypoint(config, Serializer.setupPrefix(prefix) + "waypoints." + key));
            }
        }

        discovered = new ArrayList<UUID>();
        if (Serializer.isList(config, prefix, "discovered")) {
            for (Object obj : Serializer.getList(config, prefix, "discovered")) {
                discovered.add(UUID.fromString((String) obj));
            }
        }

        ConfigurationSection section = Serializer.getConfigurationSection(config, prefix, null);
        if (section != null) {
            if (section.getKeys(false).contains("spawn")) {
                spawnPoint = new GridLocation(config, Serializer.setupPrefix(prefix) + "spawn");
            }
        }

        silentWaypoints = Serializer.getBoolean(config, prefix, "silentWaypoints");
    }

    public void serialize(YamlConfiguration config, String prefix) {
        Serializer.set(config, prefix, "uuid", playerUUID.toString());
        for (Waypoint wp : homeWaypoints) {
            wp.serialize(config, Serializer.setupPrefix(prefix) + "waypoints." + Util.getKey(wp.getName()));
        }
        Serializer.set(config, prefix, "discovered", discovered.stream().map(UUID::toString).collect(Collectors.toList()));
        if (spawnPoint != null) {
            spawnPoint.serialize(config, Serializer.setupPrefix(prefix) + "spawn");
        }
        Serializer.set(config, prefix, "silentWaypoints", silentWaypoints);
    }

    public UUID getUUID() {
        return playerUUID;
    }

    public boolean hasDiscovered(UUID uuid) {
        return discovered.contains(uuid);
    }

    public void addDiscovery(UUID uuid) {
        discovered.add(uuid);
    }

    public void removeDiscovery(UUID uuid) {
        discovered.remove(uuid);
    }

    public boolean retainDiscoveries(List<UUID> discovered) {
        return this.discovered.retainAll(discovered);
    }

    public Waypoint getWaypoint(String name) {
        String key = Util.getKey(name);

        for (Waypoint wp : homeWaypoints)
            if (Util.getKey(wp.getName()).equals(key))
                return wp;

        return null;
    }

    public Waypoint addWaypoint(Waypoint wp) {
        int maxHomes = DataManager.getManager().MAX_HOME_WAYPOINTS;
        homeWaypoints.add(wp);

        // Leave one to show waypoints are being deleted
        if (homeWaypoints.size() > maxHomes + 1)
            homeWaypoints.retainAll(homeWaypoints.subList(homeWaypoints.size() - maxHomes - 1, homeWaypoints.size()));

        return homeWaypoints.size() > maxHomes ? homeWaypoints.remove(0) : null;
    }

    public void removeWaypoint(Waypoint wp) {
        homeWaypoints.remove(wp);
    }

    public List<Waypoint> getAllWaypoints() {
        return homeWaypoints;
    }

    public Location getSpawnPoint() {
        return spawnPoint != null ? spawnPoint.getLocation() : null;
    }

    public void setSpawnPoint(Location loc) {
        spawnPoint = loc != null ? new GridLocation(loc) : null;
    }

    public boolean isSilentWaypoints() {
        return silentWaypoints;
    }

    public void setSilentWaypoints(boolean silentWaypoints) {
        this.silentWaypoints = silentWaypoints;
    }
}
