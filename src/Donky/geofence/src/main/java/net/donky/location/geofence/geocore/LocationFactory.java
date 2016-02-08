package net.donky.location.geofence.geocore;

import com.google.android.gms.location.LocationRequest;

public class LocationFactory {

    public static LocationRequest createLocationRequest(long interval, long fastestInterval, float smallestDisplacement, int priority){
        LocationRequest request = new LocationRequest();
        request.setInterval(interval);
        request.setFastestInterval(fastestInterval);
        request.setSmallestDisplacement(smallestDisplacement);
        request.setPriority(priority);
        return request;
    }
}
