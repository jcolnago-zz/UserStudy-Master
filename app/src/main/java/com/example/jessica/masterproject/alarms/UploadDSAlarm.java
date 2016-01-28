package com.example.jessica.masterproject.alarms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.example.jessica.masterproject.MainActivity;
import com.example.jessica.masterproject.R;
import com.example.jessica.masterproject.helpers.FileSaver;
import com.example.jessica.masterproject.helpers.FileUploader;

import java.util.GregorianCalendar;

public class UploadDSAlarm extends BroadcastReceiver {
    private SharedPreferences mSharedPref;

    public UploadDSAlarm() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            MainActivity.reScheduleAlarm(context.getApplicationContext(),
                    (new GregorianCalendar()).getTimeInMillis() + MainActivity.HOUR/2,
                    new Intent(context, UploadDSAlarm.class));
        }
        else {
            System.out.println("Upload DS Alarm started!");
            boolean setNextDayAlarm = false;
            String[] files = new String[2];

            mSharedPref = context.getSharedPreferences(String.valueOf(R.string.preference_file), Context.MODE_PRIVATE);

            files[0] = context.getString(R.string.scenarios_filename)
                    .substring(0, context.getString(R.string.scenarios_filename).length() - 4);
            files[1] = context.getString(R.string.demographic_filename)
                    .substring(0, context.getString(R.string.demographic_filename).length() - 4);


            System.out.println("String of files to be checked created with " + files.length + " entries.");

            if (isWiFiAvailable(context) || true) { //TODO: remove || true after testing
                for (int i = 0; i < 2; i++) {
                    if (mSharedPref.getBoolean(context.getString(R.string.upload_pending) + files[i], false)) {
                        FileUploader.upload(files[i], ".csv", context);
                    } else if (!mSharedPref.getBoolean(context.getString(R.string.upload_done) + files[i], false)) {
                        setNextDayAlarm = true;
                    }
                }

                if (setNextDayAlarm) {
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
        if (netInfo.getTypeName().equalsIgnoreCase("wifi") && netInfo.isConnected())
            return true;
        return false;
    }
}