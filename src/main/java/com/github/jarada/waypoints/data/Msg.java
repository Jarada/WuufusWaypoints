package com.github.jarada.waypoints.data;

import java.util.IllegalFormatException;
import java.util.logging.Level;

import org.bukkit.command.CommandSender;

import com.github.jarada.waypoints.PluginMain;
import com.github.jarada.waypoints.Util;

public enum Msg {

    BEACON_DISABLED("&cError: &fBeacon disabled in config!"),
    BLOCKED_CANCEL("&cWaypoint location blocked, aborting quantization procedure."),
    BORDER("&2------------------------------"),
    CAT_NOT_FOUND("&cError: &fCategory %s not found!"),
    CAT_REMOVED("&6%s &fremoved from categories."),
    CAT_RENAMED("&6%s: &fCategory renamed to &6%s&f."),
    CAT_REORDERED("&6%s: &fCategory now at order %d."),
    CAT_SETICON("&6%s: &fCategory icon set to &a%s&f:&a%s&f."),
    CAT_SERVER_ICON("&cError: &fOnly Server"),
    CMD_NO_CONSOLE("&cError: &fCommand unavailable to CONSOLE."),
    COMMAND_CANCEL("&cUnable to act while undergoing quantization."),
    DAMAGE_CANCEL("&cTransmission interrupted by kinetic interference."),
    DISCOVERABLE_WAYPOINT("&8Undiscovered Location"),
    DISCOVERED_WAYPOINT("Discovered &6%s&f! Directory updated with new entry."),
    DISCOVERY_MODE_DISABLED("&6%s: &fDiscovery mode disabled."),
    DISCOVERY_MODE_ENABLED_SERVER("&6%s: &fDiscovery mode set to &aServer-wide&f."),
    DISCOVERY_MODE_ENABLED_WORLD("&6%s: &fDiscovery mode set to &aWorld-specific&f."),
    DYNAMIC_MODE_DISABLED("&6%s: &fDynamic mode disabled."),
    DYNAMIC_MODE_ENABLED("&6%s: &fDynamic mode enabled. Waypoint will now operate on highest possible y-axis non-air block."),
    HOME_WP_ALREADY_HERE("&cError: &fHome waypoint &6%s &falready lies at this location."),
    HOME_WP_CREATED("&aSuccess: &fCoordinates received for &6%s&f."),
    HOME_WP_FULL("You have the maximum number of home waypoints, adding another will replace the oldest one."),
    HOME_WP_LOCATION_UPDATED("&aSuccess: &fCoordinates updated for &6%s&f."),
    HOME_WP_REMAINING("You can freely add %d more home waypoints."),
    HOME_WP_REPLACED("&aSuccess: &fOverflow; &6%s &freplaced with coordinates to &6%s&f."),
    INSUFFICIENT_POWER("&cInsufficient power."),
    INVALID_DURABILITY("&cError: &fInvalid durability value."),
    INVALID_MATERIAL("&cError: &fInvalid material type."),
    LIST_HOME_WAYPOINTS("&aHome Waypoints: &6%s"),
    LORE_BEACON_NAME("&aWaypoint Beacon"),
    LORE_BEACON_1("&fBroadcasts signal to"),
    LORE_BEACON_2("&fwaypoint directory for"),
    LORE_BEACON_3("&fremote connection."),
    LORE_BEACON_4("&8&oRight-click to use"),
    LORE_LINE(" &5&o%s"),
    MAX_LENGTH_EXCEEDED("&cMax length (&f%s&c) exceeded."),
    MENU_NAME("Waypoint Directory"),
    MENU_PAGE("&aPage"),
    MENU_PAGE_CLOSE("&aClose Category"),
    MENU_PAGE_NEXT("&aNext Page"),
    MENU_PAGE_PREVIOUS("&aPrevious Page"),
    MENU_WALK("&aShow Waypoints on Walk"),
    MENU_WALK_ACTIVE("&2Active"),
    MENU_WALK_SILENCED("&4Silenced"),
    NO_PERMS("&cError: &fAccess denied."),
    NO_WAYPOINTS("&fNone"),
    ONLY_SERVER_DEFINED("&cError: &fThis command only applies to server-defined waypoints."),
    OPEN_WP_MENU("&6%s: &fOpening waypoint directory."),
    PORT_TASK_1("&aPlotting course for &6%s&a. Scanning &6%s&a..."),
    PORT_TASK_2("&a ...scan complete. Course locked in."),
    PORT_TASK_3("&aSynchronizing qubits; standby for quantization."),
    PORT_TASK_4("&9Energizing..."),
    RELOADED("&aWaypoints &freloaded."),
    REMOTELY_ACCESSED("&aWaypoint Beacon: &fEstablished remote connection to waypoint directory."),
    RESPAWN_BLOCKED("&6%s: &cWaypoint registered for death-induced reintegration blocked."),
    RESPAWN_NO_POWER("&6%s: &fInsufficient power levels may have resulted in reintegration at a closer waypoint."),
    RESPAWN_NOT_FOUND("&cWaypoint registered for death-induced reintegration is no longer functional."),
    SELECTED_1(" &6%s &f&o(%s)"),
    SELECTED_2(" &aX: &f%d  &aPitch: &f%d"),
    SELECTED_3(" &aY: &f%d  &aYaw: &f%d"),
    SELECTED_4(" &aZ: &f%d"),
    SELECTED_DISCOVER(" &aDiscovery: &f%s"),
    SETHOME_DEFAULT_DESC("User-defined"),
    SET_PLAYER_SPAWN("&6%s: &fWaypoint registered as death-induced reintegration point."),
    SET_SPAWN("&fSpawn location updated for world &6%s&f."),
    USAGE_SETICON("&cUsage: &f/wp icon <Material>"),
    USAGE_SETHOME("&cUsage: &f/sethome <name ...>"),
    USAGE_WP_RENAME("&cUsage: &f/wp rename <name ...>"),
    USAGE_WP_ADD("&cUsage: &f/wp add <name ...>"),
    USAGE_WP_CATORDER("&cUsage: &f/wp catorder <order>"),
    USAGE_WP_CATSET("&cUsage: &f/wp catset <name ...>"),
    USAGE_WP_CATREMOVE("&cUsage: &f/wp catremove"),
    USAGE_WP_CATRENAME("&cUsage: &f/wp catrename <name ...>"),
    WAYPOINT_DISABLED("&6%s: &fWaypoint disabled."),
    WAYPOINT_ENABLED("&6%S: &fWaypoint enabled."),
    WORD_BED("Bed"),
    WORD_CATEGORY("Category"),
    WORD_DISABLED("Disabled"),
    WORD_SERVER_WIDE("Server-wide"),
    WORD_SPAWN("Spawn"),
    WORD_WORLD_SPECIFIC("World-specific"),
    WP_ALREADY_HERE("&cError: &fCoordinates overlap with &6%s&f."),
    WP_CATEGORY_EMPTY("&cError: &fWaypoint %s has no category."),
    WP_CATEGORY_REMOVED("&6%s: &fCategory removed."),
    WP_CATEGORY_ORDER("&6%s. &f%s"),
    WP_CATEGORY_SET("&6%s: &fCategory set for &6%s&f."),
    WP_CATEGORY_SETICON("&6%s: &fCategory icon set to &a%s&f:&a%s&f."),
    WP_CATEGORY_UNSET("&6%s: &fCategory unset from &6%s&f."),
    WP_DESC_CLEARED("&6%s: &fDescription cleared."),
    WP_DESC_UPDATED("&6%s: &fDescription updated."),
    WP_DESC_UPDATED_BOOK("&6%s: &fScanned &a%s&f. Directory data updated."),
    WP_DUPLICATE_NAME("&cError: &fA duplicate entry for \"%s\" was found."),
    WP_HINT_UPDATED("&6%s: &fHint updated."),
    WP_LIST("&aWaypoints: &6"),
    WP_LOCATION_UPDATED("&6%s: &fLocation updated."),
    WP_NO_DESC(" &oNo description."),
    WP_NOT_EXIST("&cError: &fWaypoint \"%s\" not found."),
    WP_NOT_SELECTED_ERROR("&cError: &fNo waypoint selected."),
    WP_NOT_SELECTED_ERROR_USAGE("&cUse \"&f/wp select <name>&c\" to select a waypoint."),
    WP_REMOVED("&6%s &fremoved from directory."),
    WP_RENAMED("&6%s &frenamed to &6%s&f."),
    WP_SELECTED("&fWaypoint &6%s &fselected."),
    WP_SETICON("&6%s: &fDirectory icon set to &a%s&f:&a%s&f.");

    private final String defaultMsg;

    Msg(String defaultMsg) {
        this.defaultMsg = defaultMsg;
    }

    public String getDefaultMsg() {
        return defaultMsg;
    }

    @Override
    public String toString() {
        return DataManager.getManager().getMsg(this);
    }

    public void sendTo(CommandSender sender) {
        sender.sendMessage(Util.color(toString()));
    }

    public void sendTo(CommandSender sender, Object... args) {
        String msg;

        try {
            msg = String.format(toString(), args);
        } catch (IllegalFormatException e) {
            msg = String.format(defaultMsg, args);
            PluginMain
                    .getPluginInstance()
                    .getLogger()
                    .log(Level.WARNING,
                            String.format("\"Waypoints.Messages.%s\" is misconfigured in plugin.yml.", this.name()));
        }

        sender.sendMessage(Util.color(msg));
    }

}
