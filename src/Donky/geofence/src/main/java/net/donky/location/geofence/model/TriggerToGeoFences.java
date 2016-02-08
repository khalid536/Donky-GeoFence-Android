package net.donky.location.geofence.model;

import java.util.Date;
import java.util.List;

public class TriggerToGeoFences {

    private String triggerId;
    private String condition;
    private List<String> geoFencesId;
    private String direction;
    private int time;
    private long enterTime;
    private long exitTime;
    private long dwellTime;

    public TriggerToGeoFences(String triggerId, String condition, List<String> geoFencesId, String direction, int time, long enterTime, long exitTime, long dwellTime) {
        this.triggerId = triggerId;
        this.condition = condition;
        this.geoFencesId = geoFencesId;
        this.direction = direction;
        this.time = time;
        this.enterTime = enterTime;
        this.exitTime = exitTime;
        this.dwellTime = dwellTime;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getTriggerId() {
        return triggerId;
    }

    public void setTriggerId(String triggerId) {
        this.triggerId = triggerId;
    }

    public List<String> getGeoFencesId() {
        return geoFencesId;
    }

    public void setGeoFencesId(List<String> geoFencesId) {
        this.geoFencesId = geoFencesId;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("TriggerToGeoFences{");
        sb.append("triggerId='").append(triggerId).append('\'');
        sb.append(", condition='").append(condition).append('\'');
        sb.append(", geoFencesId=").append(geoFencesId);
        sb.append(", direction='").append(direction).append('\'');
        sb.append(", time=").append(time);
        sb.append(", enterTime=").append(new Date(enterTime).toString());
        sb.append(", exitTime=").append(new Date(exitTime).toString());
        sb.append(", dwellTime=").append(new Date(dwellTime)).toString();
        sb.append('}');
        return sb.toString();
    }
}
