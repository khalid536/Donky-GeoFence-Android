package net.donky.location.geofence.analytics;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class AnalyticsTable {


    // Database table
    public static final String TABLE_NAME = "geo";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_ACCURACY = "accuracy";

    // Database creation SQL statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_NAME
            + " ("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_TIME + " text not null, "
            + COLUMN_DESCRIPTION + " text not null, "
            + COLUMN_ACCURACY + " text"
            + ");";

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion,
                                 int newVersion) {
        Log.w(AnalyticsTable.class.getName(), "Upgrading database from version "
                + oldVersion + " to " + newVersion
                + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(database);
    }
}
