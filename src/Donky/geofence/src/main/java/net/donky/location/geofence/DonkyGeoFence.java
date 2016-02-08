package net.donky.location.geofence;

import android.app.Application;
import android.util.Log;

import net.donky.core.DonkyCore;
import net.donky.core.DonkyException;
import net.donky.core.DonkyListener;
import net.donky.core.ModuleDefinition;
import net.donky.core.NotificationBatchListener;
import net.donky.core.Subscription;
import net.donky.core.events.ApplicationStopEvent;
import net.donky.core.events.DonkyEventListener;
import net.donky.core.network.ServerNotification;
import net.donky.location.DonkyLocation;
import net.donky.location.geofence.geocore.GeoFenceProcessor;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Marcin Swierczek
 * 18/05/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class DonkyGeoFence {

    // The following SDK versioning strategy must be adhered to; the strategy allows the SDK version to communicate what the nature of the changes are between versions.
    // 1 - Major version number, increment for breaking changes.
    // 2 - Minor version number, increment when adding new functionality.
    // 3 - Major bug fix number, increment every 100 bugs.
    // 4 - Minor bug fix number, increment every bug fix, roll back when reaching 99.
    private final String version = "2.0.0.0";

    public static final String TAG = DonkyGeoFence.class.getSimpleName();

    private static String TRACKING_SQLITE_HELPER = "TrackingSQLiteHelper";


    private GeoNotificationHandler geoNotificationHandler;
    /**
     * Flag set to true after init() method call is completed
     */
    private static final AtomicBoolean initialised = new AtomicBoolean(false);

    /**
     * Private constructor. Prevents instantiation from other classes.
     */
    private DonkyGeoFence() {

    }

    /**
     * Initializes singleton.
     * <p/>
     * SingletonHolder is loaded on the first execution of Singleton.getInstance()
     * or the first access to SingletonHolder.INSTANCE, not before.
     */
    private static class SingletonHolder {
        private static final DonkyGeoFence INSTANCE = new DonkyGeoFence();
    }

    /**
     * Get instance of Donky Analytics singleton.
     *
     * @return Static instance of Donky Analytics singleton.
     */
    public static DonkyGeoFence getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * Initialise Donky Automation Module.
     *
     * @param donkyListener The callback to invoke when the Module is initialised.
     */
    public static void initialiseDonkyGeoFences(final Application application, final DonkyListener donkyListener) {

        getInstance().init(application, donkyListener);

    }

    private void init(final Application application, final DonkyListener donkyListener) {
        if (!initialised.get()) {

            try {

                        DonkyLocation.initialiseDonkyLocation(application, new DonkyListener() {

                            @Override
                            public void success() {

                                GeoFenceProcessor.init(application);

                                DonkyGeoFenceController.getInstance().init(application);

                                geoNotificationHandler = new GeoNotificationHandler();


                                DonkyCore.registerModule(new ModuleDefinition(DonkyGeoFence.class.getSimpleName(), version));

                                List<Subscription<ServerNotification>> serverNotificationSubscriptions = new LinkedList<>();

                                serverNotificationSubscriptions.add(new Subscription<>(ServerNotification.NOTIFICATION_START_TRACKING_LOCATION,
                                        new NotificationBatchListener<ServerNotification>() {

                                            @Override
                                            public void onNotification(ServerNotification notification) {

                                            }

                                            @Override
                                            public void onNotification(List<ServerNotification> notification) {
                                                Log.d(TAG, ServerNotification.NOTIFICATION_START_TRACKING_LOCATION + " onNotification");
                                                geoNotificationHandler.handleTrackingNotification(notification);
                                            }
                                        }));

                                serverNotificationSubscriptions.add(new Subscription<>(ServerNotification.NOTIFICATION_TRIGGER_CONFIGURATION,
                                        new NotificationBatchListener<ServerNotification>() {

                                            @Override
                                            public void onNotification(ServerNotification notification) {
                                            }

                                            @Override
                                            public void onNotification(List<ServerNotification> notification) {
                                                Log.d(TAG, ServerNotification.NOTIFICATION_TRIGGER_CONFIGURATION + " onNotification");
                                                geoNotificationHandler.handleTrackingNotification(notification);
                                            }
                                        }));

                                serverNotificationSubscriptions.add(new Subscription<>(ServerNotification.NOTIFICATION_STOP_TRACKING_LOCATION,
                                        new NotificationBatchListener<ServerNotification>() {

                                            @Override
                                            public void onNotification(ServerNotification notification) {
                                            }

                                            @Override
                                            public void onNotification(List<ServerNotification> notification) {
                                                Log.d(TAG, ServerNotification.NOTIFICATION_STOP_TRACKING_LOCATION + " onNotification");
                                                geoNotificationHandler.handleTrackingNotification(notification);
                                            }
                                        }));

                                serverNotificationSubscriptions.add(new Subscription<>(ServerNotification.NOTIFICATION_TRIGGER_DELETED,
                                        new NotificationBatchListener<ServerNotification>() {

                                            @Override
                                            public void onNotification(ServerNotification notification) {
                                            }

                                            @Override
                                            public void onNotification(List<ServerNotification> notification) {
                                                Log.d(TAG, ServerNotification.NOTIFICATION_TRIGGER_DELETED + " onNotification");
                                                geoNotificationHandler.handleTrackingNotification(notification);
                                            }
                                        }));

                                DonkyCore.subscribeToDonkyNotifications(
                                        new ModuleDefinition(DonkyGeoFence.class.getSimpleName(), version),
                                        serverNotificationSubscriptions,
                                        true);




                                initialised.set(true);

                                if (donkyListener != null) {
                                    donkyListener.success();
                                }

                            }

                            @Override
                            public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                                if (donkyListener != null) {
                                    donkyListener.error(donkyException, null);
                                }
                            }
                        });

                DonkyCore.subscribeToLocalEvent(new DonkyEventListener<ApplicationStopEvent>(ApplicationStopEvent.class) {

                    @Override
                    public void onDonkyEvent(ApplicationStopEvent event) {
                        DonkyGeoFenceController.getInstance().notifyAppStopped();
                    }

                });

            } catch (Exception e) {

                DonkyException donkyException = new DonkyException("Error initialising DonkyGeoFence Module");
                donkyException.initCause(e);

                if (donkyListener != null) {
                    donkyListener.error(donkyException, null);
                }

            }

        } else {

            if (donkyListener != null) {
                donkyListener.success();
            }

        }
    }
}
