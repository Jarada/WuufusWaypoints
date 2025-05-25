package com.github.jarada.waypoints.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.jarada.waypoints.WaypointManager;
import com.github.jarada.waypoints.data.Waypoint;
import org.apache.commons.text.WordUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Util {

    public static String buildString(String[] args, int start, char filler) {
        StringBuilder sb = new StringBuilder();

        for (int i = start; i < args.length; i++) {
            sb.append(args[i]);

            if (i < args.length - 1)
                sb.append(filler);
        }

        return sb.toString();
    }

    public static void checkChunkLoad(final Block b) {
        final World w = b.getWorld();
        final Chunk c = b.getChunk();

        if (!w.isChunkLoaded(c)) {
            w.getChunkAt(b).load();
        }
    }

    public static String color(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public static String getKey(String string) {
        return ChatColor.stripColor(string.toLowerCase())
                .replace(" ", "_")
                .replace(".", "");
    }

    public static Location getSafeLocation(Location location) {
        if (isSafeLocation(location))
            return teleportLocation(location);

        // Nope, find closest block
        try {
            Block feet = location.getBlock();
            for (BlockFace face : new ArrayList<>(Arrays.asList(BlockFace.UP, BlockFace.NORTH, BlockFace.NORTH_EAST,
                    BlockFace.EAST, BlockFace.SOUTH_EAST, BlockFace.SOUTH, BlockFace.SOUTH_WEST, BlockFace.WEST,
                    BlockFace.NORTH_WEST))) {
                Block adjusted = feet.getRelative(face);
                if (isSafeLocation(adjusted.getLocation())) {
                    Location adjustedLocation = adjusted.getLocation();
                    adjustedLocation.setPitch(location.getPitch());
                    adjustedLocation.setYaw(location.getYaw());
                    return teleportLocation(adjustedLocation);
                }
            }
        } catch (Exception ignored) {}

        // Nope, now this is obstructed or unavailable
        return null;
    }

    public static String[] getWrappedLore(String description, int maxLineLength) {
        return WordUtils.wrap(description, maxLineLength, "\n", true).split("\\n");
    }

    public static boolean hasAccess(Player p, Waypoint wp, boolean select) {
        if (p.hasPermission("wp.access.*") || p.hasPermission("wp.access." + getKey(wp.getName())))
            return true;

        if (wp.isDiscoverable() != null
                && WaypointManager.getManager().getPlayerData(p.getUniqueId()).hasDiscovered(wp.getUUID()))
            return wp.isDiscoverable() || (select || p.getWorld().getName()
                    .equals(wp.getDynamicLocation().getWorld().getName()));

        return false;
    }

    public static boolean canDiscover(Player p, Waypoint wp) {
        return wp.isDiscoverable() != null && (wp.isDiscoverable() || p.getWorld().getName()
                .equals(wp.getDynamicLocation().getWorld().getName())) &&
                !WaypointManager.getManager().getPlayerData(p.getUniqueId()).hasDiscovered(wp.getUUID());
    }

    public static boolean isSafeLocation(Location location) {
        try {
            Block feet = location.getBlock();
            if (feet.getType().isSolid()) {
                return false; // not transparent (will suffocate)
            }
            Block head = feet.getRelative(BlockFace.UP);
            if (head.getType().isSolid()) {
                return false; // not transparent (will suffocate)
            }
            Block ground = feet.getRelative(BlockFace.DOWN);
            // returns if the ground is solid or not.
            return ground.getType().isSolid();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isSameLoc(Location a, Location b, boolean useGrid) {
        return useGrid ? (a.getBlockX() == b.getBlockX() && a.getBlockY() == b.getBlockY() && a.getBlockZ() == b
                .getBlockZ()) : (a.getX() == b.getX() && a.getY() == b.getY() && a.getZ() == b.getZ());
    }

    public static void playEffect(Location loc, Effect effect) {
        loc.getWorld().playEffect(loc, effect, 0);
    }

    public static void playParticle(Location loc, Particle particle, int count) {
        loc.getWorld().spawnParticle(particle, loc, count);
    }

    public static void playSound(Location loc, Sound sound) {
        loc.getWorld().playSound(loc, sound, 10F, 1F);
    }

    public static ItemStack setItemNameAndLore(ItemStack item, String name, List<String> lore) {
        ItemMeta im = item.getItemMeta();
        im.setDisplayName(color(name));

        if (lore != null)
            im.setLore(lore);

        item.setItemMeta(im);
        return item;
    }

    public static Location teleportLocation(Location location) {
        return location.add(0.5, 0, 0.5);
    }

}
