package net.donky.location.geofence.model;

import android.content.ContentValues;

import com.google.android.gms.location.Geofence;
import com.google.gson.annotations.SerializedName;

import net.donky.location.geofence.database.DatabaseSQLContract;

import java.util.List;

public class ControlRegion {

    private String id;
    private double latitude;
    private double longitude;
    private int radiusMetres;
    private long latExecution;

    public ControlRegion(String id, double latitude, double longitude, int radiusMetres, long latExecution) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radiusMetres = radiusMetres;
        this.latExecution = latExecution;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getRadiusMetres() {
        return radiusMetres;
    }

    public void setRadiusMetres(int radiusMetres) {
        this.radiusMetres = radiusMetres;
    }

    public long getLatExecution() {
        return latExecution;
    }

    public void setLatExecution(long latExecution) {
        this.latExecution = latExecution;
    }

    public Geofence buildGeofence(){
        return  new Geofence.Builder()
                    .setRequestId(id)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_EXIT)
                    .setNotificationResponsiveness(300000)
                    .setCircularRegion(latitude, longitude, radiusMetres <= 10 ? 10 : radiusMetres)
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .build();
    }

    public static ContentValues controlRegionToContentValues(ControlRegion controlRegion){
        ContentValues values = new ContentValues();
        values.put(DatabaseSQLContract.ControlRegionEntry.COLUMN_NAME_INTERNAL_ID, controlRegion.getId());
        values.put(DatabaseSQLContract.ControlRegionEntry.COLUMN_NAME_LATITUDE, controlRegion.getLatitude());
        values.put(DatabaseSQLContract.ControlRegionEntry.COLUMN_NAME_LONGITUDE, controlRegion.getLongitude());
        values.put(DatabaseSQLContract.ControlRegionEntry.COLUMN_NAME_RADIUS, controlRegion.getRadiusMetres());
        values.put(DatabaseSQLContract.ControlRegionEntry.COLUMN_NAME_LAST_EXECUTION_TIME, controlRegion.getLatExecution());
        return values;
    }
}
