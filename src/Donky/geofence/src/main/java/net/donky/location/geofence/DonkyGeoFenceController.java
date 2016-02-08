package net.donky.location.geofence;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import net.donky.core.DonkyCore;
import net.donky.core.DonkyException;
import net.donky.core.DonkyResultListener;
import net.donky.core.events.DonkyEventListener;
import net.donky.core.events.RegistrationChangedEvent;
import net.donky.core.logging.DLog;
import net.donky.core.network.DonkyNetworkController;
import net.donky.location.internal.LocationUpdatesCallback;
import net.donky.location.geofence.database.DonkyGeoFenceDataController;
import net.donky.location.geofence.geocore.GeoFenceProcessor;
import net.donky.location.geofence.geocore.LocationFactory;
import net.donky.location.geofence.internal.TransitionsBroadcastReceiver;
import net.donky.location.geofence.model.DonkyGeoFenceDataCallback;
import net.donky.core.network.location.GeoFence;
import net.donky.location.geofence.model.GeoFenceCrossingCallback;
import net.donky.core.network.location.Trigger;
import net.donky.location.geofence.model.TriggerFiredCallback;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by Igor Bykov
 * 8/09/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class DonkyGeoFenceController extends LocationUpdatesCallback implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public static final String ACTION_RECEIVE_GEOFENCE = "net.donky.location.ACTION_RECEIVE_GEOFENCE";

    private DLog log;


    private GoogleApiClient mGoogleApiClient;

    private Context context;

    private PendingIntent transitionPendingIntent;

    private DonkyGeoFenceDataController donkyGeoFenceDataController;

    private LinkedList<GeoFenceCrossingCallback> geoFenceCrossingCallbacks;

    private LinkedList<TriggerFiredCallback> triggerFiredCallbacks;


    /**
     * Initializes singleton.
     * <p/>
     * SingletonHolder is loaded on the first execution of Singleton.getInstance()
     * or the first access to SingletonHolder.INSTANCE, not before.
     */
    private static class SingletonHolder {
        private static final DonkyGeoFenceController INSTANCE = new DonkyGeoFenceController();
    }

    /**
     * @return Instance of GeoFence Controller singleton.
     */
    public static DonkyGeoFenceController getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * Private constructor. Prevents instantiation from other classes.
     */
    private DonkyGeoFenceController(){}

    /**
     * Initialise controller instance. This method should only be used by DonkyGeoFence.
     *
     * @param application Application instance.
     */



    public void init(Application application) {
        this.context = application;
        this.donkyGeoFenceDataController = new DonkyGeoFenceDataController(application);
        this.transitionPendingIntent = getTransitionPendingIntent();
        this.log = new DLog("GeoFencesController");
        this.mGoogleApiClient = getLocationAPI();
        this.mGoogleApiClient.connect();
        geoFenceCrossingCallbacks = new LinkedList<>();
        triggerFiredCallbacks = new LinkedList<>();
        setNewUserEvent();
    }

    private PendingIntent getTransitionPendingIntent() {
        if (transitionPendingIntent == null) {
            Intent intent = new Intent(context, TransitionsBroadcastReceiver.class);
            Integer pendingIntentId = new Random().nextInt(Integer.MAX_VALUE);
            transitionPendingIntent = PendingIntent.getService(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }
        return transitionPendingIntent;
    }

    /**
     *
     * @return
     */
    protected GoogleApiClient getLocationAPI() {
        if (mGoogleApiClient == null) {
            Log.d(DonkyGeoFence.TAG, "Initialising Location API.");
            buildGoogleApiClient();
        }
        return mGoogleApiClient;
    }

    /**
     *
     * @param location
     */
    @Override
    public void onLocationChanged(Location location) {
        Log.d(DonkyGeoFence.TAG, "on LocationChanged");
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(DonkyGeoFence.TAG, "On connected in DonkyGeoFenceController");
        if (isPermissionGranted()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(getLocationAPI(), getDefaultLocationRequest(), getPenIntent());
        }
    }

    private LocationRequest getDefaultLocationRequest(){
        return LocationFactory.createLocationRequest(3000L, 3000L, 20F, LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    protected void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    public PendingIntent getPenIntent(){
        Intent intent = new Intent(context, TransitionsBroadcastReceiver.class);
        return PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    private void setNewUserEvent() {
        DonkyCore.subscribeToLocalEvent(
                new DonkyEventListener<RegistrationChangedEvent>(RegistrationChangedEvent.class) {
                    @Override
                    public void onDonkyEvent(final RegistrationChangedEvent event) {
                        if (event != null) {
                            if (event.isReplaceRegistration()) {
                                newUserEvent();
                            }
                        }
                    }
                }
        );
    }

    private void newUserEvent(){

        donkyGeoFenceDataController.deleteAllGeoFence();

        donkyGeoFenceDataController.deleteAllTriggers();

        DonkyNetworkController.getInstance().getAllTriggers(new DonkyResultListener<List<Trigger>>() {
            @Override
            public void success(List<Trigger> result) {
                if (result == null || result.isEmpty()) {
                    return;
                }
                for (Trigger trigger : result) {
                    donkyGeoFenceDataController.saveTriggerIfNotExist(trigger);
                }

            }

            @Override
            public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                if (donkyException != null) {
                    Log.d(DonkyGeoFence.TAG, donkyException.toString());
                }
            }
        });

        DonkyNetworkController.getInstance().getAllGeoFences(new DonkyResultListener<List<GeoFence>>() {
            @Override
            public void success(List<GeoFence> result) {
                if (result == null || result.isEmpty()) {
                    return;
                }
                for (GeoFence geoFence : result) {
                    donkyGeoFenceDataController.saveGeoFenceIfNotExist(geoFence);
                }
            }

            @Override
            public void error(DonkyException donkyException, Map<String, String> validationErrors) {
                if (donkyException != null) {
                    Log.d(DonkyGeoFence.TAG, donkyException.toString());
                }
            }
        });
    }

    private boolean isPermissionGranted(){
        if (Build.VERSION.SDK_INT >= 23) {
            List<String> permissionsNeeded = new ArrayList<String>();
            final List<String> permissionsList = new ArrayList<String>();
            if (!addPermission(permissionsList, Manifest.permission.ACCESS_FINE_LOCATION))
                permissionsNeeded.add("ACCESS_FINE_LOCATION");
            if (!addPermission(permissionsList, Manifest.permission.ACCESS_COARSE_LOCATION))
                permissionsNeeded.add("ACCESS_COARSE_LOCATION");
            if (permissionsList.size() > 0) {
                if (permissionsNeeded.size() > 0) {
                    for (String permission : permissionsNeeded) {
                        Log.w(DonkyGeoFence.TAG, "You need to grant access to " + permission);
                    }
                }
                return false;
            }
            return true;
        }
        return true;
    }

    @TargetApi(23)
    private boolean addPermission(List<String> permissionsList, String permission) {
        if (context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            return false;
        }
        return true;
    }

    public void notifyAppStopped(){
        mGoogleApiClient.disconnect();
    }

    /////////////////////////////
    /////////Public API//////////
    /////////////////////////////

    /**
     *
     * @param geoFence
     */

    public void saveGeoFenceIfNotExist(final GeoFence geoFence) {
        donkyGeoFenceDataController.saveGeoFenceIfNotExist(geoFence);
    }

    /**
     *
     * @param trigger
     */
    public void saveTriggerIfNotExist(final Trigger trigger){
        donkyGeoFenceDataController.saveTriggerIfNotExist(trigger);
    }

    /**
     *
     * @param geoFence
     */
    public void deleteGeoFence(final GeoFence geoFence){
        donkyGeoFenceDataController.deleteGeoFence(geoFence);
    }

    /**
     *
     * @param trigger
     */
    public void deleteTrigger(final Trigger trigger){
        donkyGeoFenceDataController.deleteTrigger(trigger);
    }

    //FIXME
    public  List<Trigger> getTriggerRelatedToGeoFences(final Cursor cursor){
        return donkyGeoFenceDataController.getTriggerRelatedToGeoFences(cursor);
    }

    public List<GeoFence> getGeoFencesRelatedToTrigger(String triggerId){
        return donkyGeoFenceDataController.getGeoFencesRelatedToTrigger(triggerId);
    }

    /**
     * Start Tracking GoeFence
     */
    public void startTrackingGeoFences(){
        if (mGoogleApiClient.isConnected() && isPermissionGranted()){
            LocationServices.FusedLocationApi.requestLocationUpdates(getLocationAPI(), getDefaultLocationRequest(), getPenIntent());
        }
    }

    /**
     * Stop Tracking GeoFence
     */
    public void stopTrackingGeoFences(){
        if (mGoogleApiClient.isConnected()){
            LocationServices.FusedLocationApi.removeLocationUpdates(getLocationAPI(), getPenIntent());
            GeoFenceProcessor.stopControlRegion();
        }
    }

    /**
     * Get all GeoFence from database asynchronously
     * @param donkyGeoFenceDataCallback Callback for getting all geoFences asynchronously.
     */
    public void getAllGeoFences(final DonkyGeoFenceDataCallback<List<GeoFence>> donkyGeoFenceDataCallback){
        donkyGeoFenceDataController.getAllGeoFence(donkyGeoFenceDataCallback);
    }

    /**
     * Get all GeoFence from database by Id asynchronously
     * @param donkyGeoFenceDataCallback Callback for getting all geoFences asynchronously.
     */
    public void getGeoFencesById(final String geoFenceId, final DonkyGeoFenceDataCallback<GeoFence> donkyGeoFenceDataCallback){
        donkyGeoFenceDataController.getAllGeoFenceById(geoFenceId, donkyGeoFenceDataCallback);
    }

    /**
     * Get all GeoFence from database by name asynchronously
     * @param geoFenceName GeoFence Name
     * @param donkyGeoFenceDataCallback Callback for getting all geoFences asynchronously.
     */
    public void getGeoFencesByName(final String geoFenceName, final DonkyGeoFenceDataCallback<List<GeoFence>> donkyGeoFenceDataCallback){
        donkyGeoFenceDataController.getAllGeoFenceByName(geoFenceName, donkyGeoFenceDataCallback);

    }

    /**
     * Get all Triggers from database asynchronously
     * @param donkyGeoFenceDataCallback Callback for getting all triggers asynchronously.
     */
    public void getAllTriggers(final DonkyGeoFenceDataCallback<List<Trigger>> donkyGeoFenceDataCallback){
        donkyGeoFenceDataController.getAllTriggers(donkyGeoFenceDataCallback);
    }

    /**
     * Get all GeoFence related to Trigger from database by name asynchronously
     * @param triggerId trigger Id
     * @param donkyGeoFenceDataCallback Callback for getting all geoFences asynchronously.
     */
    public void getAllGeoFencesRelatedTrigger(String triggerId, final DonkyGeoFenceDataCallback<List<GeoFence>> donkyGeoFenceDataCallback){
        donkyGeoFenceDataController.getAllGeoFencesRelatedToTriggerById(triggerId, donkyGeoFenceDataCallback);
    }

    /**
     * Get all Triggers from database by Id asynchronously
     * @param triggerId trigger Id
     * @param donkyGeoFenceDataCallback Callback for getting all geoFences asynchronously.
     */
    public void getTriggersById(String triggerId, final DonkyGeoFenceDataCallback<List<Trigger>> donkyGeoFenceDataCallback){
        donkyGeoFenceDataController.getTriggersById(triggerId, donkyGeoFenceDataCallback);
    }

    /**
     * Get all Trigger from database by name asynchronously
     * @param triggerName GeoFence Name
     * @param donkyGeoFenceDataCallback Callback for getting all geoFences asynchronously.
     */
    public void getTriggersByName(String triggerName, final DonkyGeoFenceDataCallback<List<Trigger>> donkyGeoFenceDataCallback){
        donkyGeoFenceDataController.getTriggersByName(triggerName, donkyGeoFenceDataCallback);
    }

    //Notification API

    /**
     * Register listeners to get notification when GeoFence location is enter or exit
     * @param geoFenceCrossingCallback Callback to get notification
     */
    public void registerForGeoFenceCrossing(GeoFenceCrossingCallback  geoFenceCrossingCallback){
        if (!geoFenceCrossingCallbacks.contains(geoFenceCrossingCallback))
            geoFenceCrossingCallbacks.add(geoFenceCrossingCallback);
    }

    /**
     * Unregister listeners to get notification when GeoFence location is enter or exit
     * @param geoFenceCrossingCallback Callback to get notification
     */
    public void unregisterForGeoFenceCrossing(GeoFenceCrossingCallback geoFenceCrossingCallback){
        if (geoFenceCrossingCallbacks.contains(geoFenceCrossingCallback))
            geoFenceCrossingCallbacks.remove(geoFenceCrossingCallback);
    }

    /**
     * Register listeners to get notification when Trigger is fired
     * @param triggerFiredCallback Callback to get notification
     */
    public void registerForTriggerFired(TriggerFiredCallback triggerFiredCallback){
        if (!triggerFiredCallbacks.contains(triggerFiredCallback))
            triggerFiredCallbacks.add(triggerFiredCallback);
    }

    /**
     * Unregister listeners to get notification when Trigger is fired
     * @param triggerFiredCallback Callback to get notification
     */
    public void unregisterForTriggerFired(TriggerFiredCallback triggerFiredCallback){
        if (!triggerFiredCallbacks.contains(triggerFiredCallback))
            triggerFiredCallbacks.remove(triggerFiredCallback);
    }

    public void notifyTriggerFiredCallback(final Trigger trigger){
        if (triggerFiredCallbacks == null || triggerFiredCallbacks.isEmpty())
            return;
        for (TriggerFiredCallback callback : triggerFiredCallbacks)
            callback.fired(trigger);
    }

    public void notifyGeoFenceCrossedEnterCallback(final GeoFence geoFence){
        if (geoFenceCrossingCallbacks == null || geoFenceCrossingCallbacks.isEmpty())
            return;
        for (GeoFenceCrossingCallback callback : geoFenceCrossingCallbacks)
            callback.entered(geoFence);
    }

    public void notifyGeoFenceExitEnterCallback(final GeoFence geoFence){
        if (geoFenceCrossingCallbacks == null || geoFenceCrossingCallbacks.isEmpty())
            return;
        for (GeoFenceCrossingCallback callback : geoFenceCrossingCallbacks)
            callback.exited(geoFence);
    }
}
