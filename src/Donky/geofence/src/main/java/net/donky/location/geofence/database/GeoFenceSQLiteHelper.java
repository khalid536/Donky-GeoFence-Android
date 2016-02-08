package net.donky.location.geofence.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import net.donky.location.geofence.DonkyGeoFence;

/**
 * Implementation of SQLite helper interface for Core module to use when creating database.
 *
 * Created byIgor Bykov
 * 04/09/15.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class GeoFenceSQLiteHelper extends SQLiteOpenHelper {

    /**
     * If the database schema change, the database version will be incremented.
     */
    private static final String DATABASE_NAME = "GeoFence.db";
    private static final int DATABASE_VERSION = 1;

    protected static final String TEXT_TYPE = " TEXT";
    protected static final String INT_TYPE = " INT";
    protected static final String LONG_TYPE = " LONG";
    protected static final String REAL_TYPE = " REAL";
    protected static final String FLOAT_TYPE = " FLOAT";
    protected static final String COMMA_SEP = ",";

    private static SQLiteOpenHelper openHelper;

    private static final String SQL_CREATE_GEOFENCE_TABLE =
            "CREATE TABLE " + DatabaseSQLContract.GeoFenceLocationEntry.TABLE_NAME + "(" +
                    DatabaseSQLContract.GeoFenceLocationEntry._ID + " INTEGER PRIMARY KEY," +
                    DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_INTERNAL_ID + TEXT_TYPE + COMMA_SEP +
                    DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_SERVER_ID + TEXT_TYPE + COMMA_SEP +
                    DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_APPLICATION_ID + TEXT_TYPE + COMMA_SEP +
                    DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_NAME + TEXT_TYPE + COMMA_SEP +
                    DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_LABELS + TEXT_TYPE + COMMA_SEP +
                    DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_STATUS + TEXT_TYPE + COMMA_SEP +
                    DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_TYPE + TEXT_TYPE + COMMA_SEP +
                    DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_CREATED_ON + TEXT_TYPE + COMMA_SEP +
                    DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_UPDATED_ON + TEXT_TYPE + COMMA_SEP +
                    DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_LATITUDE + REAL_TYPE + COMMA_SEP +
                    DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_LONGITUDE + REAL_TYPE + COMMA_SEP +
                    DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_RADIUS + INT_TYPE + COMMA_SEP +
                    DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_ACTIVATION_ID + TEXT_TYPE + COMMA_SEP +
                    DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_ACTIVATED_ON + TEXT_TYPE + COMMA_SEP +
                    DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_HASH + TEXT_TYPE + COMMA_SEP +
                    DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_IS_ACTIVATE + INT_TYPE + COMMA_SEP +
                    DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_RELATED_TRIGGER_COUNT + INT_TYPE + COMMA_SEP +
                    DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_BOARDER_DISTANCE_TO_CURRENT_LOCATION + REAL_TYPE + COMMA_SEP +
                    DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_REAL_BOARDER_DISTANCE_TO_CURRENT_LOCATION + REAL_TYPE + COMMA_SEP +
                    DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_LAST_ENTER + REAL_TYPE + COMMA_SEP +
                    DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_LAST_EXIT + REAL_TYPE + COMMA_SEP +
                    DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_IS_INSIDE + INT_TYPE + COMMA_SEP +
                    " UNIQUE (serverId)  )";

    private static final String SQL_CREATE_TRIGGER_TABLE =
            "CREATE TABLE " + DatabaseSQLContract.TriggerEntry.TABLE_NAME + "(" +
                    DatabaseSQLContract.TriggerEntry._ID + " INTEGER PRIMARY KEY," +
                    DatabaseSQLContract.TriggerEntry.COLUMN_NAME_SERVER_ID + TEXT_TYPE + COMMA_SEP +
                    DatabaseSQLContract.TriggerEntry.COLUMN_NAME_ACTIVATION_ID + TEXT_TYPE + COMMA_SEP +
                    DatabaseSQLContract.TriggerEntry.COLUMN_NAME_TRIGGER_TYPE + TEXT_TYPE + COMMA_SEP +
                    DatabaseSQLContract.TriggerEntry.COLUMN_NAME_VALID_FROM + REAL_TYPE + COMMA_SEP +
                    DatabaseSQLContract.TriggerEntry.COLUMN_NAME_VALID_TO + REAL_TYPE + COMMA_SEP +
                    DatabaseSQLContract.TriggerEntry.COLUMN_NAME_MAXIMUM_EXECUTION + INT_TYPE + COMMA_SEP +
                    DatabaseSQLContract.TriggerEntry.COLUMN_NAME_MAXIMUM_EXECUTION_PER_INTERVAL + INT_TYPE + COMMA_SEP +
                    DatabaseSQLContract.TriggerEntry.COLUMN_NAME_MAXIMUM_EXECUTION_PER_INTERVAL_MILLIS + INT_TYPE + COMMA_SEP +
                    DatabaseSQLContract.TriggerEntry.COLUMN_NAME_MAXIMUM_EXECUTION_PER_INTERVAL_SECONDS + INT_TYPE + COMMA_SEP +
                    DatabaseSQLContract.TriggerEntry.COLUMN_NAME_EXECUTED_OVERALL + INT_TYPE + COMMA_SEP +
                    DatabaseSQLContract.TriggerEntry.COLUMN_NAME_TIME_IN_REGION + REAL_TYPE + COMMA_SEP +
                    DatabaseSQLContract.TriggerEntry.COLUMN_NAME_INTERNAL_ID + TEXT_TYPE + COMMA_SEP +
                    DatabaseSQLContract.TriggerEntry.COLUMN_NAME_TRIGGER_CONDITION + TEXT_TYPE + COMMA_SEP +
                    DatabaseSQLContract.TriggerEntry.COLUMN_NAME_TRIGGER_DIRECTION + TEXT_TYPE + COMMA_SEP +
                    DatabaseSQLContract.TriggerEntry.COLUMN_NAME_EXECUTION_COUNT + INT_TYPE + COMMA_SEP +
                    DatabaseSQLContract.TriggerEntry.COLUMN_NAME_EXECUTION_COUNT_PER_INTERVAL + INT_TYPE + COMMA_SEP +
                    DatabaseSQLContract.TriggerEntry.COLUMN_NAME_LAST_EXECUTION_TIME + INT_TYPE + COMMA_SEP +
                    " UNIQUE (serverId) )";

    private static final String SQL_CREATE_TRIGGER_TO_LOCATION_TABLE =
            "CREATE TABLE " + DatabaseSQLContract.TriggerToGeoFencesEntry.TABLE_NAME + "(" +
                    DatabaseSQLContract.TriggerToGeoFencesEntry._ID + " INTEGER PRIMARY KEY," +
                    DatabaseSQLContract.TriggerToGeoFencesEntry.COLUMN_NAME_TRIGGER_ID + TEXT_TYPE + COMMA_SEP +
                    DatabaseSQLContract.TriggerToGeoFencesEntry.COLUMN_NAME_GEOFENCE_ID + TEXT_TYPE + COMMA_SEP +
                    DatabaseSQLContract.TriggerToGeoFencesEntry.COLUMN_NAME_TRIGGER_CONDITION + TEXT_TYPE + COMMA_SEP +
                    DatabaseSQLContract.TriggerToGeoFencesEntry.COLUMN_NAME_TRIGGER_DIRECTION + TEXT_TYPE + COMMA_SEP +
                    DatabaseSQLContract.TriggerToGeoFencesEntry.COLUMN_NAME_TRIGGER_TIME_IN_REGION + INT_TYPE + COMMA_SEP +

                    DatabaseSQLContract.TriggerToGeoFencesEntry.COLUMN_NAME_ENTER_TIME + LONG_TYPE + COMMA_SEP +
                    DatabaseSQLContract.TriggerToGeoFencesEntry.COLUMN_NAME_EXIT_TIME + LONG_TYPE + COMMA_SEP +
                    DatabaseSQLContract.TriggerToGeoFencesEntry.COLUMN_NAME_DWELL_TIME + LONG_TYPE + COMMA_SEP +
                    " UNIQUE (triggerId, geofenceId) ON CONFLICT REPLACE )";

    private static final String SQL_CREATE_CONROL_REGION_TABLE =
            "CREATE TABLE " + DatabaseSQLContract.ControlRegionEntry.TABLE_NAME + "(" +
                    DatabaseSQLContract.TriggerToGeoFencesEntry._ID + " INTEGER PRIMARY KEY," +
                    DatabaseSQLContract.ControlRegionEntry.COLUMN_NAME_INTERNAL_ID + TEXT_TYPE + COMMA_SEP +
                    DatabaseSQLContract.ControlRegionEntry.COLUMN_NAME_LATITUDE + REAL_TYPE + COMMA_SEP +
                    DatabaseSQLContract.ControlRegionEntry.COLUMN_NAME_LONGITUDE + REAL_TYPE + COMMA_SEP +
                    DatabaseSQLContract.ControlRegionEntry.COLUMN_NAME_RADIUS + INT_TYPE + COMMA_SEP +
                    DatabaseSQLContract.ControlRegionEntry.COLUMN_NAME_LAST_EXECUTION_TIME + LONG_TYPE + COMMA_SEP +
                    " UNIQUE (internalId) ON CONFLICT REPLACE )";


    private static final String SQL_DELETE_LOCATION_ENTRIES =
            "DROP TABLE IF EXISTS " + DatabaseSQLContract.GeoFenceLocationEntry.TABLE_NAME;

    private static final String SQL_DELETE_LOCATION_TRIGGER_ENTRIES =
            "DROP TABLE IF EXISTS " + DatabaseSQLContract.TriggerEntry.TABLE_NAME;

    private static final String SQL_DELETE_LOCATION_TRIGGER_TO_LOCATION_ENTRIES =
            "DROP TABLE IF EXISTS " + DatabaseSQLContract.TriggerToGeoFencesEntry.TABLE_NAME;

    private static final String SQL_DELETE_CONTROL_REGION_ENTRIES =
            "DROP TABLE IF EXISTS " + DatabaseSQLContract.TriggerToGeoFencesEntry.TABLE_NAME;

    public GeoFenceSQLiteHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        openHelper = this;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(DonkyGeoFence.TAG, "onCreate " + DATABASE_NAME);
        db.execSQL(SQL_CREATE_GEOFENCE_TABLE);
        db.execSQL(SQL_CREATE_TRIGGER_TABLE);
        db.execSQL(SQL_CREATE_TRIGGER_TO_LOCATION_TABLE);
        db.execSQL(SQL_CREATE_CONROL_REGION_TABLE);
    }

    public static SQLiteOpenHelper getSQLiteOpenHelper(){
        return openHelper;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(DonkyGeoFence.TAG, "onUpgrade " + DATABASE_NAME);
        db.execSQL(SQL_DELETE_LOCATION_ENTRIES);
        db.execSQL(SQL_DELETE_LOCATION_TRIGGER_ENTRIES);
        db.execSQL(SQL_DELETE_LOCATION_TRIGGER_TO_LOCATION_ENTRIES);
        db.execSQL(SQL_DELETE_CONTROL_REGION_ENTRIES);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(DonkyGeoFence.TAG, "onUpgrade " + DATABASE_NAME);
        db.execSQL(SQL_DELETE_LOCATION_ENTRIES);
        db.execSQL(SQL_DELETE_LOCATION_TRIGGER_ENTRIES);
        db.execSQL(SQL_DELETE_LOCATION_TRIGGER_TO_LOCATION_ENTRIES);
        db.execSQL(SQL_DELETE_CONTROL_REGION_ENTRIES);
        onCreate(db);
    }
}
