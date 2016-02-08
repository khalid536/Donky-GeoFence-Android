package net.donky.location.geofence.database;

import android.app.Application;
import android.content.Context;
import android.database.Cursor;

import net.donky.core.logging.DLog;
import net.donky.core.model.DatabaseSQLHelper;
import net.donky.core.network.location.Action;
import net.donky.location.geofence.model.DonkyGeoFenceDataCallback;
import net.donky.core.network.location.GeoFence;
import net.donky.core.network.location.Trigger;
import net.donky.location.geofence.model.TriggerToGeoFence;
import net.donky.location.geofence.model.TriggerToGeoFences;

import java.util.List;

/**
 * Created by Igor Bykov
 * 04/09/15.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class DonkyGeoFenceDataController {

    private final DLog log;

    private DatabaseSQLHelper databaseSQLHelper;

    private GeoFenceDAO geoFenceDAO;

    private Context context;

    // Private constructor. Prevents instantiation from other classes.
    public DonkyGeoFenceDataController(Application application) {
        log = new DLog("TrackingDataController");
        context = application;
        geoFenceDAO = new GeoFenceDAO(context);
    }

    private boolean isGeoFenceDBAvailable(){
        return geoFenceDAO != null;
    }

    public void deleteAllGeoFence(){
        geoFenceDAO.deleteAllGeoFence();
    }

    public void deleteAllTriggers(){
        geoFenceDAO.deleteAllTriggers();
    }

    public GeoFenceDAO getGeoFenceDAO() {
        return geoFenceDAO;
    }

    public  List<Trigger> getTriggerRelatedToGeoFences(final Cursor cursor){
        return geoFenceDAO.getTriggerRelatedToGeoFences(cursor);
    }

    public List<GeoFence> getGeoFencesRelatedToTrigger(String triggerId){
        return geoFenceDAO.getGeoFencesRelatedToTrigger(triggerId);
    }

    public void saveGeoFenceIfNotExist(final GeoFence geoFence) {
        geoFenceDAO.insertOrUpdateGeoFence(geoFence);
    }

    public void deleteGeoFence(final GeoFence geoFence){
        geoFenceDAO.deleteGeoFence(geoFence);
    }

    public void deleteTrigger(final Trigger trigger){
        geoFenceDAO.deleteTrigger(trigger);
    }

    public void getAllGeoFence(DonkyGeoFenceDataCallback<List<GeoFence>> listener){
        geoFenceDAO.getAllGeoFenceLocations(listener);
    }

    public void getAllGeoFencesRelatedToTriggerByName(String triggerName, DonkyGeoFenceDataCallback<List<GeoFence>> listener){
        geoFenceDAO.getAllGeoFencesRelatedToTrigger(triggerName, listener);
    }

    public void getAllGeoFencesRelatedToTriggerById(String triggerId, DonkyGeoFenceDataCallback<List<GeoFence>> listener){
        geoFenceDAO.getAllGeoFencesRelatedToTrigger(triggerId, listener);
    }

    public void getAllGeoFenceByName(String name, DonkyGeoFenceDataCallback<List<GeoFence>> listener){
        geoFenceDAO.getAllGeoFenceLocations(listener);
    }

    public void getAllGeoFenceById(String id, DonkyGeoFenceDataCallback<GeoFence> listener){
        geoFenceDAO.getAllGeoFenceLocationsById(id, listener);
    }

    public void getAllTriggers(DonkyGeoFenceDataCallback<List<Trigger>> listener){
        geoFenceDAO.getAllTriggers(listener);
    }

    public void getTriggersByName(String triggerName, DonkyGeoFenceDataCallback<List<Trigger>> listener){
        geoFenceDAO.getAllTriggers(listener);
    }

    public void getTriggersById(String triggerId, DonkyGeoFenceDataCallback<List<Trigger>> listener){
        geoFenceDAO.getAllTriggers(listener);
    }

    public void getAllActions(DonkyGeoFenceDataCallback<List<Action>> listener){
    }

    public void getActionsByName(String triggerName, DonkyGeoFenceDataCallback<List<Action>> listener){
    }

    public void getActionsById(String triggerId, DonkyGeoFenceDataCallback<List<Action>> listener){
    }





    public void getAllTriggersToGeoFences(DonkyGeoFenceDataCallback<List<TriggerToGeoFences>> listener){
        geoFenceDAO.getAllTriggersToGeoFences(listener);
    }

    public void getAllTriggersToGeoFence(DonkyGeoFenceDataCallback<List<TriggerToGeoFence>> listener){
        geoFenceDAO.getAllTriggersToGeoFence(listener);
    }


    private void getControlRegion(){

    }

    /////////////////////
    // Trigger API
    ////////////////////

    public void saveTriggerIfNotExist(final Trigger trigger){
        geoFenceDAO.insertOrUpdateTrigger(trigger);
    }

}
