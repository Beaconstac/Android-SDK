package com.mobstac.beaconstacdemo;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ListView;
import android.widget.TextView;

import com.mobstac.beaconstac.Beaconstac;
import com.mobstac.beaconstac.core.ErrorCodes;
import com.mobstac.beaconstac.core.MSException;
import com.mobstac.beaconstac.interfaces.BeaconScannerCallbacks;
import com.mobstac.beaconstac.interfaces.MSErrorListener;
import com.mobstac.beaconstac.interfaces.MSSyncListener;
import com.mobstac.beaconstac.interfaces.Webhook;
import com.mobstac.beaconstac.models.MSAction;
import com.mobstac.beaconstac.models.MSBeacon;
import com.mobstac.beaconstac.models.MSCard;
import com.mobstac.beaconstac.models.MSMedia;
import com.mobstac.beaconstac.utils.MSConstants;
import com.mobstac.beaconstac.utils.MSLogger;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_ENABLE_BT = 1;
    private ArrayList<MSBeacon> beacons = new ArrayList<>();
    private boolean isPopupVisible = false;
    private BeaconAdapter beaconAdapter;
    private ListView beaconList;
    private TextView bCount;
    private TextView testCamped;
    private BluetoothAdapter mBluetoothAdapter;
    private Beaconstac bstac;
    private Activity mainActivity = this;
    private boolean appInForeground = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Use this check to determine whether BLE is supported on the device.
        try {
            if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))
                throw new MSException(MSConstants.ERROR_BLE_NOT_SUPPORTED, ErrorCodes.BLE_NOT_SUPPORTED);

        } catch (MSException msException) {

        }


        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = mBluetoothManager.getAdapter();
        }

        // Checks if Bluetooth is supported on the device.
        try {
            if (mBluetoothAdapter == null) {
                Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
                throw new MSException(MSConstants.ERROR_BLUETOOTH_NOT_FOUND, ErrorCodes.BLUETOOTH_NOT_FOUND);

            } else {
                if (!mBluetoothAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                }
            }
        } catch (MSException msException) {

        }


        bstac = Beaconstac.getInstance(this, "0", -1, new MSSyncListener() {
            @Override
            public void onSuccess() {
                Log.i(TAG, "Initialisation Successful");
            }

            @Override
            public void onFailure(MSException msException) {
                Log.e(TAG, "Initialisation Failed" + String.valueOf(msException.getErrorCode()));
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            checkPermission();

        init();

        bstac.setRegionParams("F94DBB23-2266-7822-3782-57BEAC0952AC", "com.mobstac.beaconstacdemo");

        bstac.startRangingBeacons(new MSErrorListener() {
            @Override
            public void onError(MSException e) {
                Log.d(TAG, "BLE not supported!");
            }
        });

        bstac.setBeaconScannerCallbacks(new BeaconScannerCallbacks() {

            @Override
            public void onRangedBeacons(ArrayList<MSBeacon> rangedBeacons) {
                bCount.setText("" + rangedBeacons.size());
                beaconAdapter.clear();
                beacons.addAll(rangedBeacons);
                beaconAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCampedBeacon(MSBeacon beacon) {

                testCamped.setText("Camped: " + beacon.getMajor() + ":" + beacon.getMinor());
                beaconAdapter.addBeacon(beacon);
                beaconAdapter.notifyDataSetChanged();
            }

            @Override
            public void onExitedBeacon(MSBeacon beacon) {

                testCamped.setText("Exited: " + beacon.getMajor() + ":" + beacon.getMinor());
                beaconAdapter.removeBeacon(beacon);
                beaconAdapter.notifyDataSetChanged();

            }

            @Override
            public void onEnteredRegion(String mRegionIdentifier) {

                beaconAdapter.clear();
                beaconAdapter.notifyDataSetChanged();
                bCount.setText("" + beacons.size());

                BeaconstacUtils.snackBar("Entered Region", mainActivity, MSConstants.COLOUR_WHITE);
            }

            @Override
            public void onExitedRegion(String mRegionIdentifier) {

                beaconAdapter.clear();
                beaconAdapter.notifyDataSetChanged();
                bCount.setText("" + beacons.size());

                BeaconstacUtils.snackBar("Exited Region", mainActivity, MSConstants.COLOUR_WHITE);
            }

            @Override
            public void onRuleTriggered(String ruleName, ArrayList<MSAction> actions) {
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
                                            if (!card.getMediaArray().isEmpty()) {
                                                m = card.getMediaArray().get(0);
                                                src = m.getMediaUrl().toString();

                                                // handle custom url types
                                                String ytId = extractYTId(src);
                                                if (ytId != null) {

                                                    ok_label = (String) messageMap.get("notificationOkLabel");
                                                    ok_action = (String) messageMap.get("notificationOkAction");
                                                    showYoutubePopup(ytId, ok_label, ok_action);

                                                } else {
                                                    dialog = new AlertDialog.Builder(MainActivity.this);
                                                    dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                                        @Override
                                                        public void onDismiss(DialogInterface dialog) {
                                                            isPopupVisible = false;
                                                        }
                                                    });
                                                    final WebView webView = new WebView(MainActivity.this);
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
                                                                try {
                                                                    Uri uri = Uri.parse(ok_actionForWebDialog); // missing 'http://' will cause crashed
                                                                    Intent openUrl = new Intent(Intent.ACTION_VIEW, uri);
                                                                    openUrl.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                                    startActivity(openUrl);
                                                                } catch (Exception e) {
                                                                    MSLogger.error("Cannot open this url");
                                                                }
                                                            }
                                                        });
                                                    }

                                                    dialog.setView(webView);
                                                    dialog.setNeutralButton("Close", null);
                                                    dialog.show();
                                                }
                                            }
                                            break;
                                        case MSCardTypePage:
                                            ArrayList<String> cardUrl = new ArrayList<>();
                                            for (int i = 0; i < card.getMediaArray().size(); i++) {
                                                m = card.getMediaArray().get(i);
                                                src = m.getMediaUrl().toString();
                                                cardUrl.add(src);
                                            }
                                            ok_label = (String) messageMap.get("notificationOkLabel");
                                            ok_action = (String) messageMap.get("notificationOkAction");
                                            showPopupDialog(card.getTitle(), card.getBody(), cardUrl, ok_label, ok_action);
                                            break;
                                    }
                                }
                                break;

                            case MSActionTypeWebpage:
                                if (!isPopupVisible) {
                                    final AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                                    dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                        @Override
                                        public void onDismiss(DialogInterface dialog) {
                                            isPopupVisible = false;
                                        }
                                    });

                                    final WebView webView = new WebView(MainActivity.this);
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

                            case MSActionTypeNotification:
                                bstac.showDefaultNotification(action);
                                break;

                            case MSActionTypeWebhook:
                                //TODO: Implement listener
                                break;
                        }
                    }
                } else {
                    for (MSAction action : actions) {
                        MSAction.MSActionType type = action.getType();
                        if (type == MSAction.MSActionType.MSActionTypeCard || type == MSAction.MSActionType.MSActionTypeNotification)
                            bstac.showDefaultNotification(action);
                    }

                    BeaconstacUtils.snackBar("Rule " + ruleName, mainActivity, MSConstants.COLOUR_WHITE);
                }
            }
        });

    }

    private void init() {
        beaconList = (ListView) findViewById(R.id.beaconListView);
        beaconAdapter = new BeaconAdapter(beacons, this);
        beaconList.setAdapter(beaconAdapter);

        bCount = (TextView) findViewById(R.id.beaconCount);
        testCamped = (TextView) findViewById(R.id.CampedView);

    }


    @Override
    protected void onPause() {
        super.onPause();
        bCount.setText("" + beacons.size());
        appInForeground = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        init();
        bCount.setText("" + beacons.size());
        appInForeground = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bstac != null)
            bstac.stopRangingBeacons(new MSErrorListener() {
                @Override
                public void onError(MSException e) {
                    Log.d(TAG, "BLE not supported!");
                }
            });
    }

    // Callback intent results
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
        }

        if (bstac != null) {
            bstac.startRangingBeacons(new MSErrorListener() {
                @Override
                public void onError(MSException e) {
                    Log.d(TAG, "BLE not supported!");
                }
            });
        }
    }


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

    private void sendNotification(String title, String text) {
        Context context = this;
        Intent activityIntent = new Intent(context.getApplicationContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context.getApplicationContext(),
                0,
                activityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context.getApplicationContext())
                .setContentText(text)
                .setContentTitle(title)
                .setSmallIcon(R.drawable.icon)
                .setContentIntent(pendingIntent);
        NotificationManager notificationManager = (NotificationManager)
                context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(870, mBuilder.build());
    }


    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                ) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    MSConstants.REQUEST_LOCATION_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MSConstants.REQUEST_LOCATION_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    bstac.startRangingBeacons(new MSErrorListener() {
                        @Override
                        public void onError(MSException msException) {
                            Log.d(TAG, "Error while ranging beacons");
                        }
                    });
                }
                return;
            }
        }
    }

}