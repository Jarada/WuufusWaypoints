package com.github.jarada.waypoints.listeners;

import com.github.jarada.waypoints.SelectionManager;
import com.github.jarada.waypoints.WaypointManager;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import com.github.jarada.waypoints.util.Util;
import com.github.jarada.waypoints.data.DataManager;
import com.github.jarada.waypoints.data.Msg;
import com.github.jarada.waypoints.data.PlayerData;
import com.github.jarada.waypoints.data.Waypoint;
import com.github.jarada.waypoints.events.WaypointInteractEvent;

public class WaypointListener implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onWaypointInteract(WaypointInteractEvent event) {
        if ((event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_BLOCK) ||
                event.getItem() == null)
            return;

        DataManager dm = DataManager.getManager();
        WaypointManager wm = WaypointManager.getManager();
        Player p = event.getPlayer();
        Waypoint wp = event.getWaypoint();
        ItemStack is = event.getItem();

        if (event.getAction() == Action.LEFT_CLICK_BLOCK && p.hasPermission("wp.player") &&
                !event.isServerDefined() && is.getType() == Material.COMPASS && !is.hasItemMeta()) {
            if (wm.getWaypoint(wp.getName()) != null) {
                wm.removeWaypoint(wp);
                DataManager.getManager().saveWaypoints();
            } else {
                PlayerData pd = event.getPlayerData();
                pd.removeWaypoint(wp);
                DataManager.getManager().savePlayerData(pd.getUUID());
            }

            SelectionManager.getManager().clearSelectionsWith(wp);
            Util.playSound(wp.getDynamicLocation(), Sound.BLOCK_GLASS_BREAK);
            Util.playEffect(wp.getDynamicLocation(), Effect.ENDER_SIGNAL);

            is.setAmount(is.getAmount() - 1);
            p.getInventory().setItemInMainHand(is);
            Msg.WP_REMOVED.sendTo(p, wp.getName());
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        if (dm.ENABLE_BEACON && dm.HANDLE_RESPAWNING && p.hasPermission("wp.respawn") && is.isSimilar(dm.BEACON)) {
            if (event.isPowered()) {
                PlayerData pd = event.getPlayerData();
                pd.setSpawnPoint(wp.getDynamicLocation());
                dm.savePlayerData(p.getUniqueId());

                if (!p.hasPermission("wp.beacon.unlimited")) {
                    is.setAmount(is.getAmount() - 1);
                    p.getInventory().setItemInMainHand(is);
                }

                Msg.SET_PLAYER_SPAWN.sendTo(p, wp.getName());
            } else {
                Msg.INSUFFICIENT_POWER.sendTo(p);
                return;
            }
        } else if (p.hasPermission("wp.player") && !event.isServerDefined()) {
            Material m = is.getType();

            if (m == Material.WRITTEN_BOOK) {
                BookMeta bm = (BookMeta) is.getItemMeta();

                if (bm.hasDisplayName() || bm.hasLore())
                    return;

                StringBuilder content = new StringBuilder();

                for (int page = 1; page <= bm.getPageCount(); page++) {
                    content.append(bm.getPage(page));

                    if (page != bm.getPageCount())
                        content.append(" ");
                }

                if (content.length() > dm.WP_DESC_MAX_LENGTH)
                    content = new StringBuilder(content.substring(0, dm.WP_DESC_MAX_LENGTH));

                p.closeInventory();
                wp.setDescription(content.toString());
                Msg.WP_DESC_UPDATED_BOOK.sendTo(p, wp.getName(), bm.getTitle());
            } else if (m == Material.COMPASS && is.hasItemMeta() && is.getItemMeta().hasDisplayName()) {
                String newWaypointName = Util.color(is.getItemMeta().getDisplayName());
                String oldName = wp.getName();

                if (!wm.renameWaypoint(wp, p, newWaypointName))
                    return;

                is.setAmount(is.getAmount() - 1);
                p.getInventory().setItemInMainHand(is);
                Msg.WP_RENAMED.sendTo(p, oldName, newWaypointName);
            } else {
                if (is.hasItemMeta())
                    return;

                wp.setIcon(m);

                int durability = 0;
                if (is instanceof Damageable) {
                    durability = ((org.bukkit.inventory.meta.Damageable)is.getItemMeta()).getDamage();
                }
                wp.setDurability((short) durability);
                Msg.WP_SETICON.sendTo(p, wp.getName(), m.toString(), durability);
            }

            is.setAmount(is.getAmount() - 1);
            p.getInventory().setItemInMainHand(is);
        } else
            return;

        Util.playSound(wp.getDynamicLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
        Util.playEffect(wp.getDynamicLocation(), Effect.ENDER_SIGNAL);
        dm.saveWaypoint(p, wp);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerMove(PlayerMoveEvent moveEvent) {
        if (!moveEvent.getPlayer().hasMetadata("Wayporting"))
            return;

        if (!Util.isSameLoc(moveEvent.getFrom(), moveEvent.getTo(), false)) {
            Location from = moveEvent.getFrom();

            from.setX(from.getBlockX());
            from.setY(from.getBlockY());
            from.setZ(from.getBlockZ());

            moveEvent.getPlayer().teleport(from, TeleportCause.PLUGIN);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerCommand(PlayerCommandPreprocessEvent cmdEvent) {
        if (cmdEvent.getPlayer().hasMetadata("Wayporting")) {
            Msg.COMMAND_CANCEL.sendTo(cmdEvent.getPlayer());
            cmdEvent.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getPlayer().hasMetadata("Wayporting"))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemDrop(PlayerDropItemEvent dropEvent) {
        if (dropEvent.getPlayer().hasMetadata("Wayporting"))
            dropEvent.setCancelled(true);
    }

}
