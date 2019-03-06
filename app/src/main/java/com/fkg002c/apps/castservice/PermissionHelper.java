package com.fkg002c.apps.castservice;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;

public class PermissionHelper {
    private static final String TAG = PermissionHelper.class.getSimpleName();

    public static final String KEY_PERMISSIONS = "permissions";
    public static final String KEY_GRANT_RESULTS = "grantResults";
    public static final String KEY_REQUEST_CODE = "requestCode";
    public static final String KEY_RESULT_RECEIVER = "resultReceiver";

    public static <T extends Context & ActivityCompat.OnRequestPermissionsResultCallback> void requestPermissions(final T context, String[] permissions, int requestCode, String notificationTitle, String notificationText, int notificationIcon) {
        ResultReceiver resultReceiver = new ResultReceiver(new Handler(Looper.getMainLooper())) {
            @Override
            protected void onReceiveResult (int resultCode, Bundle resultData) {
                String[] outPermissions = resultData.getStringArray(KEY_PERMISSIONS);
                int[] grantResults = resultData.getIntArray(KEY_GRANT_RESULTS);
                context.onRequestPermissionsResult(resultCode, outPermissions, grantResults);
            }
        };

        Intent permIntent = new Intent(context, PermissionRequestActivity.class);
        permIntent.putExtra(KEY_RESULT_RECEIVER, resultReceiver);
        permIntent.putExtra(KEY_PERMISSIONS, permissions);
        permIntent.putExtra(KEY_REQUEST_CODE, requestCode);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntent(permIntent);

        PendingIntent permPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CastService.CHANNEL_ID)
                .setSmallIcon(notificationIcon)
                .setContentTitle(notificationTitle)
                .setContentText(notificationText)
                .setOngoing(true)
                //.setCategory(Notification.CATEGORY_STATUS)
                .setAutoCancel(true)
                .setWhen(0)
                .setContentIntent(permPendingIntent)
                .setStyle(null);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(requestCode, builder.build());
    }

    public static class PermissionRequestActivity extends AppCompatActivity {
        ResultReceiver resultReceiver;
        String[] permissions;
        int requestCode;

        @Override
        public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
            Bundle resultData = new Bundle();
            resultData.putStringArray(KEY_PERMISSIONS, permissions);
            resultData.putIntArray(KEY_GRANT_RESULTS, grantResults);
            resultReceiver.send(requestCode, resultData);
            finish();
        }
        @Override
        protected void onStart() {
            super.onStart();
            resultReceiver = this.getIntent().getParcelableExtra(KEY_RESULT_RECEIVER);
            permissions = this.getIntent().getStringArrayExtra(KEY_PERMISSIONS);
            requestCode = this.getIntent().getIntExtra(KEY_REQUEST_CODE, 0);
            ActivityCompat.requestPermissions(this, permissions, requestCode);
        }
    }
}
