package fr.free.homebank.mobile.dropbox;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.dropbox.sync.android.DbxAccountManager;
import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxFileInfo;
import com.dropbox.sync.android.DbxFileSystem;
import com.dropbox.sync.android.DbxPath;

import java.util.ArrayList;
import java.util.List;

import fr.free.homebank.mobile.util.ContentValues;
import fr.free.homebank.mobile.R;
import fr.free.homebank.mobile.util.Util;

/**
 * Created by wolfgang on 2/1/14.
 */
public class DropboxFilesGetter extends AsyncTask<Void, Integer, Integer> {
    private final DbxAccountManager dbxAcctMgr;
    private ArrayList<ContentValues> fileList;
    private Context ctx;

    private Listener listener = null;

    public DropboxFilesGetter(Context context) {
        fileList = new ArrayList<ContentValues>();
        dbxAcctMgr = Util.getDbxAccountManager(context);
        ctx = context;
    }

    @Override
    protected Integer doInBackground(Void... voids) {
        try {
            DbxFileSystem dbxFs = DbxFileSystem.forAccount(dbxAcctMgr.getLinkedAccount());
            findDbxFiles(DbxPath.ROOT, dbxFs, Util.FILE_EXT, fileList);
            fileList.add(new ContentValues(ctx.getString(R.string.new_file), Util.NEW_FILE));
            return fileList.size()-1;
        } catch (DbxException.Unauthorized unauthorized) {
            unauthorized.printStackTrace();
        } catch (DbxException e) {
            e.printStackTrace();
        }

        return -1;
    }

    @Override
    protected void onPostExecute(Integer result) {
        if (result < 0) {
            fileList.clear();
            showMessage(ctx.getString(R.string.error_retrieving_files));
        } else {
            listener.onFileListReceived(fileList);
        }
    }

    private void findDbxFiles(DbxPath path, DbxFileSystem dbxFs, String fileExt, ArrayList<ContentValues> fileList) throws DbxException {
        List<DbxFileInfo> files = dbxFs.listFolder(path);
        if (!files.isEmpty()) {
            for (DbxFileInfo file : files) {
                if (file.isFolder) {
                    findDbxFiles(file.path, dbxFs, fileExt, fileList);
                } else {
                    if (file.path.getName().endsWith(fileExt))
                        fileList.add(new ContentValues(file.path.getName(), file.path.getParent() + "/" + file.path.getName()));
                }
            }
        }
    }

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
