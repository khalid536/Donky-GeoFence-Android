package net.donky.location.geofence.model;

import net.donky.core.helpers.IdHelper;

/**
 * Created by Marcin Swierczek
 * 22/05/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class DonkyGeoFenceModel {

    private String internalId;

    private String geoFenceId;

    private double latitude;

    private double longitude;

    private float radiusMeters;

    public DonkyGeoFenceModel(String geoFenceId, double latitude, double longitude, float radiusMeters) {

        this.internalId = IdHelper.generateId();
        this.geoFenceId = geoFenceId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radiusMeters = radiusMeters;

    }

    public DonkyGeoFenceModel(String internalId, String geoFenceId, double latitude, double longitude, float radiusMeters) {

        this.internalId = internalId;
        this.geoFenceId = geoFenceId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radiusMeters = radiusMeters;

    }

    public DonkyGeoFenceModel(DonkyGeoFenceModel donkyGeoFence) {
        this.internalId = donkyGeoFence.getInternalId();
        this.geoFenceId = donkyGeoFence.getGeoFenceId();
        this.latitude = donkyGeoFence.getLatitude();
        this.longitude = donkyGeoFence.getLongitude();
        this.radiusMeters = donkyGeoFence.getRadiusMeters();
    }

    public String getGeoFenceId() {
        return geoFenceId;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public float getRadiusMeters() {
        return radiusMeters;
    }

    public String getInternalId() {
        return internalId;
    }

    @Override
    public String toString() {
        return "DonkyGeoFence [Internal ID: " + internalId + "; GeoFence ID: " + geoFenceId + "; Lat: " + latitude + "; Long: " + longitude + "; Radius: " + radiusMeters+"] ";
    }
}
