package net.donky;

import android.app.Application;
import android.util.Log;

import net.donky.core.DonkyCore;
import net.donky.core.DonkyException;
import net.donky.core.DonkyListener;
import net.donky.core.analytics.DonkyAnalytics;
import net.donky.core.messaging.push.DonkyPush;
import net.donky.core.messaging.rich.inbox.ui.DonkyRichInboxUI;
import net.donky.location.geofence.DonkyGeoFence;

import java.util.Map;

/**
 * Application class
 *
 * Created by Marcin Swierczek
 * 07/04/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class Donky extends Application {

    private static final String TAG = "DonkyTestApp";

    @Override
    public void onCreate()
    {
        super.onCreate();

        /* Initialise Donky Modules before Core */

        // Initialise Donky Analytics Module
        DonkyAnalytics.initialiseAnalytics(this, new DonkyListener() {

            @Override
            public void success() {
                Log.i(TAG, "Donky Analytics initialised");
            }

            @Override
            public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                Log.e(TAG, "Donky Analytics error", donkyException);
            }

        });

        // Initialise Donky Simple Push Module
        DonkyPush.initialiseDonkyPush(this, true, new DonkyListener() {
            @Override
            public void success() {
                Log.i(TAG, "Donky Push initialised");
            }

            @Override
            public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                Log.e(TAG, "Donky Push error", donkyException);
            }
        });

        // Initialise Donky Rich UI Module
        DonkyRichInboxUI.initialiseDonkyRich(this, new DonkyListener() {

            @Override
            public void success() {
                Log.i(TAG, "Donky Rich Messaging initialised");
            }

            @Override
            public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                Log.e(TAG, "Donky Rich Messaging error", donkyException);
            }

        });

        DonkyGeoFence.initialiseDonkyGeoFences(this, new DonkyListener() {
            @Override
            public void success() {
                Log.i(TAG, "GeoFences initialised");
            }

            @Override
            public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                Log.e(TAG, "GeoFences error", donkyException);
            }
        });

        // Initialise Donky Core SDK
        DonkyCore.initialiseDonkySDK(this, "PUT_YOUR_API_KEY_HERE", new DonkyListener() {

            @Override
            public void success() {
                Log.i(TAG, "Donky Core initialised");
            }

            @Override
            public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                Log.e(TAG, "Donky Core error", donkyException);
            }
        });

    }
}