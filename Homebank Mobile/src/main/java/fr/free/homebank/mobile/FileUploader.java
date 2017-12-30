package fr.free.homebank.mobile;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by wolfgang on 2/4/14.
 */
public abstract class FileUploader extends AsyncTask<Void, Integer, Boolean> {

    protected final AlertDialog dialog;
    protected Context context;
    protected HomeActivity homeActivity;
    protected String errorMsg;
    protected String remoteFile;
    protected String localFile;

    public FileUploader(HomeActivity home, String remote, String local) {
        context = home;
        homeActivity = home;
        remoteFile = remote;
        localFile = local;

        errorMsg = home.getString(R.string.error);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(this.context.getString(R.string.upload_file));
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
            dialog.dismiss();
            showToast(context.getString(R.string.uploaded_file));
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
