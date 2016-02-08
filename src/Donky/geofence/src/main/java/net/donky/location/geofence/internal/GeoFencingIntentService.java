package net.donky.location.geofence.internal;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.os.*;
import android.util.Log;

import com.google.android.gms.location.LocationResult;

import net.donky.location.geofence.geocore.GeoFenceProcessor;

import java.util.Date;

/**
 * Created by Igor Bykov
 * 21/09/2015.
 * Listens for geofence transition changes.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class GeoFencingIntentService extends IntentService {

    private static final String TAG = GeoFencingIntentService.class.getSimpleName();


    public GeoFencingIntentService(){
        super("GeoFencingIntentService");
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
    }

    /**
     * Handles incoming intents.
     * @param intent The Intent sent by Location Services. This Intent is provided to Location
     * Services (inside a PendingIntent) when addGeofences() is called.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        Location location = LocationResult.extractResult(intent).getLastLocation();
        if (location != null) {
            GeoFenceProcessor.locationUpdate(location);
            Log.d(TAG, "Completed GeoFencingIntentService @ " + new Date(SystemClock.elapsedRealtime()).toString());
            TransitionsBroadcastReceiver.completeWakefulIntent(intent);
        }
    }



}
