package com.mobstac.beaconstacexample;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.mobstac.beaconstac.core.BeaconstacReceiver;
import com.mobstac.beaconstac.models.MSBeacon;

import java.util.ArrayList;


public class BeaconstacExampleReceiver extends BeaconstacReceiver {

    @Override
    public void exitedBeacon(Context context, MSBeacon beacon) {
        Log.v(BeaconstacExampleReceiver.class.getName(), "exited called " + beacon.getBeaconKey());
        sendNotification(context, "Exited " + beacon.getBeaconKey());
    }

    @Override
    public void rangedBeacons(Context context, ArrayList<MSBeacon> beacons) {
        Log.v(BeaconstacExampleReceiver.class.getName(), "Ranged called " + beacons.size());
        sendNotification(context, "Ranged " + beacons.size() + " beacons");
    }

    @Override
    public void campedOnBeacon(Context context, MSBeacon beacon) {
        Log.v(BeaconstacExampleReceiver.class.getName(), "camped on called " + beacon.getBeaconKey());
        sendNotification(context, "Camped " + beacon.getBeaconKey());
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
    }

    private void sendNotification(Context context, String text) {
        if (context != null) {
            Intent activityIntent = new Intent(context.getApplicationContext(), MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context.getApplicationContext(),
                    0,
                    activityIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
            );
            Notification mBuilder = new Notification.Builder(context.getApplicationContext())
                    .setContentText(text)
                    .setContentTitle("BeaconstacDemo")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(pendingIntent).build();
            NotificationManager notificationManager = (NotificationManager)
                    context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(1, mBuilder);
        }
    }
}
