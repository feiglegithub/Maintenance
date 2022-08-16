package com.feigle.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.feigle.myapplication.databinding.ActivityMain3Binding;

public class MainActivity3 extends Activity {

    private TextView mTextView;
    private ActivityMain3Binding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMain3Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mTextView = binding.text;
    }
}