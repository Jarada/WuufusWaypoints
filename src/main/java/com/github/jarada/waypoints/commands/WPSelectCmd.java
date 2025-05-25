package com.github.jarada.waypoints.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.jarada.waypoints.SelectionManager;
import com.github.jarada.waypoints.util.Util;
import com.github.jarada.waypoints.WaypointManager;
import com.github.jarada.waypoints.data.Msg;
import com.github.jarada.waypoints.data.Waypoint;

public class WPSelectCmd implements PluginCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {
        WaypointManager wm = WaypointManager.getManager();

        if (args.length == 0) {
            if (sender instanceof Player) {
                wm.openWaypointSelectionMenu((Player) sender);
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append(Msg.WP_LIST.toString());

                Waypoint[] waypoints = wm.getWaypoints().values().toArray(new Waypoint[wm.getWaypoints().size()]);
                Waypoint selectedWaypoint = SelectionManager.getManager().getSelectedWaypoint(sender);

                for (int i = 0; i < waypoints.length; i++) {
                    if (waypoints[i] == selectedWaypoint)
                        sb.append("&a* &6");

                    sb.append(waypoints[i].getName());

                    if (i < waypoints.length - 1)
                        sb.append("&6, ");
                }

                if (waypoints.length == 0)
                    sb.append(Msg.NO_WAYPOINTS.toString());

                sender.sendMessage(Util.color(sb.toString()));
            }

            return;
        }

        if (!sender.hasPermission("wp.admin")) {
            Msg.NO_PERMS.sendTo(sender);
            return;
        }

        String waypointName = Util.color(Util.buildString(args, 0, ' '));
        Waypoint wp = wm.getWaypoint(waypointName);

        if (wp == null) {
            Msg.WP_NOT_EXIST.sendTo(sender, waypointName);
            return;
        }

        if (sender instanceof Player && !Util.hasAccess((Player) sender, wp, true)) {
            Msg.NO_PERMS.sendTo(sender);
            return;
        }

        SelectionManager.getManager().setSelectedWaypoint(sender, wp);
    }

    @Override
    public boolean isConsoleExecutable() {
        return true;
    }

    @Override
    public boolean hasRequiredPerm(CommandSender sender) {
        return sender.hasPermission("wp.select.command");
    }

}
