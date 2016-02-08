package net.donky.location.geofence.geocore;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.text.format.Time;

import net.donky.location.geofence.database.CursorUtill;
import net.donky.location.geofence.database.DatabaseSQLContract;
import net.donky.location.geofence.database.GeoFenceContentProvider;
import net.donky.location.geofence.database.GeoFenceDAO;
import net.donky.core.network.location.GeoFence;

import java.util.List;

public class GeoFenceManager  {



    private Context context;
    private TriggerManager triggerManager;

    public GeoFenceManager(Context context){
        this.context = context;
        triggerManager = new TriggerManager(context);

    }

    public void processLocation(Location location){
        checkGeoFences(location);
    }

    private void checkGeoFences(Location location){
        executeEntered(location);
        executeExited(location);
    }

    private void executeEntered(Location location){
        ContentValues contentValues = new ContentValues(1);
        String selection = DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_REAL_BOARDER_DISTANCE_TO_CURRENT_LOCATION +
                " < 0";
        Cursor cursor = context.getContentResolver().query(GeoFenceContentProvider.getGeofenceUri(context), GeoFenceDAO.allColumnsGeoFence, selection, null, null);
        List<GeoFence> geoFences = CursorUtill.cursorToGeoFences(cursor);
        for (GeoFence geoFence : geoFences) {
            if (!geoFence.isInside()) {
                triggerManager.executeEnterGeoFence(geoFence, location);
                contentValues.put(DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_IS_INSIDE, 1);
                contentValues.put(DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_LAST_ENTER, getCurrentTime());
                context.getContentResolver().update(GeoFenceContentProvider.getGeofenceUri(context), contentValues,
                        DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_SERVER_ID + " = ?", new String[]{geoFence.getId()});
                contentValues.clear();
            }
        }
    }

    private void executeExited(Location location){
        ContentValues contentValues = new ContentValues(1);
        String selection = DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_REAL_BOARDER_DISTANCE_TO_CURRENT_LOCATION +
                " > 0";
        Cursor cursor = context.getContentResolver().query(GeoFenceContentProvider.getGeofenceUri(context), GeoFenceDAO.allColumnsGeoFence, selection, null, null);
        List<GeoFence> geoFences = CursorUtill.cursorToGeoFences(cursor);
        for (GeoFence geoFence : geoFences) {
            if (geoFence.isInside()) {
                triggerManager.executeExitGeoFence(geoFence, location);
                contentValues.put(DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_IS_INSIDE, 0);
                contentValues.put(DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_LAST_EXIT, getCurrentTime());
                context.getContentResolver().update(GeoFenceContentProvider.getGeofenceUri(context), contentValues,
                        DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_SERVER_ID + " = ?", new String[]{geoFence.getId()});
                contentValues.clear();
            }
        }
    }

    private long getCurrentTime(){
        Time time = new Time();
        time.setToNow();
        return time.toMillis(false);
    }

}
