package net.donky.location.geofence.analytics;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import java.util.Arrays;
import java.util.HashSet;

public class GeoContentProvider extends ContentProvider {

    private final static String AUTHORITY_POSTFIX = ".donky.geoservice.provider";

    // database
    private GeoDatabaseHelper database;

    // Used for the UriMacher
    private static final int GEO = 10;
    private static final int GEO_ID = 20;

    private static String AUTHORITY;

    private static final String BASE_PATH = "geos";

    private static Uri CONTENT_URI;

    private static UriMatcher uriMatcher;

    @Override
    public boolean onCreate() {
        createUriMatcher(getContext());
        database = new GeoDatabaseHelper(getContext());
        return true;
    }

    public static Uri getContentUri(Context context) {
        createUriMatcher(context);
        return CONTENT_URI;
    }

    private synchronized static void createUriMatcher(Context context) {

        if (context != null) {

            if (AUTHORITY == null) {
                AUTHORITY = context.getPackageName() + AUTHORITY_POSTFIX;
            }
            if (uriMatcher == null) {
                uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
                uriMatcher.addURI(AUTHORITY, BASE_PATH, GEO);
                uriMatcher.addURI(AUTHORITY, BASE_PATH + "/#", GEO_ID);
            }
            if (CONTENT_URI == null) {
                CONTENT_URI = Uri.parse("content://" + AUTHORITY
                        + "/" + BASE_PATH);
            }
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        // Using SQLiteQueryBuilder instead of query() method
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        // Check if the caller has requested a column which does not exists
        checkColumns(projection);

        // Set the table
        queryBuilder.setTables(AnalyticsTable.TABLE_NAME);

        int uriType = uriMatcher.match(uri);
        switch (uriType) {
            case GEO:
                break;
            case GEO_ID:
                // Adding the ID to the original query
                queryBuilder.appendWhere(AnalyticsTable.COLUMN_ID + "="
                        + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        SQLiteDatabase db = database.getWritableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection,
                selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = uriMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        long id = 0;
        switch (uriType) {
            case GEO:
                id = sqlDB.insert(AnalyticsTable.TABLE_NAME, null, values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(BASE_PATH + "/" + id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = uriMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsDeleted = 0;
        switch (uriType) {
            case GEO:
                rowsDeleted = sqlDB.delete(AnalyticsTable.TABLE_NAME, selection,
                        selectionArgs);
                break;
            case GEO_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = sqlDB.delete(
                            AnalyticsTable.TABLE_NAME,
                            AnalyticsTable.COLUMN_ID + "=" + id,
                            null);
                } else {
                    rowsDeleted = sqlDB.delete(
                            AnalyticsTable.TABLE_NAME,
                            AnalyticsTable.COLUMN_ID + "=" + id
                                    + " and " + selection,
                            selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {

        int uriType = uriMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsUpdated = 0;
        switch (uriType) {
            case GEO:
                rowsUpdated = sqlDB.update(AnalyticsTable.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);
                break;
            case GEO_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = sqlDB.update(AnalyticsTable.TABLE_NAME,
                            values,
                            AnalyticsTable.COLUMN_ID + "=" + id,
                            null);
                } else {
                    rowsUpdated = sqlDB.update(AnalyticsTable.TABLE_NAME,
                            values,
                            AnalyticsTable.COLUMN_ID + "=" + id
                                    + " and "
                                    + selection,
                            selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    private void checkColumns(String[] projection) {
        String[] available = {AnalyticsTable.COLUMN_TIME,
                AnalyticsTable.COLUMN_DESCRIPTION,
                AnalyticsTable.COLUMN_ACCURACY,
                AnalyticsTable.COLUMN_ID};
        if (projection != null) {
            HashSet<String> requestedColumns = new HashSet<String>(
                    Arrays.asList(projection));
            HashSet<String> availableColumns = new HashSet<String>(
                    Arrays.asList(available));
            // Check if all columns which are requested are available
            if (!availableColumns.containsAll(requestedColumns)) {
                throw new IllegalArgumentException(
                        "Unknown columns in projection");
            }
        }
    }
}
