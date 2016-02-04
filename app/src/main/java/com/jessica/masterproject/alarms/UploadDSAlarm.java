package com.jessica.masterproject.alarms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.jessica.masterproject.MainActivity;
import com.jessica.masterproject.R;
import com.jessica.masterproject.helpers.FileUploader;

import java.util.GregorianCalendar;

public class UploadDSAlarm extends BroadcastReceiver {

    public UploadDSAlarm() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("[LOG] UploadDSAlarm started.");
        if (intent.getAction() != null && intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            MainActivity.reScheduleAlarm(context.getApplicationContext(),
                    (new GregorianCalendar()).getTimeInMillis() + MainActivity.HOUR/2,
                    new Intent(context, UploadDSAlarm.class));
        }
        else {
            if (isWiFiAvailable(context)) {
                boolean setNextDayAlarm = false;
                String[] files = new String[2];

                SharedPreferences mSharedPref = context.getSharedPreferences(String.valueOf(R.string.preference_file), Context.MODE_PRIVATE);

                files[0] = context.getString(R.string.scenarios_filename)
                        .substring(0, context.getString(R.string.scenarios_filename).length() - 4);
                files[1] = context.getString(R.string.demographic_filename)
                        .substring(0, context.getString(R.string.demographic_filename).length() - 4);

                for (int i = 0; i < 2; i++) {
                    if (mSharedPref.getBoolean(context.getString(R.string.upload_pending) + files[i], false)) {
                        System.out.println("[LOG] UploadDSAlarm: uploading: " + files[i]);
                        FileUploader.upload(files[i], ".csv", context);
                    } else if (!mSharedPref.getBoolean(context.getString(R.string.upload_done) + files[i], false)) {
                        setNextDayAlarm = true;
                    }
                }

                if (setNextDayAlarm) {
                    System.out.println("[LOG] UploadDSAlarm: some are still pending, rescheduling for next day.");
                    MainActivity.reScheduleAlarm(context.getApplicationContext(),
                            (new GregorianCalendar()).getTimeInMillis() + MainActivity.DAY,
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