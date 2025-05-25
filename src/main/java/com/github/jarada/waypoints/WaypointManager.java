package com.github.jarada.waypoints;

import java.util.*;
import java.util.stream.Collectors;

import com.github.jarada.waypoints.data.*;
import com.github.jarada.waypoints.menus.WaypointMenu;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.metadata.FixedMetadataValue;

public class WaypointManager {

    private static WaypointManager wm;

    private Map<UUID, PlayerData>  players;
    private Map<String, Waypoint>  waypoints;
    private Map<String, Category>  categories;
    private Map<String, List<Waypoint>> categoryListMap;

    public WaypointManager() {
        players = new HashMap<>();
        waypoints = new LinkedHashMap<>();
        categories = new LinkedHashMap<>();
        categoryListMap = new HashMap<>();
    }

    public static WaypointManager getManager() {
        if (wm == null)
            wm = new WaypointManager();

        return wm;
    }

    public Waypoint getWaypoint(String name) {
        String key = Util.getKey(name);

        if (waypoints.containsKey(key))
            return waypoints.get(key);

        return null;
    }

    public boolean isServerDefined(Waypoint wp) {
        return waypoints.values().contains(wp);
    }

    public boolean isSystemName(String name) {
        return name.equals(Msg.WORD_BED.toString()) || name.equals(Msg.WORD_SPAWN.toString());
    }

    public void addWaypoint(Waypoint wp) {
        waypoints.put(Util.getKey(wp.getName()), wp);
        sortWaypoints();
    }

    public void removeWaypoint(Waypoint wp) {
        waypoints.remove(Util.getKey(wp.getName()));
        sortWaypoints();
    }

    public boolean renameWaypoint(Waypoint wp, Player p, String newWaypointName) {
        if (isSystemName(newWaypointName)) {
            Msg.WP_DUPLICATE_NAME.sendTo(p, newWaypointName);
            return false;
        }

        boolean serverDefined = isServerDefined(wp);

        if (serverDefined) {
            if (getWaypoint(newWaypointName) != null) {
                Msg.WP_DUPLICATE_NAME.sendTo(p, newWaypointName);
                return false;
            }

            removeWaypoint(wp);
        } else {
            PlayerData pd = getPlayerData(p.getUniqueId());

            if (pd.getWaypoint(newWaypointName) != null) {
                Msg.WP_DUPLICATE_NAME.sendTo(p, newWaypointName);
                return false;
            }

            pd.removeWaypoint(wp);
        }

        wp.setName(newWaypointName);

        if (serverDefined)
            wm.addWaypoint(wp);
        else
            getPlayerData(p.getUniqueId()).addWaypoint(wp);

        return true;
    }

    public Map<String, Waypoint> getWaypoints() {
        return waypoints;
    }

    private void sortWaypoints() {
        List<String> keys = new ArrayList<String>(waypoints.keySet());
        Collections.sort(keys);

        Map<String, Waypoint> sortedWaypoints = new LinkedHashMap<String, Waypoint>();

        for (String key : keys)
            sortedWaypoints.put(key, waypoints.get(key));

        waypoints.clear();
        waypoints.putAll(sortedWaypoints);
    }

    public PlayerData getPlayerData(UUID player) {
        return players.get(player);
    }

    public Map<UUID, PlayerData> getPlayers() {
        return players;
    }

    public Category addCategory(String name) {
        Category category = new Category(name);
        category.setOrder(categories.size() + 1);
        categories.put(category.getUUID().toString(), category);
        return category;
    }

    public void recordWaypointCategory(Waypoint waypoint) {
        if (waypoint.getCategory() != null) {
            Category category = getCategoryFromUUID(waypoint.getCategory());
            if (category != null) {
                if (!categoryListMap.containsKey(category.getUUID().toString())) {
                    categoryListMap.put(category.getUUID().toString(), new ArrayList<>());
                }
                categoryListMap.get(category.getUUID().toString()).add(waypoint);
            }
        }
    }

    public void unrecordWaypointCategory(Waypoint waypoint) {
        if (waypoint.getCategory() != null) {
            Category category = getCategoryFromUUID(waypoint.getCategory());
            if (category != null && categoryListMap.containsKey(category.getUUID().toString())) {
                categoryListMap.get(category.getUUID().toString()).remove(waypoint);
                if (categoryListMap.get(category.getUUID().toString()).isEmpty()) {
                    categoryListMap.remove(category.getUUID().toString());
                    categories.remove(category.getUUID().toString());
                }
            }
            waypoint.clearCategory();
        }
    }

    public void removeCategory(Category category) {
        List<Waypoint> toRemove = new ArrayList<>(categoryListMap.get(category.getUUID().toString()));
        toRemove.forEach(this::unrecordWaypointCategory);
    }

    public Category getCategoryFromName(String name) {
        String keyName = Util.getKey(name);
        return categories.values().stream()
                .filter(x -> Util.getKey(x.getName()).equals(keyName))
                .findFirst().orElse(null);
    }

    public Category getCategoryFromUUID(String uuid) {
        return categories.get(uuid);
    }

    public Map<String, Category> getCategories() {
        return categories;
    }

    public List<WaypointMenuItem> getMenuWaypointsForCategory(Category category, Player p, boolean select) {
        List<WaypointMenuItem> accessList = new ArrayList<>();

        for (Waypoint wp : categoryListMap.get(category.getUUID().toString())) {
            if (Util.hasAccess(p, wp, select))
                accessList.add(new WaypointMenuItem(wp));
            else if (DataManager.getManager().SHOW_DISCOVERABLE_WAYPOINTS && Util.canDiscover(p, wp))
                accessList.add(new WaypointMenuItem(wp, true));
        }

        accessList.sort(Comparator.comparing(WaypointMenuItem::getWaypointKey));
        return accessList;
    }

    public void sortCategories() {
        List<Category> sortedCategoresList = new ArrayList<>(categories.values()).stream()
                .sorted(new CategoryComparator())
                .collect(Collectors.toList());
        for (int i = 0; i < sortedCategoresList.size(); i++) {
            sortedCategoresList.get(i).setOrder(i + 1);
            sortedCategoresList.get(i).setOrderSet(false);
        }

        Map<String, Category> sortedCategories = new LinkedHashMap<>();
        sortedCategoresList.forEach(x -> {
            sortedCategories.put(x.getUUID().toString(), x);
        });

        categories.clear();
        categories.putAll(sortedCategories);
    }

    public void openWaypointMenu(Player p, Waypoint currentWaypoint, boolean addServerWaypoints,
            boolean addHomeWaypoints, boolean select) {
        List<WaypointMenuItem> accessList = new ArrayList<>();
        List<Category> categoriesList = new ArrayList<>();

        if (!select) {
            if (currentWaypoint != null) {
                Msg.OPEN_WP_MENU.sendTo(p, currentWaypoint.getName());
                p.teleport(Util.teleportLocation(currentWaypoint.getDynamicLocation()), TeleportCause.PLUGIN);
            } else {
                Msg.REMOTELY_ACCESSED.sendTo(p);
            }

            if (p.hasPermission("wp.access.spawn")) {
                if (currentWaypoint != null && currentWaypoint.getName().equals(Msg.WORD_SPAWN.toString())) {
                    accessList.add(new WaypointMenuItem(currentWaypoint));
                } else {
                    Waypoint spawn = new Waypoint(Msg.WORD_SPAWN.toString(), p.getWorld().getSpawnLocation());
                    spawn.setIcon(Material.NETHER_STAR);
                    accessList.add(new WaypointMenuItem(spawn));
                }
            }

            if (p.hasPermission("wp.access.bed") && p.getBedSpawnLocation() != null) {
                Waypoint bed = new Waypoint(Msg.WORD_BED.toString(), p.getBedSpawnLocation());
                bed.setIcon(Material.WHITE_BED);
                accessList.add(new WaypointMenuItem(bed));
            }
        }

        PlayerData pd = getPlayerData(p.getUniqueId());

        if (addServerWaypoints && (!select || p.hasPermission("wp.admin")))
            for (Waypoint wp : waypoints.values()) {
                boolean hasCategory = wp.getCategory() != null && getCategoryFromUUID(wp.getCategory()) != null;
                if (Util.hasAccess(p, wp, select)) {
                    if (hasCategory) {
                        if (!categoriesList.contains(getCategoryFromUUID(wp.getCategory())))
                            categoriesList.add(getCategoryFromUUID(wp.getCategory()));
                    } else {
                        accessList.add(new WaypointMenuItem(wp));
                    }
                } else if (DataManager.getManager().SHOW_DISCOVERABLE_WAYPOINTS && Util.canDiscover(p, wp)) {
                    if (hasCategory) {
                        if (!categoriesList.contains(getCategoryFromUUID(wp.getCategory())))
                            categoriesList.add(getCategoryFromUUID(wp.getCategory()));
                    } else {
                        accessList.add(new WaypointMenuItem(wp, true));
                    }
                }
            }

        if (addHomeWaypoints)
            accessList.addAll(pd.getAllWaypoints().stream()
                    .map(WaypointMenuItem::new)
                    .collect(Collectors.toList()));

        if (!categoriesList.isEmpty())
            accessList.addAll(0, categoriesList.stream()
                    .sorted(new CategoryComparator())
                    .map(WaypointMenuItem::new)
                    .collect(Collectors.toList()));

        p.setMetadata("InMenu", new FixedMetadataValue(PluginMain.getPluginInstance(), true));
        new WaypointMenu(p, pd, currentWaypoint, accessList, select).open();
    }

    public void openWaypointSelectionMenu(Player p) {
        Waypoint selectedWaypoint = SelectionManager.getManager().getSelectedWaypoint(p);

        if (selectedWaypoint != null)
            Msg.WP_SELECTED.sendTo(p, selectedWaypoint.getName());

        openWaypointMenu(p, selectedWaypoint, true, true, true);
    }

    public boolean setHome(Player p, String waypointName) {
        PlayerData pd = getPlayerData(p.getUniqueId());
        Location playerLoc = p.getLocation();

        if (isSystemName(waypointName)) {
            Msg.WP_DUPLICATE_NAME.sendTo(p, waypointName);
            return false;
        }

        for (Waypoint wp : pd.getAllWaypoints()) {
            if (Util.isSameLoc(playerLoc, wp.getDynamicLocation(), true)) {
                Msg.HOME_WP_ALREADY_HERE.sendTo(p, wp.getName());
                return false;
            }

            if (waypointName.equals(wp.getName())) {
                wp.setLocation(p.getLocation());
                DataManager.getManager().savePlayerData(p.getUniqueId());
                Msg.HOME_WP_LOCATION_UPDATED.sendTo(p, waypointName);
                return true;
            }
        }

        Waypoint wp = new Waypoint(waypointName, playerLoc);
        wp.setDescription(Msg.SETHOME_DEFAULT_DESC.toString());

        Waypoint replaced = pd.addWaypoint(wp);
        DataManager.getManager().savePlayerData(p.getUniqueId());

        if (replaced != null)
            Msg.HOME_WP_REPLACED.sendTo(p, replaced.getName(), wp.getName());
        else
            Msg.HOME_WP_CREATED.sendTo(p, waypointName);

        updatePlayerOnHomeCount(p, pd);

        return true;
    }

    private void updatePlayerOnHomeCount(Player p, PlayerData pd) {
        int maxHomeWaypoints = DataManager.getManager().MAX_HOME_WAYPOINTS - pd.getAllWaypoints().size();

        if (maxHomeWaypoints > 0)
            Msg.HOME_WP_REMAINING.sendTo(p, maxHomeWaypoints);
        else
            Msg.HOME_WP_FULL.sendTo(p, maxHomeWaypoints);
    }

}
