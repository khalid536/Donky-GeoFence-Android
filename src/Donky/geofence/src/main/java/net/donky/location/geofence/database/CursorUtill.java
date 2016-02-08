package net.donky.location.geofence.database;

import android.content.ContentValues;
import android.database.Cursor;

import net.donky.core.network.location.GeoFence;
import net.donky.core.network.location.LocationPoint;
import net.donky.core.network.location.Restrictions;
import net.donky.core.network.location.Trigger;
import net.donky.core.network.location.TriggerData;
import net.donky.core.network.location.Validity;
import net.donky.location.geofence.model.ControlRegion;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by work on 11/2/15.
 */
public class CursorUtill {

    public static ControlRegion cursorControlRegion(Cursor cursor){
        return new ControlRegion(
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseSQLContract.ControlRegionEntry.COLUMN_NAME_INTERNAL_ID)),
                cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseSQLContract.ControlRegionEntry.COLUMN_NAME_LATITUDE)),
                cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseSQLContract.ControlRegionEntry.COLUMN_NAME_LONGITUDE)),
                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseSQLContract.ControlRegionEntry.COLUMN_NAME_RADIUS)),
                cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseSQLContract.ControlRegionEntry.COLUMN_NAME_LAST_EXECUTION_TIME)));
    }

    public static ContentValues controlRegionToContentValues(ControlRegion controlRegion){
        ContentValues values = new ContentValues();
        values.put(DatabaseSQLContract.ControlRegionEntry.COLUMN_NAME_INTERNAL_ID, controlRegion.getId());
        values.put(DatabaseSQLContract.ControlRegionEntry.COLUMN_NAME_LATITUDE, controlRegion.getLatitude());
        values.put(DatabaseSQLContract.ControlRegionEntry.COLUMN_NAME_LONGITUDE, controlRegion.getLongitude());
        values.put(DatabaseSQLContract.ControlRegionEntry.COLUMN_NAME_RADIUS, controlRegion.getRadiusMetres());
        values.put(DatabaseSQLContract.ControlRegionEntry.COLUMN_NAME_LAST_EXECUTION_TIME, controlRegion.getRadiusMetres());
        return values;
    }

    public static List<GeoFence> cursorToGeoFences(Cursor cursor){
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

    public static GeoFence cursorToGeoFenceLocation(Cursor cursor){
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

    public static LocationPoint cursorToLocationPoint(Cursor cursor){
        return new LocationPoint(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_LATITUDE)),
                cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_LONGITUDE)));
    }


    public static Restrictions cursorToRestrictions(Cursor cursor){
        return new Restrictions(
                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseSQLContract.TriggerEntry.COLUMN_NAME_MAXIMUM_EXECUTION)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseSQLContract.TriggerEntry.COLUMN_NAME_MAXIMUM_EXECUTION_PER_INTERVAL_MILLIS)),
                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseSQLContract.TriggerEntry.COLUMN_NAME_MAXIMUM_EXECUTION_PER_INTERVAL_SECONDS)),
                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseSQLContract.TriggerEntry.COLUMN_NAME_MAXIMUM_EXECUTION_PER_INTERVAL)),
                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseSQLContract.TriggerEntry.COLUMN_NAME_EXECUTION_COUNT)),
                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseSQLContract.TriggerEntry.COLUMN_NAME_EXECUTION_COUNT_PER_INTERVAL))
        );
    }

    public static Validity cursorToValidity(Cursor cursor){
        return new Validity(
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseSQLContract.TriggerEntry.COLUMN_NAME_VALID_FROM)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseSQLContract.TriggerEntry.COLUMN_NAME_VALID_TO))
        );
    }

    private static String strSeparator = "__,__";

    private static String[] convertStringToArray(String str){
        if (str == null)
            return null;
        return str.split(strSeparator);
    }

  public static Trigger cursorToTrigger(Cursor cursor) {
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

  private static TriggerData cursorToTriggerData(Cursor cursor){
    return new TriggerData(
            null,
            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseSQLContract.TriggerEntry.COLUMN_NAME_TRIGGER_CONDITION)),
            cursor.getString(cursor.getColumnIndexOrThrow(DatabaseSQLContract.TriggerEntry.COLUMN_NAME_TRIGGER_DIRECTION)),
            cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseSQLContract.TriggerEntry.COLUMN_NAME_TIME_IN_REGION))
    );
  }

}
