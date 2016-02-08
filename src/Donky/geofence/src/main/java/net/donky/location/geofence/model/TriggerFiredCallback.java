package net.donky.location.geofence.model;

import net.donky.core.network.location.Trigger;

public interface TriggerFiredCallback {

    void fired(Trigger trigger);

}
