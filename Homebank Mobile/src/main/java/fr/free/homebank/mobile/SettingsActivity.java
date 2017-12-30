package fr.free.homebank.mobile;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;

import fr.free.homebank.mobile.dropbox.DropboxSettingsFragment;
import fr.free.homebank.mobile.gdrive.GdriveSettingsFragment;
import fr.free.homebank.mobile.local.LocalSettingsFragment;
import fr.free.homebank.mobile.util.Util;

/**
 * Created by wolfgang on 1/29/14.
 */
public class SettingsActivity extends ActionBarActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener,
        SettingsFragment.Listener {
    private static final String SETTINGS_FRAGMENT_TAG = "settings_fragment";
    public static final String LOCAL_SETTINGS_FRAGMENT_TAG = "local_settings_fragment";
    public static final String DROPBOX_SETTINGS_FRAGMENT_TAG = "dropbox_settings_fragment";
    public static final String GDRIVE_SETTINGS_FRAGMENT_TAG = "gdrive_settings_fragment";

    private static final String CHANGED = "changed";
    private static final String LOGGED_IN = "logged_in";

    private SharedPreferences sharedPref;
    private FragmentManager fragmentManager;
    private String connType;

    private Boolean changed;
    private Boolean loggedIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.action_settings);

        fragmentManager = getFragmentManager();

        SettingsFragment settingsFragment = new SettingsFragment();
        // Display the fragment as the main content.
        fragmentManager.beginTransaction()
                .replace(R.id.settings, settingsFragment, SETTINGS_FRAGMENT_TAG)
                .commit();

        fragmentManager.executePendingTransactions();

        sharedPref = settingsFragment.getPreferenceScreen().getSharedPreferences();
        connType = sharedPref.getString(SettingsFragment.KEY_PREF_SYNC_CONN_TYPE, "");

        if (connType.equals(getString(R.string.pref_syncConnectionTypes_local_val))) {
            fragmentManager.beginTransaction()
                    .replace(R.id.specificSettings, new LocalSettingsFragment(), LOCAL_SETTINGS_FRAGMENT_TAG)
                    .commit();
            loggedIn = true;
        }
        else if (connType.equals(getString(R.string.pref_syncConnectionTypes_dropbox_val))) {
            fragmentManager.beginTransaction()
                    .replace(R.id.specificSettings, new DropboxSettingsFragment(), DROPBOX_SETTINGS_FRAGMENT_TAG)
                    .commit();
            loggedIn = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Util.DROPBOX_SIGNED_IN, false);
        }
        else if (connType.equals(getString(R.string.pref_syncConnectionTypes_gdrive_val))) {
            fragmentManager.beginTransaction()
                    .replace(R.id.specificSettings, new GdriveSettingsFragment(), GDRIVE_SETTINGS_FRAGMENT_TAG)
                    .commit();
            loggedIn = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Util.GDRIVE_SIGNED_IN, false);
        }
        settingsFragment.setListener(this);

        changed = false;
        if (savedInstanceState != null) {
            changed = savedInstanceState.getBoolean(CHANGED);
            loggedIn = savedInstanceState.getBoolean(LOGGED_IN);
        }

//        Log.d("SharedPref", "listener registered");
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (!changed)
                    setResult(Activity.RESULT_OK);
                else
                    setResult(Activity.RESULT_OK, (new Intent()).putExtra(Util.SETTINGS_CHANGED, true).putExtra(Util.SIGNED_IN, loggedIn));
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case GdriveSettingsFragment.REQUEST_CODE_RESOLUTION:
                fragmentManager.findFragmentByTag(GDRIVE_SETTINGS_FRAGMENT_TAG).onActivityResult(requestCode, resultCode, data);
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    @Override
    public void onConnectionTypeChanged(String connType) {
        if (connType.equals(getString(R.string.pref_syncConnectionTypes_local_val))) {
            fragmentManager.beginTransaction()
                    .replace(R.id.specificSettings, new LocalSettingsFragment(), LOCAL_SETTINGS_FRAGMENT_TAG)
                    .commit();
            loggedIn = true;
        }
        else if (connType.equals(getString(R.string.pref_syncConnectionTypes_dropbox_val))) {
            fragmentManager.beginTransaction()
                    .replace(R.id.specificSettings, new DropboxSettingsFragment(), DROPBOX_SETTINGS_FRAGMENT_TAG)
                    .commit();
            loggedIn = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Util.DROPBOX_SIGNED_IN, false);
        }
        else if (connType.equals(getString(R.string.pref_syncConnectionTypes_gdrive_val))) {
            fragmentManager.beginTransaction()
                    .replace(R.id.specificSettings, new GdriveSettingsFragment(), GDRIVE_SETTINGS_FRAGMENT_TAG)
                    .commit();
            loggedIn = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Util.GDRIVE_SIGNED_IN, false);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(CHANGED, changed);
        outState.putBoolean(LOGGED_IN, loggedIn);
    }

//    @Override
//    public void onStart() {
//        super.onStart();
//        Log.d("SharedPref", "listener registered");
//        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
//    }

    @Override
    public void onDestroy() {
//        Log.d("SharedPref", "listener unregistered");
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (!changed)
            setResult(Activity.RESULT_OK);
        else
            setResult(Activity.RESULT_OK, (new Intent()).putExtra(Util.SETTINGS_CHANGED, changed).putExtra(Util.SIGNED_IN, loggedIn));
        finish();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (changed == false) {
            changed = true;
        }
//        Log.d("onSharedPreferenceChanged", s);
        if (s.equals(Util.GDRIVE_SIGNED_IN))
            loggedIn = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Util.GDRIVE_SIGNED_IN, false);
        if (s.equals(Util.DROPBOX_SIGNED_IN))
            loggedIn = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Util.DROPBOX_SIGNED_IN, false);

    }
}
