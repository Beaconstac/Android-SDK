# Android-SDK

## Introduction

Beaconstac SDK is an easy way to enable proximity marketing and location analytics through an iBeacon-compliant BLE network. 

## Documentation

Please refer to the API documentation on the [Beaconstac developer hub](https://beaconstac.github.io/Android-SDK/).

## Integration with your existing project in Android Studio

1. Download or clone this repo on your system.
2. Copy the `beaconstac-release.aar` file into the `libs` directory of your app. Refer the included sample app for example.
3. In the `build.gradle` file of your project, add the following in the repositories section:
        flatDir {
            dirs 'libs'
        }
![](images/repositories.png "Repositories")
4. In the `build.gradle` file of the app, add the following in the dependencies section:
        compile (name: 'beaconstac-release', ext: 'aar')
![](images/dependencies.png "Dependencies")
5. Refresh all Gradle projects.
6. Add the `BLUETOOTH` and `BLUETOOTH_ADMIN` permissions to app manifest:
        <uses-permission android:name="android.permission.BLUETOOTH" />
        <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
![](images/permissions.png "Permissions")
7. Add bluetooth `STATE_CHANGED` action to the activity `intent-filter`:
        <action android:name="android.bluetooth.adapter.action.STATE_CHANGED" />
![](images/actions.png "Actions")
6. Add the Beaconstac BLEService to your app manifest:
        <service android:name="com.mobstac.beaconstac.core.MSBLEService" android:enabled="true"/>
![](images/bleservice.png "BLEService")
7. Add values for UUID, region_indentifier filter to `strings.xml`:
        <!-- Override the defaults -->
        <string name="uuid"><!-- UUID to filter beacons by --></string>
        <string name="region_identifier"><!-- unique identifier for beacons --></string
        <!-- Optional: Override text message to be displayed when Bluetooth is disabled -->
        <string name="ble_disabled_text">Bluetooth is disabled. Beacon detection would not work.</string>
![](images/resources.png "Resources")
8. To start ranging beacons, implement `BeaconstacCallback` in your Activity or Fragment, and implement the methods `rangedBeacons`, `campedOnBeacon` and `exitedBeacon`.
![](images/callbacks.png "Callbacks")
9. You can find more information and example usage in the `BeaconstacExample` app contained in the `examples` directory of this repo.