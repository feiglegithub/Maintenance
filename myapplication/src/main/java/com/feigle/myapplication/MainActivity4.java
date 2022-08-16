package com.feigle.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.feigle.myapplication.databinding.ActivityMain4Binding;

public class MainActivity4 extends Activity {

    private TextView mTextView;
    private ActivityMain4Binding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMain4Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mTextView = binding.text;
    }
}