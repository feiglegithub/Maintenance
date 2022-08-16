package com.feigle.maintenance.service;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.WindowManager;

import com.feigle.maintenance.MainActivity;
import com.feigle.maintenance.R;
import com.feigle.maintenance.bean.Parameter;
import com.feigle.maintenance.bean.ResponseModal;
import com.feigle.maintenance.bean.RequestModal;
import com.feigle.maintenance.common.ChannelId;
import com.feigle.maintenance.common.NotificationId;
import com.feigle.maintenance.common.What;
import com.feigle.maintenance.dao.NoticeDao;
import com.feigle.maintenance.db.AppDatabase;
import com.feigle.maintenance.entity.Notice;
import com.feigle.maintenance.httpservice.NoticeService;
import com.feigle.maintenance.util.NotificationUtil;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import androidx.annotation.NonNull;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MyService extends Service {
    private String packageName = "com.feigle.maintenance";
    private final String TAG = "MyService";
    // The channel ID of the notification.
    private int MIN_BANDWIDTH_KBPS = 320;

    private Context mContext;
    private NotificationManager notificationManager;
    private TelephonyManager tm;
    private PendingIntent pendingIntent;
    private Intent notificationIntent;
    private AppDatabase db;
    private NoticeDao noticeDao;
    private Retrofit retrofit;
    private NoticeService noticeService;
    private SharedPreferences sp;
    private ConnectivityManager cm;
    private PendingIntent myServiceIntent;
    private AlarmManager am;
    private ActivityManager activityManager;
    private WifiManager wifiManager;
    private NetworkRequest networkRequest;
    private boolean isConnect = false;
    private PowerManager pm;
    private PowerManager.WakeLock wl;

    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "----------onCreate-----------");

        mContext = this;
        notificationManager = getSystemService(NotificationManager.class);
        cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        pm = (PowerManager) getSystemService(POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"maintenance:MyWakeLockTag");
        wl.acquire();

        sp = getSharedPreferences(getString(R.string.preference_file_key), MODE_PRIVATE);

        db = AppDatabase.getInstance(this);
        noticeDao = db.noticeDao();

        String prefix = sp.getString(getString(R.string.saved_prefix_key), "");
        if (!prefix.equals("") && !prefix.equals(" ")) {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .readTimeout(Duration.ofSeconds(60))
                    .writeTimeout(Duration.ofSeconds(60))
                    .build();
            retrofit = new Retrofit.Builder().baseUrl(String.format(getString(R.string.url), prefix))
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
            noticeService = retrofit.create(NoticeService.class);
        }

        notificationIntent = new Intent(mContext, MainActivity.class);
        Notification notification =
                new Notification.Builder(mContext, ChannelId.SERVICE)
                        .setContentTitle(getString(R.string.run_title))
                        .setSmallIcon(R.drawable.ic_baseline_miscellaneous_services_24)
                        .build();
        startForeground(NotificationId.SERVICE, notification);

        myServiceIntent = PendingIntent.getService(mContext, 0, new Intent(mContext, MyService.class), 0);
        am = (AlarmManager) getSystemService(ALARM_SERVICE);
//        am.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), myServiceIntent);
        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,SystemClock.elapsedRealtime(),1000,myServiceIntent);

        networkRequest = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();
        cm.registerNetworkCallback(networkRequest,networkCallback);

        wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF,"wifi_lock");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "-----------onStartCommand----------");

//        if (wifiManager.isWifiEnabled()){
            Network activeNetwork = cm.getActiveNetwork();

            if (isConnect) {
//                NetworkCapabilities capabilities = cm.getNetworkCapabilities(activeNetwork);
//                boolean transportWifi = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);

//                if (transportWifi){
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            request();
                        }
                    }).start();
//                }else{
//                    Log.d(TAG, getString(R.string.network_disconnect_tip));
//                    Intent intent1 = new Intent(Settings.ACTION_WIFI_SETTINGS);
//                    pendingIntent = PendingIntent.getActivity(mContext, 0, intent1, 0);
//                    Notification notification = NotificationUtil.build(mContext,
//                            ChannelId.TASK,
//                            getString(R.string.notification_network_title),
//                            getString(R.string.network_disconnect_tip),
//                            R.drawable.ic_baseline_miscellaneous_services_24,
//                            pendingIntent);
//                    notificationManager.notify(NotificationId.SET, notification);
//                }

            } else {
                Log.d(TAG, getString(R.string.network_disconnect_tip));
                Intent intent1 = new Intent(Settings.ACTION_WIFI_SETTINGS);
//                intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent1);
//                cm.requestNetwork(networkRequest,networkCallback);
//                boolean flag = wifiManager.reconnect();
//                if (!flag){
//                    Log.d(TAG, getString(R.string.wifi_reconnect_fail));
                    pendingIntent = PendingIntent.getActivity(mContext, 0, intent1, 0);
                    Notification notification = NotificationUtil.build(mContext,
                            ChannelId.TASK,
                            getString(R.string.notification_network_title),
                            getString(R.string.network_disconnect_tip),
                            R.drawable.ic_baseline_miscellaneous_services_24,
                            pendingIntent);
                    notificationManager.notify(NotificationId.SET, notification);
//                }
//            am.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + (1000 * 60), myServiceIntent);
            }
//        }else{
//            Log.d(TAG, getString(R.string.wifi_disable_tip));
//            boolean wifiEnabled = wifiManager.setWifiEnabled(true);
//            if (wifiEnabled){
//                boolean flag = wifiManager.reconnect();
////            cm.requestNetwork(networkRequest,networkCallback);
//                if (!flag){
//                    Log.d(TAG, getString(R.string.wifi_reconnect_fail));
//                    Intent intent1 = new Intent(Settings.ACTION_WIFI_SETTINGS);
//                    pendingIntent = PendingIntent.getActivity(mContext, 0, intent1, 0);
//                    Notification notification = NotificationUtil.build(mContext,
//                            ChannelId.SET,
//                            getString(R.string.notification_network_title),
//                            getString(R.string.network_disconnect_tip),
//                            R.drawable.ic_baseline_miscellaneous_services_24,
//                            pendingIntent);
//                    notificationManager.notify(NotificationId.SET, notification);
//                }
//            }else{
//                Log.d(TAG, getString(R.string.wifi_enabled_fail));
//                Intent intent1 = new Intent(Settings.ACTION_WIFI_SETTINGS);
//                pendingIntent = PendingIntent.getActivity(mContext, 0, intent1, 0);
//                Notification notification = NotificationUtil.build(mContext,
//                        ChannelId.SET,
//                        getString(R.string.notification_network_title),
//                        getString(R.string.wifi_disable_tip),
//                        R.drawable.ic_baseline_miscellaneous_services_24,
//                        pendingIntent);
//                notificationManager.notify(NotificationId.SET, notification);
//            }
//
//        }


        return super.onStartCommand(intent, flags, startId);
    }

    boolean isAlive() {
        AtomicBoolean flag = new AtomicBoolean(false);
        List<ActivityManager.RunningAppProcessInfo> list = activityManager.getRunningAppProcesses();
        list.forEach(item -> {
            Log.d(TAG, item.processName);
            if (packageName.equals(item.processName)) {
                flag.set(true);
                return;
            }
        });
        return flag.get();
    }

    ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(Network network) {
            Log.d(TAG + ".networkCallback", "----------------onAvailable---------------");
            isConnect = true;
        }

        @Override
        public void onLost(@NonNull Network network) {
            super.onLost(network);
            Log.d(TAG + ".networkCallback", "----------------onLost---------------");
            isConnect = false;
            cm.unregisterNetworkCallback(networkCallback);
            cm.requestNetwork(networkRequest,networkCallback);
        }

        @Override
        public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
            super.onCapabilitiesChanged(network, networkCapabilities);
            Log.d(TAG + ".networkCallback", "----------------onCapabilitiesChanged---------------");
        }
    };

    void request() {
        try {
            Log.d(TAG, "---------------request-------------------");

            String workNumber = sp.getString(getString(R.string.saved_number_key), "");

            if (workNumber.equals("") || workNumber.equals(" "))
                return;

            if (retrofit == null) {
                String prefix = sp.getString(getString(R.string.saved_prefix_key), "");
                if (!prefix.equals("") && !prefix.equals(" ")) {
                    retrofit = new Retrofit.Builder().baseUrl(String.format(getString(R.string.url), prefix))
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                    noticeService = retrofit.create(NoticeService.class);
                } else
                    return;
            }

            Parameter parameter = new Parameter();
            parameter.Value = workNumber;
            List parameters = new ArrayList<>();
            parameters.add(parameter);

            com.feigle.maintenance.bean.Context mesContext = new com.feigle.maintenance.bean.Context();
            mesContext.InvOrgId = getString(R.string.inv_org_id);

            RequestModal request = new RequestModal();
            request.Parameters = parameters;
            request.ApiType = getString(R.string.api_type);
            request.Context = mesContext;
            request.Method = "GetWatchMessage";
            Call<ResponseModal> call = noticeService.getNoticeByWorkNumber(request);

            Response<ResponseModal> res = call.execute();
            Log.d(TAG, res.code() + "，" + res.message());
            if (res.isSuccessful()) {
                if (res.body().Success) {
                    String content = res.body().Result;

                    Log.d(TAG, "result：" + content);
                    if (content.equals("") || content.equals(" "))
                        return;

                    Notice notice = new Notice();
                    notice.read = false;
                    notice.createTime = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").
                            format(new Date());
                    notice.info = content;
                    noticeDao.insert(notice);

                    noticeDao.delete(30);

                    notificationIntent.putExtra("notice", notice);
                    pendingIntent = PendingIntent.getActivity(mContext, 0, notificationIntent, 0);
                    Notification notification = NotificationUtil.build(mContext,
                            ChannelId.TASK,
                            getString(R.string.notification_title),
                            content,
                            R.drawable.ic_baseline_miscellaneous_services_24,
                            pendingIntent);
                    notificationManager.notify(NotificationId.TASK, notification);

                    MainActivity.sendMessage(What.GET_NOTICE);
                } else {
                    if (isAlive())
                        MainActivity
                                .mHandler
                                .obtainMessage(What.TOAST, res.body().Message)
                                .sendToTarget();
                }

            } else {
                MainActivity
                        .mHandler
                        .obtainMessage(What.TOAST, getString(R.string.request_fail) + ":" + res.message())
                        .sendToTarget();
            }

        } catch (IOException e) {
            MainActivity
                    .mHandler
                    .obtainMessage(What.TOAST, "请求任务通知信息异常：" + e.getMessage())
                    .sendToTarget();
            Log.d(TAG, "请求异常：" + e.getMessage());

            Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
            pendingIntent = PendingIntent.getActivity(mContext,0,intent,0);
            Notification notification = NotificationUtil.build(mContext,
                    ChannelId.SET,
                    "请求异常",
                    e.getMessage(),
                    R.drawable.ic_baseline_miscellaneous_services_24,
                    pendingIntent);
            notificationManager.notify(NotificationId.TIPS, notification);
        } finally {
//            am.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 1000, myServiceIntent);
        }
    }

    class MyRunnalbe implements Runnable {

        @Override
        public void run() {
            Log.d(TAG + ".MyRunnalbe", "----------------------------run---------------------------------");
            while (true) {
                try {
                    Thread.sleep(1000 * 2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Log.d(TAG + ".MyRunnalbe", "--------------------------------while-------------------------------");
                Network activeNetwork = cm.getActiveNetwork();
                if (activeNetwork != null) {
                    int bandwidth = cm.getNetworkCapabilities(activeNetwork).getLinkDownstreamBandwidthKbps();
                    if (bandwidth < MIN_BANDWIDTH_KBPS) {
                        NetworkRequest request = new NetworkRequest.Builder()
                                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                                .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
                                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                                .build();

                        cm.requestNetwork(request, networkCallback);
                    } else {
                    request();
                    }
                } else {
                    Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                    pendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0);
                    Notification notification = NotificationUtil.build(mContext,
                            ChannelId.SET,
                            getString(R.string.notification_network_title),
                            getString(R.string.network_disconnect_tip),
                            R.drawable.ic_baseline_miscellaneous_services_24,
                            pendingIntent);
                    notificationManager.notify(NotificationId.SET, notification);
                    try {
                        Thread.sleep(1000 * 60 * 5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
            }
        }
    }
}