package com.mobstac.beaconstacdemo;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

import com.mobstac.beaconstac.Beaconstac;
import com.mobstac.beaconstac.core.MSException;
import com.mobstac.beaconstac.interfaces.BeaconScannerCallbacks;
import com.mobstac.beaconstac.interfaces.MSErrorListener;
import com.mobstac.beaconstac.interfaces.MSSyncListener;
import com.mobstac.beaconstac.models.MBeacon;
import com.mobstac.beaconstac.models.MRule;
import com.mobstac.beaconstac.utils.MSConstants;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    Beaconstac beaconstac;
    private ArrayList<MBeacon> beacons = new ArrayList<>();
    private BeaconAdapter beaconAdapter;
    private TextView bCount;
    private TextView testCamped;
    BeaconScannerCallbacks beaconScannerCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        initializeBeaconstac();
    }

    private void init() {

        ListView beaconList = (ListView) findViewById(R.id.beaconListView);
        beaconAdapter = new BeaconAdapter(beacons, this);
        beaconList.setAdapter(beaconAdapter);

        bCount = (TextView) findViewById(R.id.beaconCount);
        testCamped = (TextView) findViewById(R.id.CampedView);

        beaconScannerCallbacks = new BeaconScannerCallbacks() {
            @Override
            public void onScannedBeacons(ArrayList<MBeacon> rangedBeacons) {
                bCount.setText(String.valueOf(rangedBeacons.size()));
                beaconAdapter.clear();
                beacons.addAll(rangedBeacons);
                beaconAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCampedBeacon(MBeacon beacon) {
                testCamped.setText("Camped: " + beacon.getMajor() + ":" + beacon.getMinor());
                beaconAdapter.addBeacon(beacon);
                beaconAdapter.notifyDataSetChanged();
            }

            @Override
            public void onExitedBeacon(MBeacon beacon) {
                testCamped.setText("Exited: " + beacon.getMajor() + ":" + beacon.getMinor());
                beaconAdapter.removeBeacon(beacon);
                beaconAdapter.notifyDataSetChanged();
            }

            @Override
            public void onRuleTriggered(MRule rule) {
            }

        };

    }

    private void initializeBeaconstac() {
        if (checkPermission())
            try {
                beaconstac = Beaconstac.initialize(getApplicationContext(), "MY_DEVELOPER_TOKEN", new MSErrorListener() {
                    @Override
                    public void onError(MSException e) {
                        Log.e("Beaconstac", "Initialization failed");
                    }
                });
            } catch (MSException e) {
                e.printStackTrace();
            }
        if (beaconstac != null)
            beaconstac.setBeaconScannerCallbacks(beaconScannerCallbacks);

    }

    private boolean checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                ) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    MSConstants.REQUEST_LOCATION_PERMISSION);
            return false;
        } else {
            return true;

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        bCount.setText(String.valueOf(beacons.size()));
    }

    @Override
    protected void onResume() {
        super.onResume();
        init();
        bCount.setText(String.valueOf(beacons.size()));
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        beaconstac.stopScanningBeacons(new MSErrorListener() {
            @Override
            public void onError(MSException msException) {

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MSConstants.REQUEST_LOCATION_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initializeBeaconstac();
                }
            }
        }
    }
}
