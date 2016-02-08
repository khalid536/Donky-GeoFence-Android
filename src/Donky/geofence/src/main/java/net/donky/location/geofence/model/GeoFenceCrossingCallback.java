package net.donky.location.geofence.model;

import net.donky.core.network.location.GeoFence;

public interface GeoFenceCrossingCallback {

    void entered(GeoFence geoFence);

    void exited(GeoFence geoFence);
}
