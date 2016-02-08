package net.donky.geo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.location.LocationRequest;

import net.donky.R;

public class CustomLocationDialogFragment extends DialogFragment {

    interface CustomLocationUpdateListener {
        void customLocation(LocationRequest locationRequest);
        void cancel();
    }

    private LocationRequest request;


    private EditText mEditTextUserId;
    private EditText interval;
    private EditText ExpirationTime;
    private EditText NumUpdates;
    private EditText Priority;
    private EditText SmallestDisplacement;
    private EditText FastestInterval;
    private EditText MaxWaitTime;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.custom_location_update_dialog_frament, null);
        mEditTextUserId = (EditText)view.findViewById(R.id.edtv_re);
        interval = (EditText)view.findViewById(R.id.edtv_interval);
        ExpirationTime = (EditText)view.findViewById(R.id.edtv_ExpirationTime);
        NumUpdates = (EditText)view.findViewById(R.id.edtv_NumUpdates);
        Priority = (EditText)view.findViewById(R.id.edtv_Priority);
        SmallestDisplacement = (EditText)view.findViewById(R.id.edtv_SmallestDisplacement);
        FastestInterval = (EditText)view.findViewById(R.id.edtv_FastestInterval);
        MaxWaitTime = (EditText)view.findViewById(R.id.edtv_MaxWaitTime);
        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .setTitle(R.string.create_custom_location_update)
                .setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (getActivity() instanceof CustomLocationUpdateListener)
                            ((CustomLocationUpdateListener)getActivity())
                                    .customLocation(create());
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (getActivity() instanceof CustomLocationUpdateListener)
                            ((CustomLocationUpdateListener)getActivity())
                                    .cancel();
                        dismiss();
                    }
                })
                .create();
    }

    private LocationRequest create(){
        LocationRequest locationRequest = new LocationRequest();
        //each 5 minutes update
        locationRequest.setInterval(300000);
        locationRequest.setFastestInterval(300000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }
}
