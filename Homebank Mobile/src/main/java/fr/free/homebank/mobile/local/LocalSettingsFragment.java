package fr.free.homebank.mobile.local;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;

import fr.free.homebank.mobile.FileChooser;
import fr.free.homebank.mobile.R;
import fr.free.homebank.mobile.util.Util;

public class LocalSettingsFragment extends PreferenceFragment implements
        Preference.OnPreferenceClickListener{
    public static final String KEY_PREF_LOCAL_FILE = "pref_local_file";
    private static final int ACTIVITY_FILE_CHOOSER = 1;

    private SharedPreferences connSharedPref;

    private PreferenceScreen prefScreen;
    private Preference localFilePref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.local_preferences);

        connSharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());

        prefScreen = getPreferenceScreen();
        localFilePref = prefScreen.findPreference(KEY_PREF_LOCAL_FILE);
    }

    @Override
    public void onResume() {
        super.onResume();
        setOnPreferenceClickListeners(new String[]{KEY_PREF_LOCAL_FILE}, null);
        localFilePref.setSummary(connSharedPref.getString(Util.LOCAL_BASENAME, ""));
    }

    @Override
    public void onPause() {
        setOnPreferenceClickListeners(null, new String[]{KEY_PREF_LOCAL_FILE});
        super.onPause();
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey().equals(KEY_PREF_LOCAL_FILE)) {
            Intent i = new Intent(getActivity(), FileChooser.class);
            i.putExtra(Util.CONNECTION_TYPE, getString(R.string.pref_syncConnectionTypes_local_val));
            startActivityForResult(i, ACTIVITY_FILE_CHOOSER);
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ACTIVITY_FILE_CHOOSER:
                if (resultCode == Util.NEW_FILE_RESULT) {
//                    showNewFileDialog();
                }
                else if (resultCode == Activity.RESULT_OK) {
                    localFilePref.setSummary(connSharedPref.getString(Util.LOCAL_BASENAME, ""));
                }
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    private void setOnPreferenceClickListeners(String[] set, String[] unset) {
        PreferenceScreen prefScreen = getPreferenceScreen();
        if (set != null)
            for (String s : set) {
                prefScreen.findPreference(s).setOnPreferenceClickListener(this);
            }
        if (unset != null)
            for (String s : unset) {
                prefScreen.findPreference(s).setOnPreferenceClickListener(null);
            }
    }
}
