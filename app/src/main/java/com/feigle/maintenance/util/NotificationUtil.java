package com.feigle.maintenance.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;

public class NotificationUtil {
    public static Notification build(Context context, String channelId, String title, String text, int icon, PendingIntent intent) {
        return new Notification.Builder(context, channelId)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(icon)
                .setContentIntent(intent)
                .setAutoCancel(true)
                .build();

    }
}
