package net.donky.location.geofence.internal;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import net.donky.location.geofence.DonkyGeoFenceController;
import net.donky.core.network.location.GeoFence;
import net.donky.location.geofence.geocore.TriggerManager;

import java.util.List;
import java.util.Random;

public class AlarmBroadcast extends WakefulBroadcastReceiver {

    public static final String TRIGGER_ID = "geofenceId";
    private Context context;

    private TriggerManager triggerManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        String id = intent.getStringExtra(TRIGGER_ID);
        if (id == null)
          return;
        triggerManager = new TriggerManager(context);
        triggerManager.executeDwellGeoFence(id);
    }
}
