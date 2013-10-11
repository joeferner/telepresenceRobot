package com.telepresenceRobot.android;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity {
    public static final String WEB_SOCKET_URL = "pref_url";
    private EditTextPreference webSocketUrl;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        webSocketUrl = (EditTextPreference) getPreferenceScreen().findPreference(WEB_SOCKET_URL);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        webSocketUrl.setSummary(sharedPreferences.getString(WEB_SOCKET_URL, ""));
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
    }

    private SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            // Let's do something a preference value changes
            if (key.equals(WEB_SOCKET_URL)) {
                webSocketUrl.setSummary(sharedPreferences.getString(WEB_SOCKET_URL, ""));
            }
        }
    };
}