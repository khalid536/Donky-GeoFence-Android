package net.donky.location.geofence;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import net.donky.core.DonkyException;
import net.donky.core.DonkyListener;
import net.donky.core.network.DonkyNetworkController;
import net.donky.core.network.ServerNotification;
import net.donky.core.network.location.GeoFence;
import net.donky.core.network.location.Trigger;
import net.donky.location.geofence.internal.GeoClientNotification;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Handler for received server notification.
 *
 * Created by Igor Bykov
 * 04/09/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class GeoNotificationHandler {

    /**
     * Handler for received server notification.
     *
     * @param serverNotifications Received server notifications.
     */
    public void handleTrackingNotification(List<ServerNotification> serverNotifications) {

        Log.d(DonkyGeoFence.TAG, "handleTrackingNotification");

        List<ServerNotification> notifications = new LinkedList<>();

        Gson gson = new Gson();

        for (ServerNotification serverNotification : serverNotifications) {

            if (ServerNotification.NOTIFICATION_START_TRACKING_LOCATION.equals(serverNotification.getType())) {

                notifications.add(serverNotification);

                JsonObject data = serverNotification.getData();

                final GeoFence geoFence = gson.fromJson(data.get("location").toString(), GeoFence.class);

                DonkyGeoFenceController.getInstance().saveGeoFenceIfNotExist(geoFence);


                DonkyNetworkController.getInstance().sendClientNotification(GeoClientNotification.createStatusLocationNotification(geoFence, true), new DonkyListener() {
                    @Override
                    public void success() {}

                    @Override
                    public void error(DonkyException donkyException, Map<String, String> validationErrors) {}
                });

            }else if (ServerNotification.NOTIFICATION_STOP_TRACKING_LOCATION.equals(serverNotification.getType())){

                notifications.add(serverNotification);

                JsonObject data = serverNotification.getData();

                final GeoFence geoFence = gson.fromJson(data.toString(), GeoFence.class);

                DonkyGeoFenceController.getInstance().deleteGeoFence(geoFence);

            }else if (ServerNotification.NOTIFICATION_TRIGGER_CONFIGURATION.equals(serverNotification.getType())){

                JsonObject data = serverNotification.getData();

                final Trigger trigger = gson.fromJson(data, Trigger.class);

                DonkyGeoFenceController.getInstance().saveTriggerIfNotExist(trigger);


            }else if (ServerNotification.NOTIFICATION_TRIGGER_DELETED.equals(serverNotification.getType())){

                JsonObject data = serverNotification.getData();

                final Trigger trigger = gson.fromJson(data, Trigger.class);

                DonkyGeoFenceController.getInstance().deleteTrigger(trigger);

                notifications.add(serverNotification);
            }
        }


    }
}
