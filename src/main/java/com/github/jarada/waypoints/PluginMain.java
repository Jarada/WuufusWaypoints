package com.github.jarada.waypoints;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.jarada.waypoints.commands.*;
import com.github.jarada.waypoints.data.*;
import com.github.jarada.waypoints.listeners.PlayerListener;
import com.github.jarada.waypoints.listeners.WaypointListener;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public class PluginMain extends JavaPlugin {

    private static PluginMain          instance;
    private Map<String, PluginCommand> commands;

    public static PluginMain getPluginInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        saveResource("CHANGELOG.txt", true);
        DataManager.getManager().loadConfig();
        DataManager.getManager().loadWaypoints();

        commands = new HashMap<>();
        commands.put("sethome", new SetHomeCmd());
        commands.put("setspawn", new SetSpawnCmd());
        commands.put("add", new WPAddCmd());
        commands.put("beacon", new WPBeaconCmd());
        commands.put("beaconperm", new WPBeaconPermanentCmd());
        commands.put("desc", new WPDescCmd());
        commands.put("discover", new WPDiscoverCmd());
        commands.put("dynamic", new WPDynamicCmd());
        commands.put("hint", new WPHintCmd());
        commands.put("icon", new WPIconCmd());
        commands.put("move", new WPMoveCmd());
        commands.put("open", new WPOpenCmd());
        commands.put("reload", new WPReloadCmd());
        commands.put("remove", new WPRemoveCmd());
        commands.put("rename", new WPRenameCmd());
        commands.put("select", new WPSelectCmd());
        commands.put("toggle", new WPToggleCmd());
        commands.put("caticon", new WPCatIconCmd());
        commands.put("catlist", new WPCatListCmd());
        commands.put("catorder", new WPCatOrderCmd());
        commands.put("catremove", new WPCatRemoveCmd());
        commands.put("catrename", new WPCatRenameCmd());
        commands.put("catset", new WPCatSetCmd());
        commands.put("catunset", new WPCatUnsetCmd());

        getCommand("wp").setExecutor(this);
        getCommand("sethome").setExecutor(this);
        getCommand("setspawn").setExecutor(this);
        getServer().getPluginManager().registerEvents(new WaypointListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);

        for (Player p : getServer().getOnlinePlayers())
            DataManager.getManager().loadPlayerData(p.getUniqueId());
        getLogger().info("Waypoints system online!");

        new UpdateChecker(this, 76603).getVersion(version -> {
            if (!this.getDescription().getVersion().equalsIgnoreCase(version)) {
                getLogger().info(String.format("Waypoints system update: %s now available!", version));
            }
        });
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        DataManager.getManager().saveWaypoints();

        for (Player p : getServer().getOnlinePlayers())
            DataManager.getManager().savePlayerData(p.getUniqueId());

        instance = null;
        commands = null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String cmd = command.getName().toLowerCase();
        PluginCommand pluginCmd = null;
        String[] param = null;

        if (cmd.equals("wp")) {
            if (args.length > 0) {
                String key = args[0].toLowerCase();
                param = Arrays.copyOfRange(args, 1, args.length);

                if (commands.containsKey(key))
                    pluginCmd = commands.get(key);
            }

            if (pluginCmd == null) {
                if (sender instanceof Player) {
                    Player p = (Player) sender;
                    List<Waypoint> playerPoints = WaypointManager.getManager().getPlayerData(p.getUniqueId())
                            .getAllWaypoints();
                    StringBuilder sb = new StringBuilder();

                    for (int i = 0; i < playerPoints.size(); i++) {
                        sb.append(playerPoints.get(i).getName());

                        if (i < playerPoints.size() - 1)
                            sb.append("&6, ");
                    }

                    if (sb.length() > 0)
                        Msg.LIST_HOME_WAYPOINTS.sendTo(sender, sb.toString());
                }

                sender.sendMessage("Wuufu's Waypoints v" + getDescription().getVersion() + " by Wuufu and Marenwynn.");
                return false;
            }
        } else if (cmd.equals("sethome")) {
            pluginCmd = commands.get("sethome");
            param = args;
        } else if (cmd.equals("setspawn")) {
            pluginCmd = commands.get("setspawn");
        }

        assert pluginCmd != null;
        if (!(sender instanceof Player) && !pluginCmd.isConsoleExecutable()) {
            Msg.CMD_NO_CONSOLE.sendTo(sender);
            return true;
        }

        if (!pluginCmd.hasRequiredPerm(sender)) {
            Msg.NO_PERMS.sendTo(sender);
            return true;
        }

        pluginCmd.execute(sender, param);
        return true;
    }

}
