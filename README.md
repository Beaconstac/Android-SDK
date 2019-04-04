# Beaconstac Android-SDK

## Introduction

Beaconstac Advanced Android SDK is meant only for specialized use cases. Please check with the support team before deciding to integrate this SDK.

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

__Note:__ If this is used, the Beaconstac SDK will automatically start scanning for beacons and trigger notifications based on rules defined on Beaconstac dashboard. You can also explicitly start or stop beacon scanning by calling `Beaconstac.getInstance(getApplicationContext()).startScanningBeacons()` and `Beaconstac.getInstance(getApplicationContext()).stopScanningBeacons()` respectively. Please refer [advanced](#3-start-scan) for more info.

__For using the SDK while the app is in the background or terminated state, please refer [Handling background scanning and delivering notifications](#handling-background-scanning-and-delivering-notifications).__

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
        Beaconstac.getInstance(getApplicationContext()).startScanningBeacons(new MSErrorListener() {
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
Beaconstac beaconstac = Beaconstac.getInstance(getApplicationContext());
```
#### 3. Start scan

```java
Beaconstac.getInstance(getApplicationContext()).startScanningBeacons(new MSErrorListener() {
    @Override
    public void onError(MSException msException) {

    }
});
```
__4. Stop scan__
```java
Beaconstac.getInstance(getApplicationContext()).stopScanningBeacons(new MSErrorListener() {
    @Override
    public void onError(MSException msException) {

    }
});
```

__5. Get beacon event callbacks__

__Note: You only need to implement this if you want to get callbacks for beacon events.__

```java
Beaconstac.getInstance(getApplicationContext()).setBeaconScannerCallbacks(new BeaconScannerCallbacks() {
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
Beaconstac.getInstance(getApplicationContext()).overrideBeaconstacNotification(new BeaconstacNotification() {
    @Override
    public void notificationTrigger(Notification.Builder notification) {

    }
});
```


__7. Add additional values to your webhooks__

```java
Beaconstac.getInstance(getApplicationContext()).addValuesToWebhook(MY_KEY, VALUE);
```

OR
```java
Beaconstac.getInstance(getApplicationContext()).addValuesToWebhook(MY_KEY_VALUE_HASHMAP);
```

__8. Set user's name__
```java
Beaconstac.getInstance(getApplicationContext()).setUserName(USER_FIRST_NAME , USER_LAST_NAME);
```

__9. Set user's email__
```java
Beaconstac.getInstance(getApplicationContext()).setUserEmail(USER_EMAIL);
```

__10. Set scan power mode__

Set the power mode for bluetooth low energy scan callbacks. Set to HIGH for frequent scans with high power consumption.
Default value is set to BALANCED.
```java
Beaconstac.getInstance(getApplicationContext()).setPowerMode(POWER_MODE);
```

__11. Set latch latency__

Set the device's willingness to camp-on to new beacons if it is already camped on to one. If set to LOW the device switches to the other beacons quickly and if set to HIGH the device's attachment will be steady.
The default value is set to MEDIUM.
```java
Beaconstac.getInstance(getApplicationContext()).setLatchLatency(LATCH_LATENCY);
```

## Handling background scanning and delivering notifications


### App running in the background

__For Android < 8__

_You can scan for beacons when the app is in the foreground or in the background._

__For Android >= 8__

_You scan for beacons when the app is in the foreground.
If you need to scan for beacons while the app is running in the background, please start a [FOREGROUND SERVICE](https://developer.android.com/guide/components/services#Foreground) and start the scan inside the service._


### App in terminated state

You can use the following method to register a `BroadcastReceiver` and get callbacks when the device enters the range of a new beacon or beacon goes out of range.

The `BroadcastReceiver` will receive callbacks as follows.

_1. Periodically<br>_
_2. When device screen is turned on._

*__Note: The `BroadcastReceiver` will be unregistered when the device reboots, please make sure you register the receiver again.__*

__BroadcastReceiver example implementation__
```java
public class MyBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Nearby.getMessagesClient(context).handleIntent(intent, new MessageListener() {
            @Override
            public void onFound(Message message) {
            }
    
            @Override
            public void onLost(Message message) {
            }
        });
    }
}
```

__1. Subscribe for updates__

```java
Beaconstac.getInstance(getApplicationContext()).subscribeForBackgroundUpdates(new MyBroadcastReceiver());
```

Once a callback is received by the receiver you can choose to start scanning for beacons using the Beaconstac SDK and once done you *must* stop the scan. Please refer to the example receiver [here](https://github.com/Beaconstac/Android-SDK/blob/master/examples/BeaconstacExample/app/src/main/java/com/mobstac/beaconstacdemo/NearbyBeaconBroadcastReceiver.java).

*__Note: Stopping the scan is required and should be a mandatory step. If not stopped, the Beaconstac SDK will keep scanning indefinitely. This will result in high power consumption and will keep on showing a persistent notification on devices running on Android 8 and above.__*


__2. Unsubscribe from updates__

```java
Beaconstac.getInstance(getApplicationContext()).unSubscribeFromBackgroundUpdates(new MyBroadcastReceiver());
```

__Due to the restriction added on Android 8 and above on running background tasks, a persistent notification will be shown when the SDK is running in the background. Please see [this](https://developer.android.com/about/versions/oreo/background) for more details.__

```
Please add the following string resources to your app with suitable changes to the text value.

<string name="background_notification_title">Notification title</string>
<string name="background_notification_subtitle">Notification subtitle</string>

Please add a drawable resource named ic_launcher to override the default icon for the persistent notification shown when the scan runs in background.
```

You can find more information and example usage in the `BeaconstacExample` app contained in the `examples` directory of this repo.
