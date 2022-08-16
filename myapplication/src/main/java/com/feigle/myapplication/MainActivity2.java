package com.feigle.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.Activity;
import android.os.Bundle;

import com.feigle.myapplication.databinding.ActivityMain2Binding;

public class MainActivity2 extends Activity {

    private ActivityMain2Binding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main2);
        User user = new User("test","User");
        binding.setUser(user);
    }
}