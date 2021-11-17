package com.Mic.microphonebooster;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import androidx.appcompat.app.AppCompatActivity;

public class Settings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setTitle("Settings");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }
    @Override
    public void finish()
    {
        super.finish();
        overridePendingTransition(R.anim.slide_out_left,R.anim.slide_out_right);
    }
    public static class RecorderPreferenceFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener{
        String format = ".mp3";
        String recordType = "mono";
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings_main);
            Preference sample_rate = findPreference("Sample_Rate");
            Preference audio_format = findPreference("chosenFormat");
            Preference record_Type = findPreference("recordtype");
            bindformatToValue(audio_format,".mp3");
            bindformatToValue(record_Type,"mono");
            bindformatToValue(sample_rate,"44kHz(CD)");
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            findPreference("chosenFormat").setOnPreferenceChangeListener((preference1, newValue) -> {
                format = newValue.toString();
                if(format.equals("48 KHz (highest)"))
                {
                    new AlertDialog.Builder(getActivity())
                            .setTitle("some phones don't support 48 KHz recording")
                            .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                            .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
                preference1.setSummary(format);
                return true;
            });
            findPreference("recordtype").setOnPreferenceChangeListener((preference12, newValue) -> {
                recordType = newValue.toString();
                if(recordType.equals("stereo"))
                {
                    new AlertDialog.Builder(getActivity())
                            .setTitle("some phones don't support stereo recording")
                            .setMessage("if the recording is choppy, change the type to mono")
                            .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                            .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
                preference12.setSummary(recordType);
                return true;
            });
            String stringValue = value.toString();
            preference.setSummary(stringValue);
            return true;
        }

        private void bindGainToValue(Preference preference,int defvalue) {
            preference.setOnPreferenceChangeListener(this);
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(preference.getContext());
            int preferenceString = preferences.getInt(preference.getKey(), defvalue);
            onPreferenceChange(preference, preferenceString);
        }
        private void bindformatToValue(Preference preference,String defvalue) {
            preference.setOnPreferenceChangeListener(this);
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(preference.getContext());
            String mmm = preferences.getString(preference.getKey(), defvalue);
            onPreferenceChange(preference, mmm);
        }
    }
}