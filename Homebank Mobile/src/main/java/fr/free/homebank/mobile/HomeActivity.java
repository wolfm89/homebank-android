package fr.free.homebank.mobile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import fr.free.homebank.mobile.dropbox.DropboxFileDownloader;
import fr.free.homebank.mobile.gdrive.GdriveFileDownloader;
import fr.free.homebank.mobile.util.DbAdapter;
import fr.free.homebank.mobile.util.Util;

public class HomeActivity extends ActionBarActivity implements
        NavigationDrawerFragment.NavigationDrawerCallbacks,
        FileDownloader.OnFileDownloadedListener {

    public static final int ACTIVITY_SETTINGS = 0;

    public static final String SPENDINGS_FRAGMENT = "spendings_fragment";
    public static final String CHARTS_FRAGMENT = "charts_fragment";

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment navigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence title;

    private SharedPreferences sharedPref;
    private String connType;

    private int fragment;
    private DbAdapter dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        navigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        title = getTitle();

        // Set up the drawer.
        navigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        dbHelper = new DbAdapter(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ACTIVITY_SETTINGS:
                if (data != null) {
//                    Log.i("SharedPreferences", String.valueOf(resultCode) + " " + String.valueOf(data.getBooleanExtra(Util.SETTINGS_CHANGED, false)) + " " + String.valueOf(data.getBooleanExtra(Util.SIGNED_IN, false)));
                    if (resultCode == Activity.RESULT_OK && data.getBooleanExtra(Util.SETTINGS_CHANGED, false) && data.getBooleanExtra(Util.SIGNED_IN, false)) {
                        emptyFileFolder();
                        downloadXhbFile();
                    }
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    private void emptyFileFolder() {
        File dir = getFilesDir();
        for (File file : dir.listFiles())
            if (file.isFile())
                file.delete();
    }

    public void onCommitButtonClick(View view) {
        ((SpendingsFragment) getSupportFragmentManager().findFragmentByTag(SPENDINGS_FRAGMENT)).onCommitButtonClick(view);
    }

    public void onUpdateButtonClick(View view) {
        ((SpendingsFragment) getSupportFragmentManager().findFragmentByTag(SPENDINGS_FRAGMENT)).onUpdateButtonClick(view);
    }

    public void onAddButtonClick(View view) {
        ((SpendingsFragment) getSupportFragmentManager().findFragmentByTag(SPENDINGS_FRAGMENT)).onAddButtonClick(view);
    }

    public void downloadXhbFile() {
        String remoteFile = null;
        String localFile = null;

        connType = sharedPref.getString(SettingsFragment.KEY_PREF_SYNC_CONN_TYPE, "");

        if (connType.equals(getString(R.string.pref_syncConnectionTypes_local_val))) {
            try {
                FileOutputStream outStream = this.openFileOutput(sharedPref.getString(Util.LOCAL_BASENAME, ""), Context.MODE_PRIVATE);
                Util.copy(new FileInputStream(new File(sharedPref.getString(Util.LOCAL_FILE, ""))), outStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
            onFileDownloaded();
        }
        else if (connType.equals(getString(R.string.pref_syncConnectionTypes_dropbox_val))) {
            remoteFile = sharedPref.getString(Util.DROPBOX_FILE, "");
            localFile = sharedPref.getString(Util.DROPBOX_BASENAME, "");
            if (remoteFile.isEmpty())
                return;

            DropboxFileDownloader fileDownloader = new DropboxFileDownloader(this, remoteFile, localFile);
            fileDownloader.execute();
        }
        else if (connType.equals(getString(R.string.pref_syncConnectionTypes_gdrive_val))) {
            remoteFile = sharedPref.getString(Util.GDRIVE_FILE, "");
            localFile = sharedPref.getString(Util.GDRIVE_BASENAME, "");
            if (remoteFile.isEmpty())
                return;

            GdriveFileDownloader fileDownloader = new GdriveFileDownloader(this, remoteFile, localFile);
            fileDownloader.execute();
        }
    }

    @Override
    public void onFileDownloaded() {
        readFileToDb();
        if (fragment == Util.SPENDINGS_FRAGMENT) {
            SpendingsFragment spendingsFragment = (SpendingsFragment) getSupportFragmentManager().findFragmentByTag(SPENDINGS_FRAGMENT);
            spendingsFragment.fillData();
        }
        else if (fragment == Util.CHARTS_FRAGMENT) {
            ChartsFragment chartsFragment = (ChartsFragment) getSupportFragmentManager().findFragmentByTag(CHARTS_FRAGMENT);
            chartsFragment.refresh();
        }
    }

    private void readFileToDb() {
        String file = null;
        dbHelper.open();
        try {
            connType = sharedPref.getString(SettingsFragment.KEY_PREF_SYNC_CONN_TYPE, "");

            if (connType.equals(getString(R.string.pref_syncConnectionTypes_local_val))) {
                file = sharedPref.getString(Util.LOCAL_BASENAME, "");
            }
            else if (connType.equals(getString(R.string.pref_syncConnectionTypes_dropbox_val))) {
                file = sharedPref.getString(Util.DROPBOX_BASENAME, "");
            }
            else if (connType.equals(getString(R.string.pref_syncConnectionTypes_gdrive_val))) {
                file = sharedPref.getString(Util.GDRIVE_BASENAME, "");
            }
            dbHelper.insertFromXhb(file);
        } catch (XmlPullParserException e) {
            showMessage(getString(R.string.error_processing_file));
        } catch (IOException e) {
            showMessage(getString(R.string.error_read_write_file));
        }
        dbHelper.close();
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (position+1 == Util.SPENDINGS_FRAGMENT) {
            fragment = Util.SPENDINGS_FRAGMENT;
            fragmentManager.beginTransaction()
                    .replace(R.id.container, SpendingsFragment.newInstance(), SPENDINGS_FRAGMENT)
                    .commit();
        }
        else if (position+1 == Util.CHARTS_FRAGMENT) {
            fragment = Util.CHARTS_FRAGMENT;
            fragmentManager.beginTransaction()
                    .replace(R.id.container, ChartsFragment.newInstance(), CHARTS_FRAGMENT)
                    .commit();
        }
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                title = getString(R.string.title_section1);
                break;
            case 2:
                title = getString(R.string.title_section2);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(title);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!navigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.home, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivityForResult(intent, ACTIVITY_SETTINGS);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onFromDateButtonClick(View v) {
        ((SpendingsFragment) getSupportFragmentManager().findFragmentByTag(SPENDINGS_FRAGMENT)).onFromDateButtonClick(v);
    }

    public void onToDateButtonClick(View v) {
        ((SpendingsFragment) getSupportFragmentManager().findFragmentByTag(SPENDINGS_FRAGMENT)).onToDateButtonClick(v);
    }

    public void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

}
