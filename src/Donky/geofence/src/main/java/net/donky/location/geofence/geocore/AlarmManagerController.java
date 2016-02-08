package net.donky.location.geofence.geocore;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import net.donky.location.geofence.internal.AlarmBroadcast;
import net.donky.core.network.location.Trigger;

/**
 * Created by Igor Bykov
 * 20/10/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class AlarmManagerController {

    private Context context;
    private android.app.AlarmManager alarmMgr;

    public AlarmManagerController(Context context){
        this.context = context;
        alarmMgr = (android.app.AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
    }

    public void setUpAlarm(Trigger trigger){
        alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() +
                        trigger.getTriggerData().getTimeInRegionSeconds() * 1000,
                getAlarmPendingIntent(trigger.getTriggerId()));
    }

    public void deleteAlarm(Trigger trigger){
        alarmMgr.cancel(getAlarmPendingIntent(trigger.getTriggerId()));
    }

    private PendingIntent getAlarmPendingIntent(String id){
        Intent intent = new Intent(context, AlarmBroadcast.class);
        intent.putExtra(AlarmBroadcast.TRIGGER_ID, id);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }
}
