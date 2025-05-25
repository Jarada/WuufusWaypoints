package com.github.jarada.waypoints.listeners;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.github.jarada.waypoints.PluginMain;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import com.github.jarada.waypoints.SelectionManager;
import com.github.jarada.waypoints.Util;
import com.github.jarada.waypoints.WaypointManager;
import com.github.jarada.waypoints.data.DataManager;
import com.github.jarada.waypoints.data.Msg;
import com.github.jarada.waypoints.data.PlayerData;
import com.github.jarada.waypoints.data.Waypoint;
import com.github.jarada.waypoints.events.BeaconUseEvent;
import com.github.jarada.waypoints.events.WaypointInteractEvent;

public class PlayerListener implements Listener {

    private DataManager     dm;
    private WaypointManager wm;

    public PlayerListener() {
        dm = DataManager.getManager();
        wm = WaypointManager.getManager();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        Action a = event.getAction();
        ItemStack is = event.getItem();
        boolean mainHand = is != null && is.equals(p.getInventory().getItemInMainHand());

        if (!mainHand || a == Action.PHYSICAL || p.hasMetadata("InMenu") || p.hasMetadata("Wayporting"))
            return;

        // Calls WaypointInteractEvent if sneaking and clicked block is a
        // waypoint
        if (p.isSneaking() && (a == Action.RIGHT_CLICK_BLOCK || a == Action.LEFT_CLICK_BLOCK)) {
            Block clicked = event.getClickedBlock();
            Location toCheckAbove = clicked.getLocation().add(0, 1D, 0);
            Set<Waypoint> waypoints = new HashSet<Waypoint>(wm.getWaypoints().values());
            waypoints.addAll(wm.getPlayerData(p.getUniqueId()).getAllWaypoints());

            for (Waypoint wp : waypoints) {
                if (Util.isSameLoc(clicked.getLocation(), wp.getDynamicLocation(), true) ||
                        Util.isSameLoc(toCheckAbove, wp.getDynamicLocation(), true)) {
                    Bukkit.getPluginManager().callEvent(
                            new WaypointInteractEvent(p, wp, a, is, clicked.isBlockPowered()));
                    event.setCancelled(true);
                    return;
                }
            }
        }

        // Creates a new home waypoint if player is sneaking and right-clicking
        // with a renamed clock
        if (p.isSneaking() && p.hasPermission("wp.player")
                && (a == Action.RIGHT_CLICK_BLOCK || a == Action.RIGHT_CLICK_AIR)) {
            if (is.getType() == Material.CLOCK && is.hasItemMeta() && is.getItemMeta().hasDisplayName()) {
                if (!wm.setHome(p, Util.color(is.getItemMeta().getDisplayName())))
                    return;

                is.setAmount(is.getAmount() - 1);
                p.getInventory().setItemInMainHand(is);

                Location strikeLoc = p.getLocation();
                strikeLoc.setY(strikeLoc.getBlockY() + 2);
                p.getWorld().strikeLightningEffect(strikeLoc);
                event.setCancelled(true);
                return;
            }
        }

        // Calls BeaconUseEvent on click if player is not sneaking and item in
        // hand is a Waypoint Beacon
        if (!p.isSneaking() && dm.ENABLE_BEACON && is.isSimilar(dm.BEACON)) {
            Bukkit.getPluginManager().callEvent(new BeaconUseEvent(p, a));
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerPickup(EntityPickupItemEvent pickupItemEvent) {
        if (pickupItemEvent.getEntity() instanceof Player) {
            Player p = (Player)pickupItemEvent.getEntity();
            if (p.hasMetadata("Wayporting")) {
                pickupItemEvent.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent moveEvent) {
        Player p = moveEvent.getPlayer();
        
        if (p.hasMetadata("InMenu"))
            return;

        Location from = moveEvent.getFrom();
        Location to = moveEvent.getTo();

        if (Util.isSameLoc(from, to, true))
            return;

        if (p.hasMetadata("Wayporting")) {
            moveEvent.setCancelled(true);
            return;
        }

        PlayerData pd = wm.getPlayerData(p.getUniqueId());
        boolean silenceWaypoints = !DataManager.getManager().SHOW_MENU_ON_WALK ||
                (pd.isSilentWaypoints() && p.getInventory().contains(dm.BEACON));

        // Spawn
        if (Util.isSameLoc(p.getWorld().getSpawnLocation(), to, true)) {
            if (!silenceWaypoints && (!dm.MENU_AT_SPAWN_REQUIRES_ACCESS || p.hasPermission("wp.access.spawn"))) {
                Waypoint spawn = new Waypoint(Msg.WORD_SPAWN.toString(), p.getWorld().getSpawnLocation());
                spawn.setIcon(Material.NETHER_STAR);
                wm.openWaypointMenu(p, spawn, true, true, false);
            }
            return;
        }

        // Global Waypoints
        for (Waypoint wp : wm.getWaypoints().values()) {
            if (Util.isSameLoc(wp.getDynamicLocation(), to, true) && (wp.isEnabled() || p.hasPermission("wp.bypass"))) {
                boolean discovered = false;
                if (wp.isDiscoverable() != null && !pd.hasDiscovered(wp.getUUID())) {
                    pd.addDiscovery(wp.getUUID());
                    dm.savePlayerData(p.getUniqueId());
                    Msg.DISCOVERED_WAYPOINT.sendTo(p, wp.getName());
                    discovered = true;
                }

                if (Util.hasAccess(p, wp, false)) {
                    if (!silenceWaypoints || discovered)
                        wm.openWaypointMenu(p, wp, true, true, false);
                    return;
                }
            }
        }

        // Check if Waypoints are ignored
        if (silenceWaypoints)
            return;

        // Player Waypoints
        for (Waypoint wp : pd.getAllWaypoints()) {
            if (Util.isSameLoc(wp.getDynamicLocation(), to, true)) {
                wm.openWaypointMenu(p, wp, false, true, false);
                return;
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent joinEvent) {
        Player p = joinEvent.getPlayer();
        PlayerData pd = dm.loadPlayerData(p.getUniqueId());

        // (v1.1.0) Note: For transition; remove later
        for (Waypoint wp : pd.getAllWaypoints())
            if (!wp.isEnabled())
                wp.setEnabled(true);

        // Cleans discoveries of deleted waypoints
        List<UUID> waypoints = new ArrayList<UUID>();

        for (Waypoint wp : wm.getWaypoints().values())
            waypoints.add(wp.getUUID());

        if (pd.retainDiscoveries(waypoints))
            dm.savePlayerData(p.getUniqueId());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent quitEvent) {
        SelectionManager.getManager().clearSelectedWaypoint(quitEvent.getPlayer());
        dm.unloadPlayerData(quitEvent.getPlayer().getUniqueId());
    }

}
