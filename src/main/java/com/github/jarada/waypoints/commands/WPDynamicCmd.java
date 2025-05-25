package com.github.jarada.waypoints.commands;

import com.github.jarada.waypoints.SelectionManager;
import com.github.jarada.waypoints.Util;
import com.github.jarada.waypoints.WaypointManager;
import com.github.jarada.waypoints.data.DataManager;
import com.github.jarada.waypoints.data.Msg;
import com.github.jarada.waypoints.data.Waypoint;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WPDynamicCmd implements PluginCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {
        Waypoint wp = SelectionManager.getManager().getSelectedWaypoint(sender);
        WaypointManager wm = WaypointManager.getManager();
        Player p = (Player) sender;

        if (wp == null) {
            Msg.WP_NOT_SELECTED_ERROR.sendTo(sender);
            Msg.WP_NOT_SELECTED_ERROR_USAGE.sendTo(sender);
            return;
        }

        if (WaypointManager.getManager().getWaypoint(wp.getName()) == null) {
            Msg.ONLY_SERVER_DEFINED.sendTo(sender);
            return;
        }

        if (wp.isDynamic()) {
            wp.setDynamic(false);
            Msg.DYNAMIC_MODE_DISABLED.sendTo(sender, wp.getName());
        } else {
            for (Waypoint check : wm.getWaypoints().values()) {
                if (check.getUUID().equals(wp.getUUID())) continue;
                if (Util.isSameLoc(wp.getLocation(), check.getYAdjustedLocation(wp.getLocation()), true)) {
                    Msg.WP_ALREADY_HERE.sendTo(p, wp.getName());
                    return;
                }
            }

            wp.setDynamic(true);
            Msg.DYNAMIC_MODE_ENABLED.sendTo(sender, wp.getName());
        }

        DataManager.getManager().saveWaypoints();
    }

    @Override
    public boolean isConsoleExecutable() {
        return false;
    }

    @Override
    public boolean hasRequiredPerm(CommandSender sender) {
        return sender.hasPermission("wp.dynamic");
    }
}
