package net.donky.geo;

import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import net.donky.R;
import net.donky.location.geofence.DonkyGeoFence;
import net.donky.location.geofence.DonkyGeoFenceController;
import net.donky.location.geofence.database.CursorUtill;
import net.donky.location.geofence.database.GeoFenceContentProvider;
import net.donky.location.geofence.database.GeoFenceDAO;
import net.donky.location.geofence.analytics.GeoContentProvider;
import net.donky.location.geofence.analytics.AnalyticsTable;
import net.donky.location.geofence.model.ControlRegion;
import net.donky.location.geofence.model.DonkyGeoFenceDataCallback;
import net.donky.core.network.location.GeoFence;

import java.util.Date;
import java.util.List;

public class DonkyGeoMap extends FragmentActivity implements LoaderManager.LoaderCallbacks<Cursor>, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private GoogleApiClient googleApiClient;
    private GoogleMap map;

    private String[] mPlanetTitles = {"1", "2", "3","4"};

    private String mTitle = "Title";
    private String mDrawerTitle = "DrawerTitle";

    private EditText mockLocationAccuracy;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private View mLeftDrawerView;
    private View mRightDrawerView;
    private ListView listview;

    private Switch mockSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.geo_map_activity);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        mockSwitch = (Switch)findViewById(R.id.mock_switch);

        mLeftDrawerView = findViewById(R.id.left_drawer);
        mockLocationAccuracy = (EditText)findViewById(R.id.accuracy_edt);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_geo);
        mapFragment.getMapAsync(this);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.mipmap.ic_drawer,  /* nav drawer icon to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        ) {

            public void onDrawerClosed(View drawerView) {
                if(drawerView.equals(mLeftDrawerView)) {
                    supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                    mDrawerToggle.syncState();
                }
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                if(drawerView.equals(mLeftDrawerView)) {
                    supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                    mDrawerToggle.syncState();
                }
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        loadGeoHistory();

        getSupportLoaderManager().initLoader(1, null, this);
        getSupportLoaderManager().initLoader(2, null, this);
    }

    private void toggleLocationStatus(boolean isEnable){
        if (googleApiClient.isConnected()) {
            try {
                LocationServices.FusedLocationApi.setMockMode(googleApiClient, isEnable);
            }catch (SecurityException e){
                Toast.makeText(DonkyGeoMap.this, "Please enable mock location in developer options", Toast.LENGTH_SHORT).show();
                mockSwitch.setChecked(false);
                return;
            }
            if (isEnable) {
                map.setOnMapClickListener(listener);
            } else {
                map.setOnMapClickListener(null);
            }
            findViewById(R.id.accuracy_layout).setVisibility(isEnable ? View.VISIBLE : View.GONE);
        }else
            Toast.makeText(this, "Google API is not available, Please wait", Toast.LENGTH_SHORT).show();
    }

    private List<GeoFence> data;

    private void setGeo(Cursor cursor){
        if (cursor.isClosed() || !cursor.moveToFirst() || cursor.getCount() == 0)
            return;
        data = CursorUtill.cursorToGeoFence(cursor);
    }



    private void drawGeoFence(){
        DonkyGeoFenceController.getInstance().getAllGeoFences(new DonkyGeoFenceDataCallback<List<GeoFence>>() {
            @Override
            public void success(List<GeoFence> data) {
                LatLng location = null;
                for (GeoFence point : data) {
                    location = new LatLng(point.getCentrePoint().getLatitude(), point.getCentrePoint().getLongitude());
                    map.addMarker(new MarkerOptions().position(location).title(point.getName()));
                    CircleOptions circleOptions = new CircleOptions().center(location).radius(point.getRadiusMetres());
                    if (point.getRealDistanceToBorder() <= 0)
                        circleOptions.strokeColor(getResources().getColor(android.R.color.holo_green_dark));
                    else
                        circleOptions.strokeColor(getResources().getColor(R.color.donky_default));
                    map.addCircle(circleOptions);
                }
            }

            @Override
            public void error(String message) {

            }
        });


    }

    private ControlRegion controlRegion;

    private void setCursorRegion(Cursor cursor){
        if (!cursor.moveToFirst() || cursor.getCount() == 0)
            return;
        controlRegion = CursorUtill.cursorControlRegion(cursor);
    }

    private void drawControlRegion(){
        if (controlRegion == null)
            return;
        LatLng location = new LatLng(controlRegion.getLatitude(), controlRegion.getLongitude());
        CircleOptions circleOptions = new CircleOptions().center(location).radius(controlRegion.getRadiusMetres());
        circleOptions.strokeColor(getResources().getColor(android.R.color.holo_red_dark));
        map.addCircle(circleOptions);
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        int t = loader.getId();
        switch (loader.getId()){
            case 0:
                adapter.swapCursor(data);
                break;
            case 1:
                setCursorRegion(data);
                updateMap();
                break;
            case 2:
                setGeo(data);
                updateMap();
                break;

        }
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
        switch (loader.getId()){
            case 0:
                adapter.swapCursor(null);
                break;

        }

    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == 0) {
            String[] projection = {AnalyticsTable.COLUMN_ID, AnalyticsTable.COLUMN_TIME,
                    AnalyticsTable.COLUMN_DESCRIPTION, AnalyticsTable.COLUMN_ACCURACY};
            return new CursorLoader(this,
                    GeoContentProvider.CONTENT_URI, projection, null, null, null);
        }else if (id == 1){
            return new CursorLoader(this,
                    GeoFenceContentProvider.CONTENT_URI_CONTROL_REGION, GeoFenceDAO.allControlRegion, null, null, null);
        }else if (id == 2){
            return new CursorLoader(this,
                    GeoFenceContentProvider.CONTENT_URI_GEOFENCE, GeoFenceDAO.allColumnsGeoFence, null, null, null);
        }
        return null;
    }

    private SimpleCursorAdapter adapter;

    private void loadGeoHistory() {
        // Fields from the database (projection)
        // Must include the _id column for the adapter to work
        String[] from = new String[] { AnalyticsTable.COLUMN_ID, AnalyticsTable.COLUMN_TIME,
                AnalyticsTable.COLUMN_DESCRIPTION, AnalyticsTable.COLUMN_ACCURACY};
        // Fields on the UI to which we map
        int[] to = new int[] { R.id.rowid, R.id.time, R.id.description, R.id.accuracy};
        getSupportLoaderManager().initLoader(0, null, this);
        adapter = new SimpleCursorAdapter(this, R.layout.row, null, from,
                to, 0);
        listview = (ListView) findViewById(R.id.right_drawer);
        listview.setAdapter(adapter);
    }

    @Override
    public void onMapReady(final GoogleMap map) {
        this.map = map;
        map.setMyLocationEnabled(true);
        map.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                setLocation(location);
            }
        });

    }

    private void updateMap(){
        if (map != null) {
            map.clear();
            drawGeoFence();
            drawControlRegion();
        }
    }

    GoogleMap.OnMapClickListener listener = new GoogleMap.OnMapClickListener() {
        @Override
        public void onMapClick(LatLng latLng) {
            if (googleApiClient.isConnected()) {
                String accuracy = mockLocationAccuracy.getText().toString();
                Location location = new Location("network");
                location.setLatitude(latLng.latitude);
                location.setLongitude(latLng.longitude);
                location.setTime(new Date().getTime());
                location.setAccuracy(Float.parseFloat(accuracy));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    location.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
                }
                LocationServices.FusedLocationApi.setMockLocation(googleApiClient, location);
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));
            }
        }
    };

    private CompoundButton.OnCheckedChangeListener checkedMockLocationChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (!isMockSettingsON(DonkyGeoMap.this)) {
                Toast.makeText(DonkyGeoMap.this, "Please enable mock location in developer options", Toast.LENGTH_SHORT).show();
                mockSwitch.setChecked(false);
            }
            if (googleApiClient.isConnected()) {
                toggleLocationStatus(isChecked);
            }else {
                Toast.makeText(DonkyGeoMap.this, "Google API is not available, Please wait", Toast.LENGTH_SHORT).show();
                mockSwitch.setChecked(false);
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.map_activty, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch(item.getItemId()){
            case R.id.history:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title.toString();
    }

    @Override
    public void onConnected(Bundle bundle) {
        LocationRequest request = new LocationRequest();
        request.setInterval(100);
        request.setFastestInterval(100);
        request.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        request.setSmallestDisplacement(1);
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, request, this);
        mockSwitch.setOnCheckedChangeListener(checkedMockLocationChangeListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (googleApiClient != null && googleApiClient.isConnected()) {
            map.setOnMyLocationChangeListener(null);
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            if (mockSwitch.isChecked())
                LocationServices.FusedLocationApi.setMockMode(googleApiClient, false);
            googleApiClient.disconnect();
        }
        finish();
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(DonkyGeoFence.TAG, "location.hasAccuracy() =  " + location.hasAccuracy() + ", location.getAccuracy() = " + location.getAccuracy());

        if (!location.hasAccuracy()) {
            return;
        }
        if (location.getAccuracy() > 45) {
            return;
        }
        setLocation(location);
    }

    private void setLocation(Location location){
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public boolean isMockSettingsON(Context context) {
        // returns true if mock location enabled, false if not enabled.
        return !Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ALLOW_MOCK_LOCATION).equals("0");
    }
}
