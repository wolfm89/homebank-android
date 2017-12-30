package fr.free.homebank.mobile.util;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.*;
import android.content.ContentValues;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.Button;
import android.widget.Toast;

import com.dropbox.sync.android.DbxAccountManager;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by wolfgang on 2/1/14.
 */
public final class Util {
    private static final String DBX_APP_KEY = "ktvth6u26gs18v4";
    private static final String DBX_APP_SECRET = "jwed8hoj12jldlv";

    public static final String GDRIVE_SIGNED_IN = "gdrive_signed_in";
    public static final String GDRIVE_FILE = "gdrive_file";
    public static final String GDRIVE_BASENAME = "gdrive_basename";
    public static final String GDRIVE_PARENT_FOLDER = "gdrive_parent_folder";
    public static final String GDRIVE_ACCOUNT = "gdrive_account";

    public static final String DROPBOX_SIGNED_IN = "dropbox_signed_in";
    public static final String DROPBOX_FILE = "dropbox_file";
    public static final String DROPBOX_BASENAME = "dropbox_basename";

    public static final String LOCAL_FILE = "local_file";
    public static final String LOCAL_BASENAME = "local_basename";

    public static final String INFOS_PREFS_NAME = "INFOS";

    public static final String NEW_FILE = "new_file";

    public static final String CONNECTION_TYPE = "connection_type";

    public static final String NEW_FILE_DIALOG_TAG = "new_file_dialog_tag";
    public static final int NEW_FILE_RESULT = 100;

    public static final int EMPTY_RESULT = 123;

    public static final String SETTINGS_CHANGED = "settings_changed";
    public static final String SIGNED_IN = "signed_in";

    public static final String FOLDER = "Homebank Mobile";
    public static final String FILE_EXT = ".xhb";

    public static final String DAY = "day";
    public static final String MONTH = "month";
    public static final String YEAR = "year";

    public static final String ID = "id";

    public static final int SPENDINGS_FRAGMENT = 1;
    public static final int CHARTS_FRAGMENT = 2;


    public static void showMessage(Context ctx, String message) {
        Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show();
    }

    public static DbxAccountManager getDbxAccountManager(Context c) {
        return DbxAccountManager.getInstance(c.getApplicationContext(), DBX_APP_KEY, DBX_APP_SECRET);
    }

    public static GoogleApiClient getGoogleApiClient (Context c, GoogleApiClient.ConnectionCallbacks cc, GoogleApiClient.OnConnectionFailedListener ocfl) {
        return new GoogleApiClient.Builder(c)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .addConnectionCallbacks(cc)
                .addOnConnectionFailedListener(ocfl)
                .build();
    }

    public static long dateToJulian(Date date) {

        GregorianCalendar calendarDate = new GregorianCalendar();
        calendarDate.setTime(date);

        Calendar calendarBegin = new GregorianCalendar(1, GregorianCalendar.JANUARY, 1);

        long millisecondsDate = calendarDate.getTimeInMillis();
        long millisecondsBegin = calendarBegin.getTimeInMillis();
        long diff = millisecondsDate - millisecondsBegin;
        long diffDays = diff / (24 * 60 * 60 * 1000) - 1;

        return diffDays;
    }

    public static Date julianToDate(long date) {
        GregorianCalendar calendarBegin = new GregorianCalendar(1, GregorianCalendar.JANUARY, 1);
        long millisecondsBegin = calendarBegin.getTimeInMillis();

        long diff = date * (24 * 60 * 60 * 1000);
        long millisecondsDate = diff + millisecondsBegin;
        int days = (int) (millisecondsDate / (24 * 60 * 60 * 1000));

        GregorianCalendar calendarDate = new GregorianCalendar();
        calendarDate.setTimeInMillis(0);
        calendarDate.add(GregorianCalendar.DAY_OF_MONTH, days + 2);

        return calendarDate.getTime();
    }

    public static void setDateButtonText(Context context, Button button, Calendar cal) {
        DateFormat dateFormat = android.text.format.DateFormat.getMediumDateFormat(context);
        Date date = cal.getTime();
        String dat = dateFormat.format(date);

        button.setText(dat);
    }

    public static class DatePickerDialogFragment extends DialogFragment {
        private Context context;
        private DatePickerDialog.OnDateSetListener listener;

        public static DatePickerDialogFragment newInstance(Context ctx, DatePickerDialog.OnDateSetListener lst, int day, int month, int year) {
            DatePickerDialogFragment frag = new DatePickerDialogFragment();
            Bundle args = new Bundle();
            args.putInt(Util.DAY, day);
            args.putInt(Util.MONTH, month);
            args.putInt(Util.YEAR, year);
            frag.setArguments(args);
            frag.setContext(ctx);
            frag.setListener(lst);
            return frag;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Bundle date = this.getArguments();

            return new DatePickerDialog(context, listener, date.getInt(Util.YEAR), date.getInt(Util.MONTH), date.getInt(Util.DAY));
        }

        public void setContext(Context context) {
            this.context = context;
        }

        public void setListener(DatePickerDialog.OnDateSetListener listener) {
            this.listener = listener;
        }
    }

    public static class CategoryExpenseCVComparable implements Comparator<ContentValues> {
        @Override
        public int compare(ContentValues cv1, ContentValues cv2) {
            double d1 = cv1.getAsDouble(DbAdapter.KEY_AMOUNT) == null ? 0 : cv1.getAsDouble(DbAdapter.KEY_AMOUNT);
            double d2 = cv2.getAsDouble(DbAdapter.KEY_AMOUNT) == null ? 0 : cv2.getAsDouble(DbAdapter.KEY_AMOUNT);
            return (d1 < d2 ? -1 : (d1 == d2 ? 0 : 1));
        }
    }

    public static void copy(FileInputStream inStream, FileOutputStream outStream) throws IOException {
        FileChannel inChannel = inStream.getChannel();
        FileChannel outChannel = outStream.getChannel();
        inChannel.transferTo(0, inChannel.size(), outChannel);
        inStream.close();
        outStream.close();
    }

}
