package fr.free.homebank.mobile.dropbox;

import android.content.Context;
import com.dropbox.sync.android.DbxAccountManager;
import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxFile;
import com.dropbox.sync.android.DbxFileSystem;
import com.dropbox.sync.android.DbxPath;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import fr.free.homebank.mobile.FileDownloader;
import fr.free.homebank.mobile.HomeActivity;
import fr.free.homebank.mobile.util.Util;

public class DropboxFileDownloader extends FileDownloader {

	private DbxAccountManager dbxAcctMgr;

    public DropboxFileDownloader(HomeActivity homeActivity, String remote, String local) {
        super(homeActivity, remote, local);
        dbxAcctMgr = Util.getDbxAccountManager(context);
    }

    @Override
	protected Boolean doInBackground(Void... params) {
        OutputStream outputStream = null;
        InputStream inputStream = null;

        try {
            DbxFileSystem dbxFs = DbxFileSystem.forAccount(dbxAcctMgr.getLinkedAccount());
            DbxFile dbxFile = dbxFs.open(new DbxPath(remoteFile));
            inputStream = dbxFile.getReadStream();
            outputStream = context.openFileOutput(localFile, Context.MODE_PRIVATE);

            int read = 0;
            byte[] bytes = new byte[1024];

            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }

            dbxFile.close();

            homeActivity.runOnUiThread(changeDialog);
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (DbxException.Unauthorized unauthorized) {
            unauthorized.printStackTrace();
        } catch (DbxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
	}

}
