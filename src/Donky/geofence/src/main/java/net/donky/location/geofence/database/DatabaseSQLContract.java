package net.donky.location.geofence.database;

import android.provider.BaseColumns;

/**
 * Created by Igor Bykov
 * 04/09/15.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class DatabaseSQLContract {

    /* Defines the table contents for Tracking */
    public static abstract class GeoFenceLocationEntry implements BaseColumns {

        public static final String TABLE_NAME = "Location";

        public static final String COLUMN_NAME_SERVER_ID = "serverId";
        public static final String COLUMN_NAME_APPLICATION_ID = "applicationId";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_RADIUS = "radius";
        public static final String COLUMN_NAME_INTERNAL_ID = "internalId";
        public static final String COLUMN_NAME_LABELS = "labels";
        public static final String COLUMN_NAME_STATUS = "status";
        public static final String COLUMN_NAME_TYPE = "type";
        public static final String COLUMN_NAME_CREATED_ON = "createdOn";
        public static final String COLUMN_NAME_UPDATED_ON = "updatedOn";
        public static final String COLUMN_NAME_ACTIVATION_ID = "activationId";
        public static final String COLUMN_NAME_ACTIVATED_ON = "activatedOn";
        public static final String COLUMN_NAME_LATITUDE = "latitude";
        public static final String COLUMN_NAME_LONGITUDE = "longitude";
        public static final String COLUMN_NAME_HASH = "hash";
        public static final String COLUMN_NAME_IS_ACTIVATE = "isActivate";
        public static final String COLUMN_NAME_RELATED_TRIGGER_COUNT = "relatedTriggerCount";
        public static final String COLUMN_NAME_BOARDER_DISTANCE_TO_CURRENT_LOCATION = "distanceToCurrentLocation";
        public static final String COLUMN_NAME_REAL_BOARDER_DISTANCE_TO_CURRENT_LOCATION = "realDistanceToCurrentLocation";
        public static final String COLUMN_NAME_IS_INSIDE = "isInside";
        public static final String COLUMN_NAME_LAST_ENTER = "lastEnter";
        public static final String COLUMN_NAME_LAST_EXIT = "lastExit";
    }

    /* Defines the table contents for Trigger */
    public static abstract class TriggerEntry implements BaseColumns {

        public static final String TABLE_NAME = "Trigger";
        public static final String COLUMN_NAME_INTERNAL_ID = "internalId";
        public static final String COLUMN_NAME_SERVER_ID = "serverId";
        public static final String COLUMN_NAME_ACTIVATION_ID = "activationId";
        public static final String COLUMN_NAME_TRIGGER_TYPE = "triggerType";
        public static final String COLUMN_NAME_TRIGGER_CONDITION = "triggerCondition";
        public static final String COLUMN_NAME_TRIGGER_DIRECTION = "triggerDirection";
        public static final String COLUMN_NAME_VALID_FROM = "validFrom";
        public static final String COLUMN_NAME_VALID_TO = "validTo";
        public static final String COLUMN_NAME_MAXIMUM_EXECUTION = "maximumExecutionsOverall";
        public static final String COLUMN_NAME_MAXIMUM_EXECUTION_PER_INTERVAL = "maximumExecutionsPerInterval";
        public static final String COLUMN_NAME_MAXIMUM_EXECUTION_PER_INTERVAL_MILLIS = "maximumExecutionsIntervalMillis";
        public static final String COLUMN_NAME_MAXIMUM_EXECUTION_PER_INTERVAL_SECONDS = "maximumExecutionsIntervalSeconds";
        public static final String COLUMN_NAME_EXECUTED_OVERALL = "executedOverall";
        public static final String COLUMN_NAME_TIME_IN_REGION = "timeInRegionSeconds";
        public static final String COLUMN_NAME_EXECUTION_COUNT = "executionCount";
        public static final String COLUMN_NAME_EXECUTION_COUNT_PER_INTERVAL = "executionCountPerInterval";
        public static final String COLUMN_NAME_LAST_EXECUTION_TIME = "lastExecutionTime";

    }

    /* Defines the table contents for Compaign */
    public static abstract class ActionEntry implements BaseColumns {

        public static final String TABLE_NAME = "Action";

    }

    /* Defines the table contents for TriggerToLocation */
    public static abstract class TriggerToGeoFencesEntry implements BaseColumns {

        public static final String TABLE_NAME = "TriggerToLocation";
        public static final String COLUMN_NAME_TRIGGER_ID = "triggerId";
        public static final String COLUMN_NAME_GEOFENCE_ID = "geofenceId";
        public static final String COLUMN_NAME_TRIGGER_CONDITION = "triggerCondition";
        public static final String COLUMN_NAME_TRIGGER_DIRECTION = "triggerDirection";
        public static final String COLUMN_NAME_TRIGGER_TIME_IN_REGION = "triggerTimeInRegion";

        public static final String COLUMN_NAME_ENTER_TIME = "enterTime";
        public static final String COLUMN_NAME_EXIT_TIME = "exitTime";
        public static final String COLUMN_NAME_DWELL_TIME = "dwellTime";

    }

    /* Defines the table contents for Tracking */
    public static abstract class ControlRegionEntry implements BaseColumns {

        public static final String TABLE_NAME = "ControlRegion";

        public static final String COLUMN_NAME_RADIUS = "radius";
        public static final String COLUMN_NAME_INTERNAL_ID = "internalId";
        public static final String COLUMN_NAME_LATITUDE = "latitude";
        public static final String COLUMN_NAME_LONGITUDE = "longitude";
        public static final String COLUMN_NAME_LAST_EXECUTION_TIME = "lastExecutionTime";
    }
}
