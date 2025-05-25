package com.github.jarada.waypoints.commands;

import com.github.jarada.waypoints.SelectionManager;
import com.github.jarada.waypoints.util.Util;
import com.github.jarada.waypoints.data.DataManager;
import com.github.jarada.waypoints.data.Msg;
import com.github.jarada.waypoints.data.Waypoint;
import org.bukkit.command.CommandSender;

public class WPHintCmd implements PluginCommand {

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
            wp.setHint("");
            dm.saveWaypoints();
            Msg.WP_DESC_CLEARED.sendTo(sender, wp.getName());
            return;
        }

        String hint = Util.buildString(args, 0, ' ');

        if (hint.length() > dm.WP_DESC_MAX_LENGTH) {
            Msg.MAX_LENGTH_EXCEEDED.sendTo(sender, dm.WP_DESC_MAX_LENGTH);
            return;
        }

        wp.setHint(hint);
        dm.saveWaypoint(sender, wp);
        Msg.WP_HINT_UPDATED.sendTo(sender, wp.getName());
    }

    @Override
    public boolean isConsoleExecutable() {
        return true;
    }

    @Override
    public boolean hasRequiredPerm(CommandSender sender) {
        return sender.hasPermission("wp.hint");
    }

}
