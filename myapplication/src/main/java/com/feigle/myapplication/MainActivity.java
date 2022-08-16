package com.feigle.myapplication;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.feigle.myapplication.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import static android.provider.CalendarContract.EXTRA_EVENT_ID;

public class MainActivity extends Activity {

    private TextView mTextView;
    private ActivityMainBinding binding;
    private NotificationManager notificationManager;
    private String CHANNEL_ID_FOREGROUND = "channel_foreground";
    private String CHANNEL_ID_SET = "channel_set";
    private String CHANNEL_ID_TASK = "channel_task";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mTextView = binding.text;

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        createNotificationChannel();
    }

    public void click(View v){
        Log.d("MainActivity","------------------click-----------------");

        Intent viewIntent = new Intent(this, MainActivity.class);
        PendingIntent viewPendingIntent =
                PendingIntent.getActivity(this, 0, viewIntent, 0);

        // Notification channel ID is ignored for Android 7.1.1
        // (API level 25) and lower.
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID_TASK)
                        .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark_focused)
                        .setContentTitle("测试")
                        .setContentText("测试")
                        .setContentIntent(viewPendingIntent);
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(this);

        // Issue the notification with notification manager.
        notificationManager.notify(001, notificationBuilder.build());
    }

    private void createNotificationChannel() {
        List<NotificationChannel> list = new ArrayList<NotificationChannel>();

        NotificationChannel channel = new NotificationChannel(CHANNEL_ID_FOREGROUND, "低", NotificationManager.IMPORTANCE_LOW);
        channel.enableLights(false);
        list.add(channel);

        channel = new NotificationChannel(CHANNEL_ID_TASK, "高", NotificationManager.IMPORTANCE_HIGH);
        channel.enableVibration(true);
        list.add(channel);

        notificationManager.createNotificationChannel(channel);
    }
}