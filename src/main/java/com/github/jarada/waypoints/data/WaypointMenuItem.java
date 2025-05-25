package com.github.jarada.waypoints.data;

import com.github.jarada.waypoints.util.Util;

public class WaypointMenuItem {

    private Waypoint waypoint;
    private Category category;
    private boolean discoverMode;

    public WaypointMenuItem(Waypoint waypoint) {
        this.waypoint = waypoint;
        this.discoverMode = false;
    }

    public WaypointMenuItem(Waypoint waypoint, boolean discoverMode) {
        this.waypoint = waypoint;
        this.discoverMode = discoverMode;
    }

    public WaypointMenuItem(Category category) {
        this.category = category;
    }

    public Waypoint getWaypoint() {
        return waypoint;
    }

    public String getWaypointKey() {
        return waypoint != null ? Util.getKey(waypoint.getName()) : null;
    }

    public Category getCategory() {
        return category;
    }

    public boolean isDiscoverMode() {
        return discoverMode;
    }

    public boolean isCategory() {
        return category != null;
    }
}
