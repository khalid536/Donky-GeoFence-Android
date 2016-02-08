package net.donky.location.geofence.analytics;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class GeoDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "geotable.db";
    private static final int DATABASE_VERSION = 1;

    public GeoDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        AnalyticsTable.onCreate(database);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion,
                          int newVersion) {
        AnalyticsTable.onUpgrade(database, oldVersion, newVersion);
    }
}
