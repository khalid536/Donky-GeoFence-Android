package net.donky.location.geofence.mock;

import net.donky.core.events.DonkyEventListener;
import net.donky.core.events.LocalEvent;

/**
 * Created by Marcin Swierczek
 * 28/03/15
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class MockDonkyEventListener<T extends LocalEvent> extends DonkyEventListener<T> {

    T event;

    boolean triggered = false;

    public MockDonkyEventListener(Class<T> type) {
        super(type);
    }

    @Override
    public void onDonkyEvent(T event) {

        this.event = event;

        this.triggered = true;

        synchronized (this) {
            notifyAll(  );
        }
    }

    public T getEvent() {
        return event;
    }

    public boolean isTriggered() {
        return triggered;
    }
}
