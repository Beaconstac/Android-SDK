package com.mobstac.beaconstacdemo;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;
import com.mobstac.beaconstac.Beaconstac;
import com.mobstac.beaconstac.core.MSException;
import com.mobstac.beaconstac.interfaces.MSErrorListener;
import com.mobstac.beaconstac.interfaces.MSSyncListener;
import com.mobstac.beaconstac.utils.Util;

public class NearbyBeaconBroadcastReceiver extends BroadcastReceiver {

    Beaconstac beaconstac = null;

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (Util.isAppIsInBackground(context.getApplicationContext())) {
            Nearby.getMessagesClient(context).handleIntent(intent, new MessageListener() {
                @Override
                public void onFound(Message message) {
                    try {
                        if (ActivityCompat.checkSelfPermission(context.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(context.getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        beaconstac = Beaconstac.initialize(context.getApplicationContext(), "MY_DEVELOPER_TOKEN", new MSSyncListener() {
                            @Override
                            public void onSuccess() {
                                beaconstac.startScanningBeacons(new MSErrorListener() {
                                    @Override
                                    public void onError(MSException msException) {

                                    }
                                });
                            }

                            @Override
                            public void onFailure(MSException msException) {

                            }
                        });
                    } catch (MSException e) {
                        e.printStackTrace();
                    }
                    if (beaconstac != null) {
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                beaconstac.stopScanningBeacons(new MSErrorListener() {
                                    @Override
                                    public void onError(MSException msException) {

                                    }
                                });
                            }
                        }, 10000); // Set the duration for which the scan needs to run for.
                    }
                }

                @Override
                public void onLost(Message message) {
                }
            });
        }
    }
}
