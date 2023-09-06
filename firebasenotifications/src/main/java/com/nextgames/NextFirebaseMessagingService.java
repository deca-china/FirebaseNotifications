package com.nextgames;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.*;
import com.helpshift.unityproxy.HelpshiftUnity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.BitmapFactory;

//import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Map;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;

/**/
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.preference.PreferenceManager;

import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.*;

import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.security.MessageDigest;
/**/

public class NextFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCM";
    private static final String FCM_TOKEN = "fcm_token";
    private String unityGameObject;
    private String unityGameMethod;
    public Context applicationContext;
    private static NextFirebaseMessagingService instance;

    private String pushToken;

    public NextFirebaseMessagingService()
    {
        Log.d(TAG, "NextFirebaseMessagingService on onSuccess");
        if (instance == null)
            instance = this;
        if (FirebaseInstanceId.getInstance() != null && FirebaseInstanceId.getInstance().getInstanceId() != null)
            FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener( new OnSuccessListener<InstanceIdResult>() {
                @Override
                public void onSuccess(InstanceIdResult instanceIdResult) {
                    Log.d(TAG, "FirebaseInstanceId on onSuccess");
                    pushToken = instanceIdResult.getToken();
                    sendPushToken();
                }
            });
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d(TAG, "On Token Refreshed" + token);
        sendRegistrationToServer(token);
    }

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    public void sendRegistrationToServer(String token) {
        Log.d(TAG, "sendRegistrationToServer");
        if (token!=null && !token.isEmpty()) {
            pushToken = token;
        }
        sendPushToken();
    }

    private void sendPushToken() {
        Log.d(TAG, "sendPushToken " + pushToken);
        if (unityGameObject != null && unityGameMethod != null) {
            Log.d(TAG, "sendPushToken calling to unity object");
            com.unity3d.player.UnityPlayer.UnitySendMessage(unityGameObject, unityGameMethod, pushToken);
        }
    }

    // Save the token to user preferences
    public void saveFcmToken(String token) {
        Log.d(TAG, "saveFcmToken");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.edit().putString(FCM_TOKEN, token).apply();
    }

    // Get Firebase Token, null if it was not retrieved and saved yet.
    public String getFcmToken(Context context) {
        Log.d(TAG, "getFcmToken");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(FCM_TOKEN, null);
    }

    private void SetCallbackOnPushTokenReceivedInternal(String gameObjectName, String methodName) {
        Log.d(TAG, "SetCallbackOnPushTokenReceivedInternal");
        unityGameObject = gameObjectName;
        unityGameMethod = methodName;

        if (pushToken != null && !pushToken.isEmpty()) {
            sendPushToken();
        }
    }

    public static void setCallbackOnPushTokenReceived(String gameObjectName, String methodName) {
        Log.d(TAG, "setCallbackOnPushTokenReceived");
        if (instance != null) {
            Log.d(TAG, "setCallbackOnPushTokenReceived instance is not null");
            instance.SetCallbackOnPushTokenReceivedInternal(gameObjectName, methodName);
        }
    }

    private Context GetApplicationContext()
    {
        if (applicationContext == null)
        {
            return getApplicationContext();
        }
        else
        {
            return applicationContext;
        }
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // If the application is in the foreground handle both data and notification messages here.
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be inadb litiated.
        Log.d(TAG, "onMessageReceived");
        super.onMessageReceived(remoteMessage);

        Map<String, String> data = remoteMessage.getData();
        Log.d(TAG, "NextFirebaseMessaging service: onMessageReceived, data: " + data);

        if (data.get("origin") != null && data.get("origin").equals("helpshift")) {
            Log.d(TAG, "OnMessageReceived this is a helpshift notification");
            try
            {
                Class.forName("com.helpshift.unityproxy.HelpshiftUnity");
                Log.d(TAG, "Helpshift message received");
                HelpshiftUnity.handlePush(GetApplicationContext(), data);
                //sendNotification(GetApplicationContext(), data);
            }
            catch (ClassNotFoundException e) {
                Log.e(TAG, "helpshift class does not exist");
                return;
            }
        }
        else if (data.get("service") != null) {
            Log.d(TAG, "OnMessageReceived from GaaS?");
            data.put("sentTime", Long.toString(remoteMessage.getSentTime()));
            sendNotification(GetApplicationContext(), data);
        }

        Log.d(TAG, "From: " + remoteMessage.getFrom());
        if (remoteMessage.getNotification() != null)
                Log.d(TAG, "Notification Message Body: " + remoteMessage.getNotification().getBody());
    }

    @SuppressWarnings("deprecation")
    public void sendNotification(Context context, Map<String, String> data) {

        PackageManager pm = context.getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage(context.getPackageName());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        // Create the pending intent to launch the activity
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent,
                PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);

        String message = data.get("alert");
        String origin = data.get("origin");

        int smallIcon;
        try {
            smallIcon = context.getResources().getIdentifier("notification_icon", "mipmap", context.getPackageName());
        }
        catch (Resources.NotFoundException ex) {
            smallIcon = context.getResources().getIdentifier("app_icon", "mipmap", context.getPackageName());
            Log.e(TAG, ex.getMessage());
        }
        int icLauncherIcon;
        try {
            icLauncherIcon = context.getResources().getIdentifier("ic_launcher", "mipmap", context.getPackageName());
        }
        catch (Resources.NotFoundException ex) {
            Log.e(TAG, ex.getMessage());
            icLauncherIcon = context.getResources().getIdentifier("app_icon", "mipmap", context.getPackageName());
        }

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Notification.Builder notificationBuilder;

        boolean canParseSentTime = true;
        long sentTime = 0L;
        try {
            sentTime = Long.parseLong(data.get("sentTime"));
        }
        catch (NumberFormatException ex) {
            canParseSentTime = false;
        }


        // Create channels only for android > 8.0 ( Oreo )
        NotificationUtils mNotificationUtils = new NotificationUtils(context);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationBuilder = new Notification.Builder(context, mNotificationUtils.DEFAULT_NOTIFICATION_CHANNEL_ID);

            mNotificationUtils.createDefaultChannel();
            notificationBuilder.setChannelId(mNotificationUtils.DEFAULT_NOTIFICATION_CHANNEL_ID);
        }
        else
        {
            notificationBuilder = new Notification.Builder(context);
        }

        notificationBuilder.setSmallIcon(smallIcon);
        notificationBuilder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), icLauncherIcon));
        notificationBuilder.setContentTitle(origin);
        notificationBuilder.setContentText(message);
        notificationBuilder.setAutoCancel(true);
        notificationBuilder.setContentIntent(pendingIntent);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
            notificationBuilder.setShowWhen(canParseSentTime);
            if (canParseSentTime) {
                notificationBuilder.setWhen(sentTime);
            }
        }

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }


}