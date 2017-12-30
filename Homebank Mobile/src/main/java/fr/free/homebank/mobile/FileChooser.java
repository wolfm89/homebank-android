package fr.free.homebank.mobile;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import net.rdrei.android.dirchooser.DirectoryChooserActivity;
import net.rdrei.android.dirchooser.DirectoryChooserFragment;

import java.io.File;
import java.util.ArrayList;

import fr.free.homebank.mobile.dropbox.DropboxFilesGetter;
import fr.free.homebank.mobile.gdrive.GdriveFilesGetter;
import fr.free.homebank.mobile.util.ContentValues;
import fr.free.homebank.mobile.util.Util;

public class FileChooser extends ListActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        DropboxFilesGetter.Listener,
        GdriveFilesGetter.Listener,
        DirectoryChooserFragment.OnFragmentInteractionListener {

    private ArrayAdapter<ContentValues> adapter;

    private GoogleApiClient googleApiClient;
    private String connType;

    private ProgressBar progBar;

    private DirectoryChooserFragment mDialog;

    int REQUEST_DIRECTORY = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_chooser);

        progBar = (ProgressBar) findViewById(R.id.progressBar);
        progBar.setVisibility(View.VISIBLE);

        connType = getIntent().getStringExtra(Util.CONNECTION_TYPE);

        if (connType.equals(getString(R.string.pref_syncConnectionTypes_dropbox_val))) {
            DropboxFilesGetter fg = new DropboxFilesGetter(this);
            fg.setListener(this);
            fg.execute();
        }
        else if (connType.equals(getString(R.string.pref_syncConnectionTypes_gdrive_val))) {
            googleApiClient = Util.getGoogleApiClient(this, this, this);
            if (googleApiClient.isConnected()) {
                GdriveFilesGetter fg = new GdriveFilesGetter(this, googleApiClient,
                        PreferenceManager.getDefaultSharedPreferences(this).getString(Util.GDRIVE_PARENT_FOLDER, ""));
                fg.setListener(this);
                fg.execute();
            }
            else
                googleApiClient.connect();
        }
        else if (connType.equals(getString(R.string.pref_syncConnectionTypes_local_val))) {
            mDialog = DirectoryChooserFragment.newInstance(getString(R.string.new_dir_name), null);
            mDialog.show(getFragmentManager(), null);
        }
    }

    @Override
    public void onFileListReceived(ArrayList<ContentValues> fileList) {
        if (fileList.isEmpty()) {
            setResult(Util.EMPTY_RESULT);
            finish();
            Util.showMessage(this, getString(R.string.no_files_found));
            return;
        }
        progBar.setVisibility(View.GONE);
        adapter = new ArrayAdapter<ContentValues>(
                this, android.R.layout.simple_list_item_1,
                fileList);
        setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        String selectedFile = ((ContentValues) l.getItemAtPosition(position)).getValue();

        if (selectedFile.equals(Util.NEW_FILE)) {
            setResult(Util.NEW_FILE_RESULT);
            finish();
            return;
        }

        String basename = "";
        String file = "";
        if (connType.equals(getString(R.string.pref_syncConnectionTypes_dropbox_val))) {
            basename = Util.DROPBOX_BASENAME;
            file = Util.DROPBOX_FILE;
        }
        else if (connType.equals(getString(R.string.pref_syncConnectionTypes_gdrive_val))) {
            basename = Util.GDRIVE_BASENAME;
            file = Util.GDRIVE_FILE;
        }
        else if (connType.equals(getString(R.string.pref_syncConnectionTypes_local_val))) {
            basename = Util.LOCAL_BASENAME;
            file = Util.LOCAL_FILE;
        }

        String selectedBasename = ((ContentValues) l.getItemAtPosition(position)).getName();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        String currentFile = sharedPref.getString(basename, "");

        if (currentFile.length() != 0)
                deleteFile(currentFile);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(file, selectedFile);
        editor.putString(basename, selectedBasename);
        editor.commit();

        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onConnected(Bundle bundle) {
        GdriveFilesGetter fg = new GdriveFilesGetter(this, googleApiClient,
                PreferenceManager.getDefaultSharedPreferences(this).getString(Util.GDRIVE_PARENT_FOLDER, ""));
        fg.setListener(this);
        fg.execute();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onSelectDirectory(@NonNull final String path) {
        mDialog.dismiss();
        onFileListReceived(findFilesInDir(new File(path)));
    }

    private ArrayList<ContentValues> findFilesInDir(File parentDir) {
        ArrayList<ContentValues> inFiles = new ArrayList<ContentValues>();
        File[] files = parentDir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                inFiles.addAll(findFilesInDir(file));
            } else {
                if(file.getName().endsWith(Util.FILE_EXT)){
                    inFiles.add(new ContentValues(file.getName(), file.getAbsolutePath()));
                }
            }
        }
        return inFiles;
    }

    @Override
    public void onCancelChooser() {
        mDialog.dismiss();
        onFileListReceived(new ArrayList<ContentValues>());
    }
}
