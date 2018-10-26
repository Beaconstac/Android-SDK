package com.mobstac.beaconstacdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NearbyBeaconBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(NearbyBeaconBroadcastReceiver.class.getName(), "Beacons found in background");
    }

}
