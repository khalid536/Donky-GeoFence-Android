package net.donky.location.geofence.model;

public interface DonkyGeoFenceDataCallback<T> {

    void success(T data);
    void error(String message);
}
