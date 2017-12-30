package fr.free.homebank.mobile.gdrive;

import android.os.Bundle;
import android.os.ConditionVariable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Contents;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import fr.free.homebank.mobile.FileUploader;
import fr.free.homebank.mobile.HomeActivity;
import fr.free.homebank.mobile.util.Util;

/**
 * Created by wolfgang on 2/4/14.
 */
public class GdriveFileUploader extends FileUploader implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient googleApiClient;
    private ConditionVariable condition;

    public GdriveFileUploader(HomeActivity homeActivity, String remote, String local) {
        super(homeActivity, remote, local);
        googleApiClient = Util.getGoogleApiClient(context, this, this);
        condition = new ConditionVariable(false);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        googleApiClient.connect();
        condition.block();

        OutputStream outputStream = null;
        InputStream inputStream = null;

        DriveFile file = Drive.DriveApi.getFile(googleApiClient, DriveId.decodeFromString(remoteFile));
        Contents contents = file.openContents(googleApiClient, DriveFile.MODE_WRITE_ONLY, null).await().getContents();
        outputStream = contents.getOutputStream();
        try {
            inputStream = context.openFileInput(localFile);

            int read = 0;
            byte[] bytes = new byte[1024];

            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }

            file.commitAndCloseContents(googleApiClient, contents);
            return true;
        } catch (FileNotFoundException e) {
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
        return true;
    }

    @Override
    public void onConnected(Bundle bundle) {
        condition.open();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}