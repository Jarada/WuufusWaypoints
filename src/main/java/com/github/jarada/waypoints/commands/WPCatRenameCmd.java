package com.github.jarada.waypoints.commands;

import com.github.jarada.waypoints.WaypointManager;
import com.github.jarada.waypoints.data.Category;
import com.github.jarada.waypoints.data.DataManager;
import com.github.jarada.waypoints.data.Msg;
import com.github.jarada.waypoints.data.Waypoint;
import org.bukkit.command.CommandSender;

public class WPCatRenameCmd extends WPCatCmd {

    @Override
    public void execute(CommandSender sender, String[] args) {
        WaypointManager wm = WaypointManager.getManager();

        Waypoint wp = getSelectedWaypoint(wm, sender);
        if (wp == null)
            return;

        if (args.length == 0) {
            Msg.USAGE_WP_CATRENAME.sendTo(sender);
            return;
        }

        String categoryName = getCategoryName(args, sender);
        if (categoryName == null)
            return;

        if (wp.getCategory() != null) {
            Category category = wm.getCategoryFromUUID(wp.getCategory());
            String oldName = category.getName();
            category.setName(categoryName);
            DataManager.getManager().saveWaypoints();
            Msg.CAT_RENAMED.sendTo(sender, oldName, category.getName());
        } else {
            Msg.WP_CATEGORY_EMPTY.sendTo(sender, wp.getName());
        }
    }

    @Override
    public boolean isConsoleExecutable() {
        return true;
    }

    @Override
    public boolean hasRequiredPerm(CommandSender sender) {
        return sender.hasPermission("wp.category.rename");
    }
}
