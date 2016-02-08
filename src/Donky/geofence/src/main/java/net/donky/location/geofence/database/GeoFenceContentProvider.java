package net.donky.location.geofence.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;

public class GeoFenceContentProvider extends ContentProvider {

    private final static String AUTHORITY_POSTFIX = ".donky.geofence.provider";

    // Used for the UriMacher
    private static final int GEOFENCE = 1;
    private static final int TRIGGER = 2;
    private static final int ACTION = 3;
    private static final int TRIGGER_TO_GEOFENCE = 4;
    private static final int CONTROL_REGION = 5;

    private static String AUTHORITY;

    private static String BASE_PATH;

    private static final String GEO_FENCE_PATH =  DatabaseSQLContract.GeoFenceLocationEntry.TABLE_NAME;
    private static final String TRIGGER_PATH = DatabaseSQLContract.TriggerEntry.TABLE_NAME;
    private static final String ACTION_PATH =  DatabaseSQLContract.ActionEntry.TABLE_NAME;
    private static final String TRIGGER_TO_GEOFENCE_PATH =  DatabaseSQLContract.TriggerToGeoFencesEntry.TABLE_NAME;
    private static final String CONTROL_REGION_PATH =  DatabaseSQLContract.ControlRegionEntry.TABLE_NAME;

    private static Uri CONTENT_URI_GEOFENCE;
    private static Uri CONTENT_URI_TRIGGER;
    private static Uri CONTENT_URI_ACTION;
    private static Uri CONTENT_URI_TRIGGER_TO_GEOFENCE;
    private static Uri CONTENT_URI_CONTROL_REGION;

    private static UriMatcher uriMatcher;

    private GeoFenceSQLiteHelper database;

    @Override
    public boolean onCreate() {
        createUriMatcher(getContext());
        database = new GeoFenceSQLiteHelper(getContext());
        return true;
    }

    public static Uri getGeofenceUri(Context context) {
        createUriMatcher(context);
        return CONTENT_URI_GEOFENCE;
    }

    public static Uri getTriggerUri(Context context) {
        createUriMatcher(context);
        return CONTENT_URI_TRIGGER;
    }

    public static Uri getActionUri(Context context) {
        createUriMatcher(context);
        return CONTENT_URI_ACTION;
    }

    public static Uri getTriggerToGeofenceUri(Context context) {
        createUriMatcher(context);
        return CONTENT_URI_TRIGGER_TO_GEOFENCE;
    }

    public static Uri getControlRegionUri(Context context) {
        createUriMatcher(context);
        return CONTENT_URI_CONTROL_REGION;
    }

    public static String getAuthority(Context context) {
        createUriMatcher(context);
        return AUTHORITY;
    }

    private synchronized static void createUriMatcher(Context context) {

        if (context != null) {
            if (AUTHORITY == null) {
                AUTHORITY = context.getPackageName() + AUTHORITY_POSTFIX;
            }
            if (BASE_PATH == null) {
                BASE_PATH = "content://" + AUTHORITY + "/";
            }
            if (uriMatcher == null) {
                uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
                uriMatcher.addURI(AUTHORITY, GEO_FENCE_PATH, GEOFENCE);
                uriMatcher.addURI(AUTHORITY, TRIGGER_PATH, TRIGGER);
                uriMatcher.addURI(AUTHORITY, ACTION_PATH, ACTION);
                uriMatcher.addURI(AUTHORITY, TRIGGER_TO_GEOFENCE_PATH, TRIGGER_TO_GEOFENCE);
                uriMatcher.addURI(AUTHORITY, CONTROL_REGION_PATH, CONTROL_REGION);
            }
            if (CONTENT_URI_GEOFENCE == null) {
                CONTENT_URI_GEOFENCE = Uri.parse(BASE_PATH + GEO_FENCE_PATH);
            }
            if (CONTENT_URI_TRIGGER == null) {
                CONTENT_URI_TRIGGER = Uri.parse(BASE_PATH + TRIGGER_PATH);
            }
            if (CONTENT_URI_ACTION == null) {
                CONTENT_URI_ACTION = Uri.parse(BASE_PATH + ACTION_PATH);
            }
            if (CONTENT_URI_TRIGGER_TO_GEOFENCE == null) {
                CONTENT_URI_TRIGGER_TO_GEOFENCE = Uri.parse(BASE_PATH + TRIGGER_TO_GEOFENCE_PATH);
            }
            if (CONTENT_URI_CONTROL_REGION == null) {
                CONTENT_URI_CONTROL_REGION = Uri.parse(BASE_PATH + CONTROL_REGION_PATH);
            }
        }
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        int uriType = uriMatcher.match(uri);
        switch (uriType) {
            case GEOFENCE:
                queryBuilder.setTables(DatabaseSQLContract.GeoFenceLocationEntry.TABLE_NAME);
                break;
            case TRIGGER:
                queryBuilder.setTables(DatabaseSQLContract.TriggerEntry.TABLE_NAME);
                break;
            case ACTION:
                queryBuilder.setTables(DatabaseSQLContract.ActionEntry.TABLE_NAME);
                break;
            case TRIGGER_TO_GEOFENCE:
                queryBuilder.setTables(DatabaseSQLContract.TriggerToGeoFencesEntry.TABLE_NAME);
                break;
            case CONTROL_REGION:
                queryBuilder.setTables(DatabaseSQLContract.ControlRegionEntry.TABLE_NAME);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        SQLiteDatabase db = database.getWritableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        Context context = getContext();
        if (context != null) {
            cursor.setNotificationUri(context.getContentResolver(), uri);
        }
        return cursor;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        int uriType = uriMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        long id = 0;
        switch (uriType) {
            case GEOFENCE:
                id = sqlDB.insertWithOnConflict(DatabaseSQLContract.GeoFenceLocationEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
                break;
            case TRIGGER:
                id = sqlDB.insertWithOnConflict(DatabaseSQLContract.TriggerEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
                break;
            case ACTION:
                id = sqlDB.insert(DatabaseSQLContract.ActionEntry.TABLE_NAME, null, values);
                break;
            case TRIGGER_TO_GEOFENCE:
                id = sqlDB.insert(DatabaseSQLContract.TriggerToGeoFencesEntry.TABLE_NAME, null, values);
                break;
            case CONTROL_REGION:
                id = sqlDB.insertWithOnConflict(DatabaseSQLContract.ControlRegionEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        Context context = getContext();
        if (context != null) {
            context.getContentResolver().notifyChange(uri, null);
        }
        return Uri.parse(BASE_PATH + id);
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        int uriType = uriMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsDeleted = 0;
        switch (uriType) {
            case GEOFENCE:
                rowsDeleted = sqlDB.delete(DatabaseSQLContract.GeoFenceLocationEntry.TABLE_NAME, selection,
                        selectionArgs);
                break;
            case TRIGGER:
                rowsDeleted = sqlDB.delete(DatabaseSQLContract.TriggerEntry.TABLE_NAME, selection,
                        selectionArgs);
                break;
            case ACTION:
                rowsDeleted = sqlDB.delete(DatabaseSQLContract.ActionEntry.TABLE_NAME, selection,
                        selectionArgs);
                break;
            case TRIGGER_TO_GEOFENCE:
                rowsDeleted = sqlDB.delete(DatabaseSQLContract.TriggerToGeoFencesEntry.TABLE_NAME, selection,
                        selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        Context context = getContext();
        if (context != null) {
            context.getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        int uriType = uriMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsUpdated = 0;
        switch (uriType) {
            case GEOFENCE:
                rowsUpdated = sqlDB.update(DatabaseSQLContract.GeoFenceLocationEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);
                break;
            case TRIGGER:
                rowsUpdated = sqlDB.update(DatabaseSQLContract.TriggerEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);
                break;
            case ACTION:
                rowsUpdated = sqlDB.update(DatabaseSQLContract.ActionEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);
                break;
            case TRIGGER_TO_GEOFENCE:
                rowsUpdated = sqlDB.update(DatabaseSQLContract.TriggerToGeoFencesEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);
                break;
            case CONTROL_REGION:
                rowsUpdated = sqlDB.update(DatabaseSQLContract.ControlRegionEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        Context context = getContext();
        if (context != null) {
            context.getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }
}
