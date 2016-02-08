package net.donky.location.geofence.mock;

import net.donky.core.DonkyException;
import net.donky.core.DonkyListener;

import java.util.Map;

/**
 * Created by Marcin Swierczek
 * 27/03/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class MockDonkyListener implements DonkyListener {

    DonkyException donkyException;

    Map<String, String> validationErrors;

    @Override
    public void success() {

        synchronized (this) {
            notifyAll();
        }
    }

    @Override
    public void error(DonkyException donkyException, Map<String, String> validationErrors) {

        this.donkyException = donkyException;
        this.validationErrors = validationErrors;

        synchronized (this) {
            notifyAll(  );
        }
    }

    public DonkyException getDonkyException() {
        return donkyException;
    }

    public Map<String, String> getValidationErrors() {
        return validationErrors;
    }
}
