package net.donky.geo;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import net.donky.R;
import net.donky.core.network.location.GeoFence;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.InjectView;

public class GeoFenceDetailFragment extends BaseFragment implements OnMapReadyCallback {

    private FragmentActivity myContext;

    @InjectView(R.id.geo_name)
    TextView geoName;

    @InjectView(R.id.geo_labels)
    TextView geoLabels;

    @InjectView(R.id.geo_created_on)
    TextView geoCreate;

    @InjectView(R.id.geo_updated_on)
    TextView geoUpdate;

    @InjectView(R.id.geo_last_entered)
    TextView geoEntered;

    @InjectView(R.id.geo_last_exit)
    TextView geoExited;

    @InjectView(R.id.geo_is_inside)
    TextView geoInside;

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        if (activity instanceof FragmentActivity)
            myContext = (FragmentActivity) activity;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.geo_fence_detail_fragment, container, false);
        SupportMapFragment mapFragment = (SupportMapFragment) myContext.getSupportFragmentManager()
                .findFragmentById(R.id.map_geo_details);
        mapFragment.getMapAsync(this);
        return view;
    }

    private GeoFence geoFence;

    public static final String GEOFENCE = " geofence";

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        geoFence = (GeoFence)getArguments().getSerializable(GEOFENCE);
        populateGeoFence();
    }


    private void populateGeoFence() {
        if (geoFence == null)
            return;
        DateFormat formatter = new SimpleDateFormat("yyyy/MMM/dd HH:mm:ss");
        StringBuilder builder = new StringBuilder(geoFence.getLabels().length);
        for (int i = 0; i < geoFence.getLabels().length; ++i) {
            builder.append(geoFence.getLabels()[i]);
            if (i != geoFence.getLabels().length - 1)
                builder.append(", ");
        }
        geoName.setText(geoFence.getName());
        geoLabels.setText(builder.toString());
        geoCreate.setText(geoFence.getCreatedOn());
        geoUpdate.setText(geoFence.getUpdatedOn());
        geoInside.setText(geoFence.getRealDistanceToBorder() < 0 ? "Yes" : "No");

        Date date = new Date(geoFence.getLastEnter());
        geoEntered.setText(formatter.format(date));
        date = new Date(geoFence.getLastExit());
        geoExited.setText(formatter.format(date));
    }

    @Override
    public void onMapReady(GoogleMap map) {
        map.setMyLocationEnabled(true);
        LatLng location = new LatLng(geoFence.getCentrePoint().getLatitude(), geoFence.getCentrePoint().getLongitude());
        map.addMarker(new MarkerOptions().position(location).title(geoFence.getName()));
        CircleOptions circleOptions = new CircleOptions().center(location).radius(geoFence.getRadiusMetres());
        if (geoFence.getRealDistanceToBorder() < 0)
            circleOptions.strokeColor(getResources().getColor(android.R.color.holo_green_dark));
        else
            circleOptions.strokeColor(getResources().getColor(R.color.donky_default));
        map.addCircle(circleOptions);

        final CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(location)
                .zoom(17)
                .bearing(90)
                .tilt(30)
                .build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

    }
}
