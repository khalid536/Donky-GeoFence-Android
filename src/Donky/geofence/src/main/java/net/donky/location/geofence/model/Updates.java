package net.donky.location.geofence.model;

public interface Updates<T> {
    void insert(T object);
    void update(T object);
    void delete(T object);
}
