package com.telepresenceRobot.android;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity {
    public static final String SERVER_HOSTNAME = "pref_hostname";
    public static final String SERVER_PORT = "pref_port";
    private EditTextPreference serverHostname;
    private EditTextPreference serverPort;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        serverHostname = (EditTextPreference) getPreferenceScreen().findPreference(SERVER_HOSTNAME);
        serverPort = (EditTextPreference) getPreferenceScreen().findPreference(SERVER_PORT);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        serverHostname.setSummary(sharedPreferences.getString(SERVER_HOSTNAME, ""));
        serverPort.setSummary("" + sharedPreferences.getString(SERVER_PORT, "" + TelepresenceServerClientService.DEFAULT_PORT));
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
            if (key.equals(SERVER_HOSTNAME)) {
                serverHostname.setSummary(sharedPreferences.getString(SERVER_HOSTNAME, ""));
            }
            if (key.equals(SERVER_PORT)) {
                serverPort.setSummary("" + sharedPreferences.getString(SERVER_PORT, "" + TelepresenceServerClientService.DEFAULT_PORT));
            }
        }
    };
}