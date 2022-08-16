package com.feigle.maintenance.runnable;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.provider.Settings;
import android.util.Log;

import com.feigle.maintenance.MainActivity;
import com.feigle.maintenance.R;
import com.feigle.maintenance.bean.Parameter;
import com.feigle.maintenance.bean.RequestModal;
import com.feigle.maintenance.bean.ResponseModal;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RequestNotice implements Runnable{
    private String TAG = "RequestNotice";

    private PendingIntent pendingIntent;
    private Retrofit retrofit;
    private NoticeService noticeService;

    private SharedPreferences preferences;
    private Context mContext;
    private NoticeDao noticeDao;
    private NotificationManager notificationManager;
    private ConnectivityManager cm;

    public RequestNotice(SharedPreferences preferences, Context mContext, NoticeDao noticeDao, NotificationManager notificationManager, ConnectivityManager cm) {
        this.preferences = preferences;
        this.mContext = mContext;
        this.noticeDao = noticeDao;
        this.notificationManager = notificationManager;
        this.cm = cm;
    }

    @Override
    public void run() {
        Log.d(TAG, "----------------------------run---------------------------------");

        while (true){
            Log.d(TAG, "--------------------------------while-------------------------------");
            Network activeNetwork = cm.getActiveNetwork();

            if (activeNetwork != null) {
                NetworkCapabilities networkCapabilities = cm.getNetworkCapabilities(activeNetwork);
                boolean hasTransport = networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);

                if (hasTransport){
                    try {
                        Thread.sleep(1000);

                        String workNumber = preferences.getString(mContext.getString(R.string.saved_number_key), "");

                        if (workNumber.equals("") || workNumber.equals(" "))
                            continue;

                        if (retrofit == null) {
                            String prefix = preferences.getString(mContext.getString(R.string.saved_prefix_key), "");
                            if (!prefix.equals("") && !prefix.equals(" ")) {
                                retrofit = new Retrofit.Builder().baseUrl(String.format(mContext.getString(R.string.url), prefix))
                                        .addConverterFactory(GsonConverterFactory.create())
                                        .build();
                                noticeService = retrofit.create(NoticeService.class);
                            } else
                                continue;
                        }

                        Parameter parameter = new Parameter();
                        parameter.Value = workNumber;
                        List parameters = new ArrayList<>();
                        parameters.add(parameter);

                        com.feigle.maintenance.bean.Context mesContext = new com.feigle.maintenance.bean.Context();
                        mesContext.InvOrgId = mContext.getString(R.string.inv_org_id);

                        RequestModal request = new RequestModal();
                        request.Parameters = parameters;
                        request.ApiType = mContext.getString(R.string.api_type);
                        request.Context = mesContext;
                        request.Method = "GetWatchMessage";
                        Call<ResponseModal> call = noticeService.getNoticeByWorkNumber(request);

                        Response<ResponseModal> res = call.execute();
                        Log.d(TAG, res.code() + "，"+res.message());
                        if (res.isSuccessful()) {
                            if (res.body().Success) {
                                String content = res.body().Result;

                                Log.d(TAG,"result："+content);
                                if (content.equals("") || content.equals(" "))
                                    continue;

                                Notice notice = new Notice();
                                notice.read = false;
                                notice.createTime = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").
                                        format(new Date());
                                notice.info = content;
                                noticeDao.insert(notice);

                                noticeDao.delete(30);

                                Intent intent = new Intent(mContext,MainActivity.class);
                                pendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0);
                                Notification notification = NotificationUtil.build(mContext,
                                        ChannelId.TASK,
                                        mContext.getString(R.string.notification_title),
                                        content,
                                        R.drawable.ic_baseline_miscellaneous_services_24,
                                        pendingIntent);
                                notificationManager.notify(NotificationId.TASK, notification);

                                MainActivity.sendMessage(What.GET_NOTICE);
                            } else
                                MainActivity
                                        .mHandler
                                        .obtainMessage(What.TOAST, res.body().Message)
                                        .sendToTarget();

                        } else
                            MainActivity
                                    .mHandler
                                    .obtainMessage(What.TOAST, mContext.getString(R.string.request_fail) + ":" + res.message())
                                    .sendToTarget();

                    } catch (IOException | InterruptedException e) {
                        MainActivity
                                .mHandler
                                .obtainMessage(What.TOAST, "请求任务通知信息异常：" + e.getMessage())
                                .sendToTarget();
                        Log.d(TAG, "请求异常：" + e.getMessage());

                        Notification notification = NotificationUtil.build(mContext,
                                ChannelId.TASK,
                                "请求异常",
                                e.getMessage(),
                                R.drawable.ic_baseline_miscellaneous_services_24,
                                pendingIntent);
                        notificationManager.notify(NotificationId.TASK, notification);
                    }
                }else{
                    Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                    pendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0);
                    Notification notification = NotificationUtil.build(mContext,
                            ChannelId.TASK,
                            mContext.getString(R.string.notification_network_title),
                            mContext.getString(R.string.network_disconnect_tip),
                            R.drawable.ic_baseline_miscellaneous_services_24,
                            pendingIntent);
                    notificationManager.notify(NotificationId.SET, notification);
                    try {
                        Thread.sleep(1000 * 60 * 1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                Log.d(TAG,mContext.getString(R.string.network_disconnect_tip));
                MainActivity
                        .mHandler
                        .obtainMessage(What.TOAST, mContext.getString(R.string.network_disconnect_tip))
                        .sendToTarget();

                Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                pendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0);
                Notification notification = NotificationUtil.build(mContext,
                        ChannelId.TASK,
                        mContext.getString(R.string.notification_network_title),
                        mContext.getString(R.string.network_disconnect_tip),
                        R.drawable.ic_baseline_miscellaneous_services_24,
                        pendingIntent);
                notificationManager.notify(NotificationId.SET, notification);
                try {
                    Thread.sleep(1000 * 60 * 1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
