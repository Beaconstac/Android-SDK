package com.mobstac.beaconstacdemo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.mobstac.beaconstac.models.MBeacon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class BeaconAdapter extends BaseAdapter {
    private ArrayList<MBeacon> beacons;
    private Context ctx;
    private LayoutInflater myInflator;

    BeaconAdapter(ArrayList<MBeacon> arr, Context c) {
        super();
        beacons = arr;
        ctx = c;
        myInflator = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    void addBeacon(MBeacon beacon) {
        if(!beacons.contains(beacon)) {
            beacons.add(beacon);
        }
    }

    void removeBeacon(MBeacon beacon) {
        if(beacons.contains(beacon)) {
            beacons.remove(beacon);
        }
    }

    void clear() {
        beacons.clear();
    }

    @Override
    public int getCount() {
        return beacons.size();
    }

    @Override
    public MBeacon getItem(int position) {
        return beacons.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public void notifyDataSetChanged() {
        Collections.sort(beacons, new Comparator<MBeacon>() {
            @Override
            public int compare(MBeacon lhs, MBeacon rhs) {
                if (lhs.getRSSI() > rhs.getRSSI())
                    return -1;
                else if (lhs.getRSSI() < rhs.getRSSI())
                    return 1;
                else
                    return 0;
            }
        });

        super.notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {

        if (view == null) {
            view = myInflator.inflate(R.layout.beacon_view, parent, false);
        }
        MBeacon beacon = beacons.get(position);

        TextView name = (TextView) view.findViewById(R.id.device_name);
        name.setText(beacon.getName().toString());

        TextView key = (TextView) view.findViewById(R.id.device_address);
        key.setText("Major: " + beacon.getMajor() + "\t\t\t Minor: " + beacon.getMinor() +
            " \t\t\t  Filtered RSSI: " + beacon.getRSSI());

        if (beacon.getIsCampedOn()) {
            view.setBackgroundResource(android.R.color.holo_green_light);
        } else {
            view.setBackgroundResource(android.R.color.background_light);
        }

        return view;

    }
}
