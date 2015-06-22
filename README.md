# Android-SDK

## Introduction

Beaconstac SDK is an easy way to enable proximity marketing and location analytics through an iBeacon-compliant BLE network. 

## Documentation

Please refer to the API documentation on the [Beaconstac developer hub](https://beaconstac.github.io/Android-SDK/).

## Integration with your existing project in Android Studio

1. Download or clone this repo on your system.
2. Copy the [beaconstac-release.aar](https://github.com/Beaconstac/Android-SDK/blob/master/BeaconstacSDK/beaconstac-release.aar) file into the `libs` directory of your app. Refer the included sample app for example.
3. In the `build.gradle` file of your project, add the following in the repositories section

        flatDir {
            dirs 'libs'
        }
![](images/repositories.png "Repositories")
4. In the `build.gradle` file of the app, add the following in the dependencies section:

        compile (name: 'beaconstac-release', ext: 'aar')
        compile 'com.mcxiaoke.volley:library:1.0.16'
	    compile 'com.google.android.gms:play-services:7.5.0'
	    compile 'com.crittercism:crittercism-android-agent:5.0.6'
5. Refresh all Gradle projects.
6. Create a file `beaconstac.xml` in the `values` folder containing configurations for Beaconstac SDK. 

		<?xml version="1.0" encoding="utf-8"?>
        <resources>
            <!-- whether rule processing is enabled -->
            <bool name="ruleProcessingEnabled">true</bool>
            
            <!-- time interval between rule syncs -->
            <integer name="ruleSyncInterval">86400</integer>
            
            <!-- events for which rules would be processed -->
            <string-array name="ruleEvents">
                <item>CAMPED</item>
                <item>EXITED</item>
            </string-array>

            <!-- whether analytics is enabled -->
            <bool name="analyticsEnabled">true</bool>
            
            <!-- time interval between analytics posting -->
            <integer name="analyticsPostInterval">900</integer>

            <!-- Beaconstac API token -->
            <string name="api_key"></string>
            
            <!-- Organization id -->
            <integer name="organization_id">0</integer>
            
            <!-- Provider authority -->
		    <string name="provider">com.mobstac.beaconstacexample.provider</string>
        </resources>
7. Add the following permissions to app manifest:

        <uses-permission android:name="android.permission.BLUETOOTH" />
        <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
        <uses-permission android:name="android.permission.INTERNET" />
        <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
        <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
        <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
8. Add the Beaconstac BLEService to your app manifest:

        <service android:name="com.mobstac.beaconstac.core.MSBLEService" android:enabled="true"/>
9. Should you choose to implement your own BroadcastReceiver (required if beacon detection has to work when the app is not running), extend `com.mobstac.beaconstac.core.BeaconstacReceiver` class and implement methods to handle the `rangedBeacons`, `campedOnBeacon`, `exitedBeacon`, `triggeredRule`, `enteredRegion` and `exitedRegion` events. The `BeaconstacExample` app contains an example of each type - directly using `BeaconstacReceiver` in the activity (this will require registering and unregistering it to receive intents in the activity itself), and extending `BeaconstacReceiver` and registering it to receive `actions` declared in the app manifest.
10. Add the Beaconstac-provided actions in the app manifest that you wish to listen for, in your BroadcastReceiver. From the `BeaconstacExample` app manifest:

        <receiver android:name=".BeaconstacExampleReceiver" android:exported="false">
            <intent-filter>
                <action android:name="com.mobstac.beaconstac.intent.action.RANGED_BEACON" />
                <action android:name="com.mobstac.beaconstac.intent.action.CAMPED_BEACON" />
                <action android:name="com.mobstac.beaconstac.intent.action.EXITED_BEACON" />
                <action android:name="com.mobstac.beaconstac.intent.action.TRIGGERED_RULE" />
                <action android:name="com.mobstac.beaconstac.intent.action.ENTERED_REGION" />
                <action android:name="com.mobstac.beaconstac.intent.action.EXITED_REGION" />            </intent-filter>
        </receiver>
11. Add `provider` to the manifest. Please implement your own ContentProvider that extends `com.mobstac.beaconstac.provider.MSContentProvider`. From the `BeaconstacExample` app:

		<provider
            android:name=".MyContentProvider"
            android:authorities="@string/provider"
            android:enabled="true"
            android:exported="false"
            android:multiprocess="true"
            android:syncable="true" >
12. To monitor beacon regions, configure the `UUID` and `region_identifier`.

        // set region parameters (UUID and unique region identifier)
        Beaconstac.getInstance(this)
	            .setRegionParams("F94DBB23-2266-7822-3782-57BEAC0952AC",
                "com.mobstac.beaconstacexample");
13. Call `startService` on `com.mobstac.beaconstac.core.MSBLEService` in the `onCreate` method of the Application or MainActivity after configuring the `UUID` and `region_identifier` as mentioned in the last step. Start the Service in a new thread.

        // start MSBLEService
        startService(new Intent(this, MSBLEService.class));
14. You can find more information and example usage in the `BeaconstacExample` app contained in the `examples` directory of this repo. Remember to set `organization_id` and `api_key` in `beaconstac.xml`.
