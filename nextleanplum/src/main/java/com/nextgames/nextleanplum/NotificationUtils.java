package com.nextgames.nextleanplum;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;

public class NotificationUtils extends ContextWrapper{

    public final String DEFAULT_NOTIFICATION_CHANNEL_ID = this.getPackageName();
    public final String DEFAULT_NOTIFICATION_CHANNEL_NAME = getApplicationInfo().loadLabel(getPackageManager()).toString();


    public NotificationUtils(Context base) {
        super(base);
    }

    public void createChannel(String channelId, String channelName, int visibility, int importance) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, importance);
            notificationChannel.enableLights(true);
            notificationChannel.setShowBadge(true);
            notificationChannel.setLockscreenVisibility(visibility);

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    public void createChannel(String channelId, String channelName, int importance) {
        createChannel(channelId, channelName, Notification.VISIBILITY_PUBLIC, importance);
    }

    public void createChannel(String channelId, String channelName) {
        createChannel(channelId, channelName, Notification.VISIBILITY_PUBLIC, NotificationManager.IMPORTANCE_DEFAULT);
    }

    public void createDefaultChannel() {
        createChannel(DEFAULT_NOTIFICATION_CHANNEL_ID, DEFAULT_NOTIFICATION_CHANNEL_NAME, Notification.VISIBILITY_PUBLIC, NotificationManager.IMPORTANCE_DEFAULT);
    }
}
