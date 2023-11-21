// Copyright 2013 Google Inc. All Rights Reserved.

package com.blautic.pikkucam.drive;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.blautic.pikkucam.PikkuCamApplication;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;

import java.util.concurrent.CountDownLatch;

/**
 * An AsyncTask that maintains a connected client.
 */
public abstract class ApiClientAsyncTask<Params, Progress, Result>
        extends AsyncTask<Params, Progress, Result> {

    private static final int REQUEST_CODE_SIGN_IN = 1;
    private GoogleApiClient mClient;

    public ApiClientAsyncTask(Context context) {
        //DriveServiceHelper mDriveServiceHelper = new DriveServiceHelper();
        Log.d("TAG", "TASK DRIVE");
        mClient=((PikkuCamApplication) context.getApplicationContext()).getPikkuGoogleApiClient();
        /*
        GoogleApiClient.Builder builder = new GoogleApiClient.Builder(context)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE);
        mClient = builder.build();
        */
        if (mClient == null) {

            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build();
            GoogleSignInClient mGooglesInClient = GoogleSignIn.getClient(context,gso);
            //GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
           mClient = mGooglesInClient.asGoogleApiClient();
            }


    }

    @Override
    protected final Result doInBackground(Params... params) {
        Log.d("TAG", "in background");
        final CountDownLatch latch = new CountDownLatch(1);
        mClient.registerConnectionCallbacks(new ConnectionCallbacks() {
            @Override
            public void onConnectionSuspended(int cause) {
            }

            @Override
            public void onConnected(Bundle arg0) {
                latch.countDown();
            }
        });

        mClient.registerConnectionFailedListener(new OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(ConnectionResult arg0) {
                latch.countDown();
            }
        });
        Log.d("TAG", "about to connect");
        mClient.connect();
        try {
            latch.await();
        } catch (InterruptedException e) {
            return null;
        }
        Log.d("TAG", "Connected:"+mClient.isConnected());
        if (!mClient.isConnected()) {
            return null;
        }
        try {
            return doInBackgroundConnected(params);
        } finally {
            mClient.disconnect();
        }
    }

    /**
     * Override this method to perform a computation on a background thread, while the client is
     * connected.
     */
    protected abstract Result doInBackgroundConnected(Params... params);

    /**
     * Gets the GoogleApliClient owned by this async task.
     */
    protected GoogleApiClient getGoogleApiClient() {
        return mClient;
    }

}
