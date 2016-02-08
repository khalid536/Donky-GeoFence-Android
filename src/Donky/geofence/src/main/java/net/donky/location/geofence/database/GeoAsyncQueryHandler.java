package net.donky.location.geofence.database;

import android.content.AsyncQueryHandler;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

/**
 * GeoAsyncQueryHandler class for background insert/update/query/delete operations
 */
public class GeoAsyncQueryHandler extends AsyncQueryHandler {

    private QueryHandlerCompleteListener queryHandlerCompleteListener;
    private QueryHandlerInsertListener queryHandlerInsertListener;
    private QueryHandlerUpdateListener queryHandlerUpdateListener;
    private QueryHandlerDeleteListener queryHandlerDeleteListener;

    public GeoAsyncQueryHandler(Context context){
        super(context.getContentResolver());
    }

    @Override
    protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
        if (queryHandlerCompleteListener != null)
            queryHandlerCompleteListener.onQueryComplete(token, cookie, cursor);
    }

    @Override
    protected void onInsertComplete(int token, Object cookie, Uri uri) {
        if (queryHandlerInsertListener != null)
            queryHandlerInsertListener.onInsertComplete(token, cookie, uri);
    }

    @Override
    protected void onUpdateComplete(int token, Object cookie, int result) {
        if (queryHandlerUpdateListener != null)
            queryHandlerUpdateListener.onUpdateComplete(token, cookie, result);
    }

    @Override
    protected void onDeleteComplete(int token, Object cookie, int result) {
        if (queryHandlerDeleteListener != null)
            queryHandlerDeleteListener.onDeleteComplete(token, cookie, result);
    }

    public GeoAsyncQueryHandler setQueryHandlerCompleteListener(QueryHandlerCompleteListener queryHandlerCompleteListener) {
        this.queryHandlerCompleteListener = queryHandlerCompleteListener;
        return this;
    }

    public GeoAsyncQueryHandler setQueryHandlerInsertListener(QueryHandlerInsertListener queryHandlerInsertListener) {
        this.queryHandlerInsertListener = queryHandlerInsertListener;
        return this;
    }

    public GeoAsyncQueryHandler setQueryHandlerUpdateListener(QueryHandlerUpdateListener queryHandlerUpdateListener) {
        this.queryHandlerUpdateListener = queryHandlerUpdateListener;
        return this;
    }

    public GeoAsyncQueryHandler setQueryHandlerDeleteListener(QueryHandlerDeleteListener queryHandlerDeleteListener) {
        this.queryHandlerDeleteListener = queryHandlerDeleteListener;
        return this;
    }

    public interface QueryHandlerCompleteListener {
        void onQueryComplete(int token, Object cookie, Cursor cursor);
    }

    public interface QueryHandlerInsertListener {
        void onInsertComplete(int token, Object cookie, Uri uri);
    }

    public interface QueryHandlerUpdateListener {
        void onUpdateComplete(int token, Object cookie, int result);
    }

    public interface QueryHandlerDeleteListener {
        void onDeleteComplete(int token, Object cookie, int result);
    }
}
