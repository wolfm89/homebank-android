package fr.free.homebank.mobile.gdrive;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;

import java.util.ArrayList;

import fr.free.homebank.mobile.util.ContentValues;
import fr.free.homebank.mobile.R;
import fr.free.homebank.mobile.util.Util;

/**
 * Created by wolfgang on 2/2/14.
 */
public class GdriveFilesGetter extends AsyncTask<Void, Integer, Integer> implements
        ResultCallback {
    private static final String TITLE = "new.xhb";

    private GoogleApiClient googleApiClient;
    private ArrayList<ContentValues> fileList;
    private Context ctx;

    DriveId parent;

    private Listener listener = null;

    public GdriveFilesGetter(Context context, GoogleApiClient gapic, String parentId) {
        fileList = new ArrayList<ContentValues>();
        googleApiClient = gapic;
        ctx = context;
        parent = DriveId.decodeFromString(parentId);
    }

    @Override
    protected Integer doInBackground(Void... voids) {
        Query query = new Query.Builder().addFilter(Filters.and
                (Filters.contains(SearchableField.TITLE, Util.FILE_EXT), Filters.in(SearchableField.PARENTS, parent))).build();
        MetadataBuffer resultSet = Drive.DriveApi.query(googleApiClient, query).await().getMetadataBuffer();
//        Log.i("MATCHED_FILES", String.valueOf(resultSet.getCount()) + " files matched");
        for (Metadata file : resultSet) {
            fileList.add(new ContentValues(file.getTitle(), file.getDriveId().encodeToString()));
        }
        fileList.add(new ContentValues(ctx.getString(R.string.new_file), Util.NEW_FILE));
        return fileList.size()-1;
    }

    @Override
    protected void onPostExecute(Integer result) {
        if (result < 0) {
            fileList.clear();
            showMessage(ctx.getString(R.string.error_retrieving_files));
        } else {
            listener.onFileListReceived(fileList);
        }
        //googleApiClient.disconnect();
    }

    @Override
    public void onResult(Result result) {
        if (result instanceof DriveApi.ContentsResult) {
            if (!result.getStatus().isSuccess()) {
                showMessage(ctx.getString(R.string.error_creating_file));
                return;
            }
            MetadataChangeSet changeSet = new MetadataChangeSet.Builder().setTitle(TITLE).build();
            Drive.DriveApi.getRootFolder(googleApiClient).createFile(googleApiClient, changeSet,
                    ((DriveApi.ContentsResult) result).getContents());
        }
        else if (result instanceof DriveFolder.DriveFileResult) {
            if (!result.getStatus().isSuccess()) {
                showMessage(ctx.getString(R.string.error_creating_file));
                return;
            }
            showMessage(ctx.getString(R.string.created_file)); // + ((DriveFolder.DriveFileResult) result).getDriveFile().getDriveId());
            fileList.add(new ContentValues(TITLE,
                    ((DriveFolder.DriveFileResult) result).getDriveFile().getDriveId().encodeToString()));
        }
    }

//    @Override
//    public void onResult(DriveApi.ContentsResult contentsResult) {
//
//        if (!contentsResult.getStatus().isSuccess()) {
//            showMessage("Error while trying to create new file contents");
//            return;
//        }
//        MetadataChangeSet changeSet = new MetadataChangeSet.Builder().setTitle(TITLE).build();
//        Drive.DriveApi.getRootFolder(googleApiClient).createFile(googleApiClient, changeSet, contentsResult.getContents());
//    }

//    @Override
//    public void onCreateFile(DriveFolder.DriveFileResult result) {
//        if (!result.getStatus().isSuccess()) {
//            showMessage("Error while trying to create the file");
//            return;
//        }
//        showMessage("Created a file: " + result.getDriveFile().getDriveId());
//        fileList.add(new ContentValues(TITLE, result.getDriveFile().getDriveId().encodeToString()));
//    }

    public interface Listener {
        public void onFileListReceived(ArrayList<ContentValues> fileList);
    }

    public void setListener(Listener l) {
        listener = l;
    }

    public void showMessage(String message) {
        Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show();
    }
}
