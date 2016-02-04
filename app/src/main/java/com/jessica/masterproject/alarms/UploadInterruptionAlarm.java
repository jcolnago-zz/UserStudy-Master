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

public class UploadInterruptionAlarm extends BroadcastReceiver {

    public UploadInterruptionAlarm() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("[LOG] UploadInterruptionAlarm started.");
        if (intent.getAction() != null && intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            MainActivity.reScheduleAlarm(context.getApplicationContext(),
                    (new GregorianCalendar()).getTimeInMillis() + MainActivity.HOUR/2,
                    new Intent(context, UploadInterruptionAlarm.class));
        }
        else {
            boolean setNextDayAlarm = false;

            SharedPreferences mSharedPref = context.getSharedPreferences(String.valueOf(R.string.preference_file), Context.MODE_PRIVATE);
            SharedPreferences.Editor mEditor = mSharedPref.edit();

            int lastInterruptionUp = mSharedPref.getInt(context.getString(R.string.last_interruption_uploaded), 0);

            String[] files = new String[MainActivity.INTERRUPTIONS - lastInterruptionUp];

            for (int i = 0; i < MainActivity.INTERRUPTIONS - lastInterruptionUp; i++) {
                files[i] = (lastInterruptionUp + i + 1) + "_" + context.getString(R.string.interruption_filename)
                        .substring(0, context.getString(R.string.interruption_filename).length() - 4);
            }

            if (isWiFiAvailable(context)) {
                for (int i = 0; i < MainActivity.INTERRUPTIONS - lastInterruptionUp; i++) {
                    if (mSharedPref.getBoolean(context.getString(R.string.upload_pending) + files[i], false)) {
                        System.out.println("   [LOG] UploadInterruptionAlarm: uploading: " + files[i]);
                        FileUploader.upload(files[i], ".csv", context);
                        mEditor.putInt(context.getString(R.string.last_interruption_uploaded), Integer.parseInt(files[i].split("_")[0]));
                        mEditor.commit();
                    } else {
                        if (!mSharedPref.getBoolean(context.getString(R.string.upload_done) + files[i], false)) {
                            setNextDayAlarm = true;
                        }
                    }
                }

                if (setNextDayAlarm) {
                    System.out.println("   [LOG] UploadInterruptionAlarm: some pending. Reschedule for next day.");
                    MainActivity.reScheduleAlarm(context.getApplicationContext(),
                            (new GregorianCalendar()).getTimeInMillis() + MainActivity.DAY,
                            intent);
                }

            } else {
                System.out.println("   [LOG] UploadInterruptionAlarm: no wifi. Reschedule for next 30min.");
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
