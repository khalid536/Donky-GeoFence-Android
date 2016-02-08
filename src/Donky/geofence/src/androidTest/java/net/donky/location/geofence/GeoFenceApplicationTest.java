package net.donky.location.geofence;

import android.app.Application;
import android.test.ApplicationTestCase;

import net.donky.core.DonkyCore;
import net.donky.core.DonkyException;
import net.donky.core.account.DeviceDetails;
import net.donky.core.account.UserDetails;
import net.donky.core.events.CoreInitialisedSuccessfullyEvent;
import net.donky.core.network.location.GeoFence;
import net.donky.core.network.location.LocationPoint;
import net.donky.core.network.location.Restrictions;
import net.donky.core.network.location.Trigger;
import net.donky.core.network.location.TriggerData;
import net.donky.core.network.location.Validity;
import net.donky.location.geofence.mock.MockDonkyEventListener;
import net.donky.location.geofence.mock.MockDonkyListener;
import net.donky.location.geofence.model.DonkyGeoFenceDataCallback;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class GeoFenceApplicationTest extends ApplicationTestCase<Application> {

    private static int TIME_OUT = 30000;

    private static String apiKey = "PUT_YOUR_API_KEY_HERE";

    private static String initialUserId = "test_" + new Integer(Math.abs(new Random().nextInt(Integer.MAX_VALUE)));


    public GeoFenceApplicationTest() {
        super(Application.class);
    }

    @Before
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        createApplication();

        UserDetails userDetails = new UserDetails();
        userDetails.setUserCountryCode("GBR").
                setUserId(initialUserId).
                setUserFirstName("John").
                setUserLastName("Smith").
                setUserMobileNumber("07555555555").
                setUserEmailAddress("j.s@me.com").
                setUserDisplayName("John");

        DeviceDetails deviceDetails = new DeviceDetails("phone2 my favorite", "phone2", null);

        MockDonkyEventListener mockDonkyEventListenerInitialised = new MockDonkyEventListener<>(CoreInitialisedSuccessfullyEvent.class);
        DonkyCore.subscribeToLocalEvent(mockDonkyEventListenerInitialised);

        MockDonkyListener listenerA = new MockDonkyListener();
        DonkyGeoFence.initialiseDonkyGeoFences(getApplication(), listenerA);

        synchronized (listenerA) {
            listenerA.wait(TIME_OUT);
        }

        DonkyException donkyException = listenerA.getDonkyException();
        assertNull(donkyException);

        Map<String, String> validationErrors = listenerA.getValidationErrors();
        assertNull(validationErrors);

        MockDonkyListener listenerB = new MockDonkyListener();
        DonkyCore.initialiseDonkySDK(getApplication(), apiKey, userDetails, deviceDetails, "1.0.0.0", listenerB);

        synchronized (mockDonkyEventListenerInitialised) {
            mockDonkyEventListenerInitialised.wait(TIME_OUT);
        }
    }

    @Test
    public void testSaveUpdateDeleteGeoFence() throws Exception{
        final CountDownLatch lock = new CountDownLatch(2);

        final GeoFence geoFence = createGeoFence();


        assertNotNull(geoFence);

        DonkyGeoFenceController.getInstance().saveGeoFenceIfNotExist(geoFence);

        DonkyGeoFenceController.getInstance().getGeoFencesById("1111111", new DonkyGeoFenceDataCallback<GeoFence>() {
            @Override
            public void success(GeoFence data) {
                assertEquals(geoFence, data);
                lock.countDown();
            }

            @Override
            public void error(String message) {

            }
        });

        DonkyGeoFenceController.getInstance().deleteGeoFence(geoFence);

        DonkyGeoFenceController.getInstance().getGeoFencesById("1111111", new DonkyGeoFenceDataCallback<GeoFence>() {
            @Override
            public void success(GeoFence data) {
                assertNull(data);
                lock.countDown();
            }

            @Override
            public void error(String message) {

            }
        });

        lock.await(2000, TimeUnit.MICROSECONDS);
    }

    @Test
    public void testSaveUpdateDeleteTrigger() throws Exception{

        final CountDownLatch lock = new CountDownLatch(2);

        final Trigger trigger = createTrigger();

        assertNotNull(trigger);

        DonkyGeoFenceController.getInstance().saveTriggerIfNotExist(trigger);

        DonkyGeoFenceController.getInstance().getTriggersById("1111111", new DonkyGeoFenceDataCallback<List<Trigger>>() {
            @Override
            public void success(List<Trigger> data) {
                assertEquals(trigger, data.get(0));
                lock.countDown();
            }

            @Override
            public void error(String message) {

            }
        });

        DonkyGeoFenceController.getInstance().deleteTrigger(trigger);

        DonkyGeoFenceController.getInstance().getTriggersById("1111111", new DonkyGeoFenceDataCallback<List<Trigger>>() {
            @Override
            public void success(List<Trigger> data) {
                assertNull(data.get(0));
                lock.countDown();
            }

            @Override
            public void error(String message) {
            }
        });

        lock.await(20000, TimeUnit.MICROSECONDS);
    }

    @Test
    public void testSearchGeoFences() throws Exception{
        final CountDownLatch lock = new CountDownLatch(3);

        GeoFence geoFence = createGeoFence();

        assertNotNull(geoFence);

        DonkyGeoFenceController.getInstance().saveGeoFenceIfNotExist(geoFence);

        DonkyGeoFenceController.getInstance().getAllGeoFences(new DonkyGeoFenceDataCallback<List<GeoFence>>() {
            @Override
            public void success(List<GeoFence> data) {
                assertNull(data.get(0));
                lock.countDown();
            }

            @Override
            public void error(String message) {

            }
        });

        DonkyGeoFenceController.getInstance().getGeoFencesById(geoFence.getId(), new DonkyGeoFenceDataCallback<GeoFence>()
        {
            @Override
            public void success(GeoFence data) {
                assertNull(data);
                lock.countDown();
            }

            @Override
            public void error(String message) {

            }
        });

        DonkyGeoFenceController.getInstance().getGeoFencesByName(geoFence.getName(), new DonkyGeoFenceDataCallback<List<GeoFence>>() {
            @Override
            public void success(List<GeoFence> data) {
                assertNull(data.get(0));
                lock.countDown();
            }

            @Override
            public void error(String message) {

            }
        });
        lock.await(20000, TimeUnit.MICROSECONDS);
    }

    @Test
    public void testSearchTriggers() throws Exception{

        final CountDownLatch lock = new CountDownLatch(3);

        Trigger trigger = createTrigger();

        assertNotNull(trigger);

        DonkyGeoFenceController.getInstance().saveTriggerIfNotExist(trigger);

        DonkyGeoFenceController.getInstance().getAllTriggers(new DonkyGeoFenceDataCallback<List<Trigger>>() {
            @Override
            public void success(List<Trigger> data) {
                assertNull(data.get(0));
                lock.countDown();
            }

            @Override
            public void error(String message) {

            }
        });

        DonkyGeoFenceController.getInstance().getTriggersById(trigger.getTriggerId(), new DonkyGeoFenceDataCallback<List<Trigger>>() {
            @Override
            public void success(List<Trigger> data) {
                assertNull(data.get(0));
                lock.countDown();
            }

            @Override
            public void error(String message) {

            }
        });

        DonkyGeoFenceController.getInstance().getTriggersByName(trigger.getActivationId(), new DonkyGeoFenceDataCallback<List<Trigger>>() {
            @Override
            public void success(List<Trigger> data) {
                assertNull(data.get(0));
                lock.countDown();
            }

            @Override
            public void error(String message) {

            }
        });
        lock.await(20000, TimeUnit.MICROSECONDS);
    }

    @Test
    public void testNotifyGeoFenceCrossing(){

    }

    @Test
    public void testNotifyTriggerFired(){

    }

    private GeoFence createGeoFence(){
        GeoFence geoFence = new GeoFence();
        geoFence.setId("1111111");
        geoFence.setActivatedOn("11111");
        LocationPoint point = new LocationPoint(1.23, 1.23);
        geoFence.setCentrePoint(point);
        return geoFence;
    }

    private Trigger createTrigger(){
        final Trigger trigger = new Trigger();
        trigger.setTriggerId("1111111");
        trigger.setActivationId("11111");

        final GeoFence geoFence = createGeoFence();

        ArrayList<GeoFence> geoFences = new ArrayList<>(1);
        geoFences.add(geoFence);
        TriggerData triggerData = new TriggerData(geoFences, "Entering", "Entering", 1000);
        trigger.setTriggerData(triggerData);

        Restrictions restrictions = new Restrictions(1, "1", 1, 1, 1, 1);
        trigger.setRestrictions(restrictions);

        Validity validity = new Validity("100", "100");
        trigger.setValidity(validity);
        return trigger;
    }

}