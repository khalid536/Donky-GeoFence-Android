package net.donky.location.geofence.geocore;

import android.app.Application;
import android.location.Location;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Igor Bykov
 * 20/10/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class GeoFenceProcessor {

    /**
     * Flag set to true after init() method call is completed
     */
    private static final AtomicBoolean initialised = new AtomicBoolean(false);

    /**
     * Private constructor. Prevents instantiation from other classes.
     */
    private GeoFenceProcessor() {}

    /**
     * Initializes singleton.
     * <p/>
     * SingletonHolder is loaded on the first execution of Singleton.getInstance()
     * or the first access to SingletonHolder.INSTANCE, not before.
     */
    private static class SingletonHolder {
        private static final GeoFenceProcessor INSTANCE = new GeoFenceProcessor();
    }

    /**
     * Get instance of Donky Analytics singleton.
     *
     * @return Static instance of Donky Analytics singleton.
     */
    public static GeoFenceProcessor getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private static ControlRegionManager controlRegionManager;
    private static GeoFenceManager geoFenceManager;

    public static void init(Application application) {
        if (!initialised.get()) {
            controlRegionManager = new ControlRegionManager(application);
            geoFenceManager = new GeoFenceManager(application);
            initialised.set(true);
        }
    }

    public static synchronized void locationUpdate(Location location){
        if (controlRegionManager.processControlRegion(location)) {
            return;
        }
        geoFenceManager.processLocation(location);
    }

    public static void stopControlRegion(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                controlRegionManager.stopTrackingControlRegion();
            }
        }).start();
    }

}

