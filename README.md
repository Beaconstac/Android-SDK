# Beaconstac Android-SDK

## Introduction

Beaconstac SDK is an easy way to enable proximity marketing and location analytics through an iBeacon-compliant BLE network.

## Integrate with your existing project in Android Studio

### In the `build.gradle` file of the app, add the following in the dependencies section:
```groovy
implementation 'com.android.support.constraint:constraint-layout:1.0.2'
compile 'com.mobstac.beaconstac:proximity:3.*'
```
Latest version
 [ ![Download](https://api.bintray.com/packages/mobstac/maven/proximity/images/download.svg) ](https://bintray.com/mobstac/maven/proximity/_latestVersion)

## Permissions

__Beaconstac requires the following permissions__
```xml

<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
```

It is not necessary to explicitly add these permissions to your app. They will be added automatically when you include the SDK.

### Runtime permissions

Since Android 6.0, Android has introduced the concept of runtime permissions. Beaconstac SDK requires one runtime permission:

__Location__

Beaconstac requires the location permission to scan for nearby beacons. Beaconstac SDK's initialize() will fail if location permission is denied.

### Prerequisites

1. Please extract your developer token from the Beaconstac dashboard under "My Account".

2. Internet access is required to initialize the SDK.

3. Bluetooth enabled for scanning beacons.

## Usage

### Use our one line integration

__Note:__ If this is used, the Beaconstac SDK will automatically start scanning for beacons and trigger notifications based on rules defined on Beaconstac dashboard. You can also explicitly start or stop beacon scanning by calling `Beaconstac.getInstance().startScanningBeacons()` and `Beaconstac.getInstance().stopScanningBeacons()` respectively. Please refer [advanced](#3-start-scan) for more info.


```java
Beaconstac.initialize(getApplicationContext(), MY_DEVELOPER_TOKEN, new MSErrorListener() {
    @Override
    public void onError(MSException msException) {
        Log.d("Beaconstac", msException.getErrorMessage());
    }
});
```

### OR

### Use our advanced integration

__1. Initialise the SDK with your developer token (preferably in the Application class)__

```java

Beaconstac.initialize(getApplicationContext(), MY_DEVELOPER_TOKEN, new MSSyncListener() {
    @Override
    public void onSuccess() {
        Log.d("Beaconstac", "Initialization successful");
        Beaconstac.getInstance().startScanningBeacons(new MSErrorListener() {
            @Override
            public void onError(MSException msException) {

            }
        });
    }

    @Override
    public void onFailure(MSException e) {
        Log.d("Beaconstac", "Initialization failed");
    }

});
```

__2. Get Beaconstac instance__
```java
Beaconstac beaconstac = Beaconstac.getInstance();
```
#### 3. Start scan

```java
Beaconstac.getInstance().startScanningBeacons(new MSErrorListener() {
    @Override
    public void onError(MSException msException) {

    }
});
```
__4. Stop scan__
```java
Beaconstac.getInstance().stopScanningBeacons(new MSErrorListener() {
    @Override
    public void onError(MSException msException) {

    }
});
```

__5. Get beacon event callbacks__

__Note: You only need to implement this if you want to get callbacks for beacon events.__

```java
Beaconstac.getInstance().setBeaconScannerCallbacks(new BeaconScannerCallbacks() {
    @Override
    public void onScannedBeacons(ArrayList<MBeacon> rangedBeacons) {
    }

    @Override
    public void onCampedBeacon(MBeacon beacon) {
    }

    @Override
    public void onExitedBeacon(MBeacon beacon) {
    }

    @Override
    public void onRuleTriggered(MRule rule) {
    }

});
```

__6. Override Beaconstac SDK's notification__

__Note: If you implement this method Beaconstac SDK will not trigger any notification. A `Notification.Builder` object will returned to the app and it will be the application's responsibility to modify and trigger the notifications.__

```java
Beaconstac.getInstance().overrideBeaconstacNotification(new BeaconstacNotification() {
    @Override
    public void notificationTrigger(Notification.Builder notification) {

    }
});
```


__7. Add additional values to your webhooks__

```java
Beaconstac.getInstance().addValuesToWebhook(MY_KEY, VALUE);
```

OR
```java
Beaconstac.getInstance().addValuesToWebhook(MY_KEY_VALUE_HASHMAP);
```

__8. Set user's name__
```java
Beaconstac.getInstance().setUserName(USER_FIRST_NAME , USER_LAST_NAME);
```

__9. Set user's email__
```java
Beaconstac.getInstance().setUserEmail(USER_EMAIL);
```

__10. Set scan power mode__

Set the power mode for bluetooth low energy scan callbacks. Set to HIGH for frequent scans with high power consumption.
Default value is set to BALANCED.
```java
Beaconstac.getInstance().setPowerMode(POWER_MODE);
```

__11. Set latch latency__

Set the device's willingness to camp-on to new beacons if it is already camped on to one. If set to LOW the device switches to the other beacons quickly and if set to HIGH the device's attachment will be steady.
The default value is set to MEDIUM.
```java
Beaconstac.getInstance().setLatchLatency(LATCH_LATENCY);
```

## Handling background events


__12. Subscribe for background updates__

Subscribe for updates when a Beaconstac beacon is found nearby. Sends a callback to the registered receiver.
```java
Beaconstac.getInstance().subscribeForBackgroundUpdates(RECEIVER_EXTENDING_BROADCAST_RECEIVER);
```

Once a callback is received by the receiver you can start scanning for beacons using the Beaconstac SDK and once done you should stop the scan. Please refer the example receiver [here](https://github.com/Beaconstac/Android-SDK/blob/master/examples/BeaconstacExample/app/src/main/java/com/mobstac/beaconstacdemo/NearbyBeaconBroadcastReceiver.java).

*__Note: Stopping the scan is required and should be a mandatory step as if not stopped the Beaconstac SDK will not stop the scan and will keep on scanning indefinitely. This will result in high power consumption and will keep on showing a persistent notification on devices running on Android 8 and above.__*


__13. Unsubscribe from background updates__

Unsubscribe from updates when a Beaconstac beacon if found nearby.
```java
Beaconstac.getInstance().unScribeFromBackgroundUpdates(RECEIVER_EXTENDING_BROADCAST_RECEIVER);
```

__Due to the restriction added on Android 8 and above on running background tasks a persistent notification will be shown when the SDK is running in the background. Please see [this](https://developer.android.com/about/versions/oreo/background) for more details.__

```
Please add the following string resources to your app with suitable changes to the text value.

<string name="background_notification_title">Notification title</string>
<string name="background_notification_subtitle">Notification subtitle</string>

Please add a drawable resource named ic_launcher to override the default icon for the persistent notification when the scan runs in background.
```

You can find more information and example usage in the `BeaconstacExample` app contained in the `examples` directory of this repo.
