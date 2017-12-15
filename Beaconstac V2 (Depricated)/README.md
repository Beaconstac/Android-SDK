# Android-SDK (Deprecated)

## Please use the new version of the SDK.

## Introduction

Beaconstac SDK is an easy way to enable proximity marketing and location analytics through an iBeacon-compliant BLE network.

## Documentation

Please refer to the API documentation on the [Beaconstac developer hub](http://docs.beaconstac.com/docs/references/android/).

## Integration with your existing project in Android Studio

1. You can obtain the SDK from Gradle using -
```groovy
compile 'com.mobstac.beaconstac:beaconstac_sdk:2.0.4'
```
Latest version<br>
 [ ![Download](https://api.bintray.com/packages/mobstac/maven/beaconstac_sdk/images/download.svg) ](https://bintray.com/mobstac/maven/beaconstac_sdk/_latestVersion)

2. Refresh all Gradle projects. 

3. Add `uses-feature` tag to app manifest:

       <uses-feature
        android:name="android.hardware.bluetooth_le"
        android:required="false" />
        
4. Add the following permissions to app manifest:

        <uses-permission android:name="android.permission.BLUETOOTH" />
        <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
        <uses-permission android:name="android.permission.INTERNET" />
        <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
        <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
        <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
        
5. Add the Beaconstac BLEService to your app manifest:

        <service android:name="com.mobstac.beaconstac.MSBLEService" android:enabled="true"/>        

6. To get the Beaconstac instance, insert the `developer token` and `organisation id` and add a MSSyncListener:

       // Note that this will sync Beacons, Rules and Notifications implicitly but Tags and Places will not be synced.   
       Beaconstac bstacInstance = Beaconstac.getInstance(this, "Developer_token", organisation id , new MSSyncListener() {
            @Override
            public void onSuccess() {
                //Initialization successful.
            }

            @Override
            public void onFailure(MSException msException) {
               //Initialization failed.
            }
        });    
        
7. You can also sync them individually if required:

       // To sync Beacons
       bstacInstance.syncBeacons(new MSSyncListener() {
            @Override
            public void onSuccess() {
               //Beacons Synced
            }

            @Override
            public void onFailure(MSException e) {
               //Failed to sync Beacons
            }
        });
        
        // To sync Rules
        bstacInstance.syncRules(new MSSyncListener() {
            @Override
            public void onSuccess() {
               //Rules synced
            }

            @Override
            public void onFailure(MSException e) {
               //Failed to sync Rules
            }
        });
        
        // To sync Notifications
        bstacInstance.syncNotifications(new MSSyncListener() {
            @Override
            public void onSuccess() {
               //Notifications Synced
            }

            @Override
            public void onFailure(MSException e) {
               //Failed to sync Notifications
            }
        });
        
        // To sync Tags
        bstacInstance.syncTags(new MSSyncListener() {
            @Override
            public void onSuccess() {
               //Tags synced
            }

            @Override
            public void onFailure(MSException e) {
               //Failed to sync tags
            }
        });
        
        // To sync Places
        bstacInstance.syncPlaces(new MSSyncListener() {
            @Override
            public void onSuccess() {
               //Places synced
            }

            @Override
            public void onFailure(MSException e) {
               //Failed to sync places
            }
        });
   
        
8. To monitor beacon regions, configure the `UUID` and `region_identifier`.

        // set region parameters (UUID and unique region identifier)
        bstacInstance.setRegionParams("Enter your UUID here",
                "com.mobstac.beaconstacexample");
                
9. Call `startRangingBeacons` on the `Beaconstac` instance after configuring the params as mentioned in the previous step. The method will need a `MSErrorListener` to handle error in ranging beacons.

        // start scanning beacons
        bstacInstance.startRangingBeacons(new MSErrorListener() {
            @Override
            public void onError(MSException e) {
                //BLE not supoorted
            }
        });
        
10. If you want to stop scanning for the beacons, call `stopRangingBeacons` on the `Beaconstac` instance. The method will need a `MSErrorListener` to handle error in stop ranging beacons.

        // stop scanning
        bstacInstance.stopRangingBeacons(new MSErrorListener() {
                @Override
                public void onError(MSException e) {
                    //BLE not supported
                }
            });
            
11. You need to add a `BeaconScannerCallbacks` to get the events that are being triggered.

       // This is a listener to listen to events being triggered
       bstacInstance.setBeaconScannerCallbacks(new BeaconScannerCallbacks() {
            @Override
            public void onRangedBeacons(ArrayList<MSBeacon> arrayList) {
               
            }

            @Override
            public void onCampedBeacon(MSBeacon msBeacon) {
                
            }

            @Override
            public void onExitedBeacon(MSBeacon msBeacon) {
                
            }

            @Override
            public void onEnteredRegion(String s) {
                
            }

            @Override
            public void onExitedRegion(String s) {
                
            }

            @Override
            public void onRuleTriggered(String s, ArrayList<MSAction> arrayList) {
                

            }
        }); 
            
You can find more information and example usage in the `BeaconstacExample` app contained in the `examples` directory of this repo.