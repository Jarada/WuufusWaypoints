package com.github.jarada.waypoints.commands;

import com.github.jarada.waypoints.WaypointManager;
import com.github.jarada.waypoints.data.Category;
import com.github.jarada.waypoints.data.DataManager;
import com.github.jarada.waypoints.data.Msg;
import com.github.jarada.waypoints.data.Waypoint;
import org.bukkit.command.CommandSender;

public class WPCatSetCmd extends WPCatCmd {

    @Override
    public void execute(CommandSender sender, String[] args) {
        WaypointManager wm = WaypointManager.getManager();

        Waypoint wp = getSelectedWaypoint(wm, sender);
        if (wp == null)
            return;

        if (args.length == 0) {
            Msg.USAGE_WP_CATSET.sendTo(sender);
            return;
        }

        String categoryName = getCategoryName(args, sender);
        if (categoryName == null)
            return;

        Category category = wm.getCategoryFromName(categoryName);
        if (category == null)
            category = wm.addCategory(categoryName);

        if (!category.getUUID().toString().equals(wp.getCategory())) {
            wm.unrecordWaypointCategory(wp);
            wp.setCategory(category);
            wm.recordWaypointCategory(wp);
            DataManager.getManager().saveWaypoint(sender, wp);
        }
        Msg.WP_CATEGORY_SET.sendTo(sender, category.getName(), wp.getName());
    }

    @Override
    public boolean isConsoleExecutable() {
        return true;
    }

    @Override
    public boolean hasRequiredPerm(CommandSender sender) {
        return sender.hasPermission("wp.category.set");
    }

}
