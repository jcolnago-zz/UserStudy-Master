package com.jessica.masterproject.alarms;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.jessica.masterproject.R;

public class CancelAlarm extends BroadcastReceiver {

    public CancelAlarm() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("[LOG] CancelAlarm started.");
        // Cancel notification and sets interruption as done (no file means N/As)

        SharedPreferences mSharedPref = context.getSharedPreferences(String.valueOf(R.string.preference_file), Context.MODE_PRIVATE);
        SharedPreferences.Editor mEditor = mSharedPref.edit();

        int notificationId = intent.getIntExtra("notificationId", -1);
        String filename = intent.getStringExtra("interruption");

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (!mSharedPref.getBoolean(context.getString(R.string.upload_pending) + filename, false)
                && !mSharedPref.getBoolean(context.getString(R.string.upload_done) + filename, false)) {
            // cancel notification
            notificationManager.cancel(notificationId);

            int missed = mSharedPref.getInt(context.getString(R.string.missed_interruptions), 0);
            mEditor.putInt(context.getString(R.string.missed_interruptions), ++missed);
            // set it as done, so it won't try to upload an non-existing file.
            mEditor.putBoolean(context.getString(R.string.upload_done) + filename, true);
            mEditor.commit();

        }

    }
}
