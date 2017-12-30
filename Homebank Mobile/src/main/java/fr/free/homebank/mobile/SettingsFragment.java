package fr.free.homebank.mobile;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.widget.Toast;

/**
 * Created by wolfgang on 1/29/14.
 */
public class SettingsFragment extends PreferenceFragment implements
        SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String KEY_PREF_SYNC_CONN_TYPE = "pref_syncConnectionType";

    private ListPreference connectionPref = null;
    private SharedPreferences sharedPref = null;
    private PreferenceScreen prefScreen;

    private Listener listener = null;

    public interface Listener {
        public void onConnectionTypeChanged(String value);
    }

    public void setListener(Listener l) {
        listener = l;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        connectionPref = (ListPreference) findPreference(KEY_PREF_SYNC_CONN_TYPE);
        prefScreen = getPreferenceScreen();
        sharedPref = prefScreen.getSharedPreferences();

        connectionPref.setSummary(connectionPref.getEntry());
    }

    @Override
    public void onResume() {
        super.onResume();
        sharedPref.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        sharedPref.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(KEY_PREF_SYNC_CONN_TYPE)) {
            connectionPref.setSummary(connectionPref.getEntry());
            listener.onConnectionTypeChanged(connectionPref.getValue());
        }
    }

    public void showMessage(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }
}