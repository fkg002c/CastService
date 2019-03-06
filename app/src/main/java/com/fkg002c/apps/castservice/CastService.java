package com.fkg002c.apps.castservice;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class CastService extends Service implements ActivityCompat.OnRequestPermissionsResultCallback {
    private static final String TAG = CastService.class.getSimpleName();

    public static final String CHANNEL_ID = "CastServiceChannel";

    private static final String START = "com.fkg002c.apps.castservice.START";
    private static final String STOP = "com.fkg002c.apps.castservice.STOP";

    private static final int REQUEST_CODE_ASK_PERMISSIONS = 1;
    private static final String[] REQUIRED_PERMISSIONS = new String[]{
            Manifest.permission.RECORD_AUDIO};

    private StreamThread mThread = null;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate() called");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(1, getCastNotification());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private Notification getCastNotification() {
        NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_ID, "Cast Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        NotificationManager mgr = getSystemService(NotificationManager.class);
        mgr.createNotificationChannel(serviceChannel);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Cast Service run")
                .setSmallIcon(R.drawable.ic_microphone).build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand() called, intent.getAction() is " +  intent.getAction() + ", startId is " + startId);
        switch (intent.getAction()) {
            case START:
                checkPermission();
                break;
            case STOP:
                stopSelf();
                stopThread();
                break;
            default:
                //DO NOTHING
                break;
        }
        return START_STICKY;
    }

    private void startThread() {
        Log.e(TAG, "startThread() called");

        if (mThread == null) {
            mThread =  new StreamThread(5006);
            mThread.start();
        }
    }

    private void stopThread() {
        Log.e(TAG, "stopThread() called");
        if (mThread != null) {
            mThread.selfDestroy();
            mThread = null;
        }
    }

    private void checkPermission() {
        final List<String> missingPermissions = new ArrayList<String>();
        // check all required dynamic permissions
        for (final String permission : REQUIRED_PERMISSIONS) {
            final int result = ContextCompat.checkSelfPermission(this, permission);
            if (result != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }
        if (!missingPermissions.isEmpty()) {
            // request all missing permissions
            final String[] permissions = missingPermissions
                    .toArray(new String[missingPermissions.size()]);
            PermissionHelper.requestPermissions(this,
                    permissions, REQUEST_CODE_ASK_PERMISSIONS,
                    "Permissions are required",
                    "Please allow:" + missingPermissions,
                    R.drawable.ic_microphone);

        } else {
            startThread();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                for (int index = permissions.length - 1; index >= 0; --index) {
                    if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                        // exit the app if one permission is not granted
                        Toast.makeText(this, "Required permission '" + permissions[index]
                                + "' not granted, exiting", Toast.LENGTH_LONG).show();
                        System.out.println("We may NOT start cast service");
                        stopSelf();
                        return;
                    }
                }
                // all required permissions were granted
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForeground(1, getCastNotification());
                }
                startThread();
                break;
        }
    }
}
