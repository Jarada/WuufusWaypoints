package com.github.jarada.waypoints.data;

import java.io.Serializable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;

public class GridLocation implements Serializable {

    private String            worldName;
    private int               x, y, z;
    private float             pitch, yaw;

    public GridLocation(Location loc) {
        setLocation(loc);
    }

    public GridLocation(YamlConfiguration config, String prefix) {
        worldName = Serializer.getString(config, prefix, "world");
        x = Serializer.getInt(config, prefix, "x");
        y = Serializer.getInt(config, prefix, "y");
        z = Serializer.getInt(config, prefix, "z");
        pitch = Serializer.getFloat(config, prefix, "pitch");
        yaw = Serializer.getFloat(config, prefix, "yaw");
    }

    public void serialize(YamlConfiguration config, String prefix) {
        Serializer.set(config, prefix, "world", worldName);
        Serializer.set(config, prefix, "x", x);
        Serializer.set(config, prefix, "y", y);
        Serializer.set(config, prefix, "z", z);
        Serializer.set(config, prefix, "pitch", pitch);
        Serializer.set(config, prefix, "yaw", yaw);
    }

    public Location getLocation() {
        return new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
    }

    public Location getHighestLocation() {
        World world = Bukkit.getWorld(worldName);
        if (world == null) return null;
        Block block = world.getHighestBlockAt(x, z);
        return new Location(world, block.getX(), block.getY() + 1, block.getZ(), yaw, pitch);
    }

    public void setLocation(Location loc) {
        worldName = loc.getWorld().getName();
        x = loc.getBlockX();
        y = loc.getBlockY();
        z = loc.getBlockZ();
        pitch = loc.getPitch();
        yaw = loc.getYaw();
    }

}
