package com.jessica.masterproject.alarms;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.jessica.masterproject.MainActivity;

public class CancelAlarm extends BroadcastReceiver {

    public CancelAlarm() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // Cancel notification and sets interruption as done (no file means N/As)
        SharedPreferences mSharedPref = context.getSharedPreferences(MainActivity.SP_PREFERENCE_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor mEditor = mSharedPref.edit();

        int notificationId = intent.getIntExtra("notificationId", -1);
        String filename = intent.getStringExtra("interruption");

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (!(mSharedPref.getBoolean(MainActivity.SP_UPLOAD_PENDING + filename, false)
                || mSharedPref.getBoolean(MainActivity.SP_UPLOAD_DONE + filename, false))) {
            // cancel notification
            notificationManager.cancel(notificationId);

            // Increment missed counter
            int missed = mSharedPref.getInt(MainActivity.SP_MISSED_INTERRUPTIONS, 0);
            mEditor.putInt(MainActivity.SP_MISSED_INTERRUPTIONS, ++missed);

            // set it as done, so it won't try to upload an non-existing file.
            mEditor.putBoolean(MainActivity.SP_UPLOAD_DONE + filename, true);
            mEditor.commit();
        }
    }
}
