package net.donky.location.geofence.geocore;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.text.format.Time;

import net.donky.core.DonkyException;
import net.donky.core.DonkyListener;
import net.donky.core.helpers.DateAndTimeHelper;
import net.donky.core.network.DonkyNetworkController;
import net.donky.location.geofence.DonkyGeoFenceController;
import net.donky.location.geofence.database.CursorUtill;
import net.donky.location.geofence.database.DatabaseSQLContract;
import net.donky.location.geofence.database.GeoFenceContentProvider;
import net.donky.location.geofence.database.GeoFenceDAO;
import net.donky.location.geofence.analytics.GeoContentProvider;
import net.donky.location.geofence.analytics.AnalyticsTable;
import net.donky.location.geofence.internal.GeoClientNotification;
import net.donky.core.network.location.GeoFence;
import net.donky.core.network.location.Trigger;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by Igor Bykov
 * 20/10/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class TriggerManager {

    private Context context;
    private AlarmManagerController alarmManagerController;

    public TriggerManager(Context context){
        this.context = context;
        alarmManagerController = new AlarmManagerController(context);

    }

    public void executeEnterGeoFence(GeoFence geoFence, Location location){

        sentLocationCrossed(geoFence, "Entering");

        startDwell(geoFence);

        DonkyGeoFenceController.getInstance().notifyGeoFenceCrossedEnterCallback(geoFence);

        String selection = DatabaseSQLContract.TriggerToGeoFencesEntry.COLUMN_NAME_GEOFENCE_ID +
                " = ? and " + DatabaseSQLContract.TriggerToGeoFencesEntry.COLUMN_NAME_TRIGGER_DIRECTION + " = ?";

        List<Trigger> triggers = getTriggerRelatedToGeoFence(selection, new String[]{geoFence.getId(), "EnteringRegion"});

        if (!isValidList(triggers))
            return;

        for (Trigger trigger : triggers) {
            if (isTriggerRestrictionValid(trigger)) {

                sentTriggerExecuted(geoFence, trigger, location, "EnteringRegion");
                DonkyGeoFenceController.getInstance().notifyTriggerFiredCallback(trigger);
                updateTriggerExecutionCountAndLastExecutionTime(trigger);

            }
        }

    }

    public void executeExitGeoFence(GeoFence geoFence, Location location){

        sentLocationCrossed(geoFence, "Exiting");

        stopDwell(geoFence);

        DonkyGeoFenceController.getInstance().notifyGeoFenceExitEnterCallback(geoFence);

        String selection = DatabaseSQLContract.TriggerToGeoFencesEntry.COLUMN_NAME_GEOFENCE_ID +
                " = ? and " + DatabaseSQLContract.TriggerToGeoFencesEntry.COLUMN_NAME_TRIGGER_DIRECTION + " = ?";

        List<Trigger> triggers = getTriggerRelatedToGeoFence(selection, new String[]{geoFence.getId(), "LeavingRegion"});

        if (!isValidList(triggers))
            return;

        for (Trigger trigger : triggers) {
            if (isTriggerRestrictionValid(trigger)) {
                sentTriggerExecuted(geoFence, trigger, location, "LeavingRegion");
                updateTriggerExecutionCountAndLastExecutionTime(trigger);
            }
        }

    }

    public void executeDwellGeoFence(String triggerId){
        Cursor cursor = context.getContentResolver().query(GeoFenceContentProvider.getTriggerUri(context),
                GeoFenceDAO.allColumnTrigger,
                DatabaseSQLContract.TriggerEntry.COLUMN_NAME_SERVER_ID + " = ? ",
                new String[]{triggerId},
                null);
        if (cursor == null || !cursor.moveToFirst() || cursor.getCount() == 0)
            return;
        Trigger trigger = CursorUtill.cursorToTrigger(cursor);
        if (isTriggerRestrictionValid(trigger)){
            sentTriggerExecuted(trigger, "TimeInside");
            DonkyGeoFenceController.getInstance().notifyTriggerFiredCallback(trigger);
            updateTriggerExecutionCountAndLastExecutionTime(trigger);
        }
    }

    private boolean isValidList(List list){
        return list != null && !list.isEmpty();
    }


    private void startDwell(GeoFence geoFence){
        String selection = DatabaseSQLContract.TriggerToGeoFencesEntry.COLUMN_NAME_GEOFENCE_ID + " = ? and " + DatabaseSQLContract.TriggerToGeoFencesEntry.COLUMN_NAME_TRIGGER_CONDITION + " = ?";
        List<Trigger> triggers = getTriggerRelatedToGeoFence(selection, new String[]{geoFence.getId(), "TimeInside"});

        if (!isValidList(triggers))
            return;

        for (Trigger trigger : triggers) {
            if (isTriggerRestrictionValid(trigger))
                alarmManagerController.setUpAlarm(trigger);
        }
    }

    private void stopDwell(GeoFence geoFence){
        String selection = DatabaseSQLContract.TriggerToGeoFencesEntry.COLUMN_NAME_GEOFENCE_ID + " = ? and "
                + DatabaseSQLContract.TriggerToGeoFencesEntry.COLUMN_NAME_TRIGGER_CONDITION + " = ? and "
                + DatabaseSQLContract.TriggerToGeoFencesEntry.COLUMN_NAME_EXIT_TIME + " < "
                + DatabaseSQLContract.TriggerToGeoFencesEntry.COLUMN_NAME_ENTER_TIME;
        List<Trigger> triggers = getTriggerRelatedToGeoFence(selection, new String[]{geoFence.getId(), "TimeInside"});


        if (triggers == null || triggers.isEmpty())
            return;

        for (Trigger trigger : triggers)
            alarmManagerController.deleteAlarm(trigger);
    }

    private List<Trigger> getTriggerRelatedToGeoFence(String selection, String[] selectionArgs){
        Cursor cursor = context.getContentResolver().query(GeoFenceContentProvider.getTriggerToGeofenceUri(context), GeoFenceDAO.allColumnTriggersToGeoFences, selection, selectionArgs, null);
        List<Trigger> triggers = DonkyGeoFenceController.getInstance().getTriggerRelatedToGeoFences(cursor);
        if (cursor != null)
            cursor.close();
        return triggers;
    }

    private boolean isTriggerRestrictionValid(Trigger trigger){
        if (trigger == null)
            return false;

        if (trigger.getValidity() != null && trigger.getValidity().getValidFrom() != null && trigger.getValidity().getValidTo() != null) {
            if (DateAndTimeHelper.parseUtcDate(trigger.getValidity().getValidTo()).getTime() < getCurrentTime())
                return false;
        }

        int count = trigger.getRestrictions().getExecutorCount();

        if (trigger.getRestrictions().getMaximumExecutions() != 0 && count + 1 > trigger.getRestrictions().getMaximumExecutions())
            return false;


        return !((trigger.getLastExecutionTime() + 60000 > getCurrentTime()) ||

                (trigger.getRestrictions().getMaximumExecutionsPerInterval() != 0 &&

                        trigger.getRestrictions().getMaximumExecutionsPerInterval() <
                                trigger.getRestrictions().getExecutorCountPerInterval()));

    }

    DateFormat formatter = new SimpleDateFormat("yyyy/MMM/dd HH:mm:ss");

    private void sentLocationCrossed(final GeoFence geoFence, final String direction){
        DonkyNetworkController.getInstance().sendClientNotification(GeoClientNotification.createLocationCrossedNotification(geoFence, direction), new DonkyListener() {
            @Override
            public void success() {

                ContentValues values = new ContentValues();
                Date date = new Date(getCurrentTime());
                values.put(AnalyticsTable.COLUMN_TIME, formatter.format(date) );
                values.put(AnalyticsTable.COLUMN_DESCRIPTION, geoFence.getName() + " " + direction);
                context.getContentResolver().insert(GeoContentProvider.getContentUri(context), values);
            }

            @Override
            public void error(DonkyException donkyException, Map<String, String> validationErrors) {

            }
        });

    }

    private void sentTriggerExecuted(final GeoFence geoFence, final Trigger trigger, final Location location, String direction){
        DonkyNetworkController.getInstance().sendClientNotification(GeoClientNotification.createTriggerExecutedNotification(geoFence, trigger, location, direction), new DonkyListener() {
            @Override
            public void success() {
                updateTriggerExecutionCountAndLastExecutionTime(trigger);
                ContentValues values = new ContentValues();
                Date date = new Date(getCurrentTime());
                values.put(AnalyticsTable.COLUMN_TIME, formatter.format(date) );
                values.put(AnalyticsTable.COLUMN_DESCRIPTION, "Trigger Executed");
                context.getContentResolver().insert(GeoContentProvider.getContentUri(context), values);
            }

            @Override
            public void error(DonkyException donkyException, Map<String, String> validationErrors) {

            }
        });

    }

    private void sentTriggerExecuted(final Trigger trigger, String direction){
        DonkyNetworkController.getInstance().sendClientNotification(GeoClientNotification.createTriggerExecutedNotification(trigger, direction), new DonkyListener() {
            @Override
            public void success() {
                updateTriggerExecutionCountAndLastExecutionTime(trigger);
                ContentValues values = new ContentValues();
                Date date = new Date(getCurrentTime());
                values.put(AnalyticsTable.COLUMN_TIME, formatter.format(date) );
                values.put(AnalyticsTable.COLUMN_DESCRIPTION, "Trigger Executed");
                context.getContentResolver().insert(GeoContentProvider.getContentUri(context), values);
            }

            @Override
            public void error(DonkyException donkyException, Map<String, String> validationErrors) {

            }
        });

    }


    private void updateTriggerExecutionCountAndLastExecutionTime(Trigger trigger){
        ContentValues contentValues = new ContentValues(1);
        int count = trigger.getRestrictions().getExecutorCount() + 1;
        contentValues.put(DatabaseSQLContract.TriggerEntry.COLUMN_NAME_EXECUTION_COUNT, count);
        contentValues.put(DatabaseSQLContract.TriggerEntry.COLUMN_NAME_LAST_EXECUTION_TIME, getCurrentTime());
        String selection = DatabaseSQLContract.TriggerEntry.COLUMN_NAME_SERVER_ID + " = ?";
        //TODO check
        if (getCurrentTime() - trigger.getLastExecutionTime() > trigger.getRestrictions().getMaximumExecutionsIntervalSeconds())
            contentValues.put(DatabaseSQLContract.TriggerEntry.COLUMN_NAME_EXECUTION_COUNT_PER_INTERVAL, 0);
        else
            contentValues.put(DatabaseSQLContract.TriggerEntry.COLUMN_NAME_LAST_EXECUTION_TIME, trigger.getRestrictions().getExecutorCountPerInterval() + 1);
        context.getContentResolver().update(GeoFenceContentProvider.getTriggerUri(context), contentValues, selection, new String[]{trigger.getTriggerId()});
    }

    private void updateGeoFenceEnterTime(String type, String geoFenceId){
        String selection = DatabaseSQLContract.TriggerToGeoFencesEntry.COLUMN_NAME_GEOFENCE_ID + " = ? and " + DatabaseSQLContract.TriggerToGeoFencesEntry.COLUMN_NAME_TRIGGER_CONDITION + " = ?";
        List<Trigger> triggers = getTriggerRelatedToGeoFence(selection, new String[]{geoFenceId, "TimeInside"});
        ContentValues contentValues = new ContentValues(1);
        if (type.equals("EnteringRegion")){
            contentValues.put(DatabaseSQLContract.TriggerToGeoFencesEntry.COLUMN_NAME_ENTER_TIME, getCurrentTime());
        }else if (type.equals("LeavingRegion")){
            contentValues.put(DatabaseSQLContract.TriggerToGeoFencesEntry.COLUMN_NAME_EXIT_TIME, getCurrentTime());
        }
        String where = DatabaseSQLContract.TriggerToGeoFencesEntry.COLUMN_NAME_GEOFENCE_ID + " = ? and "
                + DatabaseSQLContract.TriggerToGeoFencesEntry.COLUMN_NAME_TRIGGER_ID + " = ? and "
                + DatabaseSQLContract.TriggerToGeoFencesEntry.COLUMN_NAME_TRIGGER_CONDITION + " = ?";

        if (triggers == null || triggers.isEmpty())
            return;

        for (Trigger trigger : triggers)
            context.getContentResolver().update(GeoFenceContentProvider.getTriggerToGeofenceUri(context), contentValues, where, new String[]{geoFenceId, trigger.getTriggerId(), "TimeInside"});
    }

    private long getCurrentTime(){
        Time time = new Time();
        time.setToNow();
        return time.toMillis(false);
    }

}
