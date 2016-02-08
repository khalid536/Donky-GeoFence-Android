package net.donky.location.geofence.internal;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import net.donky.location.geofence.DonkyGeoFence;
import net.donky.location.geofence.geocore.GeoFenceProcessor;

/**
 * Created by Igor Bykov
 * 21/09/2015.
 * Listens for geofence transition changes.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class GeofenceTransitionsIntentService extends IntentService {

    private static final String TAG = GeofenceTransitionsIntentService.class.getSimpleName();

    public GeofenceTransitionsIntentService(){
        super("GeofenceTransitionsIntentService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    /**
     * Handles incoming intents.
     * @param intent The Intent sent by Location Services. This Intent is provided to Location
     * Services (inside a PendingIntent) when addGeofences() is called.
     */
    @Override
    protected void onHandleIntent(Intent intent) {


        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            Log.d(DonkyGeoFence.TAG, "geofencingEvent.hasError()");
            return;
        }
        int geofenceTransition = geofencingEvent.getGeofenceTransition();
        Log.d(DonkyGeoFence.TAG, "geofenceTransition " + geofenceTransition);
        for (Geofence geofence : geofencingEvent.getTriggeringGeofences()) {
            Log.d(DonkyGeoFence.TAG, geofence.getRequestId());
            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            }else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {
            }else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                update(geofencingEvent.getTriggeringLocation());
            }

        }
    }

    private void update(Location location){

        GeoFenceProcessor.locationUpdate(location);
    }
}
