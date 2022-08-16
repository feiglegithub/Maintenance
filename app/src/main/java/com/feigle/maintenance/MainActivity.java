package com.feigle.maintenance;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.wear.widget.WearableLinearLayoutManager;
import androidx.wear.widget.WearableRecyclerView;

import com.feigle.maintenance.common.ChannelId;
import com.feigle.maintenance.common.NotificationId;
import com.feigle.maintenance.common.What;
import com.feigle.maintenance.dao.NoticeDao;
import com.feigle.maintenance.databinding.ActivityMainBinding;
import com.feigle.maintenance.db.AppDatabase;
import com.feigle.maintenance.entity.Notice;
import com.feigle.maintenance.runnable.RequestNotice;
import com.feigle.maintenance.service.MyService;
import com.feigle.maintenance.util.NetworkManager;
import com.feigle.maintenance.util.NotificationUtil;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    private static RecyclerView recyclerView;
    private ActivityMainBinding binding;
    private NotificationManager notificationManager;
    private AppDatabase db;
    private static NoticeDao noticeDao;
    private static List<Notice> notices = new ArrayList<>();
    private static MyAdapter myAdapter;
    private static Context mContext;
    private SharedPreferences preferences;
    private String workNumber;
    private String prefix;
    private Thread requestNotice;
    private ConnectivityManager cm;
    private String className = "com.feigle.maintenance.service.MyService";
    private boolean isRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "----------------------------onCreate-----------------------------------");
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mContext = this;

        preferences = getSharedPreferences(getString(R.string.preference_file_key), MODE_PRIVATE);
        workNumber = preferences.getString(getString(R.string.saved_number_key), "");
        prefix = preferences.getString(getString(R.string.saved_prefix_key), "");

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        createNotificationChannel();

        db = AppDatabase.getInstance(this);
        noticeDao = db.noticeDao();

        myAdapter = new MyAdapter(notices);
        recyclerView = binding.recyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(myAdapter);

//        requestNotice = new Thread(new RequestNotice(preferences,this,noticeDao,notificationManager,cm));
//        requestNotice.start();
//        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
//        List<ActivityManager.RunningServiceInfo> list = am.getRunningServices(30);
//        list.forEach(item->{
//            Log.d(TAG,item.service.getClassName());
//            if (className.equals(item.service.getClassName()))
//            {
//                isRunning = true;
//                return;
//            }
//        });
//        if (!isRunning)
            startForegroundService(new Intent(this, MyService.class));

        new Thread(new GetNotice()).start();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "------------------------onStart-------------------------------------");
        if (!NetworkManager.isOnline(this)) {
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(Settings.ACTION_WIFI_SETTINGS), 0);
            Notification notification = NotificationUtil.build(this,
                    ChannelId.TASK,
                    getString(R.string.notification_network_title),
                    getString(R.string.network_disconnect_tip),
                    R.drawable.ic_baseline_miscellaneous_services_24,
                    pendingIntent);
            notificationManager.notify(NotificationId.SET, notification);
            Toast.makeText(this,R.string.network_disconnect_tip,Toast.LENGTH_LONG).show();
        }

//        if (workNumber.equals("") || workNumber.equals(" "))
//            startActivity(new Intent(this, SettingActivity1.class));
//        else if (prefix.equals("") || prefix.equals(" "))
//            startActivity(new Intent(this, SettingActivity2.class));
        if(workNumber.equals("") || workNumber.equals(" ") || prefix.equals("") || prefix.equals(" "))
            startActivity(new Intent(this,SettingsActivity.class));
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "-----------------------onStop-------------------");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "-----------------------onDestroy-------------------");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getRepeatCount() == 0) {
            if (keyCode == KeyEvent.KEYCODE_STEM_1) {
                startActivity(new Intent(this, SettingsActivity.class));
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    public static Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            switch (message.what) {
                case What.REFRESH:
                    myAdapter.notifyDataSetChanged();
                    break;
                case What.GET_NOTICE:
                    new Thread(new MainActivity.GetNotice()).start();
                    break;
                case What.TOAST:
                    Toast.makeText(mContext, message.obj.toString(), Toast.LENGTH_LONG).show();
                    break;
            }
            return false;
        }
    });

    public static void sendMessage(int what) {
        Message msg = mHandler.obtainMessage();
        msg.what = what;
        msg.sendToTarget();
    }

    static class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        List<Notice> notices;

        public class ViewHolder extends RecyclerView.ViewHolder {

            private final TextView textView;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                textView = itemView.findViewById(R.id.textView);
            }

            public TextView getTextView() {
                return textView;
            }
        }

        public MyAdapter(List<Notice> notices) {
            this.notices = notices;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.text_row_item, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Notice notice = notices.get(position);
            TextView tv = holder.getTextView();
            tv.setText(notice.info + "\n" + notice.createTime);
            if (notice.read) {
                tv.setBackgroundColor(mContext.getColor(R.color.read_background));
            }
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    notice.read = true;
                    Log.d(TAG+".MyAdapter","已读 "+notice.createTime);

                    new Thread(new UpdateRead(notice)).start();
                }
            });
        }

        @Override
        public int getItemCount() {
            return notices.size();
        }
    }

    static class GetNotice implements Runnable {

        @Override
        public void run() {
            notices.clear();
            notices.addAll(noticeDao.getAll());

            sendMessage(What.REFRESH);
        }
    }

    static class UpdateRead implements Runnable {

        Notice notice;

        public UpdateRead(Notice notice) {
            this.notice = notice;
        }

        @Override
        public void run() {
            noticeDao.update(notice);
            sendMessage(What.GET_NOTICE);
        }
    }

    private void createNotificationChannel() {
        List<NotificationChannel> list = new ArrayList<NotificationChannel>();

        NotificationChannel channel = new NotificationChannel(ChannelId.SERVICE, getString(R.string.channel_foreground_name), NotificationManager.IMPORTANCE_LOW);
        list.add(channel);

        channel = new NotificationChannel(ChannelId.SET, getString(R.string.channel_set_name), NotificationManager.IMPORTANCE_LOW);
        list.add(channel);

        channel = new NotificationChannel(ChannelId.TASK, getString(R.string.channel_task_name), NotificationManager.IMPORTANCE_HIGH);
        channel.enableLights(true);
        channel.enableVibration(true);
        list.add(channel);

        notificationManager.createNotificationChannels(list);
    }
}