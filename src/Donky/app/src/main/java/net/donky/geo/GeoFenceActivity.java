package net.donky.geo;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationRequest;

import net.donky.R;
import net.donky.core.DonkyCore;
import net.donky.core.events.OnPauseEvent;
import net.donky.core.events.OnResumeEvent;
import net.donky.location.DonkyLocationController;
import net.donky.location.geofence.DonkyGeoFence;
import net.donky.location.internal.LocationUpdatesCallback;
import net.donky.location.geofence.DonkyGeoFenceController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class GeoFenceActivity extends AppCompatActivity implements CustomLocationDialogFragment.CustomLocationUpdateListener{


    @InjectView(R.id.viewpager)
    ViewPager pager;
    @InjectView(R.id.sliding_tabs)
    TabLayout tabLayout;

    @InjectView(R.id.switch_monitoring_state_geo_fences)
    Switch switchGeoFences;

    @InjectView(R.id.switch_monitoring_location)
    Switch switchLocation;

    @InjectView(R.id.geofence_details_layout)
    LinearLayout geoFencesLayout;

    @InjectView(R.id.location_layout)
    LinearLayout locationLayout;

    @InjectView(R.id.txv_monitoring_geo_fences)
    TextView geoFencesText;

    @InjectView(R.id.txv_monitoring_location)
    TextView locationText;

    @InjectView(R.id.tv_lat)
    TextView locationLat;

    @InjectView(R.id.tv_lng)
    TextView locationLng;

    @InjectView(R.id.location_type)
    CheckBox loactionType;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.geo_fence_layout);
        ButterKnife.inject(this);
        pager.setAdapter(new TabsFragmentPagerAdapter(getFragmentManager(), setupPagerItems()));
        tabLayout.setupWithViewPager(pager);

        switchGeoFences.setOnCheckedChangeListener(onCheckedGeoFences);
        switchLocation.setOnCheckedChangeListener(onCheckedLocation);

        loactionType.setOnCheckedChangeListener(onCheckedLocationType);

    }

    private CompoundButton.OnCheckedChangeListener onCheckedGeoFences = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked){
                if (Build.VERSION.SDK_INT >= 23) {
                    startTrackingGeoGence();
                } else {
                    DonkyGeoFenceController.getInstance().startTrackingGeoFences();
                }
                geoFencesText.setText(getString(R.string.stop_monitoring_geo_fences));
                geoFencesLayout.setVisibility(View.VISIBLE);
            }else {
                DonkyGeoFenceController.getInstance().stopTrackingGeoFences();
                geoFencesText.setText(getString(R.string.start_monitoring_geo_fences));
                geoFencesLayout.setVisibility(View.GONE);
            }
            invalidateOptionsMenu();
        }
    };

    private CompoundButton.OnCheckedChangeListener onCheckedLocation = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked){
                if (Build.VERSION.SDK_INT >= 23) {
                    startTrackingLocation();
                } else {
                    DonkyLocationController.getInstance().registerLocationListener(locationUpdatesCallback);
                }
                locationText.setText(getString(R.string.stop_monitoring_location));
                locationLayout.setVisibility(View.VISIBLE);
            }else {
                DonkyLocationController.getInstance().unregisterLocationListener(locationUpdatesCallback);
                locationText.setText(getString(R.string.start_monitoring_location));
                ((TextView)findViewById(R.id.tv_lat)).setText("");
                ((TextView) findViewById(R.id.tv_lng)).setText("");
                locationLayout.setVisibility(View.GONE);
            }
        }
    };

    private CompoundButton.OnCheckedChangeListener onCheckedLocationType = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (!isChecked){
                DialogFragment newFragment = new CustomLocationDialogFragment();
                newFragment.show(getFragmentManager(), "dialog");
            }
        }
    };

    private List<TabsFragmentPagerAdapter.PagerItem> setupPagerItems(){
        List<TabsFragmentPagerAdapter.PagerItem> pagerItems = new ArrayList<>(2);
        TabsFragmentPagerAdapter.PagerItem pagerItem1
                = new TabsFragmentPagerAdapter.PagerItem(GeoFenceFragment.class, getString(R.string.geofences));
        TabsFragmentPagerAdapter.PagerItem pagerItem2
                = new TabsFragmentPagerAdapter.PagerItem(TriggersFragment.class, getString(R.string.triggers));
        pagerItems.add(pagerItem1);
        pagerItems.add(pagerItem2);
        return pagerItems;
    }

    @Override
    protected void onResume() {
        super.onResume();

        // For Analytic Module
        DonkyCore.publishLocalEvent(new OnResumeEvent());

    }

    @Override
    protected void onPause() {
        super.onPause();

        if (switchLocation.isChecked())
            DonkyLocationController.getInstance().unregisterLocationListener(locationUpdatesCallback);

        // For Analytic Module
        DonkyCore.publishLocalEvent(new OnPauseEvent());

    }

    private LocationUpdatesCallback locationUpdatesCallback = new LocationUpdatesCallback() {
        @Override
        public void onConnected(Bundle bundle) {

        }

        @Override
        public void onLocationChanged(Location location) {
            ((TextView)findViewById(R.id.tv_lat)).setText(String.valueOf(location.getLatitude()));
            ((TextView)findViewById(R.id.tv_lng)).setText(String.valueOf(location.getLongitude()));
        }

    };

    @Override
    public void customLocation(LocationRequest locationRequest) {

        //Unregister Previous one
        DonkyLocationController.getInstance().unregisterLocationListener(locationUpdatesCallback);

        DonkyLocationController.getInstance().registerLocationListener(locationUpdatesCallback, locationRequest);

    }

    @Override
    public void cancel() {
        loactionType.setChecked(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.map:
                Intent intent = new Intent(GeoFenceActivity.this, DonkyGeoMap.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.map).setVisible(switchGeoFences.isChecked());
        return true;
    }

    final private int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS_LOCATION = 124;
    final private int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS_GEOFENCE = 125;

    @TargetApi(23)
    private void startTrackingLocation() {
        List<String> permissionsList = permissions();
        if (!permissionsList.isEmpty()) {
            requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                    REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS_LOCATION);
            return;
        }
        DonkyLocationController.getInstance().registerLocationListener(locationUpdatesCallback);
    }

    @TargetApi(23)
    private void startTrackingGeoGence() {
        List<String> permissionsList = permissions();
        if (!permissionsList.isEmpty()) {
            requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                    REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS_GEOFENCE);
            return;
        }
        DonkyGeoFenceController.getInstance().startTrackingGeoFences();
    }

    private List<String> permissions(){
        List<String> permissionsNeeded = new ArrayList<String>();
        final List<String> permissionsList = new ArrayList<String>();
        if (!addPermission(permissionsList, Manifest.permission.ACCESS_FINE_LOCATION))
            permissionsNeeded.add("ACCESS_FINE_LOCATION");
        if (!addPermission(permissionsList, Manifest.permission.ACCESS_COARSE_LOCATION))
            permissionsNeeded.add("ACCESS_COARSE_LOCATION");
        return permissionsList;

    }

    @TargetApi(23)
    private boolean addPermission(List<String> permissionsList, String permission) {
        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            // Check for Rationale Option
            if (!shouldShowRequestPermissionRationale(permission))
                return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS_LOCATION:
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS_GEOFENCE:
            {
                Map<String, Integer> perms = new HashMap<String, Integer>();
                // Initial
                perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.READ_CONTACTS, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.WRITE_CONTACTS, PackageManager.PERMISSION_GRANTED);
                // Fill with results
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);
                // Check for ACCESS_FINE_LOCATION
                if (perms.get(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                    // All Permissions Granted
                    if (requestCode == REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS_LOCATION) {
                        DonkyLocationController.getInstance().registerLocationListener(locationUpdatesCallback);
                    } else if (requestCode == REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS_GEOFENCE){
                        DonkyGeoFenceController.getInstance().startTrackingGeoFences();
                    }
                } else {
                    // Permission Denied
                    Toast.makeText(GeoFenceActivity.this, "Some Permission is Denied", Toast.LENGTH_SHORT)
                            .show();

                    if (requestCode == REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS_LOCATION) {
                        switchLocation.setChecked(false);
                        onCheckedLocation.onCheckedChanged(switchLocation, false);
                    } else if (requestCode == REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS_GEOFENCE){
                        onCheckedGeoFences.onCheckedChanged(switchGeoFences, false);
                        switchGeoFences.setChecked(false);
                    }
                }
            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
