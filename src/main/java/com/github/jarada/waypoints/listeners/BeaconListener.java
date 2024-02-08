package com.github.jarada.waypoints.listeners;

import java.util.Iterator;

import com.github.jarada.waypoints.data.BeaconConsumptionMode;
import org.bukkit.GameMode;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.github.jarada.waypoints.WaypointManager;
import com.github.jarada.waypoints.data.DataManager;
import com.github.jarada.waypoints.events.BeaconUseEvent;

public class BeaconListener implements Listener {

    private static BeaconListener listener;

    public static BeaconListener getListener() {
        if (listener == null)
            listener = new BeaconListener();

        return listener;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBeaconUse(BeaconUseEvent useEvent) {
        Player p = useEvent.getPlayer();
        Action a = useEvent.getAction();

        if (p.hasPermission("wp.beacon.use") && (a == Action.RIGHT_CLICK_BLOCK || a == Action.RIGHT_CLICK_AIR)) {
            if (DataManager.getManager().BEACON_CONSUMPTION_MODE == BeaconConsumptionMode.MENU &&
                    !p.hasPermission("wp.beacon.unlimited")) {
                ItemStack is = p.getInventory().getItemInMainHand();
                is.setAmount(is.getAmount() - 1);
                p.getInventory().setItemInMainHand(is);
            }

            WaypointManager.getManager().openWaypointMenu(p, null, p.hasPermission("wp.beacon.server"), true, false);
        } else if (p.hasPermission("wp.select.beacon") && (a == Action.LEFT_CLICK_BLOCK || a == Action.LEFT_CLICK_AIR)) {
            WaypointManager.getManager().openWaypointSelectionMenu(p);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onInventoryInteract(InventoryClickEvent clickEvent) {
        DataManager dm = DataManager.getManager();
        InventoryType type = clickEvent.getInventory().getType();
        InventoryAction a = clickEvent.getAction();
        HumanEntity p = clickEvent.getWhoClicked();

        if (dm.BEACON_UNLIMITED_PERMANENT && clickEvent.getWhoClicked().hasPermission("wp.beacon.unlimited")) {
            if (!(type == InventoryType.PLAYER || type == InventoryType.CREATIVE || type == InventoryType.CRAFTING) ||
                    isBeaconImmovable(p, type)) {
                if (clickEvent.getCurrentItem() != null && clickEvent.getCurrentItem().isSimilar(dm.BEACON))
                    clickEvent.setCancelled(true);

                if (clickEvent.getHotbarButton() != -1 && (a == InventoryAction.HOTBAR_MOVE_AND_READD || a == InventoryAction.HOTBAR_SWAP)) {
                    ItemStack is = clickEvent.getView().getBottomInventory().getItem(clickEvent.getHotbarButton());

                    if (is != null && is.isSimilar(dm.BEACON))
                        clickEvent.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onItemDrop(PlayerDropItemEvent dropEvent) {
        DataManager dm = DataManager.getManager();

        if (dm.BEACON_UNLIMITED_PERMANENT && dropEvent.getPlayer().hasPermission("wp.beacon.unlimited"))
            if (dropEvent.getItemDrop().getItemStack().isSimilar(dm.BEACON))
                dropEvent.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent deathEvent) {
        DataManager dm = DataManager.getManager();

        if (dm.BEACON_UNLIMITED_PERMANENT && deathEvent.getEntity().hasPermission("wp.beacon.unlimited")) {
            Iterator<ItemStack> it = deathEvent.getDrops().iterator();

            while (it.hasNext())
                if (it.next().isSimilar(dm.BEACON))
                    it.remove();
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent joinEvent) {
        giveBeacon(joinEvent.getPlayer());
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent respawnEvent) {
        giveBeacon(respawnEvent.getPlayer());
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent changeEvent) {
        giveBeacon(changeEvent.getPlayer());
    }

    public void giveBeacon(Player p) {
        DataManager dm = DataManager.getManager();

        // If BEACON_UNLIMITED_PERMANENT is true, gives players with
        // "wp.beacon.unlimited" a Waypoint Beacon if they don't have one
        if (dm.BEACON_UNLIMITED_PERMANENT && p.hasPermission("wp.beacon.unlimited") &&
                doesWorldGivePermanentBeacons(p.getWorld().getName())) {
            assignBeacon(p);
        }
    }

    public void assignBeacon(Player p) {
        DataManager dm = DataManager.getManager();
        PlayerInventory inv = p.getInventory();

        if (!inv.containsAtLeast(dm.BEACON, 1)) {
            int desiredSlot = dm.BEACON_UNLIMITED_PERMANENT_SLOT;
            if (desiredSlot > 0) {
                desiredSlot -= 1;

                if (desiredSlot < 36 && inv.getItem(desiredSlot) == null) {
                    // The player's hotbar is indexed 0-8 in setItem(). The order goes: 0-8 hotbar, 9-35 normal inventory, 36 boots,
                    // 37 leggings, 38 chestplate, and 39 helmet. For indexes > 39 an ArrayIndexOutOfBoundsException will be thrown.
                    inv.setItem(desiredSlot, dm.BEACON);
                    return;
                }
            }

            int emptySlot = inv.firstEmpty();
            if (emptySlot > -1)
                inv.setItem(emptySlot, dm.BEACON);
        }
    }

    private boolean isBeaconImmovable(HumanEntity player, InventoryType type) {
        if (player.getGameMode() == GameMode.CREATIVE || type == InventoryType.CREATIVE)
            return false;

        DataManager dm = DataManager.getManager();
        return dm.BEACON_UNLIMITED_PERMANENT_IMMOVABLE && dm.BEACON_UNLIMITED_PERMANENT_SLOT > 0 &&
                dm.BEACON_UNLIMITED_PERMANENT_SLOT < 10;
    }

    private boolean doesWorldGivePermanentBeacons(String worldName) {
        DataManager dm = DataManager.getManager();
        return dm.BEACON_UNLIMITED_PERMANENT_WORLDS.isEmpty() ||
                dm.BEACON_UNLIMITED_PERMANENT_WORLDS.contains(worldName);
    }

}
