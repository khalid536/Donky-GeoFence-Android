package net.donky.location.geofence.internal;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import net.donky.location.geofence.DonkyGeoFence;

/**
 * Created by Marcin Swierczek
 * 02/06/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class BootCompletedBroadcastReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(DonkyGeoFence.TAG, "BootCompletedBroadcastReceiver");

    }

}
