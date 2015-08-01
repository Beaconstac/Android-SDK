package com.mobstac.beaconstacexample;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mobstac.beaconstac.core.Beaconstac;
import com.mobstac.beaconstac.core.BeaconstacReceiver;
import com.mobstac.beaconstac.core.MSBLEService;
import com.mobstac.beaconstac.core.MSConstants;
import com.mobstac.beaconstac.core.MSPlace;
import com.mobstac.beaconstac.models.MSAction;
import com.mobstac.beaconstac.models.MSBeacon;
import com.mobstac.beaconstac.models.MSCard;
import com.mobstac.beaconstac.models.MSMedia;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executors;


public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private ArrayList<MSBeacon> beacons = new ArrayList<MSBeacon>();

    private BeaconAdapter beaconAdapter;
    private TextView bCount;
    private TextView testCamped;
    private Intent BLEServiceIntent;

    private BluetoothAdapter mBluetoothAdapter;
    private static final int REQUEST_ENABLE_BT = 1;

    private boolean registered = false;
    private boolean isPopupVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        BLEServiceIntent = new Intent(getApplicationContext(), MSBLEService.class);

        // set region parameters (UUID and unique region identifier)
        Beaconstac.getInstance(this).
                setRegionParams("B9407F30-F5F8-466E-AFF9-25556B57FE6D", //"F94DBB23-2266-7822-3782-57BEAC0952AC",
                        "com.mobstac.beaconstacexample");
        // start MSBLEService
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                startService(BLEServiceIntent);
            }
        });
    }

    private void initList() {
        ListView beaconList = (ListView) findViewById(R.id.beaconListView);
        beaconAdapter   = new BeaconAdapter(beacons, this);
        beaconList.setAdapter(beaconAdapter);

        bCount = (TextView) findViewById(R.id.beaconCount);
        testCamped = (TextView) findViewById(R.id.CampedView);
        registerBroadcast();
    }

    @Override
    protected void onPause() {
        super.onPause();
        beaconAdapter.clear();
        beaconAdapter.notifyDataSetChanged();
        bCount.setText("" + beacons.size());
        unregisterBroadcast();
        isPopupVisible = true;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initList();
        bCount.setText("" + beacons.size());
        registerBroadcast();
        isPopupVisible = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterBroadcast();

        // Uncomment the following lines in order to
        // stop the service when this activity is destroyed
        /*
        if (BLEServiceIntent != null)
            stopService(BLEServiceIntent);
        */
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

    private void registerBroadcast() {
        if (!registered) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(MSConstants.BEACONSTAC_INTENT_RANGED_BEACON);
            intentFilter.addAction(MSConstants.BEACONSTAC_INTENT_CAMPED_BEACON);
            intentFilter.addAction(MSConstants.BEACONSTAC_INTENT_EXITED_BEACON);
            intentFilter.addAction(MSConstants.BEACONSTAC_INTENT_RULE_TRIGGERED);
            intentFilter.addAction(MSConstants.BEACONSTAC_INTENT_ENTERED_REGION);
            intentFilter.addAction(MSConstants.BEACONSTAC_INTENT_EXITED_REGION);
            registerReceiver(myBroadcastReceiver, intentFilter);
            registered = true;
        }
    }

    private void unregisterBroadcast() {
        if (registered) {
            unregisterReceiver(myBroadcastReceiver);
            registered = false;
        }
    }

    BeaconstacReceiver myBroadcastReceiver = new BeaconstacReceiver() {
        @Override
        public void exitedBeacon(Context context, MSBeacon beacon) {
            testCamped.setText("Exited: " + beacon.getMajor() + ":" + beacon.getMinor());
            beaconAdapter.notifyDataSetChanged();
        }

        @Override
        public void rangedBeacons(Context context, ArrayList<MSBeacon> rangedBeacons) {
            beaconAdapter.clear();
            bCount.setText("" + rangedBeacons.size());
            beacons.addAll(rangedBeacons);
            beaconAdapter.notifyDataSetChanged();
        }

        @Override
        public void campedOnBeacon(Context context, MSBeacon beacon) {
            testCamped.setText("Camped: " + beacon.getMajor() + ":" + beacon.getMinor());
            beaconAdapter.addBeacon(beacon);
            beaconAdapter.notifyDataSetChanged();
        }

        @Override
        public void triggeredRule(Context context, String ruleName, ArrayList<MSAction> actions) {
            HashMap<String, Object> messageMap;
            AlertDialog.Builder dialogBuilder;

            if (!isPopupVisible) {
                for (MSAction action : actions) {

                    messageMap = action.getMessage();

                    switch (action.getType()) {
                        // handle action type Popup
                        case MSActionTypePopup:
                            dialogBuilder = new AlertDialog.Builder(context);
                            messageMap = action.getMessage();
                            dialogBuilder.setTitle(action.getName()).setMessage((String) messageMap.get("text"));
                            AlertDialog dialog = dialogBuilder.create();
                            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    isPopupVisible = false;
                                }
                            });
                            dialog.show();
                            isPopupVisible = true;
                            break;

                        // handle the action type Card
                        case MSActionTypeCard:
                            MSCard card = (MSCard) messageMap.get("card");

                            switch (card.getType()) {
                                case MSCardTypePhoto:
                                case MSCardTypeMedia:
                                    MSMedia m = card.getMediaArray().get(0);
                                    String src = m.getMediaUrl().toString();

                                    dialogBuilder = new AlertDialog.Builder(context);
                                    dialogBuilder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                        @Override
                                        public void onDismiss(DialogInterface dialog) {
                                            isPopupVisible = false;
                                        }
                                    });

                                    final WebView webView = new WebView(context);
                                    webView.loadUrl(src);

                                    dialogBuilder.setView(webView);
                                    dialogBuilder.setPositiveButton("Close", null);
                                    dialogBuilder.show();

                                    isPopupVisible = true;
                            }
                            break;

                        // handle action type webpage
                        case MSActionTypeWebpage:
                            if (!isPopupVisible) {
                                dialogBuilder = new AlertDialog.Builder(context);
                                dialogBuilder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialog) {
                                        isPopupVisible = false;
                                    }
                                });

                                final WebView webView = new WebView(context);
                                webView.loadUrl(messageMap.get("url").toString());

                                dialogBuilder.setView(webView);
                                dialogBuilder.setPositiveButton("Close", null);
                                dialogBuilder.show();

                                isPopupVisible = true;

                            }
                            break;
                    }
                }
                Toast.makeText(getApplicationContext(), "Rule " + ruleName, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void enteredRegion(Context context, String region) {
            beaconAdapter.clear();
            beaconAdapter.notifyDataSetChanged();
            bCount.setText("" + beacons.size());
            Toast.makeText(getApplicationContext(), "Entered region", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void exitedRegion(Context context, String region) {
            beaconAdapter.clear();
            beaconAdapter.notifyDataSetChanged();
            bCount.setText("" + beacons.size());
            Toast.makeText(getApplicationContext(), "Exited region", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void enteredGeofence(Context context, ArrayList<MSPlace> arrayList) {
            Toast.makeText(getApplicationContext(), "Entered geofence", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void exitedGeofence(Context context, ArrayList<MSPlace> arrayList) {
            Toast.makeText(getApplicationContext(), "Exited geofence", Toast.LENGTH_SHORT).show();
        }
    };
}
