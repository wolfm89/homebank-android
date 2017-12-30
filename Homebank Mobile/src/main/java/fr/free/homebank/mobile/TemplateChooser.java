package fr.free.homebank.mobile;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import fr.free.homebank.mobile.util.DbAdapter;

public class TemplateChooser extends DialogFragment {

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface TemplateChooserListener {
        public void onTemplateClick(android.content.ContentValues template);
    }

    // Use this instance of the interface to deliver action events
    TemplateChooserListener mListener;

    ArrayList<ContentValues> fullTemplates;

    DbAdapter dbHelper;
    Context ctx;

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (TemplateChooserListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Build the dialog and set up the button click handlers

        this.ctx = getActivity().getApplicationContext();
        this.dbHelper = new DbAdapter(this.ctx);

        dbHelper.open();
        fullTemplates = dbHelper.getAllFromTable(DbAdapter.TABLE_FAVORITE);
        ArrayList<fr.free.homebank.mobile.util.ContentValues> templates = new ArrayList<fr.free.homebank.mobile.util.ContentValues>();
        for (ContentValues template : dbHelper.getAllFromTable(DbAdapter.TABLE_FAVORITE)) {
            templates.add(new fr.free.homebank.mobile.util.ContentValues(template.getAsString(DbAdapter.KEY_TEXT),
                    template.getAsString(DbAdapter.KEY_ROWID)));
        }
        dbHelper.close();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.choose_template);

        ArrayAdapter<fr.free.homebank.mobile.util.ContentValues> adapter = new ArrayAdapter<fr.free.homebank.mobile.util.ContentValues>(
                ctx, android.R.layout.simple_list_item_1,
                templates) {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        View view = super.getView(position, convertView, parent);
                        TextView text = (TextView) view.findViewById(android.R.id.text1);
                        text.setTextColor(Color.BLACK);
                        return view;
            }};
        builder.setAdapter(adapter,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Send the positive button event back to the host activity
                        mListener.onTemplateClick(fullTemplates.get(which));
                    }
                });

        return builder.create();
    }
}
