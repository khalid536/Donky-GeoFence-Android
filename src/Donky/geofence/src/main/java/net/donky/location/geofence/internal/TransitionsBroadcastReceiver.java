package net.donky.location.geofence.internal;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.SystemClock;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationResult;

/**
 * Created by Igor Bykov
 * 21/09/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class TransitionsBroadcastReceiver extends WakefulBroadcastReceiver {

    private static final String TAG = TransitionsBroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (LocationResult.hasResult(intent)) {
            Intent serviceIntent = new Intent(context, GeoFencingIntentService.class);
            serviceIntent.putExtras(intent);
            Log.d(TAG, "Starting service @ " + SystemClock.elapsedRealtime());
            startWakefulService(context, serviceIntent);
        }
    }

}