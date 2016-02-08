package net.donky.location.geofence;

import android.content.Intent;
import android.test.ServiceTestCase;

import net.donky.location.geofence.internal.GeoFencingIntentService;

public class GeoFenceServiceTest extends ServiceTestCase<GeoFencingIntentService> {

    public GeoFenceServiceTest(){
        super(GeoFencingIntentService.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    public void testNewAreaNullLocation()
    {
        Intent intent = new Intent(getContext(), GeoFencingIntentService.class);
        startService(intent);
    }
}
