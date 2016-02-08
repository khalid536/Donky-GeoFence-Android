package net.donky.geo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import net.donky.NotificationProcessor;
import net.donky.R;
import net.donky.core.DonkyCore;
import net.donky.core.events.OnCreateEvent;
import net.donky.core.events.OnPauseEvent;
import net.donky.core.events.OnResumeEvent;
import net.donky.location.DonkyLocationController;
import net.donky.location.internal.LocationUpdatesCallback;
import net.donky.location.geofence.DonkyGeoFenceController;
import net.donky.location.DonkyLocationController;

import java.util.ArrayList;
import java.util.List;

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

        DonkyCore.publishLocalEvent(new OnCreateEvent(getIntent()));


        switchGeoFences.setOnCheckedChangeListener(onCheckedGeoFences);
        switchLocation.setOnCheckedChangeListener(onCheckedLocation);

        loactionType.setOnCheckedChangeListener(onCheckedLocationType);

    }

    private CompoundButton.OnCheckedChangeListener onCheckedGeoFences = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked){
                DonkyGeoFenceController.getInstance().startTrackingGeoFences();
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
                DonkyLocationController.getInstance().registerLocationListener(locationUpdatesCallback);
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
}
