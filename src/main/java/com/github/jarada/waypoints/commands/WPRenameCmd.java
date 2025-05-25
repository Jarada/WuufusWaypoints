package com.github.jarada.waypoints.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.jarada.waypoints.SelectionManager;
import com.github.jarada.waypoints.util.Util;
import com.github.jarada.waypoints.WaypointManager;
import com.github.jarada.waypoints.data.DataManager;
import com.github.jarada.waypoints.data.Msg;
import com.github.jarada.waypoints.data.Waypoint;

public class WPRenameCmd implements PluginCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {
        WaypointManager wm = WaypointManager.getManager();
        Waypoint wp = SelectionManager.getManager().getSelectedWaypoint(sender);

        if (wp == null) {
            Msg.WP_NOT_SELECTED_ERROR.sendTo(sender);
            Msg.WP_NOT_SELECTED_ERROR_USAGE.sendTo(sender);
            return;
        }

        if (args.length == 0) {
            Msg.USAGE_WP_RENAME.sendTo(sender);
            return;
        }

        String newWaypointName = Util.color(Util.buildString(args, 0, ' '));
        int maxLength = DataManager.getManager().WP_NAME_MAX_LENGTH;

        if (ChatColor.stripColor(newWaypointName).length() > maxLength) {
            Msg.MAX_LENGTH_EXCEEDED.sendTo(sender, maxLength);
            return;
        }

        String oldName = wp.getName();
        if (!wm.renameWaypoint(wp, (Player) sender, newWaypointName))
            return;

        DataManager.getManager().saveWaypoint(sender, wp);
        Msg.WP_RENAMED.sendTo(sender, oldName, newWaypointName);
    }

    @Override
    public boolean isConsoleExecutable() {
        return true;
    }

    @Override
    public boolean hasRequiredPerm(CommandSender sender) {
        return sender.hasPermission("wp.rename");
    }

}
