package fr.free.homebank.mobile;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.sync.android.DbxAccountManager;

import java.io.FileOutputStream;

/**
 * Created by wolfgang on 2/4/14.
 */
public abstract class FileDownloader extends AsyncTask<Void, Integer, Boolean> {

    protected final AlertDialog dialog;
    protected Context context;
    protected HomeActivity homeActivity;
    protected String errorMsg;
    protected String remoteFile;
    protected String localFile;

    protected OnFileDownloadedListener listener = null;

    public interface OnFileDownloadedListener {
        public void onFileDownloaded();
    }

    protected Runnable changeDialog = new Runnable() {
        public void run() {
            dialog.setMessage(context.getString(R.string.update_database));
        }
    };

    public FileDownloader(HomeActivity home, String remote, String local) {
        context = home;
        homeActivity = home;
        remoteFile = remote;
        localFile = local;

        errorMsg = home.getString(R.string.error);

        try {
            listener = (OnFileDownloadedListener) home;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(home.toString()
                    + " must implement OnFileDownloadedListener");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(this.context.getString(R.string.download_file));
        builder.setCancelable(false);
        dialog = builder.show();
        TextView messageText = (TextView) dialog.findViewById(android.R.id.message);
        messageText.setGravity(Gravity.CENTER);
        dialog.show();
    }

    @Override
    abstract protected Boolean doInBackground(Void... params);

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            listener.onFileDownloaded();
            dialog.dismiss();
            showToast(context.getString(R.string.downloaded_file));
        } else {
            dialog.dismiss();
            showToast(errorMsg);
        }
    }

    protected void showToast(String msg) {
        Toast error = Toast.makeText(context, msg, Toast.LENGTH_LONG);
        error.show();
    }
}
