package com.feigle.maintenance;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.feigle.maintenance.databinding.ActivitySettingsBinding;

public class SettingsActivity extends Activity {

    private TextView mTextView;
    private ActivitySettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
    
    public void onClick(View v){
        switch (v.getId()){
            case R.id.buttonSetting1:
                startActivity(new Intent(this,SettingActivity1.class));
                break;
            case R.id.buttonSetting2:
                startActivity(new Intent(this,SettingActivity2.class));
                break;
            default:
                break;
        }
    }
}