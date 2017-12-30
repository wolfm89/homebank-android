package fr.free.homebank.mobile.dropbox;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.widget.Toast;

import com.dropbox.sync.android.DbxAccount;
import com.dropbox.sync.android.DbxAccountManager;
import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxFile;
import com.dropbox.sync.android.DbxFileSystem;
import com.dropbox.sync.android.DbxPath;

import fr.free.homebank.mobile.FileChooser;
import fr.free.homebank.mobile.NewFileDialogFragment;
import fr.free.homebank.mobile.R;
import fr.free.homebank.mobile.util.Util;

/**
 * Created by wolfgang on 1/30/14.
 */
public class DropboxSettingsFragment extends PreferenceFragment implements
        Preference.OnPreferenceClickListener,
        DbxAccount.Listener,
        NewFileDialogFragment.NewFileDialogListener {

    public static final String KEY_PREF_DROPBOX_FILE = "pref_dropbox_file";
    public static final String KEY_PREF_DROPBOX_USER = "pref_dropbox_user";

    private static final int ACTIVITY_LINK_TO_DBX = 0;
    private static final int ACTIVITY_FILE_CHOOSER = 1;

    private DbxAccountManager dbxAcctMgr;

    private PreferenceScreen prefScreen;
    private Preference dbxUserPref;
    private Preference dbxFilePref;
    private SharedPreferences connSharedPref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.dropbox_preferences);

        dbxAcctMgr = Util.getDbxAccountManager(getActivity().getApplicationContext());
        connSharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());

        prefScreen = getPreferenceScreen();
        dbxUserPref = prefScreen.findPreference(KEY_PREF_DROPBOX_USER);
        dbxFilePref = prefScreen.findPreference(KEY_PREF_DROPBOX_FILE);
    }

    @Override
    public void onResume() {
        super.onResume();
        setOnPreferenceClickListeners(new String[] { KEY_PREF_DROPBOX_FILE, KEY_PREF_DROPBOX_USER }, null);
        if (dbxAcctMgr.hasLinkedAccount()) {
            if (dbxAcctMgr.getLinkedAccount().getAccountInfo() != null)
                dbxUserPref.setSummary(dbxAcctMgr.getLinkedAccount().getAccountInfo().displayName);
            dbxUserPref.setTitle(R.string.pref_sign_out);
            dbxFilePref.setEnabled(true);
            dbxFilePref.setSummary(connSharedPref.getString(Util.DROPBOX_BASENAME, ""));
        }
        else {
            dbxUserPref.setTitle(R.string.pref_sign_in);
            dbxUserPref.setSummary("");
            dbxFilePref.setEnabled(false);
            dbxFilePref.setSummary("");
        }
    }

    @Override
    public void onPause() {
        setOnPreferenceClickListeners(null, new String[] { KEY_PREF_DROPBOX_FILE, KEY_PREF_DROPBOX_USER });
        super.onPause();
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey().equals(KEY_PREF_DROPBOX_FILE)) {
            if (dbxAcctMgr.hasLinkedAccount()) {
                Intent i = new Intent(getActivity(), FileChooser.class);
                i.putExtra(Util.CONNECTION_TYPE, getString(R.string.pref_syncConnectionTypes_dropbox_val));
                startActivityForResult(i, ACTIVITY_FILE_CHOOSER);
            }
        }
        else if (preference.getKey().equals(KEY_PREF_DROPBOX_USER)) {
            if(dbxAcctMgr.hasLinkedAccount()) {
                dbxAcctMgr.unlink();
                showMessage(getString(R.string.dropbox_unlinked));
                preference.setTitle(getString(R.string.pref_sign_in));
                preference.setSummary("");
                dbxFilePref.setEnabled(false);
                dbxFilePref.setSummary("");
                connSharedPref.edit().putBoolean(Util.DROPBOX_SIGNED_IN, false).commit();
                SharedPreferences.Editor ed = connSharedPref.edit();
                ed.putBoolean(Util.DROPBOX_SIGNED_IN, false)
                        .putString(Util.DROPBOX_BASENAME, "")
                        .putString(Util.DROPBOX_FILE, "");
                ed.commit();

                return true;
            }
            dbxAcctMgr.startLink(this, ACTIVITY_LINK_TO_DBX);
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ACTIVITY_LINK_TO_DBX:
                if (resultCode == Activity.RESULT_OK) {
                    showMessage(getString(R.string.dropbox_linked));
                    dbxUserPref.setTitle(R.string.pref_sign_out);
                    dbxFilePref.setEnabled(true);
                    connSharedPref.edit().putBoolean(Util.DROPBOX_SIGNED_IN, true).commit();
//                    Log.d("SharedPref", "db signed in");
                    dbxAcctMgr.getLinkedAccount().addListener(this);
                } else {
                    showMessage(getString(R.string.dropbox_linking_cancelled));
                    // ... Link failed or was cancelled by the user.
                }
                break;
            case ACTIVITY_FILE_CHOOSER:
                if (resultCode == Util.NEW_FILE_RESULT) {
                    showNewFileDialog();
                }
                else if (resultCode == Activity.RESULT_OK) {
                    dbxFilePref.setSummary(connSharedPref.getString(Util.DROPBOX_BASENAME, ""));
                }
                /*else if (resultCode == HomebankMobile.RESULT_NETWORK_ERROR) {
                    AlertDialog alertDialog = new AlertDialog.Builder(this)
                            .create();
                    alertDialog.setTitle(getString(R.string.error));
                    alertDialog
                            .setMessage(getString(R.string.network_error));
                    alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.retry),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    Intent i = new Intent(getApplicationContext(),
                                            FileChooser.class);
                                    startActivityForResult(i,
                                            HomebankMobile.ACTIVITY_FILE_CHOOSER);
                                    dialog.cancel();
                                }
                            });
                    alertDialog.show();
                }*/
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    private void showNewFileDialog() {
        // Create an instance of the dialog fragment and show it
        NewFileDialogFragment dialog = NewFileDialogFragment.newInstance();
        dialog.show(getFragmentManager(), Util.NEW_FILE_DIALOG_TAG);
    }

    @Override
    public void onAccountChange(DbxAccount dbxAccount) {
        if (dbxAccount.getAccountInfo() != null && dbxAccount.isLinked())
            dbxUserPref.setSummary(dbxAccount.getAccountInfo().displayName);
        else
            dbxUserPref.setSummary("");
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

    public void showMessage(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog, String filename) {
        if (!filename.endsWith(Util.FILE_EXT))
            filename = filename + Util.FILE_EXT;

        try {
            DbxFileSystem dbxFs = DbxFileSystem.forAccount(dbxAcctMgr.getLinkedAccount());
            DbxFile file = dbxFs.create(new DbxPath(DbxPath.ROOT, filename));
            showMessage(getString(R.string.created_file));

            String currentFile = connSharedPref.getString(Util.DROPBOX_BASENAME, "");

            if (currentFile.length() != 0)
                getActivity().deleteFile(currentFile);

            SharedPreferences.Editor editor = connSharedPref.edit();
            editor.putString(Util.DROPBOX_FILE, file.getInfo().path.getParent() + "/" + file.getInfo().path.getName());
            editor.putString(Util.DROPBOX_BASENAME, file.getInfo().path.getName());
            editor.commit();

            dbxFilePref.setSummary(filename);

        } catch (DbxException.Unauthorized unauthorized) {
            unauthorized.printStackTrace();
        } catch (DbxException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {

    }
}
