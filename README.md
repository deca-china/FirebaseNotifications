### NextActivity

## How to build:

From the root folder run  `.gradlew clean assembleRelease` for release aar outputs or `.gradlew clean assembleDebug" for debug outputs.
That will generate different aar files that needs to be then copied to the Unity project.

The outputs are the following ones:

1. firebaesnotifications-release.aar Logic to handle creation / refresh or fetch of push token, and push notifications.

2. nextactivity-release.aar Main Android activity for the games, extending from the UnityActivity.
3. nextsharing-release.aar Basic sharing plugin, to share images and / or text


#### NextFirebaseMessagingService:

Handles the push notifications, Android push token etc. Available API is:

`setCallbackOnPushTokenReceived(sring gameObjectName, String methodName)` 

Will tell to the service to call to the given gameObject's methodName passing an string which would be the push token, when received.

#### LocalNotifications:

Handles local notifications, using the previous class as base. But it is deprecated and will be completely replaced by Unitys implementation.

#### NextSharing

1. `public static void ShareText(String shareText, String shareTitle, String urlLink)` 
2. `public static void ShareImage(String shareText, String shareTitle, String imagePath, String urlLink)`
3. `public static void ShareImage(String shareText, String shareTitle, String imagePath)`

#### NextActivity:

1. `public static String getFcmToken(Context context)` 
2. `public String getPushToken() `
3. `public String getDeviceName()`
4. `public String getAdvertisingId()`
5. `public String getInstallerPackageName()`
6. `public boolean wasMusicPlayingBeforeUnityStart()`
7. `public void setNotificationChannel(String channelId, String channelName, int importance)`
8. `public int getStreamVolume()`
9. `public String getAndroidId()`
10. `public boolean isOnWifi()`
11.`public String getSHA1FromFile(String filename)`
12. `public boolean isNetworkAvailable()`
13. `public boolean canOpenURLScheme(final String URLScheme)`
14. `public String getCountryCode()`