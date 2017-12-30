package fr.free.homebank.mobile;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.EditText;

import fr.free.homebank.mobile.util.Util;

/**
 * Created by wolfgang on 2/3/14.
 */
public class NewFileDialogFragment extends DialogFragment {
    private EditText input;
    private NewFileDialogListener listener;

    public interface NewFileDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog, String filename);
        public void onDialogNegativeClick(DialogFragment dialog);
    }

    public static NewFileDialogFragment newInstance() {
        NewFileDialogFragment frag = new NewFileDialogFragment();
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.new_file))
                .setMessage(getString(R.string.new_file_msg));

        input = new EditText(getActivity());
        if (savedInstanceState != null)
            input.setText(savedInstanceState.getString(Util.NEW_FILE));
        builder.setView(input);

        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String value = input.getText().toString();
                        listener.onDialogPositiveClick(NewFileDialogFragment.this, value);
                    }
        })
               .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int whichButton) {
                       listener.onDialogNegativeClick(NewFileDialogFragment.this);
                   }
               });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(Util.NEW_FILE, input.getText().toString());
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            String connType = PreferenceManager.getDefaultSharedPreferences(activity).getString(SettingsFragment.KEY_PREF_SYNC_CONN_TYPE, "");
            if (connType.equals(getString(R.string.pref_syncConnectionTypes_dropbox_val))) {
                Fragment dropboxFrag = activity.getFragmentManager().findFragmentByTag(SettingsActivity.DROPBOX_SETTINGS_FRAGMENT_TAG);
                listener = (NewFileDialogListener) dropboxFrag;
            }
            else if (connType.equals(getString(R.string.pref_syncConnectionTypes_gdrive_val))) {
                Fragment gdriveFrag = activity.getFragmentManager().findFragmentByTag(SettingsActivity.GDRIVE_SETTINGS_FRAGMENT_TAG);
                listener = (NewFileDialogListener) gdriveFrag;
            }
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }
}