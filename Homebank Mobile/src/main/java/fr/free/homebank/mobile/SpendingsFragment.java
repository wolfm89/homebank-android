package fr.free.homebank.mobile;



import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;

import fr.free.homebank.mobile.dropbox.DropboxFileUploader;
import fr.free.homebank.mobile.gdrive.GdriveFileUploader;
import fr.free.homebank.mobile.util.AlternateRowCursorAdapter;
import fr.free.homebank.mobile.util.DbAdapter;
import fr.free.homebank.mobile.util.Util;


/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * Use the {@link SpendingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class SpendingsFragment extends ListFragment implements
        AdapterView.OnItemSelectedListener,
        AdapterView.OnItemLongClickListener,
        DatePickerDialog.OnDateSetListener,
        SwipeRefreshLayout.OnRefreshListener {

    public static final int ACTIVITY_EDIT = 1;

    private static final int FROM_DATE = 0;
    private static final int TO_DATE = 1;

    private DbAdapter dbHelper;

    private Boolean loggedIn;
    private long account;
    private int whichDatePicker;

    private TextView totalTextView;
    private Button fromDateButton;
    private Button toDateButton;
    private LinearLayout loggedInLayout;
    private RelativeLayout loggedOutLayout;
    private Spinner accSpinner;
    private Calendar fromDate;
    private Calendar toDate;
    private SwipeRefreshLayout swipeLayout;

    private SharedPreferences sharedPref;
    private String connType;

    private Cursor accountsCursor;
    private Cursor transactionsCursor;

    private DecimalFormat amountFormatter = new DecimalFormat("#,##0.00");
    private int COL_DATE;
    private int COL_PAYEE;
    private int COL_AMOUNT;
    private int COL_CATEGORY;
    private int COL_TEXT;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SpendingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SpendingsFragment newInstance() {
        SpendingsFragment fragment = new SpendingsFragment();
        return fragment;
    }
    public SpendingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ACTIVITY_EDIT:
//                Log.i("LIFECYCLE", "deleteTempTrans");
                getActivity().getSharedPreferences(EditActivity.TEMP_TRANS, Activity.MODE_PRIVATE).edit().clear().commit();
                if (resultCode == Activity.RESULT_OK)
                    fillData();
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new DbAdapter(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_spendings, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Android widgets
        View rootView = getView();
        totalTextView = (TextView) rootView.findViewById(R.id.total_text_view);
        fromDateButton = (Button) rootView.findViewById(R.id.from_date_button);
        toDateButton = (Button) rootView.findViewById(R.id.to_date_button);
        loggedInLayout = (LinearLayout) rootView.findViewById(R.id.logged_in_layout);
        loggedOutLayout = (RelativeLayout) rootView.findViewById(R.id.logged_out_layout);
        accSpinner = (Spinner) rootView.findViewById(R.id.acc_spinner);
        accSpinner.setOnItemSelectedListener(this);

//        swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
//        swipeLayout.setOnRefreshListener(this);

        fromDate = Calendar.getInstance();
        fromDate.add(Calendar.MONTH, -2);
        toDate = Calendar.getInstance();
        Util.setDateButtonText(getActivity(), fromDateButton, fromDate);
        Util.setDateButtonText(getActivity(), toDateButton, toDate);

        getListView().setLongClickable(true);
        getListView().setOnItemLongClickListener(this);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        connType = sharedPref.getString(SettingsFragment.KEY_PREF_SYNC_CONN_TYPE, "");
        if (connType.equals(getString(R.string.pref_syncConnectionTypes_local_val))) {
            setLoggedInView(!sharedPref.getString(Util.LOCAL_FILE, "").equals(""));
        }
        else if (connType.equals(getString(R.string.pref_syncConnectionTypes_dropbox_val))) {
            setLoggedInView(sharedPref.getBoolean(Util.DROPBOX_SIGNED_IN, false) && !sharedPref.getString(Util.DROPBOX_FILE, "").equals(""));
        }
        else if (connType.equals(getString(R.string.pref_syncConnectionTypes_gdrive_val))) {
            setLoggedInView(sharedPref.getBoolean(Util.GDRIVE_SIGNED_IN, false) && !sharedPref.getString(Util.GDRIVE_FILE, "").equals(""));
        }

       if (loggedIn)
            fillData();
    }

    private void setLoggedInView(boolean logIn) {
        loggedIn = logIn;

        if (loggedIn) {
            loggedInLayout.setVisibility(View.VISIBLE);
            loggedOutLayout.setVisibility(View.GONE);
        } else {
            loggedInLayout.setVisibility(View.GONE);
            loggedOutLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((HomeActivity) activity).onSectionAttached(Util.SPENDINGS_FRAGMENT);
    }

    /**
     * Map from view fields to database columns.
     */
    private AlternateRowCursorAdapter.ViewBinder viewBinder = new AlternateRowCursorAdapter.ViewBinder() {
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            TextView textview = (TextView) view;
            String dat;
            if (columnIndex == COL_DATE) {
                Long dateTime = cursor.getLong(columnIndex);
                Date date = Util.julianToDate(dateTime);
                DateFormat dateFormat = android.text.format.DateFormat
                        .getDateFormat(getActivity());
                dat = dateFormat.format(date);
                textview.setText(dat);
            }
            if (columnIndex == COL_PAYEE) {
                String payee = cursor.getString(columnIndex);
                textview.setText(payee);
            }
            if (columnIndex == COL_AMOUNT) {
                Double amount = cursor.getDouble(columnIndex);
                if (amount != null)
                    textview.setText(amountFormatter.format(amount));
            }
            if (columnIndex == COL_CATEGORY) {
                String category = cursor.getString(columnIndex);
                textview.setText(category);
            }
            if (columnIndex == COL_TEXT) {
                String text = cursor.getString(columnIndex);
                textview.setText(text);
            }
            return true;
        }
    };

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        long fromDate = Util.dateToJulian(this.fromDate.getTime());
        long toDate = Util.dateToJulian(this.toDate.getTime());

        account = id;

        dbHelper.open();
        transactionsCursor = dbHelper.fetchAllTransactions(account, fromDate, toDate);

        // int i;
        // for (i = 0; i < mTransactionsCursor.getColumnNames().length; i++)
        // Log.i("column names", mTransactionsCursor.getColumnNames()[i]);
        COL_DATE = transactionsCursor.getColumnIndex(DbAdapter.KEY_DATE);
        COL_PAYEE = transactionsCursor.getColumnIndex(DbAdapter.KEY_PAYEE);
        COL_AMOUNT = transactionsCursor.getColumnIndex(DbAdapter.KEY_AMOUNT);
        COL_CATEGORY = transactionsCursor.getColumnIndex(DbAdapter.KEY_CATEGORY);
        COL_TEXT = transactionsCursor.getColumnIndex(DbAdapter.KEY_TEXT);

        String[] from = new String[] { DbAdapter.KEY_DATE, DbAdapter.KEY_PAYEE,
                DbAdapter.KEY_AMOUNT, DbAdapter.KEY_CATEGORY,
                DbAdapter.KEY_TEXT };
        int[] to = new int[] { R.id.date, R.id.payee, R.id.amount,
                R.id.category, R.id.text };

        AlternateRowCursorAdapter list = new AlternateRowCursorAdapter(
                getActivity(), R.layout.row, transactionsCursor,
                from, to);
        list.setViewBinder(viewBinder);
        setListAdapter(list);

        DecimalFormat df = new DecimalFormat(",##0.00");
        df.setPositivePrefix("+");

        double total = dbHelper.getTotal(account);

        totalTextView.setText(" " + df.format(total));
        totalTextView.setTextColor(total < 0 ? Color.RED : Color
                .parseColor("#04B404"));

        dbHelper.close();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
        DialogFragment deleteDialogFragment = DeleteAlertDialogFragment.newInstance(id);
        deleteDialogFragment.show(getFragmentManager(), "deleteDialog");

        return true;
    }

    public void onUpdateButtonClick(View view) {
        ((HomeActivity) getActivity()).downloadXhbFile();
    }

    public void onCommitButtonClick(View view) {
        String file = null;

        dbHelper.open();
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

        dbHelper.writeToXhb(file);
        dbHelper.close();

        uploadXhbFile();
    }

    public void onAddButtonClick(View view) {
        Intent i = new Intent(getActivity(), EditActivity.class);
        i.putExtra(DbAdapter.KEY_ACC, account);
        i.putExtra(DbAdapter.KEY_ROWID, Long.valueOf(0));
        startActivityForResult(i, ACTIVITY_EDIT);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Intent i = new Intent(getActivity(), EditActivity.class);
        i.putExtra(DbAdapter.KEY_ROWID, id);
        i.putExtra(DbAdapter.KEY_ACC, account);
        startActivityForResult(i, ACTIVITY_EDIT);
    }

    private void uploadXhbFile() {
        String remoteFile = null;
        String localFile = null;

        connType = sharedPref.getString(SettingsFragment.KEY_PREF_SYNC_CONN_TYPE, "");

        if (connType.equals(getString(R.string.pref_syncConnectionTypes_local_val))) {
            remoteFile = sharedPref.getString(Util.LOCAL_FILE, "");
            localFile = sharedPref.getString(Util.LOCAL_BASENAME, "");

            try {
                FileInputStream inStream = ((HomeActivity) getActivity()).openFileInput(
                        sharedPref.getString(Util.LOCAL_BASENAME, ""));
                Util.copy(inStream, new FileOutputStream(new File(sharedPref.getString(Util.LOCAL_FILE, ""))));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if (connType.equals(getString(R.string.pref_syncConnectionTypes_dropbox_val))) {
            remoteFile = sharedPref.getString(Util.DROPBOX_FILE, "");
            localFile = sharedPref.getString(Util.DROPBOX_BASENAME, "");

            DropboxFileUploader fileUploader = new DropboxFileUploader((HomeActivity) getActivity(), remoteFile, localFile);
            fileUploader.execute();
        }
        else if (connType.equals(getString(R.string.pref_syncConnectionTypes_gdrive_val))) {
            remoteFile = sharedPref.getString(Util.GDRIVE_FILE, "");
            localFile = sharedPref.getString(Util.GDRIVE_BASENAME, "");

            GdriveFileUploader fileUploader = new GdriveFileUploader((HomeActivity) getActivity(), remoteFile, localFile);
            fileUploader.execute();
        }
    }

    public void fillData() {
        dbHelper.open();
        // if (mAccSpinner.getSelectedItem() != null)
        // Log.i("Acc class name",
        // mAccSpinner.getSelectedItem().getClass().getName());

        accountsCursor = dbHelper.fetchAllAccounts();

        String[] from = new String[] { DbAdapter.KEY_NAME };
        int[] to = new int[] { android.R.id.text1 };

        SimpleCursorAdapter sca = new SimpleCursorAdapter(getActivity(),
                android.R.layout.simple_spinner_item, accountsCursor, from, to);
        accSpinner.setAdapter(sca);
        sca.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Log.i("mAccount", String.valueOf(mAccount));
        if (account != -1)
            accSpinner.setSelection((int) account - 1);
        accSpinner.setOnItemSelectedListener(this);

        dbHelper.close();
    }

    public void onFromDateButtonClick(View v) {
        whichDatePicker = FROM_DATE;

        int fromDay = fromDate.get(Calendar.DATE);
        int fromMonth = fromDate.get(Calendar.MONTH);
        int fromYear = fromDate.get(Calendar.YEAR);

        DialogFragment fromDialogFragment = Util.DatePickerDialogFragment.newInstance(getActivity(), this, fromDay, fromMonth, fromYear);
        fromDialogFragment.show(getFragmentManager(), "fromDatePicker");
    }

    public void onToDateButtonClick(View v) {
        whichDatePicker = TO_DATE;
        int toDay = toDate.get(Calendar.DATE);
        int toMonth = toDate.get(Calendar.MONTH);
        int toYear = toDate.get(Calendar.YEAR);

        DialogFragment toDialogFragment = Util.DatePickerDialogFragment.newInstance(getActivity(), this, toDay, toMonth, toYear);
        toDialogFragment.show(getFragmentManager(), "toDatePicker");
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        if (whichDatePicker == FROM_DATE) {
            if (fromDate.get(Calendar.YEAR) == year
                    && fromDate.get(Calendar.MONTH) == monthOfYear
                    && fromDate.get(Calendar.DAY_OF_MONTH) == dayOfMonth)
                return;
            fromDate.set(year, monthOfYear, dayOfMonth);
            if (fromDate.after(toDate))
                fromDate.set(toDate.get(Calendar.YEAR),
                        toDate.get(Calendar.MONTH),
                        toDate.get(Calendar.DATE));
            Util.setDateButtonText(getActivity(), fromDateButton, fromDate);
            fillData();
        } else if (whichDatePicker == TO_DATE) {
            if (toDate.get(Calendar.YEAR) == year
                    && toDate.get(Calendar.MONTH) == monthOfYear
                    && toDate.get(Calendar.DAY_OF_MONTH) == dayOfMonth)
                return;
            toDate.set(year, monthOfYear, dayOfMonth);
            Util.setDateButtonText(getActivity(), toDateButton, toDate);
            fillData();
        }
    }

    @Override
    public void onRefresh() {
        onUpdateButtonClick(swipeLayout);
    }

    public static class DeleteAlertDialogFragment extends DialogFragment {

        public static DeleteAlertDialogFragment newInstance(long id) {
            DeleteAlertDialogFragment frag = new DeleteAlertDialogFragment();
            Bundle args = new Bundle();
            args.putLong(Util.ID, id);
            frag.setArguments(args);
            return frag;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Bundle args = this.getArguments();
            final long id = args.getLong(Util.ID);

            return new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.delete_msg)
                    .setPositiveButton(R.string.yes,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    ((SpendingsFragment) getFragmentManager().findFragmentByTag(HomeActivity.SPENDINGS_FRAGMENT)).doPositiveDeleteClick(dialog, id);
                                }
                            }
                    )
                    .setNegativeButton(R.string.no,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    ((SpendingsFragment) getFragmentManager().findFragmentByTag(HomeActivity.SPENDINGS_FRAGMENT)).doNegativeDeleteClick(dialog, -1);
                                }
                            }
                    )
                    .create();
        }
    }

    private void doNegativeDeleteClick(DialogInterface dialog, long id) {
        dialog.dismiss();
    }

    private void doPositiveDeleteClick(DialogInterface dialog, long id) {
        dbHelper.open();
        dbHelper.deleteTransaction(id);
        dbHelper.close();
        dialog.dismiss();
        fillData();
    }
}
