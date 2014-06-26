package com.example.showaddress;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import android.app.ListActivity;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends ListActivity implements LocationListener,
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {
    private final static String TAG = "MainActivity";

    private ArrayList<Address> addrsList;
    private ArrayAdapter<Address> addrsAdapter;
    private Button startStopButton;

    private LocationRequest locationRequest;
    private LocationClient locationClient;
    private boolean isUpdatingLocation, isGettingAddresses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
        setContentView(R.layout.activity_main);

        addrsList = new ArrayList<Address>();
        addrsAdapter = new ArrayAdapter<Address>(this, 0, addrsList) {
            @Override
            public View getView(int pos, View view, ViewGroup parent) {
                if (view == null) {
                    LayoutInflater inflater = LayoutInflater.from(getContext());
                    view = inflater.inflate(android.R.layout.simple_list_item_2, parent, false);
                }
                Address addr = getItem(pos);
                ((TextView) view.findViewById(android.R.id.text1)).setText(addr.getAddressLine(1));
                ((TextView) view.findViewById(android.R.id.text2)).setText(addr.getAddressLine(0));
                return view;
            }
        };
        setListAdapter(addrsAdapter);
        startStopButton = (Button) findViewById(R.id.start_stop_button);

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5 * 1000); // 5 sec.
        locationRequest.setFastestInterval(1 * 1000); // 1 sec.
        locationClient = new LocationClient(this, this, this);
        isUpdatingLocation = false;
        isGettingAddresses = false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart");
        locationClient.connect();
    }

    @Override
    protected void onStop() {
        Log.i(TAG, "onStop");
        if (locationClient.isConnected())
            stopPeriodicUpdate();
        locationClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnectionFailed(ConnectionResult arg0) {
        Log.i(TAG, "onConnectionFailed");
    }

    @Override
    public void onConnected(Bundle arg0) {
        Log.i(TAG, "onConnected");
        startStopButton.setEnabled(true);
    }

    @Override
    public void onDisconnected() {
        Log.i(TAG, "onDisconnected");
        startStopButton.setEnabled(false);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, "onLocationChanged");
        if (!isGettingAddresses) {
            isGettingAddresses = true;
            (new GetAddressTask(this)).execute(location);
        }
    }

    public void onClickStartStopButton(View view) {
        if (!isUpdatingLocation)
            startPeriodicUpdate();
        else
            stopPeriodicUpdate();
    }

    private void startPeriodicUpdate() {
        isUpdatingLocation = true;
        startStopButton.setText(getString(R.string.start_stop_button_stop_label));
        locationClient.requestLocationUpdates(locationRequest, this);
    }

    private void stopPeriodicUpdate() {
        isUpdatingLocation = false;
        startStopButton.setText(getString(R.string.start_stop_button_start_label));
        locationClient.removeLocationUpdates(this);
    }

    private class GetAddressTask extends AsyncTask<Location, Void, List<Address>> {
        private final static String TAG = "GetAddressTask";

        private Context context;

        public GetAddressTask(Context context) {
            super();
            this.context = context;
        }

        @Override
        protected List<Address> doInBackground(Location... params) {
            Log.i(TAG, "doInBackground");
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            Location loc = params[0];
            List<Address> addrs = null;
            try {
                addrs = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 10);
            }
            catch (IOException e) {}
            return addrs;
        }

        @Override
        public void onPostExecute(List<Address> addrs) {
            addrsAdapter.clear();
            addrsAdapter.addAll(addrs);
            isGettingAddresses = false;
        }

    }

}
