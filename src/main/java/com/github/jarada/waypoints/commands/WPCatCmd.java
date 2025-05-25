package com.github.jarada.waypoints.commands;

import com.github.jarada.waypoints.SelectionManager;
import com.github.jarada.waypoints.util.Util;
import com.github.jarada.waypoints.WaypointManager;
import com.github.jarada.waypoints.data.DataManager;
import com.github.jarada.waypoints.data.Msg;
import com.github.jarada.waypoints.data.Waypoint;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public abstract class WPCatCmd implements PluginCommand {

    protected Waypoint getSelectedWaypoint(WaypointManager wm, CommandSender sender) {
        Waypoint wp = SelectionManager.getManager().getSelectedWaypoint(sender);

        if (wp == null) {
            Msg.WP_NOT_SELECTED_ERROR.sendTo(sender);
            Msg.WP_NOT_SELECTED_ERROR_USAGE.sendTo(sender);
            return null;
        }

        if (wm.getWaypoint(wp.getName()) == null) {
            Msg.ONLY_SERVER_DEFINED.sendTo(sender);
            return null;
        }

        return wp;
    }

    protected String getCategoryName(String[] args, CommandSender sender) {
        String categoryName = Util.color(Util.buildString(args, 0, ' '));
        int maxLength = DataManager.getManager().WP_NAME_MAX_LENGTH;

        if (ChatColor.stripColor(categoryName).length() > maxLength) {
            Msg.MAX_LENGTH_EXCEEDED.sendTo(sender, maxLength);
            return null;
        }

        return categoryName;
    }

}
