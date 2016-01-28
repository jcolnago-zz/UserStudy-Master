package com.example.jessica.masterproject.alarms;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.example.jessica.masterproject.MainActivity;
import com.example.jessica.masterproject.R;
import com.example.jessica.masterproject.helpers.FileUploader;

import java.util.Arrays;

public class CancelAlarm extends BroadcastReceiver {

    private SharedPreferences mSharedPref;
    private SharedPreferences.Editor mEditor;

    public CancelAlarm() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("Cancel Alarm started!");

        mSharedPref = context.getSharedPreferences(String.valueOf(R.string.preference_file), Context.MODE_PRIVATE);
        mEditor = mSharedPref.edit();

        int notificationId = intent.getIntExtra("notificationId", -1);
        String filename = intent.getStringExtra("interruption");

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);

        if (!mSharedPref.getBoolean(context.getString(R.string.upload_pending)+filename,false)
                && !mSharedPref.getBoolean(context.getString(R.string.upload_done)+filename,false)) {
            // cancel notification
            notificationManager.cancel(notificationId);

            // set it as done, so it won't try to upload an non-existing file.
            mEditor.putBoolean(context.getString(R.string.upload_done) + filename, true);
            mEditor.commit();

        }

    }
}
