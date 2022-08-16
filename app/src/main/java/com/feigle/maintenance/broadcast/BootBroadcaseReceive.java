package com.feigle.maintenance.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.feigle.maintenance.MainActivity;
import com.feigle.maintenance.service.MyService;

public class BootBroadcaseReceive extends BroadcastReceiver {
    private String TAG = "BootBroadcaseReceive";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG,"-----------------------onReceive-----------------------");
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            Intent intent1 = new Intent(context,MainActivity.class);
            intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent1);
        }
    }
}
