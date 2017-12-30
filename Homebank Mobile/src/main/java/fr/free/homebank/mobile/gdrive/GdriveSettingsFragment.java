package fr.free.homebank.mobile.gdrive;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;

import fr.free.homebank.mobile.FileChooser;
import fr.free.homebank.mobile.NewFileDialogFragment;
import fr.free.homebank.mobile.R;
import fr.free.homebank.mobile.util.Util;

public class GdriveSettingsFragment extends PreferenceFragment implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        Preference.OnPreferenceClickListener,
        NewFileDialogFragment.NewFileDialogListener,
        ResultCallback {
//        DriveFolder.OnCreateFolderCallback,
//        DriveFolder.OnChildrenRetrievedCallback,
//        DriveApi.OnNewContentsCallback, DriveFolder.OnCreateFileCallback {
    public static final String KEY_PREF_GDRIVE_FILE = "pref_gdrive_file";
    public static final String KEY_PREF_GDRIVE_USER = "pref_gdrive_user";

    public static final int REQUEST_CODE_RESOLUTION = 0;
    private static final int ACTIVITY_FILE_CHOOSER = 1;

    private static final String TAG = "BaseDriveActivity";

    private GoogleApiClient googleApiClient;

    private PreferenceScreen prefScreen;
    private Preference gdriveUserPref;
    private Preference gdriveFilePref;
    private SharedPreferences connSharedPref;

    private Boolean createFile = false;
    private String filename;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.gdrive_preferences);

        googleApiClient = Util.getGoogleApiClient(getActivity().getApplicationContext(), this, this);

        connSharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());

        prefScreen = getPreferenceScreen();
        gdriveUserPref = prefScreen.findPreference(KEY_PREF_GDRIVE_USER);
        gdriveFilePref = prefScreen.findPreference(KEY_PREF_GDRIVE_FILE);
    }

    @Override
    public void onResume() {
        super.onResume();
        setOnPreferenceClickListeners(new String[] { KEY_PREF_GDRIVE_FILE, KEY_PREF_GDRIVE_USER }, null);
        if (connSharedPref.getBoolean(Util.GDRIVE_SIGNED_IN, false)) {
            gdriveUserPref.setSummary(connSharedPref.getString(Util.GDRIVE_ACCOUNT, ""));
            gdriveUserPref.setTitle(R.string.pref_sign_out);
            gdriveFilePref.setEnabled(true);
            gdriveFilePref.setSummary(connSharedPref.getString(Util.GDRIVE_BASENAME, ""));
        }
        else {
            gdriveUserPref.setTitle(R.string.pref_sign_in);
            gdriveUserPref.setSummary("");
            gdriveFilePref.setEnabled(false);
            gdriveFilePref.setSummary("");
        }
    }

    @Override
    public void onPause() {
        setOnPreferenceClickListeners(null, new String[]{KEY_PREF_GDRIVE_FILE, KEY_PREF_GDRIVE_USER});
        if (googleApiClient.isConnected())
            googleApiClient.disconnect();
        super.onPause();
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey().equals(KEY_PREF_GDRIVE_FILE)) {
            if (connSharedPref.getBoolean(Util.GDRIVE_SIGNED_IN, false)) {
                Intent i = new Intent(getActivity(), FileChooser.class);
                i.putExtra(Util.CONNECTION_TYPE, getString(R.string.pref_syncConnectionTypes_gdrive_val));
                startActivityForResult(i, ACTIVITY_FILE_CHOOSER);
            }
        }
        else if (preference.getKey().equals(KEY_PREF_GDRIVE_USER)) {
            if(connSharedPref.getBoolean(Util.GDRIVE_SIGNED_IN, false)) {
                preference.setTitle(getString(R.string.pref_sign_in));
                preference.setSummary("");
                SharedPreferences.Editor ed = connSharedPref.edit();
                ed.putBoolean(Util.GDRIVE_SIGNED_IN, false)
                    .putString(Util.GDRIVE_BASENAME, "")
                    .putString(Util.GDRIVE_FILE, "")
                    .putString(Util.GDRIVE_PARENT_FOLDER, "");
                ed.commit();
                showMessage(getString(R.string.gdrive_unlinked));
                gdriveFilePref.setSummary("");
                gdriveFilePref.setEnabled(false);
                return true;
            }
            googleApiClient.connect();
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_RESOLUTION:
                if (resultCode == Activity.RESULT_OK) {
                    String account = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    connSharedPref.edit().putString(Util.GDRIVE_ACCOUNT, account).commit();
                    gdriveUserPref.setSummary(account);
                    googleApiClient.connect();
                }
                break;
            case ACTIVITY_FILE_CHOOSER:
                if (resultCode == Util.NEW_FILE_RESULT) {
                    showNewFileDialog();
                }
                else if (resultCode == Activity.RESULT_OK) {
                    gdriveFilePref.setSummary(connSharedPref.getString(Util.GDRIVE_BASENAME, ""));

                    Intent i = new Intent();
                    i.putExtra("FILE_CHANGED", true);
                    getActivity().setResult(Activity.RESULT_OK, i);
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "GoogleApiClient connection failed: " + connectionResult.toString());
        if (!connectionResult.hasResolution()) {
//            showMessage("Has no Resolution");
            GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), getActivity(), 0).show();
            return;
        }
        try {
            showMessage(getString(R.string.sign_in_required));
            connectionResult.startResolutionForResult(getActivity(), REQUEST_CODE_RESOLUTION);
        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG, "Exception while starting resolution activity", e);
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (createFile) {
            Drive.DriveApi.newContents(googleApiClient).setResultCallback(this);
            createFile = false;
        }
        else {
            gdriveUserPref.setTitle(R.string.pref_sign_out);
            connSharedPref.edit().putBoolean(Util.GDRIVE_SIGNED_IN, true).commit();

            Drive.DriveApi.getRootFolder(googleApiClient).listChildren(googleApiClient).setResultCallback(this);
            gdriveUserPref.setSummary(connSharedPref.getString(Util.GDRIVE_ACCOUNT, ""));
            gdriveFilePref.setEnabled(true);
            showMessage(getString(R.string.gdrive_linked));
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        //showMessage("Google Drive unlinked");
    }

    public void onChildrenRetrieved(DriveApi.MetadataBufferResult metadataBufferResult) {
        for (Metadata file : metadataBufferResult.getMetadataBuffer()) {
            if (file.isFolder() && file.getTitle().equals(Util.FOLDER)) {
                connSharedPref.edit().putString(Util.GDRIVE_PARENT_FOLDER, file.getDriveId().encodeToString()).commit();
                return;
            }
        }
        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle(Util.FOLDER).build();
        Drive.DriveApi.getRootFolder(googleApiClient).createFolder(
                googleApiClient, changeSet).setResultCallback(this);
    }

    public void onCreateFolder(DriveFolder.DriveFolderResult driveFolderResult) {
        if (!driveFolderResult.getStatus().isSuccess()) {
            showMessage(getString(R.string.error_creating_folder));
            return;
        }
        showMessage(getString(R.string.created_folder));
        connSharedPref.edit().putString(Util.GDRIVE_PARENT_FOLDER, driveFolderResult.getDriveFolder().getDriveId().encodeToString()).commit();
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

    private void showNewFileDialog() {
        // Create an instance of the dialog fragment and show it
        NewFileDialogFragment dialog = NewFileDialogFragment.newInstance();
        dialog.show(getFragmentManager(), Util.NEW_FILE_DIALOG_TAG);
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog, String f) {
        if (!f.endsWith(Util.FILE_EXT))
            f = f + Util.FILE_EXT;
        filename = f;
        createFile = true;
        if (!googleApiClient.isConnected())
            googleApiClient.connect();
        else
            Drive.DriveApi.newContents(googleApiClient).setResultCallback(this);
    }

    public void onNewContents(DriveApi.ContentsResult contentsResult) {
        if (!contentsResult.getStatus().isSuccess()) {
            showMessage(getString(R.string.error_creating_file));
            return;
        }

        MetadataChangeSet changeSet = new MetadataChangeSet.Builder().setTitle(filename).setMimeType("text/xml").build();
        DriveId parentId = DriveId.decodeFromString(connSharedPref.getString(Util.GDRIVE_PARENT_FOLDER, ""));
        DriveFolder parent = Drive.DriveApi.getFolder(googleApiClient, parentId);
        parent.createFile(googleApiClient, changeSet, contentsResult.getContents()).setResultCallback(this);
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {

    }

    public void showMessage(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    public void onCreateFile(DriveFolder.DriveFileResult driveFileResult) {
        if (!driveFileResult.getStatus().isSuccess()) {
            showMessage(getString(R.string.error_creating_file));
            return;
        }
        showMessage(getString(R.string.created_file));

        String currentFile = connSharedPref.getString(Util.GDRIVE_BASENAME, "");

        if (currentFile.length() != 0)
            getActivity().deleteFile(currentFile);

        SharedPreferences.Editor editor = connSharedPref.edit();
        editor.putString(Util.GDRIVE_FILE, driveFileResult.getDriveFile().getDriveId().encodeToString());
        editor.putString(Util.GDRIVE_BASENAME, filename);
        editor.commit();

        gdriveFilePref.setSummary(filename);
    }

    @Override
    public void onResult(Result result) {
        if (result instanceof DriveFolder.DriveFileResult) {
            onCreateFile((DriveFolder.DriveFileResult) result);
        }
        else if (result instanceof DriveApi.ContentsResult) {
            onNewContents((DriveApi.ContentsResult) result);
        }
        else if (result instanceof DriveFolder.DriveFolderResult) {
            onCreateFolder((DriveFolder.DriveFolderResult) result);
        }
        else if (result instanceof DriveApi.MetadataBufferResult) {
            onChildrenRetrieved((DriveApi.MetadataBufferResult) result);
        }
    }
}
