package com.nextgames.nextleanplum;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.iid.InstanceIdResult;
import com.helpshift.unityproxy.HelpshiftUnity;
import com.leanplum.LeanplumPushFirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.nextgames.NextFirebaseMessagingService;

import java.util.Map;

public class NextLeanplumMessagingService extends LeanplumPushFirebaseMessagingService {
    private static final String TAG = "FCM Leanplum";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Map<String, String> data = remoteMessage.getData();
        Log.d(TAG, "NextLeanplumMessagingService OnMessageReceived throught Leanplum messaging");
        NextFirebaseMessagingService nextService = new NextFirebaseMessagingService();
        nextService.applicationContext = getApplicationContext();
        nextService.onMessageReceived(remoteMessage);
    }

    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d(TAG, "On Token refreshed" + token);
        NextFirebaseMessagingService nextService = new NextFirebaseMessagingService();
        nextService.applicationContext = getApplicationContext();
        nextService.onNewToken(token);
    }

    public void onSuccess(InstanceIdResult instanceIdResult) {
        Log.d(TAG, "FirebaseInstanceId on onSuccess");
        NextFirebaseMessagingService nextService = new NextFirebaseMessagingService();
        nextService.onNewToken(instanceIdResult.getToken());
    }
}
