package com.feigle.maintenance;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.feigle.maintenance.databinding.ActivitySetting2Binding;

public class SettingActivity2 extends Activity {

    private TextView mTextView;
    private ActivitySetting2Binding binding;
    private EditText editTextPrefix;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySetting2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        editTextPrefix = binding.editTextPrefix;
    }

    @Override
    protected void onStart() {
        super.onStart();
        String prefix = getSharedPreferences(getString(R.string.preference_file_key), MODE_PRIVATE).getString(getString(R.string.saved_prefix_key), "");
        editTextPrefix.setText(prefix);
    }

    public void onClick(View v) {
        String text = editTextPrefix.getText().toString();
        if (text.equals("") || text.equals(" ")) {
            Toast.makeText(this, R.string.empty_tip, Toast.LENGTH_LONG).show();
            return;
        }
        SharedPreferences.Editor editor = getSharedPreferences(getString(R.string.preference_file_key), MODE_PRIVATE).edit();
        editor.putString(getString(R.string.saved_prefix_key), text);
        editor.apply();
        finish();
    }
}