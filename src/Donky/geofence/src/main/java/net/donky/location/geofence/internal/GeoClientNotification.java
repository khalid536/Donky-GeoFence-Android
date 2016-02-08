package net.donky.location.geofence.internal;

import android.location.Location;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import net.donky.core.helpers.DateAndTimeHelper;
import net.donky.core.network.ClientNotification;
import net.donky.core.network.location.GeoFence;
import net.donky.core.network.location.Trigger;

import org.json.JSONException;
import org.json.JSONObject;

public class GeoClientNotification extends ClientNotification {
	
	/**
	 * Client Notifications types.
	 */
	public enum GeoNotificationType {
		GeoFenceCrossed,
		TriggerExecuted,
		GeoFenceDeploymentStatus

	}

	protected GeoClientNotification(String type, String id) {
		super(type, id);
	}

	/**
	 * Create 'Location Crossed' client notification.
	 *
	 * @param geoFence Description of the message.
	 * @return 'Location Crossed' Client Notification
	 */
	public static ClientNotification createLocationCrossedNotification(GeoFence geoFence, String direction) {

		GeoClientNotification n = new GeoClientNotification(GeoNotificationType.GeoFenceCrossed.toString(), geoFence.getId());

		Gson gson = new Gson();

		try {

			n.data = new JSONObject(gson.toJson(createLocationCrossed(geoFence, direction)));

		} catch (JSONException e) {

			e.printStackTrace();

		}

		return n;
	}

	public static ClientNotification createStatusLocationNotification(GeoFence geoFence, boolean isSuccess) {

		GeoClientNotification n = new GeoClientNotification(GeoNotificationType.GeoFenceDeploymentStatus.toString(), geoFence.getId());

		Gson gson = new Gson();

		try {

			n.data = new JSONObject(gson.toJson(createTrackedLocationStatus(geoFence, isSuccess)));

		} catch (JSONException e) {

			e.printStackTrace();

		}

		return n;
	}

	public static ClientNotification createTriggerExecutedNotification(GeoFence geoFence, Trigger trigger, Location location, String direction) {

		GeoClientNotification n = new GeoClientNotification(GeoNotificationType.TriggerExecuted.toString(), trigger.getTriggerId());

		Gson gson = new Gson();

		try {

			n.data = new JSONObject(gson.toJson(createTriggerExecuted(geoFence, trigger, location, direction)));

		} catch (JSONException e) {

			e.printStackTrace();

		}

		return n;
	}

	public static ClientNotification createTriggerExecutedNotification(Trigger trigger,String direction) {

	  GeoClientNotification n = new GeoClientNotification(GeoNotificationType.TriggerExecuted.toString(), trigger.getTriggerId());

	  Gson gson = new Gson();

	  try {

		n.data = new JSONObject(gson.toJson(createTriggerExecuted(trigger, direction)));

	  } catch (JSONException e) {

		e.printStackTrace();

	  }

	  return n;
	}

	private static TriggerExecuted createTriggerExecuted(GeoFence geoFence, Trigger trigger, Location location, String direction){
		TriggerExecuted triggerExecuted = new TriggerExecuted();
		triggerExecuted.triggerId = trigger.getTriggerId();
		triggerExecuted.location = createCentrePoint(location.getLatitude(), location.getLongitude());
		triggerExecuted.timeStamp = DateAndTimeHelper.getCurrentUTCTime();
		triggerExecuted.triggerType = "GeoFence";
		triggerExecuted.radiusMetres = geoFence.getRadiusMetres();
		triggerExecuted.triggerDirection = direction;
		triggerExecuted.type = GeoNotificationType.TriggerExecuted.toString();
		return triggerExecuted;
	}

  private static TriggerExecuted createTriggerExecuted(Trigger trigger, String direction){
	TriggerExecuted triggerExecuted = new TriggerExecuted();
	triggerExecuted.triggerId = trigger.getTriggerId();
	triggerExecuted.location = null;
	triggerExecuted.timeStamp = DateAndTimeHelper.getCurrentUTCTime();
	triggerExecuted.triggerType = "GeoFence";
	triggerExecuted.radiusMetres = 0;
	triggerExecuted.triggerDirection = direction;
	triggerExecuted.type = GeoNotificationType.TriggerExecuted.toString();
	return triggerExecuted;
  }


  private static TrackedLocationStatus createTrackedLocationStatus(GeoFence geoFence, boolean isSuccess){
		TrackedLocationStatus trackedLocationStatus = new TrackedLocationStatus();
		trackedLocationStatus.id = geoFence.getId();
		trackedLocationStatus.type = GeoNotificationType.GeoFenceDeploymentStatus.toString();
		trackedLocationStatus.activationId = geoFence.getActivationId();
		trackedLocationStatus.activatedOn = geoFence.getActivatedOn();
		trackedLocationStatus.processedOn = DateAndTimeHelper.getCurrentUTCTime();
		trackedLocationStatus.success = isSuccess;
		return trackedLocationStatus;
	}

	private static LocationCrossed createLocationCrossed(GeoFence geoFence, String direction){
		LocationCrossed crossed = new LocationCrossed();
		crossed.id = geoFence.getId();
		crossed.location = createCentrePoint(geoFence.getCentrePoint().getLatitude(),
				geoFence.getCentrePoint().getLongitude());
		crossed.direction = direction;
		crossed.timestamp = DateAndTimeHelper.getCurrentUTCTime();
		crossed.timeInRegionSeconds = 0;
		crossed.type = GeoNotificationType.GeoFenceCrossed.toString();
		return crossed;
	}

	private static CentrePoint createCentrePoint(double lat, double lng){
		return new CentrePoint(lat, lng);
	}

	private static class LocationCrossed {
		@SerializedName("Id")
		private String id;
		@SerializedName("Location")
		private CentrePoint location;
		@SerializedName("Direction")
		private String direction;
		@SerializedName("Timestamp")
		private String timestamp;
		@SerializedName("TimeInRegionSeconds")
		private int timeInRegionSeconds;
		@SerializedName("type")
		private String type;
	}

	private static class CentrePoint {
		@SerializedName("Latitude")
		protected double latitude;
		@SerializedName("Longitude")
		protected double longitude;
		
		public CentrePoint(double latitude, double longitude) {
			this.latitude = latitude;
			this.longitude = longitude;
		}
	}
	

	
	private static class TriggerExecuted {
		@SerializedName("TriggerId")
		private String triggerId;
		@SerializedName("TriggerType")
		private String triggerType;
		@SerializedName("TimeStamp")
		private String timeStamp;
		@SerializedName("type")
		private String type;
		@SerializedName("Location")
		private CentrePoint location;
		@SerializedName("radiusMetres")
		private int radiusMetres;
		@SerializedName("triggerDirection")
		private String triggerDirection;
	}
	
	private static class TrackedLocationStatus {
		@SerializedName("id")
		private String id;
		@SerializedName("type")
		private String type;
		@SerializedName("activationId")
		private String activationId;
		@SerializedName("activatedOn")
		private String activatedOn;
		@SerializedName("processedOn")
		private String processedOn;
		@SerializedName("Success")
		private boolean success;
	}

}