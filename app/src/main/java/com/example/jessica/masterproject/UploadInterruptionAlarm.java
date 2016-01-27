package com.example.jessica.masterproject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class UploadInterruptionAlarm extends BroadcastReceiver {
    private SharedPreferences mSharedPref;

    public UploadInterruptionAlarm() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            MainActivity.reScheduleAlarm(context.getApplicationContext(), MainActivity.MINUTE/2, new Intent(context, UploadInterruptionAlarm.class));
        }
        else {
            System.out.println("Alarm started!");
            boolean setNextDayAlarm = false;

            mSharedPref = context.getSharedPreferences(String.valueOf(R.string.preference_file), Context.MODE_PRIVATE);
            int lastInterruption = mSharedPref.getInt(context.getString(R.string.last_interruption), 0);

            String[] files = new String[150 - lastInterruption];

            for (int i = 0; i < 150 - lastInterruption; i++) {
                files[i] = (lastInterruption + i + 1) + "_" + context.getString(R.string.scenarios_filename)
                        .substring(0, context.getString(R.string.scenarios_filename).length() - 4);
            }

            System.out.println("String of files to be checked created with " + files.length + " entries.");

            if (isWiFiAvailable(context) || true) { //TODO: remove || true after testing
                System.out.println("Wifi Available");

                for (int i = 0; i < 150 - lastInterruption; i++) {
                    if (mSharedPref.getBoolean(context.getString(R.string.upload_pending) + files[i], false)) {
                        System.out.println("Uploading " + files[i]);
                        FileUploader.upload(files[i], ".csv", context);
                    } else if (!mSharedPref.getBoolean(context.getString(R.string.upload_done) + files[i], false)) {
                        System.out.println(files[i] + " not done yet.");
                        setNextDayAlarm = true;
                    }
                }

                if (setNextDayAlarm) {
                    //TODO: Change this back to DAY
                    System.out.println("...rescheduling...");
                    MainActivity.reScheduleAlarm(context.getApplicationContext(), MainActivity.MINUTE / 2, intent);
                }

            } else {
                System.out.println("Wifi not available");
                //TODO: Change this back to HOUR/2
                MainActivity.reScheduleAlarm(context.getApplicationContext(), MainActivity.MINUTE / 3, intent);
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
