package net.donky.location.geofence.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import net.donky.core.logging.DLog;
import net.donky.core.network.location.GeoFence;
import net.donky.core.network.location.LocationPoint;
import net.donky.core.network.location.Restrictions;
import net.donky.core.network.location.Trigger;
import net.donky.core.network.location.TriggerData;
import net.donky.core.network.location.Validity;
import net.donky.location.geofence.DonkyGeoFence;
import net.donky.location.geofence.model.ControlRegion;
import net.donky.location.geofence.model.DonkyGeoFenceDataCallback;
import net.donky.location.geofence.model.TriggerToGeoFence;
import net.donky.location.geofence.model.TriggerToGeoFences;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.hsr.geohash.GeoHash;

/**
 * Database Access Object for GeoFence.
 * Created by Igor Bykov
 * 04/09/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class GeoFenceDAO {

    private final DLog log;

    public static String[] allColumnsGeoFence = {
            DatabaseSQLContract.GeoFenceLocationEntry._ID,
            DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_SERVER_ID,
            DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_APPLICATION_ID,
            DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_NAME,
            DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_RADIUS,
            DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_INTERNAL_ID,
            DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_LABELS,
            DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_STATUS,
            DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_TYPE,
            DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_LONGITUDE,
            DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_LATITUDE,
            DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_HASH,
            DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_CREATED_ON,
            DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_UPDATED_ON,
            DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_ACTIVATION_ID,
            DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_IS_ACTIVATE,
            DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_ACTIVATED_ON,
            DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_RELATED_TRIGGER_COUNT,
            DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_BOARDER_DISTANCE_TO_CURRENT_LOCATION,
            DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_REAL_BOARDER_DISTANCE_TO_CURRENT_LOCATION,
            DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_IS_INSIDE,
            DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_LAST_ENTER,
            DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_LAST_EXIT
    };

    public static String[] allColumnTrigger = {
            DatabaseSQLContract.TriggerEntry._ID,
            DatabaseSQLContract.TriggerEntry.COLUMN_NAME_SERVER_ID,
            DatabaseSQLContract.TriggerEntry.COLUMN_NAME_ACTIVATION_ID,
            DatabaseSQLContract.TriggerEntry.COLUMN_NAME_INTERNAL_ID,
            DatabaseSQLContract.TriggerEntry.COLUMN_NAME_TRIGGER_TYPE,
            DatabaseSQLContract.TriggerEntry.COLUMN_NAME_TRIGGER_CONDITION,
            DatabaseSQLContract.TriggerEntry.COLUMN_NAME_TRIGGER_DIRECTION,
            DatabaseSQLContract.TriggerEntry.COLUMN_NAME_VALID_FROM,
            DatabaseSQLContract.TriggerEntry.COLUMN_NAME_VALID_TO,
            DatabaseSQLContract.TriggerEntry.COLUMN_NAME_MAXIMUM_EXECUTION,
            DatabaseSQLContract.TriggerEntry.COLUMN_NAME_MAXIMUM_EXECUTION_PER_INTERVAL,
            DatabaseSQLContract.TriggerEntry.COLUMN_NAME_MAXIMUM_EXECUTION_PER_INTERVAL_MILLIS,
            DatabaseSQLContract.TriggerEntry.COLUMN_NAME_MAXIMUM_EXECUTION_PER_INTERVAL_SECONDS,
            DatabaseSQLContract.TriggerEntry.COLUMN_NAME_EXECUTED_OVERALL,
            DatabaseSQLContract.TriggerEntry.COLUMN_NAME_TIME_IN_REGION,
            DatabaseSQLContract.TriggerEntry.COLUMN_NAME_EXECUTION_COUNT,
            DatabaseSQLContract.TriggerEntry.COLUMN_NAME_EXECUTION_COUNT_PER_INTERVAL,
            DatabaseSQLContract.TriggerEntry.COLUMN_NAME_LAST_EXECUTION_TIME
    };

    public static String[] allColumnTriggersToGeoFences = {
            DatabaseSQLContract.TriggerToGeoFencesEntry._ID,
            DatabaseSQLContract.TriggerToGeoFencesEntry.COLUMN_NAME_TRIGGER_ID,
            DatabaseSQLContract.TriggerToGeoFencesEntry.COLUMN_NAME_GEOFENCE_ID,
            DatabaseSQLContract.TriggerToGeoFencesEntry.COLUMN_NAME_TRIGGER_CONDITION,
            DatabaseSQLContract.TriggerToGeoFencesEntry.COLUMN_NAME_TRIGGER_DIRECTION,
            DatabaseSQLContract.TriggerToGeoFencesEntry.COLUMN_NAME_TRIGGER_TIME_IN_REGION,
            DatabaseSQLContract.TriggerToGeoFencesEntry.COLUMN_NAME_ENTER_TIME,
            DatabaseSQLContract.TriggerToGeoFencesEntry.COLUMN_NAME_EXIT_TIME,
            DatabaseSQLContract.TriggerToGeoFencesEntry.COLUMN_NAME_DWELL_TIME
    };

    public static String[] allControlRegion = {
            DatabaseSQLContract.ControlRegionEntry._ID,
            DatabaseSQLContract.ControlRegionEntry.COLUMN_NAME_INTERNAL_ID,
            DatabaseSQLContract.ControlRegionEntry.COLUMN_NAME_LATITUDE,
            DatabaseSQLContract.ControlRegionEntry.COLUMN_NAME_LONGITUDE,
            DatabaseSQLContract.ControlRegionEntry.COLUMN_NAME_RADIUS,
            DatabaseSQLContract.ControlRegionEntry.COLUMN_NAME_LAST_EXECUTION_TIME
    };

    /**
     * Database Access Object for Donky Client Notifications.
     *
     * @param databaseSQLHelper Helper class instance to translate to SQL
     */

    private GeoAsyncQueryHandler asyncQueryHandler;
//    private SQLiteOpenHelper databaseSQLHelper;
    private Context context;

    public GeoFenceDAO(Context context) {
        this.context = context;
//        FIXME remove databaseSQLHelper from here, everything should be with content resolver
//        this.databaseSQLHelper = GeoFenceSQLiteHelper.getSQLiteOpenHelper();
        log = new DLog("GeoFenceDAO");
        asyncQueryHandler = new GeoAsyncQueryHandler(context);
    }

    /**
     * Get geoFenceLocation with given internal id. Internal id is known only by the client SDK, not on the network.
     *
     * @param id geoFenceLocation internal id.
     * @return GeoFence with given internal id.
     */
    public GeoFence getGeoFenceLocation(String id) {
        Cursor cursor = context.getContentResolver().query(GeoFenceContentProvider.getGeofenceUri(context),
                allColumnsGeoFence,
                DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_SERVER_ID + " = ?",
                new String[]{id},
                null);
        cursor.moveToFirst();
        if (cursor.getCount() == 0)
            return null;
        GeoFence geoFence = cursorToGeoFenceLocation(cursor);
        cursor.close();
        return geoFence;
    }

    public void deleteAllGeoFence(){
        new GeoAsyncQueryHandler(context).setQueryHandlerDeleteListener(new GeoAsyncQueryHandler.QueryHandlerDeleteListener() {
            @Override
            public void onDeleteComplete(int token, Object cookie, int result) {

            }
        }).startDelete(1, null, GeoFenceContentProvider.getGeofenceUri(context), null, null);
    }

    public void deleteAllTriggers(){
        new GeoAsyncQueryHandler(context).setQueryHandlerDeleteListener(new GeoAsyncQueryHandler.QueryHandlerDeleteListener() {
            @Override
            public void onDeleteComplete(int token, Object cookie, int result) {

            }
        }).startDelete(1, null, GeoFenceContentProvider.getTriggerUri(context), null, null);
    }

    /**
     * Get Trigger with given internal id. Internal id is known only by the client SDK, not on the network.
     *
     * @param id trigger internal id.
     * @return Trigger with given internal id.
     */
    public Trigger getTrigger(String id) {
        Cursor cursor = context.getContentResolver().query(GeoFenceContentProvider.getTriggerUri(context),
                allColumnTrigger,
                DatabaseSQLContract.TriggerEntry.COLUMN_NAME_SERVER_ID + " = ? ",
                new String[]{id},
                null);
        if (!cursor.moveToFirst() || cursor.getCount() == 0)
            return null;
        return cursorToTrigger(cursor);
    }

    /**
     * Add single Donky client tracking to database.
     *
     * @param database            SQLite Database.
     * @param geoFence    Tracking to be saved.
     */
    private synchronized void saveGeoFenceLocation(SQLiteDatabase database, GeoFence geoFence) {
        if (geoFence == null || geoFence.getCentrePoint() == null)
            return;
        Log.d(DonkyGeoFence.TAG, "saveGeoFenceLocation" + geoFence.toString());
        long insertIdLocation = database.insert(DatabaseSQLContract.GeoFenceLocationEntry.TABLE_NAME, null, putGeoFenceToContentValue(geoFence));
    }

    /**
     * Add GeoFence to database.
     *
     * @param geoFence GeoFence to be saved.
     */

    public void insertOrUpdateGeoFence(final GeoFence geoFence) {
        if (geoFence == null || geoFence.getCentrePoint() == null)
            return;
        ContentValues values =  putGeoFenceToContentValue(geoFence);
        new GeoAsyncQueryHandler(context).setQueryHandlerInsertListener(new GeoAsyncQueryHandler.QueryHandlerInsertListener() {
            @Override
            public void onInsertComplete(int token, Object cookie, Uri uri) {
                if (uri.getLastPathSegment().equals("-1"))
                    updateGeoFence(geoFence);
            }
        }).startInsert(1, null, GeoFenceContentProvider.getGeofenceUri(context), values);
    }



    private void updateGeoFence(final GeoFence geoFence) {
        if (geoFence == null || geoFence.getCentrePoint() == null)
            return;
        ContentValues values = putGeoFenceToContentValue(geoFence);
        new GeoAsyncQueryHandler(context).setQueryHandlerUpdateListener(new GeoAsyncQueryHandler.QueryHandlerUpdateListener() {
            @Override
            public void onUpdateComplete(int token, Object cookie, int result) {
            }
        }).startUpdate(1, null, GeoFenceContentProvider.getGeofenceUri(context), values,
                DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_SERVER_ID + " = ?", new String[]{geoFence.getId()});
    }


    private ContentValues putGeoFenceToContentValue(GeoFence geoFence){
        ContentValues valuesLocation = new ContentValues();
        valuesLocation.put(DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_SERVER_ID, geoFence.getId());
        valuesLocation.put(DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_APPLICATION_ID, geoFence.getApplicationId());
        valuesLocation.put(DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_NAME, geoFence.getName());
        valuesLocation.put(DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_LABELS, convertArrayToString(geoFence.getLabels()));
        valuesLocation.put(DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_STATUS, geoFence.getStatus());
        valuesLocation.put(DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_RADIUS, geoFence.getRadiusMetres());
        valuesLocation.put(DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_TYPE, geoFence.getType());
        valuesLocation.put(DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_CREATED_ON, geoFence.getCreatedOn());
        valuesLocation.put(DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_UPDATED_ON, geoFence.getUpdatedOn());
        valuesLocation.put(DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_ACTIVATION_ID, geoFence.getActivationId());
        valuesLocation.put(DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_LATITUDE, geoFence.getCentrePoint().getLatitude());
        valuesLocation.put(DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_LONGITUDE, geoFence.getCentrePoint().getLongitude());
        valuesLocation.put(DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_RELATED_TRIGGER_COUNT, geoFence.getRelatedTriggers() == null ? 0 :
                geoFence.getRelatedTriggers().length);
        valuesLocation.put(DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_REAL_BOARDER_DISTANCE_TO_CURRENT_LOCATION, geoFence.getRealDistanceToBorder());
        valuesLocation.put(DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_HASH,
                GeoHash.withCharacterPrecision(geoFence.getCentrePoint().getLatitude(), geoFence.getCentrePoint().getLongitude(), 12).toBase32());
        return valuesLocation;
    }

    /**
     * Delete single Donky client GeoFence from database.
     *
     * @param geoFence    GeoFence to be deleted.
     */
    public void deleteGeoFence(final GeoFence geoFence) {
        if (geoFence == null || geoFence.getId() == null)
            return;
        Log.d(DonkyGeoFence.TAG, "deleteGeoFence" + geoFence.toString());
        new GeoAsyncQueryHandler(context).setQueryHandlerDeleteListener(new GeoAsyncQueryHandler.QueryHandlerDeleteListener() {
            @Override
            public void onDeleteComplete(int token, Object cookie, int result) {
                if (result == 1) {
                    Log.d(DonkyGeoFence.TAG, "deleteGeoFence");
                } else {
                    Log.d(DonkyGeoFence.TAG, "deleteGeoFence");

                }
            }
        }).startDelete(1, null, GeoFenceContentProvider.getGeofenceUri(context), DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_SERVER_ID + " = ?", new String[]{geoFence.getId()});

    }

    /**
     * Add or Update Trigger to database.
     *
     * @param trigger Trigger to be saved.
     */


    /**
     * Add or Update Trigger to database.
     *
     * @param trigger Trigger to be saved.
     */
    public void insertOrUpdateTrigger(final Trigger trigger) {
        if (trigger == null || trigger.getTriggerData() == null || trigger.getTriggerData().getRegions() == null)
            return;
        ContentValues values =  putTriggerToContentValue(trigger);
        new GeoAsyncQueryHandler(context).setQueryHandlerInsertListener(new GeoAsyncQueryHandler.QueryHandlerInsertListener() {
            @Override
            public void onInsertComplete(int token, Object cookie, Uri uri) {
                Log.d(DonkyGeoFence.TAG, "insertOrUpdateTrigger onInsertComplete");
                if (uri.getLastPathSegment().equals("-1"))
                    updateTrigger(trigger);
                else {
                    saveTriggerToLocation(trigger);
                }
            }
        }).startInsert(1, null, GeoFenceContentProvider.getTriggerUri(context), values);
    }

    private void updateTrigger(final Trigger trigger){
        Log.d(DonkyGeoFence.TAG, "updateTrigger");
        ContentValues values =  putTriggerToContentValue(trigger);
        new GeoAsyncQueryHandler(context).setQueryHandlerUpdateListener(new GeoAsyncQueryHandler.QueryHandlerUpdateListener() {
            @Override
            public void onUpdateComplete(int token, Object cookie, int result) {
                Log.d(DonkyGeoFence.TAG, "updateTrigger onUpdateComplete");
                updateTriggerToLocation(trigger);
            }
        }).startUpdate(1, null, GeoFenceContentProvider.getTriggerUri(context), values,
                DatabaseSQLContract.TriggerEntry.COLUMN_NAME_SERVER_ID + " = ?", new String[]{trigger.getTriggerId()});
    }

    private void saveTriggerToLocation(Trigger trigger){
        Log.d(DonkyGeoFence.TAG, "saveTriggerToLocation");
        if (trigger == null)
            return;
        if (trigger.getTriggerData().getRegions().isEmpty())
            return;
        for (GeoFence location : trigger.getTriggerData().getRegions()) {
            ContentValues values = triggerToGeoLocationToContentValues(location, trigger);
            new GeoAsyncQueryHandler(context).setQueryHandlerInsertListener(new GeoAsyncQueryHandler.QueryHandlerInsertListener() {
                @Override
                public void onInsertComplete(int token, Object cookie, Uri uri) {
                    Log.d(DonkyGeoFence.TAG, "saveTriggerToLocation onInsertComplete");

                }
            }).startInsert(1, null, GeoFenceContentProvider.getTriggerToGeofenceUri(context), values);
        }
    }

    private ContentValues triggerToGeoLocationToContentValues(GeoFence location, Trigger trigger){
        ContentValues values = new ContentValues();
        values.put(DatabaseSQLContract.TriggerToGeoFencesEntry.COLUMN_NAME_TRIGGER_ID, trigger.getTriggerId());
        values.put(DatabaseSQLContract.TriggerToGeoFencesEntry.COLUMN_NAME_GEOFENCE_ID, location.getId());
        values.put(DatabaseSQLContract.TriggerToGeoFencesEntry.COLUMN_NAME_TRIGGER_CONDITION, trigger.getTriggerData().getCondition());
        values.put(DatabaseSQLContract.TriggerToGeoFencesEntry.COLUMN_NAME_TRIGGER_DIRECTION, trigger.getTriggerData().getDirection());
        values.put(DatabaseSQLContract.TriggerToGeoFencesEntry.COLUMN_NAME_TRIGGER_TIME_IN_REGION, trigger.getTriggerData().getTimeInRegionSeconds());
        return values;
    }

    private void updateTriggerToLocation(final Trigger trigger){
        Log.d(DonkyGeoFence.TAG, "updateTriggerToLocation");
        new GeoAsyncQueryHandler(context).setQueryHandlerDeleteListener(new GeoAsyncQueryHandler.QueryHandlerDeleteListener() {
            @Override
            public void onDeleteComplete(int token, Object cookie, int result) {
                Log.d(DonkyGeoFence.TAG, "updateTriggerToLocation onDeleteComplete");
                saveTriggerToLocation(trigger);
            }
        }).startDelete(1, null, GeoFenceContentProvider.getTriggerToGeofenceUri(context),
                DatabaseSQLContract.TriggerToGeoFencesEntry.COLUMN_NAME_TRIGGER_ID + " = ?", new String[]{trigger.getTriggerId()});
    }

    /**
     * Delete Trigger from database.
     *
     * @param trigger   Trigger to be deleted.
     */
    public void deleteTrigger(final Trigger trigger) {
        if (trigger == null)
            return;

        new GeoAsyncQueryHandler(context).setQueryHandlerDeleteListener(new GeoAsyncQueryHandler.QueryHandlerDeleteListener() {
            @Override
            public void onDeleteComplete(int token, Object cookie, int result) {
                if (result == 1){
                    new GeoAsyncQueryHandler(context).setQueryHandlerDeleteListener(new GeoAsyncQueryHandler.QueryHandlerDeleteListener() {
                        @Override
                        public void onDeleteComplete(int token, Object cookie, int result) {
                            if (result == 1){
                                Log.d(DonkyGeoFence.TAG, "Trigger To GeoFences ");
                            }
                        }
                    }).startDelete(1, null, GeoFenceContentProvider.getTriggerToGeofenceUri(context), DatabaseSQLContract.TriggerToGeoFencesEntry.COLUMN_NAME_TRIGGER_ID + " = ?", new String[]{trigger.getTriggerId()});

                }
            }
        }).startDelete(1, null, GeoFenceContentProvider.getTriggerUri(context), DatabaseSQLContract.TriggerEntry.COLUMN_NAME_SERVER_ID + " = ?", new String[]{trigger.getTriggerId()});
    }

    private ContentValues putTriggerToContentValue(Trigger trigger) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseSQLContract.TriggerEntry.COLUMN_NAME_SERVER_ID, trigger.getTriggerId());
        contentValues.put(DatabaseSQLContract.TriggerEntry.COLUMN_NAME_ACTIVATION_ID, trigger.getActivationId());
        contentValues.put(DatabaseSQLContract.TriggerEntry.COLUMN_NAME_TRIGGER_TYPE, trigger.getTriggerType());
        contentValues.put(DatabaseSQLContract.TriggerEntry.COLUMN_NAME_TRIGGER_CONDITION, trigger.getTriggerData().getCondition());
        contentValues.put(DatabaseSQLContract.TriggerEntry.COLUMN_NAME_TRIGGER_DIRECTION, trigger.getTriggerData().getDirection());
        contentValues.put(DatabaseSQLContract.TriggerEntry.COLUMN_NAME_TIME_IN_REGION, trigger.getTriggerData().getTimeInRegionSeconds());
        contentValues.put(DatabaseSQLContract.TriggerEntry.COLUMN_NAME_MAXIMUM_EXECUTION, trigger.getRestrictions().getMaximumExecutions());
        contentValues.put(DatabaseSQLContract.TriggerEntry.COLUMN_NAME_MAXIMUM_EXECUTION_PER_INTERVAL, trigger.getRestrictions().getMaximumExecutionsPerInterval());
        contentValues.put(DatabaseSQLContract.TriggerEntry.COLUMN_NAME_MAXIMUM_EXECUTION_PER_INTERVAL_MILLIS, trigger.getRestrictions().getMaximumExecutionsInterval());
        contentValues.put(DatabaseSQLContract.TriggerEntry.COLUMN_NAME_MAXIMUM_EXECUTION_PER_INTERVAL_SECONDS, trigger.getRestrictions().getMaximumExecutionsIntervalSeconds());
        contentValues.put(DatabaseSQLContract.TriggerEntry.COLUMN_NAME_EXECUTED_OVERALL, 0);
        contentValues.put(DatabaseSQLContract.TriggerEntry.COLUMN_NAME_TIME_IN_REGION, trigger.getTriggerData().getTimeInRegionSeconds());
        contentValues.put(DatabaseSQLContract.TriggerEntry.COLUMN_NAME_VALID_FROM, trigger.getValidity().getValidFrom());
        contentValues.put(DatabaseSQLContract.TriggerEntry.COLUMN_NAME_VALID_TO, trigger.getValidity().getValidTo());
        return contentValues;
    }


    /**
     *
     * @param triggerId
     * @return
     */
    public List<GeoFence> getGeoFencesRelatedToTrigger(String triggerId){
        Cursor cursor = context.getContentResolver().query(GeoFenceContentProvider.getTriggerToGeofenceUri(context),
                allColumnTriggersToGeoFences,
                DatabaseSQLContract.TriggerToGeoFencesEntry.COLUMN_NAME_TRIGGER_ID + " = ?",
                new String[]{triggerId},
                null);
        List<GeoFence> geoFences = new ArrayList<GeoFence>(cursor.getCount());
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            GeoFence geoFence = getGeoFenceLocation(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseSQLContract.TriggerToGeoFencesEntry.COLUMN_NAME_GEOFENCE_ID)));
            geoFences.add(geoFence);
            cursor.moveToNext();
        }
        cursor.close();
        return geoFences;
    }

    /**
     *
     * @param cursor
     * @return
     */
    public List<Trigger> getTriggerRelatedToGeoFences(Cursor cursor){
        List<Trigger> geoFenceLocations = new ArrayList<Trigger>(cursor.getCount());
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Trigger trigger = getTrigger(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseSQLContract.TriggerToGeoFencesEntry.COLUMN_NAME_TRIGGER_ID)));
            geoFenceLocations.add(trigger);
            cursor.moveToNext();
        }
        cursor.close();
        return geoFenceLocations;
    }

    public ControlRegion cursorControlRegion(Cursor cursor){
        return new ControlRegion(
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseSQLContract.ControlRegionEntry.COLUMN_NAME_INTERNAL_ID)),
                cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseSQLContract.ControlRegionEntry.COLUMN_NAME_LATITUDE)),
                cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseSQLContract.ControlRegionEntry.COLUMN_NAME_LONGITUDE)),
                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseSQLContract.ControlRegionEntry.COLUMN_NAME_RADIUS)),
                cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseSQLContract.ControlRegionEntry.COLUMN_NAME_LAST_EXECUTION_TIME)));
    }

    public ContentValues controlRegionToContentValues(ControlRegion controlRegion){
        ContentValues values = new ContentValues();
        values.put(DatabaseSQLContract.ControlRegionEntry.COLUMN_NAME_INTERNAL_ID, controlRegion.getId());
        values.put(DatabaseSQLContract.ControlRegionEntry.COLUMN_NAME_LATITUDE, controlRegion.getLatitude());
        values.put(DatabaseSQLContract.ControlRegionEntry.COLUMN_NAME_LONGITUDE, controlRegion.getLongitude());
        values.put(DatabaseSQLContract.ControlRegionEntry.COLUMN_NAME_RADIUS, controlRegion.getRadiusMetres());
        values.put(DatabaseSQLContract.ControlRegionEntry.COLUMN_NAME_LAST_EXECUTION_TIME, controlRegion.getRadiusMetres());
        return values;
    }

    private GeoFence cursorToGeoFenceLocation(Cursor cursor){
        return new GeoFence(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_APPLICATION_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_NAME)),
                cursorToLocationPoint(cursor),
                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_RADIUS)),
                convertStringToArray(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_LABELS))),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_TYPE)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_SERVER_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_STATUS)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_CREATED_ON)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_UPDATED_ON)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_ACTIVATION_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_ACTIVATED_ON)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_HASH)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_INTERNAL_ID)),
                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_IS_ACTIVATE)),
                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_RELATED_TRIGGER_COUNT)),
                cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_BOARDER_DISTANCE_TO_CURRENT_LOCATION)),
                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_IS_INSIDE)),
                cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_REAL_BOARDER_DISTANCE_TO_CURRENT_LOCATION)),
                cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_LAST_ENTER)),
                cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_LAST_EXIT)));
    }

    private LocationPoint cursorToLocationPoint(Cursor cursor){
        return new LocationPoint(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_LATITUDE)),
                                 cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_LONGITUDE)));
    }

    public Trigger cursorToTrigger(Cursor cursor) {
        return new Trigger(
                null,
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseSQLContract.TriggerEntry.COLUMN_NAME_ACTIVATION_ID)),
                cursorToRestrictions(cursor),
                cursorToTriggerData(cursor),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseSQLContract.TriggerEntry.COLUMN_NAME_SERVER_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseSQLContract.TriggerEntry.COLUMN_NAME_TRIGGER_TYPE)),
                cursorToValidity(cursor),
                cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseSQLContract.TriggerEntry.COLUMN_NAME_LAST_EXECUTION_TIME))
        );
    }

    private TriggerData cursorToTriggerData(Cursor cursor){
        return new TriggerData(
                getGeoFencesRelatedToTrigger(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseSQLContract.TriggerEntry.COLUMN_NAME_SERVER_ID))),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseSQLContract.TriggerEntry.COLUMN_NAME_TRIGGER_CONDITION)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseSQLContract.TriggerEntry.COLUMN_NAME_TRIGGER_DIRECTION)),
                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseSQLContract.TriggerEntry.COLUMN_NAME_TIME_IN_REGION))
        );
    }

    private Restrictions cursorToRestrictions(Cursor cursor){
        return new Restrictions(
                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseSQLContract.TriggerEntry.COLUMN_NAME_MAXIMUM_EXECUTION)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseSQLContract.TriggerEntry.COLUMN_NAME_MAXIMUM_EXECUTION_PER_INTERVAL_MILLIS)),
                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseSQLContract.TriggerEntry.COLUMN_NAME_MAXIMUM_EXECUTION_PER_INTERVAL_SECONDS)),
                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseSQLContract.TriggerEntry.COLUMN_NAME_MAXIMUM_EXECUTION_PER_INTERVAL)),
                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseSQLContract.TriggerEntry.COLUMN_NAME_EXECUTION_COUNT)),
                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseSQLContract.TriggerEntry.COLUMN_NAME_EXECUTION_COUNT_PER_INTERVAL))
        );
    }

    private Validity cursorToValidity(Cursor cursor){
        return new Validity(
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseSQLContract.TriggerEntry.COLUMN_NAME_VALID_FROM)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseSQLContract.TriggerEntry.COLUMN_NAME_VALID_TO))
        );
    }

    public void getAllGeoFenceLocations(final DonkyGeoFenceDataCallback<List<GeoFence>> donkyDataListener){
        new GeoAsyncQueryHandler(context).setQueryHandlerCompleteListener(new GeoAsyncQueryHandler.QueryHandlerCompleteListener() {
            @Override
            public void onQueryComplete(int token, Object cookie, Cursor cursor) {
                donkyDataListener.success(cursorToGeoFences(cursor));
            }
        }).startQuery(1, null, GeoFenceContentProvider.getGeofenceUri(context), allColumnsGeoFence, null, null, null);
    }

    public void getAllGeoFenceLocationsById(String id, final DonkyGeoFenceDataCallback<GeoFence> donkyDataListener){
        new GeoAsyncQueryHandler(context).setQueryHandlerCompleteListener(new GeoAsyncQueryHandler.QueryHandlerCompleteListener() {
            @Override
            public void onQueryComplete(int token, Object cookie, Cursor cursor) {
                donkyDataListener.success(cursorToGeoFence(cursor));
            }
        }).startQuery(1, null, GeoFenceContentProvider.getGeofenceUri(context), allColumnsGeoFence,
                DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_SERVER_ID + " = ?", new String[]{id}, null);
    }



    public void getAllTriggers(final DonkyGeoFenceDataCallback<List<Trigger>> donkyDataListener){
        new GeoAsyncQueryHandler(context).setQueryHandlerCompleteListener(new GeoAsyncQueryHandler.QueryHandlerCompleteListener() {
            @Override
            public void onQueryComplete(int token, Object cookie, Cursor cursor) {
                donkyDataListener.success(cursorToTriggers(cursor));
            }
        }).startQuery(1, null, GeoFenceContentProvider.getTriggerUri(context), allColumnTrigger, null, null, null);
    }

    public void getAllGeoFencesRelatedToTrigger(String triggerId, final DonkyGeoFenceDataCallback<List<GeoFence>> donkyDataListener){
        donkyDataListener.success(getGeoFencesRelatedToTrigger(triggerId));
    }

    public TriggerToGeoFences cursorTriggerToGeoFences(Cursor cursor){
        return new TriggerToGeoFences(
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseSQLContract.TriggerToGeoFencesEntry.COLUMN_NAME_TRIGGER_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseSQLContract.TriggerToGeoFencesEntry.COLUMN_NAME_TRIGGER_CONDITION)),
                null,
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseSQLContract.TriggerToGeoFencesEntry.COLUMN_NAME_TRIGGER_DIRECTION)),
                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseSQLContract.TriggerToGeoFencesEntry.COLUMN_NAME_TRIGGER_TIME_IN_REGION)),
                cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseSQLContract.TriggerToGeoFencesEntry.COLUMN_NAME_ENTER_TIME)),
                cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseSQLContract.TriggerToGeoFencesEntry.COLUMN_NAME_EXIT_TIME)),
                cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseSQLContract.TriggerToGeoFencesEntry.COLUMN_NAME_DWELL_TIME))
        );
    }

    public void getAllTriggersToGeoFence(final DonkyGeoFenceDataCallback<List<TriggerToGeoFence>> donkyDataListener){
        new GeoAsyncQueryHandler(context).setQueryHandlerCompleteListener(new GeoAsyncQueryHandler.QueryHandlerCompleteListener() {
            @Override
            public void onQueryComplete(int token, Object cookie, Cursor cursor) {
                List<TriggerToGeoFence> triggers = new ArrayList<>(cursor.getCount());
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    TriggerToGeoFence toGeoFence = cursorTriggerToGeoFence(cursor);
                    triggers.add(toGeoFence);
                    cursor.moveToNext();
                }
                donkyDataListener.success(triggers);
            }
        }).startQuery(1, null, GeoFenceContentProvider.getTriggerToGeofenceUri(context), allColumnTriggersToGeoFences, null, null, null);
    }

    public TriggerToGeoFence cursorTriggerToGeoFence(Cursor cursor){
        return new TriggerToGeoFence(
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseSQLContract.TriggerToGeoFencesEntry.COLUMN_NAME_TRIGGER_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseSQLContract.TriggerToGeoFencesEntry.COLUMN_NAME_TRIGGER_CONDITION)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseSQLContract.TriggerToGeoFencesEntry.COLUMN_NAME_GEOFENCE_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseSQLContract.TriggerToGeoFencesEntry.COLUMN_NAME_TRIGGER_DIRECTION)),
                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseSQLContract.TriggerToGeoFencesEntry.COLUMN_NAME_TRIGGER_TIME_IN_REGION)),
                cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseSQLContract.TriggerToGeoFencesEntry.COLUMN_NAME_ENTER_TIME)),
                cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseSQLContract.TriggerToGeoFencesEntry.COLUMN_NAME_EXIT_TIME)),
                cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseSQLContract.TriggerToGeoFencesEntry.COLUMN_NAME_DWELL_TIME))
        );
    }

    public void getAllTriggersToGeoFences(final DonkyGeoFenceDataCallback<List<TriggerToGeoFences>> donkyDataListener){
        new GeoAsyncQueryHandler(context).setQueryHandlerCompleteListener(new GeoAsyncQueryHandler.QueryHandlerCompleteListener() {
            @Override
            public void onQueryComplete(int token, Object cookie, Cursor cursor) {
                Set<String> triggerIds = new HashSet<>(cursor.getCount());
                List<TriggerToGeoFences> triggers = new ArrayList<>(cursor.getCount());
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    String triggerId = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseSQLContract.TriggerToGeoFencesEntry.COLUMN_NAME_TRIGGER_ID));
                    if (triggerIds.contains(triggerId)) {
                        cursor.moveToNext();
                        continue;
                    }
                    else
                        triggerIds.add(triggerId);
                    TriggerToGeoFences triggerToGeoFences = cursorTriggerToGeoFences(cursor);
                  Cursor cursorTrigger = context.getContentResolver().query(GeoFenceContentProvider.getTriggerToGeofenceUri(context),
                          allColumnTriggersToGeoFences,
                          DatabaseSQLContract.TriggerToGeoFencesEntry.COLUMN_NAME_TRIGGER_ID + " = ?",
                          new String[]{triggerId},
                          null);
                  if (!cursorTrigger.moveToFirst() || cursor.getCount() == 0)
                    return;
                    ArrayList<String> geoFencesId = new ArrayList<>(cursor.getCount());
                    while (!cursorTrigger.isAfterLast()){
                        String geoId = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseSQLContract.TriggerToGeoFencesEntry.COLUMN_NAME_GEOFENCE_ID));
                        geoFencesId.add(geoId);
                        cursorTrigger.moveToNext();
                    }
                    triggerToGeoFences.setGeoFencesId(geoFencesId);
                    cursorTrigger.close();
                    triggers.add(triggerToGeoFences);
                    cursor.moveToNext();
                }
                donkyDataListener.success(triggers);
            }
        }).startQuery(1, null, GeoFenceContentProvider.getTriggerToGeofenceUri(context), allColumnTriggersToGeoFences, null, null, null);
    }

    public List<GeoFence> cursorToGeoFences(Cursor cursor){
        List<GeoFence> locationPoint = new ArrayList<>(cursor.getCount());
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            GeoFence point = cursorToGeoFenceLocation(cursor);
            locationPoint.add(point);
            cursor.moveToNext();
        }
        cursor.close();
        return locationPoint;
    }

    public GeoFence cursorToGeoFence(Cursor cursor){
        List<GeoFence> locationPoint = new ArrayList<>(cursor.getCount());
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            GeoFence point = cursorToGeoFenceLocation(cursor);
            locationPoint.add(point);
            cursor.moveToNext();
        }
        cursor.close();
        return locationPoint.isEmpty() ? null : locationPoint.get(0);
    }

    public List<Trigger> cursorToTriggers(Cursor cursor){
        List<Trigger> triggerArrayList = new ArrayList<>(cursor.getCount());
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Trigger trigger = cursorToTrigger(cursor);
            triggerArrayList.add(trigger);
            cursor.moveToNext();
        }
        cursor.close();
        return triggerArrayList;
    }

    private String strSeparator = "__,__";

    private String convertArrayToString(String[] array){
        if (array == null || array.length == 0)
            return "";
        StringBuilder builder = new StringBuilder(array.length);
        for (int i = 0;i<array.length; i++) {
            builder.append(array[i]);
            if(i<array.length-1){
                builder.append(strSeparator);
            }
        }
        return builder.toString();
    }

    private String[] convertStringToArray(String str){
        if (str == null)
            return null;
        return str.split(strSeparator);
    }
}
