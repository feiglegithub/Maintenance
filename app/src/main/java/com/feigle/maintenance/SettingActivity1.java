package com.feigle.maintenance;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.feigle.maintenance.databinding.ActivitySetting1Binding;

public class SettingActivity1 extends Activity {

    private String TAG = "SettingActivity1";
    private TextView mTextView;
    private ActivitySetting1Binding binding;
    private EditText editTextNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySetting1Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        editTextNumber = binding.editTextNumber;
    }

    @Override
    protected void onStart() {
        super.onStart();
        String number = getSharedPreferences(getString(R.string.preference_file_key),MODE_PRIVATE).getString(getString(R.string.saved_number_key),"");
        editTextNumber.setText(number);
    }

    public void onClick(View v){
        String text = editTextNumber.getText().toString();
        if (text.equals("") || text.equals(" "))
            Toast.makeText(this,R.string.empty_tip,Toast.LENGTH_LONG).show();
        else {
            SharedPreferences.Editor editor = getSharedPreferences("com.feigle.maintenance",MODE_PRIVATE).edit();
            editor.putString(getString(R.string.saved_number_key),text);
            editor.apply();
            finish();
        }
    }
}