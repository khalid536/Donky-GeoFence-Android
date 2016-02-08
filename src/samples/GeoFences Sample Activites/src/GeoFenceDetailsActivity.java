package net.donky.geo;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import net.donky.R;

public class GeoFenceDetailsActivity extends FragmentActivity {

    public static final String GEO_TYPE = "geo_type";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.geo_fence_details_acvtivity);
        String type = getIntent().getExtras().getString(GEO_TYPE);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        if (type != null){
            findViewById(R.id.content_frame).setVisibility(View.VISIBLE);
            GeoFenceDetailFragment fenceDetailFragment = new GeoFenceDetailFragment();
            fenceDetailFragment.setArguments(getIntent().getExtras());
            transaction.add(R.id.content, fenceDetailFragment).commit();
        }else {
            TriggersDetailFragment triggersDetailFragment = new TriggersDetailFragment();
            triggersDetailFragment.setArguments(getIntent().getExtras());
            transaction.add(R.id.content, triggersDetailFragment).commit();
        }
    }
}
