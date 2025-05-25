package com.github.jarada.waypoints.tasks;

import com.github.jarada.waypoints.WaypointManager;
import com.github.jarada.waypoints.data.*;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.metadata.FixedMetadataValue;

import com.github.jarada.waypoints.PluginMain;
import com.github.jarada.waypoints.Util;

public class TeleportTask implements Listener, Runnable {

    private PluginMain      pm;
    private DataManager     dm;
    private WaypointManager wm;

    private int             counter;
    private Player          p;
    private Waypoint        wp;
    private Location        destination;

    public TeleportTask(Player p, Waypoint wp) {
        pm = PluginMain.getPluginInstance();
        dm = DataManager.getManager();
        wm = WaypointManager.getManager();

        destination = Util.getSafeLocation(wp.getDynamicLocation());
        counter = p.hasPermission("wp.instant") ? 1 : 5;
        this.p = p;
        this.wp = wp;

        p.setMetadata("Wayporting", new FixedMetadataValue(pm, true));
        Bukkit.getPluginManager().registerEvents(this, pm);
    }

    public void destroy() {
        p.removeMetadata("Wayporting", pm);
        HandlerList.unregisterAll(this);

        counter = 0;
        p = null;
        wp = null;
        destination = null;
    }

    @Override
    public void run() {
        if (p == null)
            return;

        switch (counter) {
            case 5:
                dm.WARP_EFFECT.playLoadingEffectAtLocation(p.getLocation());
                Msg.PORT_TASK_1.sendTo(p, wp.getName(), p.getName());
                if (destination == null) {
                    counter = 2;
                }
                break;
            case 4:
                Msg.PORT_TASK_2.sendTo(p);
                break;
            case 3:
                Msg.PORT_TASK_3.sendTo(p);
                break;
            case 2:
                if (destination != null)
                    Util.checkChunkLoad(destination.getBlock());
                break;
            case 1:
                if (destination != null) {
                    Msg.PORT_TASK_4.sendTo(p);

                    Location from = p.getLocation();
                    from.setY(from.getY() + 2);

                    Location to = wp.getDynamicLocation();
                    to.setY(to.getY() + 2);

                    dm.WARP_EFFECT.playWarpingEffectAtLocation(from, false);
                    p.teleport(destination, TeleportCause.COMMAND);
                    dm.WARP_EFFECT.playWarpingEffectAtLocation(destination, !from.equals(to));
                } else {
                    Msg.BLOCKED_CANCEL.sendTo(p);
                }
                break;
            default:
                destroy();
                break;
        }

        if (counter-- > 0) {
            dm.WARP_EFFECT.playTickEffectAtLocation(p.getLocation(), counter, false);
            dm.WARP_EFFECT.playTickEffectAtLocation(destination, counter, true);
            Bukkit.getScheduler().runTaskLater(pm, this, 20L);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    void onPlayerDamage(EntityDamageEvent damageEvent) {
        if (p == null || !p.getUniqueId().equals(damageEvent.getEntity().getUniqueId()) || counter < 2)
            return;

        Msg.DAMAGE_CANCEL.sendTo(p);
        destroy();
    }

}
