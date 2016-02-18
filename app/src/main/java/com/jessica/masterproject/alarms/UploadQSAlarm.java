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

public class UploadQSAlarm extends BroadcastReceiver {

    public UploadQSAlarm() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            MainActivity.reScheduleAlarm(context.getApplicationContext(),
                    (new GregorianCalendar()).getTimeInMillis(),
                    new Intent(context, UploadQSAlarm.class));
        }
        else {
            boolean resetAlarm = false;
            String[] files = new String[3];

            SharedPreferences mSharedPref = context.getSharedPreferences(MainActivity.SP_PREFERENCE_FILE, Context.MODE_PRIVATE);

            files[0] = MainActivity.SCENARIOS_FILENAME;
            files[1] = MainActivity.QUESTIONNAIRE_FILENAME;
            files[2] = MainActivity.SCENARIOS_DECISIONS_FILENAME;

            if (isWiFiAvailable(context)) {
                for (int i = 0; i < 3; i++) {
                    if (mSharedPref.getBoolean(MainActivity.SP_UPLOAD_PENDING + files[i], false)) {
                        FileUploader.upload(files[i], MainActivity.FILE_FORMAT, context);
                    } else if (!mSharedPref.getBoolean(MainActivity.SP_UPLOAD_DONE + files[i], false)) {
                        resetAlarm = true;
                    }
                }

                if (resetAlarm) {
                    MainActivity.reScheduleAlarm(context.getApplicationContext(),
                            (new GregorianCalendar()).getTimeInMillis() + MainActivity.HOUR,
                            intent);
                }

            } else {
                System.err.println("WiFi not available. Rescheduling to check in half an hour.");
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