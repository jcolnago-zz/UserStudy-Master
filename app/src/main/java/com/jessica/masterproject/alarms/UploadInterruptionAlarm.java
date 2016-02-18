package com.jessica.masterproject.alarms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.jessica.masterproject.MainActivity;
import com.jessica.masterproject.helpers.FileUploader;

import java.util.GregorianCalendar;

public class UploadInterruptionAlarm extends BroadcastReceiver {

    public UploadInterruptionAlarm() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            MainActivity.reScheduleAlarm(context.getApplicationContext(),
                    (new GregorianCalendar()).getTimeInMillis(),
                    new Intent(context, UploadInterruptionAlarm.class));
        }
        else {
            boolean setNextDayAlarm = false;

            SharedPreferences mSharedPref = context.getSharedPreferences(MainActivity.SP_PREFERENCE_FILE, Context.MODE_PRIVATE);
            SharedPreferences.Editor mEditor = mSharedPref.edit();

            int lastInterruptionUp = mSharedPref.getInt(MainActivity.SP_LAST_INTERRUPTION_UPLOADED, 0);
            int pending = MainActivity.INTERRUPTIONS - lastInterruptionUp;

            String[] files = new String[pending];

            for (int i = 0; i < pending; i++) {
                files[i] = (lastInterruptionUp + i + 1) + "_" + MainActivity.INTERRUPTIONS_FILENAME;
            }

            if (isWiFiAvailable(context)) {
                for (int i = 0; i < pending; i++) {
                    if (mSharedPref.getBoolean(MainActivity.SP_UPLOAD_PENDING + files[i], false)) {
                        FileUploader.upload(files[i], ".csv", context);
                        mEditor.putInt(MainActivity.SP_LAST_INTERRUPTION_UPLOADED, Integer.parseInt(files[i].split("_")[0]));
                        mEditor.commit();
                    } else {
                        if (!mSharedPref.getBoolean(MainActivity.SP_UPLOAD_DONE + files[i], false)) {
                            setNextDayAlarm = true;
                        }
                    }
                }

                if (setNextDayAlarm) {
                    MainActivity.reScheduleAlarm(context.getApplicationContext(),
                            (new GregorianCalendar()).getTimeInMillis() + MainActivity.DAY,
                            intent);
                }

            } else {
                MainActivity.reScheduleAlarm(context.getApplicationContext(),
                        (new GregorianCalendar()).getTimeInMillis() + MainActivity.HOUR/2,
                        intent);
            }
        }
    }

    private boolean isWiFiAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
        if (netInfo != null)
            return netInfo.getTypeName().equalsIgnoreCase("wifi") && netInfo.isConnected();
        return false;
    }

}
