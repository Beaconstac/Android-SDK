# Beaconstac Android-SDK

## Introduction

Beaconstac SDK is an easy way to enable proximity marketing and location analytics through an iBeacon-compliant BLE network.

## Integrate with your existing project in Android Studio

### In the `build.gradle` file of the app, add the following in the dependencies section:
```groovy
implementation 'com.android.support.constraint:constraint-layout:1.0.2'
compile 'com.mobstac.beaconstac:proximity:3.0.2'
```
Latest version<br>
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
        Beaconstac.getInstance().startScan(new MSErrorListener() {
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
beaconstac.addValuesToWebhook(MY_KEY, VALUE);
```

OR
```java
beaconstac.addValuesToWebhook(MY_KEY_VALUE_HASHMAP);
```

__8. Set user's name__
```java
Beaconstac.getInstance().setUserName(USER_FIRST_NAME , USER_LAST_NAME);
```

__9. Set user's email__
```java
Beaconstac.getInstance().setUserEmail(USER_EMAIL);
```

You can find more information and example usage in the `BeaconstacExample` app contained in the `examples` directory of this repo.
