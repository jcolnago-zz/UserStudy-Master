package com.jessica.masterproject.alarms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.jessica.masterproject.MainActivity;

import java.text.ParseException;
import java.util.GregorianCalendar;

public class ResetAlarm extends BroadcastReceiver {

    public ResetAlarm() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            GregorianCalendar intTime = new GregorianCalendar();
            //Set up alarm for resetting values for final activities
            try {
                intTime.setTime(MainActivity.FORMAT.parse(MainActivity.INTERRUPTIONS_END));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            MainActivity.scheduleAlarm(context,
                    intTime.getTimeInMillis(),
                    new Intent(context, ResetAlarm.class));
        }
        else {
            SharedPreferences mSharedPref = context.getSharedPreferences(MainActivity.SP_PREFERENCE_FILE, Context.MODE_PRIVATE);
            SharedPreferences.Editor mEditor = mSharedPref.edit();

            if (!mSharedPref.getBoolean(MainActivity.SP_RESET, false)) {
                mEditor.putBoolean(MainActivity.SP_RESET, true);
                mEditor.putInt(MainActivity.SP_CURRENT_SCENARIO, 0);
                mEditor.putBoolean(MainActivity.SP_UPLOAD_PENDING + MainActivity.SCENARIOS_FILENAME, false);
                mEditor.putBoolean(MainActivity.SP_UPLOAD_PENDING + MainActivity.SCENARIOS_DECISIONS_FILENAME, false);
                mEditor.putBoolean(MainActivity.SP_UPLOAD_PENDING + MainActivity.QUESTIONNAIRE_FILENAME, false);
                mEditor.putBoolean(MainActivity.SP_UPLOAD_DONE + MainActivity.SCENARIOS_FILENAME, false);
                mEditor.putBoolean(MainActivity.SP_UPLOAD_DONE + MainActivity.SCENARIOS_DECISIONS_FILENAME, false);
                mEditor.putBoolean(MainActivity.SP_UPLOAD_DONE + MainActivity.QUESTIONNAIRE_FILENAME, false);
                mEditor.commit();
            }
        }
    }
}
