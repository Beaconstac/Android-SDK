package com.mobstac.beaconstacexample;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mobstac.beaconstac.callbacks.BeaconstacCallback;
import com.mobstac.beaconstac.core.Beaconstac;
import com.mobstac.beaconstac.models.MSBeacon;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity implements BeaconstacCallback {

    private static final String TAG = MainActivity.class.getSimpleName();

    private ArrayList<MSBeacon> beacons = new ArrayList<MSBeacon>();

    private BeaconAdapter beaconAdapter;
    private ListView beaconList;
    private TextView bCount;

    private TextView testCamped;

    private BluetoothAdapter mBluetoothAdapter;
    private static final int REQUEST_ENABLE_BT = 1;
    String UUID;
    String REGION_IDENTIFIER;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        UUID = getApplicationContext().getResources().getString(R.string.uuid);
        REGION_IDENTIFIER = getApplicationContext().getResources().getString(R.string.region_identifier);

        // Use this check to determine whether BLE is supported on the device.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = mBluetoothManager.getAdapter();
        }

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            Toast.makeText(this, "Unable to obtain a BluetoothAdapter", Toast.LENGTH_LONG).show();
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }

        if (savedInstanceState == null) {
            initList();
        }

    }

    private void initList() {
        beaconList      = (ListView) findViewById(R.id.beaconListView);
        beaconAdapter   = new BeaconAdapter(beacons, this);
        beaconList.setAdapter(beaconAdapter);

        bCount = (TextView) findViewById(R.id.beaconCount);
        testCamped = (TextView) findViewById(R.id.CampedView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        Beaconstac.getInstance(this.getApplicationContext()).onPause();
        beaconAdapter.clear();
        beaconAdapter.notifyDataSetChanged();
        bCount.setText("" + beacons.size());
    }

    @Override
    protected void onStart() {
        if (mBluetoothAdapter != null)
            Beaconstac.getInstance(this.getApplicationContext()).startRangingBeacons(UUID, REGION_IDENTIFIER, null, this);
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mBluetoothAdapter != null)
            Beaconstac.getInstance(this.getApplicationContext()).onResume();
        initList();
        bCount.setText("" + beacons.size());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    // Callback intent results
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    // Handle callback raised from Beaconstac when
    // the app leaves the proximity of the camped on beacon
    @Override
    public void exitedBeacon(final MSBeacon beacon) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                testCamped.setText("Exited " + beacon.getMajor() + ":" + beacon.getMinor());
                beaconAdapter.notifyDataSetChanged();
            }
        });

    }

    // Handle callback raised from Beaconstac when
    // the app updates the list of ranged beacons
    @Override
    public void rangedBeacons(final List<MSBeacon> rangedBeacons) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                bCount.setText("" + rangedBeacons.size());
                beaconAdapter.clear();
                beacons.addAll(rangedBeacons);
                beaconAdapter.notifyDataSetChanged();
            }
        });

    }

    // Handle callback raised from Beaconstac when
    // the app has camped on to a new beacon
    @Override
    public void campedOnBeacon(final MSBeacon beacon) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                testCamped.setText("Camped " + beacon.getMajor() + ":" + beacon.getMinor());
                beaconAdapter.addBeacon(beacon);
                beaconAdapter.notifyDataSetChanged();
            }
        });
    }
}
