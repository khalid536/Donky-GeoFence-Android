package net.donky.location.geofence.model;

import java.util.Date;
import java.util.List;

public class TriggerToGeoFence {

    private String triggerId;
    private String condition;
    private String geoFencesId;
    private String direction;
    private int time;
    private long enterTime;
    private long exitTime;
    private long dwellTime;

    public TriggerToGeoFence(String triggerId, String condition, String geoFencesId, String direction, int time, long enterTime, long exitTime, long dwellTime) {
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

    public String getGeoFencesId() {
        return geoFencesId;
    }

    public void setGeoFencesId(String geoFencesId) {
        this.geoFencesId = geoFencesId;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public long getEnterTime() {
        return enterTime;
    }

    public void setEnterTime(long enterTime) {
        this.enterTime = enterTime;
    }

    public long getExitTime() {
        return exitTime;
    }

    public void setExitTime(long exitTime) {
        this.exitTime = exitTime;
    }

    public long getDwellTime() {
        return dwellTime;
    }

    public void setDwellTime(long dwellTime) {
        this.dwellTime = dwellTime;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("TriggerToGeoFence{");
        sb.append("triggerId='").append(triggerId).append('\'');
        sb.append(", condition='").append(condition).append('\'');
        sb.append(", geoFencesId='").append(geoFencesId).append('\'');
        sb.append(", direction='").append(direction).append('\'');
        sb.append(", time=").append(time);
        sb.append(", enterTime=").append(new Date(enterTime).toString());
        sb.append(", exitTime=").append(new Date(exitTime).toString());
        sb.append(", dwellTime=").append(new Date(dwellTime).toString());
        sb.append('}');
        return sb.toString();
    }
}
