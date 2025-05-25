package com.github.jarada.waypoints;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.github.jarada.waypoints.data.Category;
import com.github.jarada.waypoints.data.Msg;
import com.github.jarada.waypoints.data.Waypoint;
import com.github.jarada.waypoints.util.Util;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SelectionManager {

    private static SelectionManager sm;

    private Map<UUID, Waypoint>     selectedWaypoints;
    private Waypoint                consoleSelection;

    public SelectionManager() {
        selectedWaypoints = new HashMap<>();
    }

    public static SelectionManager getManager() {
        if (sm == null)
            sm = new SelectionManager();

        return sm;
    }

    public Waypoint getSelectedWaypoint(CommandSender sender) {
        if (sender instanceof Player) {
            UUID playerUUID = ((Player) sender).getUniqueId();

            if (selectedWaypoints.containsKey(playerUUID))
                return selectedWaypoints.get(playerUUID);

            return null;
        } else {
            return consoleSelection;
        }
    }

    public void setSelectedWaypoint(CommandSender sender, Waypoint wp) {
        if (sender instanceof Player)
            selectedWaypoints.put(((Player) sender).getUniqueId(), wp);
        else
            consoleSelection = wp;

        sendSelectionInfo(sender, wp);
    }

    public void clearSelectedWaypoint(CommandSender sender) {
        if (sender instanceof Player) {
            UUID playerUUID = ((Player) sender).getUniqueId();

            if (selectedWaypoints.containsKey(playerUUID))
                selectedWaypoints.remove(playerUUID);
        } else {
            consoleSelection = null;
        }
    }

    public void clearSelectionsWith(Waypoint wp) {
        for (UUID playerUUID : selectedWaypoints.keySet()) {
            Waypoint selected = selectedWaypoints.get(playerUUID);

            if (selected != null && selected == wp)
                selectedWaypoints.remove(playerUUID);
        }

        if (consoleSelection != null && consoleSelection == wp)
            consoleSelection = null;
    }

    private void sendSelectionInfo(CommandSender sender, Waypoint wp) {
        boolean serverDefined = WaypointManager.getManager().isServerDefined(wp);
        Location loc = wp.getDynamicLocation();
        String displayName = wp.getName();
        Category category = wp.getCategory() != null ? WaypointManager.getManager().getCategoryFromUUID(wp.getCategory()) : null;

        if (serverDefined && !wp.isEnabled())
            displayName += Util.color(" &f[&c" + Msg.WORD_DISABLED.toString() + "&f]");

        Msg.BORDER.sendTo(sender);
        Msg.SELECTED_1.sendTo(sender, displayName, loc.getWorld().getName());
        if (category != null)
            Msg.WP_CATEGORY_ORDER.sendTo(sender, Integer.toString(category.getOrder()), category.getName());
        Msg.BORDER.sendTo(sender);
        Msg.SELECTED_2.sendTo(sender, loc.getBlockX(), (int) loc.getPitch());
        Msg.SELECTED_3.sendTo(sender, loc.getBlockY(), (int) loc.getYaw());
        Msg.SELECTED_4.sendTo(sender, loc.getBlockZ());

        if (serverDefined) {
            String discoveryMode = wp.isDiscoverable() == null ? Msg.WORD_DISABLED.toString() : (
                    Boolean.TRUE.equals(wp.isDiscoverable()) ? Msg.WORD_SERVER_WIDE.toString() :
                            Msg.WORD_WORLD_SPECIFIC.toString());
            sender.sendMessage("");
            Msg.SELECTED_DISCOVER.sendTo(sender, discoveryMode);
        }

        Msg.BORDER.sendTo(sender);

        if (wp.getDescription().length() == 0)
            Msg.WP_NO_DESC.sendTo(sender);
        else
            for (String line : Util.getWrappedLore(wp.getDescription(), 35))
                Msg.LORE_LINE.sendTo(sender, ChatColor.stripColor(Util.color(line)));

        Msg.BORDER.sendTo(sender);

        if (wp.getHint().length() > 0) {
            for (String line : Util.getWrappedLore(wp.getHint(), 35))
                Msg.LORE_LINE.sendTo(sender, ChatColor.stripColor(Util.color(line)));
            Msg.BORDER.sendTo(sender);
        }
    }

}
