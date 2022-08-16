package com.feigle.myapplication;

import android.util.Log;
import android.view.View;

public class MyHandlers {
    private String TAG = "MyHandlers";
    public void onClickFirend(View v){
        Log.d(TAG,"----------------------------"+v.getId()+"-----------------------");
    }
}
