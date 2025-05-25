package com.github.jarada.waypoints.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.jarada.waypoints.util.Util;
import com.github.jarada.waypoints.WaypointManager;
import com.github.jarada.waypoints.data.DataManager;
import com.github.jarada.waypoints.data.Msg;

public class SetHomeCmd implements PluginCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player p = (Player) sender;

        if (args.length == 0) {
            Msg.USAGE_SETHOME.sendTo(p);
            return;
        }

        String waypointName = Util.color(Util.buildString(args, 0, ' '));
        int maxLength = DataManager.getManager().WP_NAME_MAX_LENGTH;

        if (ChatColor.stripColor(waypointName).length() > maxLength) {
            Msg.MAX_LENGTH_EXCEEDED.sendTo(p, maxLength);
            return;
        }

        WaypointManager.getManager().setHome(p, waypointName);
    }

    @Override
    public boolean isConsoleExecutable() {
        return false;
    }

    @Override
    public boolean hasRequiredPerm(CommandSender sender) {
        return sender.hasPermission("wp.sethome");
    }

}
