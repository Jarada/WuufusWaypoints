package com.github.jarada.waypoints.commands;

import org.bukkit.command.CommandSender;

import com.github.jarada.waypoints.SelectionManager;
import com.github.jarada.waypoints.util.Util;
import com.github.jarada.waypoints.data.DataManager;
import com.github.jarada.waypoints.data.Msg;
import com.github.jarada.waypoints.data.Waypoint;

public class WPDescCmd implements PluginCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {
        DataManager dm = DataManager.getManager();
        Waypoint wp = SelectionManager.getManager().getSelectedWaypoint(sender);

        if (wp == null) {
            Msg.WP_NOT_SELECTED_ERROR.sendTo(sender);
            Msg.WP_NOT_SELECTED_ERROR_USAGE.sendTo(sender);
            return;
        }

        if (args.length == 0) {
            wp.setDescription("");
            dm.saveWaypoints();
            Msg.WP_DESC_CLEARED.sendTo(sender, wp.getName());
            return;
        }

        String desc = Util.buildString(args, 0, ' ');

        if (desc.length() > dm.WP_DESC_MAX_LENGTH) {
            Msg.MAX_LENGTH_EXCEEDED.sendTo(sender, dm.WP_DESC_MAX_LENGTH);
            return;
        }

        wp.setDescription(desc);
        dm.saveWaypoint(sender, wp);
        Msg.WP_DESC_UPDATED.sendTo(sender, wp.getName());
    }

    @Override
    public boolean isConsoleExecutable() {
        return true;
    }

    @Override
    public boolean hasRequiredPerm(CommandSender sender) {
        return sender.hasPermission("wp.desc");
    }

}
