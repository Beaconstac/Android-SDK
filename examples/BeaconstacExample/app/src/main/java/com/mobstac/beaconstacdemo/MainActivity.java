package com.mobstac.beaconstacdemo;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.mobstac.beaconstac.core.Beaconstac;
import com.mobstac.beaconstac.core.BeaconstacReceiver;
import com.mobstac.beaconstac.core.MSConstants;
import com.mobstac.beaconstac.core.MSPlace;
import com.mobstac.beaconstac.core.PlaceSyncReceiver;
import com.mobstac.beaconstac.core.Webhook;
import com.mobstac.beaconstac.models.MSAction;
import com.mobstac.beaconstac.models.MSBeacon;
import com.mobstac.beaconstac.models.MSCard;
import com.mobstac.beaconstac.models.MSMedia;
import com.mobstac.beaconstac.utils.MSException;
import com.mobstac.beaconstac.utils.MSLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private ArrayList<MSBeacon> beacons = new ArrayList<>();

    private boolean isPopupVisible = false;
    private BeaconAdapter beaconAdapter;
    private ListView beaconList;
    private TextView bCount;

    private TextView testCamped;

    private BluetoothAdapter mBluetoothAdapter;
    private static final int REQUEST_ENABLE_BT = 1;

    private Beaconstac bstac;

    private boolean registered = false;
    private boolean appInForeground = false;


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

        bstac = Beaconstac.getInstance(this);
        bstac.setRegionParams("F94DBB23-2266-7822-3782-57BEAC0952AC", "com.mobstac.beaconstacdemo");
        bstac.syncRules();


//         if location is enabled
        LocationManager locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {

            bstac.syncPlaces();


            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(MSConstants.BEACONSTAC_INTENT_PLACE_SYNC_SUCCESS);
            intentFilter.addAction(MSConstants.BEACONSTAC_INTENT_PLACE_SYNC_FAILURE);

            registerReceiver(new PlaceSyncReceiver() {

                @Override
                public void onSuccess(Context context) {
                    bstac.enableGeofences(true);

                    // start ranging
                    try {
                        bstac.startRangingBeacons();
                    } catch (MSException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Context context) {
                    MSLogger.error("Error syncing geofence");
                }

            }, intentFilter);

        } else {

            try {
                bstac.startRangingBeacons();
            } catch (MSException e) {
                e.printStackTrace();
            }
        }

        init();
    }

    private void init() {
        beaconList = (ListView) findViewById(R.id.beaconListView);
        beaconAdapter = new BeaconAdapter(beacons, this);
        beaconList.setAdapter(beaconAdapter);

        bCount = (TextView) findViewById(R.id.beaconCount);
        testCamped = (TextView) findViewById(R.id.CampedView);

        registerBroadcast();
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
            MSAction.handler = new Webhook() {
                @Override
                public void onWebHookResponse(NetworkResponse response) {
                    Toast.makeText(getApplicationContext(), "Webhook completed", Toast.LENGTH_SHORT).show();
                }
            };
        }
    }

    private void unregisterBroadcast() {
        if (registered) {
            unregisterReceiver(myBroadcastReceiver);
            registered = false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterBroadcast();
        beaconAdapter.clear();
        beaconAdapter.notifyDataSetChanged();
        bCount.setText("" + beacons.size());
        appInForeground = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        init();
        bCount.setText("" + beacons.size());
        registerBroadcast();
        appInForeground = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bstac != null)
            try {
                bstac.stopRangingBeacons();
            } catch (MSException e) {
                e.printStackTrace();
            }
    }

    // Callback intent results
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
        }

        if (bstac != null) {
            try {
                bstac.startRangingBeacons();
            } catch (MSException e) {
                e.printStackTrace();
            }
        }
    }

    BeaconstacReceiver myBroadcastReceiver = new BeaconstacReceiver() {
        @Override
        public void exitedBeacon(Context context, MSBeacon beacon) {
            testCamped.setText("Exited: " + beacon.getMajor() + ":" + beacon.getMinor());
            beaconAdapter.removeBeacon(beacon);
            beaconAdapter.notifyDataSetChanged();
        }

        @Override
        public void rangedBeacons(Context context, ArrayList<MSBeacon> rangedBeacons) {
            bCount.setText("" + rangedBeacons.size());
            beaconAdapter.clear();
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
        public void triggeredRule(final Context context, String ruleName, ArrayList<MSAction> actions) {
            HashMap<String, Object> messageMap;
            MSLogger.log("appInForeground " + appInForeground);
            if (appInForeground) {
                for (MSAction action : actions) {
                    messageMap = action.getMessage();
                    String ok_label = "";
                    String ok_action = "";
                    switch (action.getType()) {
                        case MSActionTypePopup:
                            if (!isPopupVisible) {
                                isPopupVisible = true;
                                ok_label = (String) messageMap.get("notificationOkLabel");
                                ok_action = (String) messageMap.get("notificationOkAction");
                                showPopupDialog(action.getName(), (String) messageMap.get("text"), null, ok_label, ok_action);
                            }
                            break;

                        case MSActionTypeCard:
                            if (!isPopupVisible) {
                                isPopupVisible = true;
                                MSCard card = (MSCard) messageMap.get("card");
                                MSMedia m;
                                String src;
                                AlertDialog.Builder dialog;

                                String title = card.getTitle();

                                switch (card.getType()) {
                                    case MSCardTypePhoto:
                                        ArrayList<String> urls = new ArrayList<>();
                                        for (int i = 0; i < card.getMediaArray().size(); i++) {
                                            m = card.getMediaArray().get(i);
                                            src = m.getMediaUrl().toString();
                                            urls.add(src);
                                        }
                                        ok_label = (String) messageMap.get("notificationOkLabel");
                                        ok_action = (String) messageMap.get("notificationOkAction");
                                        showPopupDialog(title, null, urls, ok_label, ok_action);
                                        break;

                                    case MSCardTypeSummary:
                                        ArrayList<String> cardUrls = new ArrayList<>();
                                        for (int i = 0; i < card.getMediaArray().size(); i++) {
                                            m = card.getMediaArray().get(i);
                                            src = m.getMediaUrl().toString();
                                            cardUrls.add(src);
                                        }
                                        ok_label = (String) messageMap.get("notificationOkLabel");
                                        ok_action = (String) messageMap.get("notificationOkAction");
                                        showPopupDialog(card.getTitle(), card.getBody(), cardUrls, ok_label, ok_action);
                                        break;

                                    case MSCardTypeMedia:
                                        m = card.getMediaArray().get(0);
                                        src = m.getMediaUrl().toString();

                                        // handle custom url types
                                        String ytId = extractYTId(src);
                                        if (ytId != null) {

                                            ok_label = (String) messageMap.get("notificationOkLabel");
                                            ok_action = (String) messageMap.get("notificationOkAction");
                                            showYoutubePopup(ytId, ok_label, ok_action);

                                        } else {

                                            dialog = new AlertDialog.Builder(context);
                                            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                                @Override
                                                public void onDismiss(DialogInterface dialog) {
                                                    isPopupVisible = false;
                                                }
                                            });
                                            final WebView webView = new WebView(context);
                                            webView.getSettings().setJavaScriptEnabled(true);
                                            webView.setWebViewClient(new WebViewClient());
                                            webView.loadUrl(src);

                                            ok_label = (String) messageMap.get("notificationOkLabel");
                                            final String ok_actionForWebDialog = (String) messageMap.get("notificationOkAction");
                                            if (ok_label != null && !ok_label.equals("") && ok_label.trim().length() != 0) {

                                                dialog.setPositiveButton(ok_label, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialog.dismiss();
                                                        Uri uri = Uri.parse(ok_actionForWebDialog); // missing 'http://' will cause crashed
                                                        Intent openUrl = new Intent(Intent.ACTION_VIEW, uri);
                                                        openUrl.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                        startActivity(openUrl);
                                                    }
                                                });
                                            }

                                            dialog.setView(webView);
                                            dialog.setNeutralButton("Close", null);
                                            dialog.show();

                                            isPopupVisible = true;
                                        }

                                        break;
                                }
                            }
                            break;

                        case MSActionTypeWebpage:
                            if (!isPopupVisible) {
                                final AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialog) {
                                        isPopupVisible = false;
                                    }
                                });

                                final WebView webView = new WebView(context);
                                webView.getSettings().setJavaScriptEnabled(true);
                                webView.setWebViewClient(new WebViewClient());
                                webView.loadUrl(messageMap.get("url").toString());

                                dialog.setView(webView);
                                dialog.setPositiveButton("Close", null);
                                dialog.show();

                                isPopupVisible = true;
                            }
                            break;

                        case MSActionTypeCustom:
                            MSLogger.log("Card id: " + action.getActionID());
                            break;
                    }
                }
            }
            Toast.makeText(getApplicationContext(), "Rule " + ruleName, Toast.LENGTH_SHORT).show();
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
        public void enteredGeofence(Context context, ArrayList<MSPlace> places) {
            MSLogger.log("Entered Geofence " + places.get(0).getPlaceID() + "");
            Toast.makeText(getApplicationContext(), "Entered Geofence " + places.get(0).getName(), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void exitedGeofence(Context context, ArrayList<MSPlace> places) {
            MSLogger.log("Exited Geofence " + places.get(0).getPlaceID() + "");
            Toast.makeText(getApplicationContext(), "Exited Geofence " + places.get(0).getName(), Toast.LENGTH_SHORT).show();
        }
    };

    /**
     * Opens a dialogFragment to display offers
     *
     * @param title Title of dialog (pass null to hide title)
     * @param text  Summary of dialog (pass null to hide summary)
     * @param url   ArrayList containing URLs of images (pass null to hide images)
     */
    private void showPopupDialog(String title, String text, ArrayList<String> url, String... ok_data) {
        String ok_label = "";
        String ok_action = "";

        if (ok_data.length == 2) {
            if (ok_data[0] != null && ok_data[1] != null) {
                ok_label = ok_data[0];
                ok_action = ok_data[1];
            }
        }


        FragmentManager fragmentManager = getSupportFragmentManager();
        ImageCarouselDialog imageCarouselDialog =
                ImageCarouselDialog.newInstance(title, text, url, ok_label, ok_action);
        imageCarouselDialog.setRetainInstance(true);
        isPopupVisible = true;

        imageCarouselDialog.show(fragmentManager, "Dialog Fragment");
    }

    public void setIsPopupVisible(boolean isPopupVisible) {
        this.isPopupVisible = isPopupVisible;
    }

    /**
     * Displays a popup to show youTubevideos
     *
     * @param youTubeID ID of the video extracted from the URL
     */
    private void showYoutubePopup(String youTubeID, String... ok_data) {
        String ok_label = "";
        String ok_action = "";

        if (ok_data.length == 2) {
            if (ok_data[0] != null && ok_data[1] != null) {
                ok_label = ok_data[0];
                ok_action = ok_data[1];
            }
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        YoutubePlayerDialog youtubePlayerDialog =
                YoutubePlayerDialog.newInstance(youTubeID, ok_label, ok_action);
        youtubePlayerDialog.setRetainInstance(true);
        isPopupVisible = true;
        youtubePlayerDialog.show(fragmentManager, "Dialog Fragment");

    }

    /**
     * regex to get youtube video id from url
     *
     * @param ytUrl URL of the video
     * @return Youtube video ID
     */

    public static String extractYTId(String ytUrl) {
        String vId = null;
        Pattern pattern = Pattern.compile(".*(?:youtu.be\\/|v\\/|u\\/\\w\\/|embed\\/|watch\\?v=)([^#\\&\\?]*).*");
        Matcher matcher = pattern.matcher(ytUrl);
        if (matcher.matches()) {
            vId = matcher.group(1);
        }
        return vId;
    }

}