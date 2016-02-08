package net.donky.location.geofence.model;

import com.google.gson.annotations.SerializedName;

import net.donky.core.network.location.GeoFence;

public class LocationData {

    @SerializedName("location")
    private GeoFence location;

    public LocationData(){}

    public LocationData(GeoFence geoFence) {
        this.location = geoFence;
    }

    public GeoFence getLocation() {
        return location;
    }

    public void setLocation(GeoFence geoFence) {
        this.location = geoFence;
    }

    @Override
    public String toString() {
        return "LocationData{" +
                "location=" + location +
                '}';
    }
}
