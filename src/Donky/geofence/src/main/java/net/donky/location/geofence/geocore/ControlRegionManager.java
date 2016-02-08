package net.donky.location.geofence.geocore;

import android.Manifest;
import android.app.PendingIntent;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.format.Time;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import net.donky.location.geofence.DonkyGeoFence;
import net.donky.location.geofence.database.CursorUtill;
import net.donky.location.geofence.database.DatabaseSQLContract;
import net.donky.location.geofence.database.GeoFenceContentProvider;
import net.donky.location.geofence.database.GeoFenceDAO;
import net.donky.location.geofence.internal.GeofenceTransitionsIntentService;
import net.donky.location.geofence.model.ControlRegion;
import net.donky.core.network.location.GeoFence;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ControlRegionManager implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private String id = "donky";

    private Context context;

    private GoogleApiClient mGoogleApiClient;
    private PendingIntent transitionPendingIntent;

    private List<Geofence> mGeoFenceList;


    public ControlRegionManager(Context context) {
        this.context = context;
        mGeoFenceList = new ArrayList<>();
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    private ControlRegion initControlRegion(Location location) {
        ControlRegion controlRegion = getControlRegion(location);
        uploadControlRegion(controlRegion);
        return controlRegion;
    }


    public boolean processControlRegion(Location location) {

        ControlRegion controlRegion = getControlRegion();

        if (controlRegion == null) {
            controlRegion = initControlRegion(location);
            if (getControlRegion() == null) {
                return true;
            }
        }

        float distance = location.distanceTo(createLocation(controlRegion));

        if ((distance - controlRegion.getRadiusMetres() - location.getAccuracy()) < 0) {
            if (!isGeoFencesInsideControlRegion(controlRegion)) {
                return true;
            }
        }
        uploadControlRegion(getControlRegion(location));

        return false;
    }

    private boolean isGeoFencesInsideControlRegion(ControlRegion region) {
        String selection = DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_REAL_BOARDER_DISTANCE_TO_CURRENT_LOCATION +
                " = 0.0";
        Cursor cursor = context.getContentResolver().query(GeoFenceContentProvider.getGeofenceUri(context), GeoFenceDAO.allColumnsGeoFence, selection, null, null);
        List<GeoFence> geoFences = CursorUtill.cursorToGeoFences(cursor);
        if (geoFences == null || geoFences.isEmpty()) {
            return false;
        }
        Location location = new Location("");
        location.setLongitude(region.getLongitude());
        location.setLatitude(region.getLatitude());
        Location newLocation = new Location("");
        for (GeoFence geoFence : geoFences) {
            newLocation.setLatitude(geoFence.getCentrePoint().getLatitude());
            newLocation.setLongitude(geoFence.getCentrePoint().getLongitude());
            double distance = location.distanceTo(newLocation);
            if (distance - region.getRadiusMetres() < 0)
                return true;
        }
        return false;
    }

    private ControlRegion getControlRegion() {
        Cursor cursor = context.getContentResolver().query(GeoFenceContentProvider.getControlRegionUri(context),
                GeoFenceDAO.allControlRegion, null, null, null);
        if (cursor == null || cursor.getCount() <= 0)
            return null;
        cursor.moveToFirst();
        ControlRegion controlRegion = CursorUtill.cursorControlRegion(cursor);
        cursor.close();
        return controlRegion;
    }

    private Location createLocation(ControlRegion controlRegion) {
        Location location = new Location("");
        location.setLatitude(controlRegion.getLatitude());
        location.setLongitude(controlRegion.getLongitude());
        return location;
    }

    private ControlRegion getControlRegion(Location location) {
        updateDistances(location);
        Cursor cursor = queryForNearestGeoFence();
        if (!cursor.moveToFirst() || cursor.getCount() == 0)
            return null;
        cursor.moveToFirst();
        GeoFence locationPoints = CursorUtill.cursorToGeoFenceLocation(cursor);
        return getControlRegion(location, locationPoints);
    }

    private Cursor queryForNearestGeoFence() {
        String LIMIT = " LIMIT 1";
        String SORT = DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_BOARDER_DISTANCE_TO_CURRENT_LOCATION + " ASC";
        return context.getContentResolver().query(GeoFenceContentProvider.getGeofenceUri(context), GeoFenceDAO.allColumnsGeoFence, null, null, SORT + LIMIT);
    }

    private ControlRegion getControlRegion(Location location, GeoFence geoFence) {
        if (geoFence == null)
            return null;
        return new ControlRegion(id, location.getLatitude(), location.getLongitude(), (int) geoFence.getDistanceToBorder(), getCurrentTime());
    }

    public void connect() {
        mGoogleApiClient.blockingConnect();
    }

    public boolean isConnected() {
        return mGoogleApiClient.isConnected();
    }


    public void uploadControlRegion(ControlRegion controlRegion) {
        if (!isConnected())
            connect();
        if (controlRegion == null)
            return;
        mGeoFenceList.clear();
        mGeoFenceList.add(controlRegion.buildGeofence());
        if (mGeoFenceList.size() > 0) {

            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.w(DonkyGeoFence.TAG, "ACCESS_FINE_LOCATION permission required.");
                return;
            }

            Status status = LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    getGeoFencingRequest(),
                    getTransitionPendingIntent()).await();
            Log.d(DonkyGeoFence.TAG, Thread.currentThread().getName() + " GeofencingService ");
            if (status.isSuccess()) {
                ContentValues values = ControlRegion.controlRegionToContentValues(controlRegion);
                context.getContentResolver().insert(GeoFenceContentProvider.getControlRegionUri(context), values);
                for (Geofence geoFenceLocation : mGeoFenceList) {
                    Log.d(DonkyGeoFence.TAG, "Geo Added Success " + geoFenceLocation.toString());
                }

                disconnect();

            } else {
                Log.d(DonkyGeoFence.TAG, "Geo Added not Success" + status.isSuccess());
            }
        }
    }

    private GeofencingRequest getGeoFencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_EXIT);
        builder.addGeofences(mGeoFenceList);
        return builder.build();
    }

    public void disconnect(){
        mGoogleApiClient.disconnect();
    }


    /**
     * Create a PendingIntent that triggers an IntentService in your app when a
     * geofence transition occurs.
     */
    private PendingIntent getTransitionPendingIntent() {

        if (transitionPendingIntent == null) {

            Intent intent = new Intent(context, GeofenceTransitionsIntentService.class);
            Integer pendingIntentId = new Random().nextInt(Integer.MAX_VALUE);

            transitionPendingIntent = PendingIntent.getService(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }

        return transitionPendingIntent;

    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void updateDistances(Location location){
        Location loc = new Location("");
        Cursor cursor = context.getContentResolver().query(GeoFenceContentProvider.getGeofenceUri(context), GeoFenceDAO.allColumnsGeoFence, null, null, null);
        List<GeoFence> geoFences = CursorUtill.cursorToGeoFences(cursor);
        ArrayList<ContentProviderOperation> contentProviderOperations = new ArrayList<>(geoFences.size());
        for (GeoFence geoFence : geoFences){
            loc.setLatitude(geoFence.getCentrePoint().getLatitude());
            loc.setLongitude(geoFence.getCentrePoint().getLongitude());
            double distance = location.distanceTo(loc) - geoFence.getRadiusMetres();
            double moduleDistance = Math.abs(distance);
            ContentProviderOperation contentProviderOperation =
                    ContentProviderOperation.newUpdate(GeoFenceContentProvider.getGeofenceUri(context))
                            .withSelection(DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_SERVER_ID + " = ?", new String[]{geoFence.getId()})
                            .withValue(DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_BOARDER_DISTANCE_TO_CURRENT_LOCATION, moduleDistance)
                            .withValue(DatabaseSQLContract.GeoFenceLocationEntry.COLUMN_NAME_REAL_BOARDER_DISTANCE_TO_CURRENT_LOCATION, distance)
                            .build();
            contentProviderOperations.add(contentProviderOperation);
       }
       try {
            context.getContentResolver().applyBatch(GeoFenceContentProvider.getAuthority(context), contentProviderOperations);
       }catch (Exception e){
            e.printStackTrace();
       }
    }

    private long getCurrentTime(){
        Time time = new Time();
        time.setToNow();
        return time.toMillis(false);
    }

    public void stopTrackingControlRegion(){
        if (!isConnected())
            connect();
        Status status =
                LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, getTransitionPendingIntent()).await();
        if (status.isSuccess())
            Log.d(DonkyGeoFence.TAG, "GeoFence successfully deleted");
        else
            Log.d(DonkyGeoFence.TAG, "GeoFence was not successfully deleted");
    }
}
