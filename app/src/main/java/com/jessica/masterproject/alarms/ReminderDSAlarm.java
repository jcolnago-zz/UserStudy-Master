package com.jessica.masterproject.alarms;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.jessica.masterproject.MainActivity;
import com.jessica.masterproject.MotherActivity;
import com.jessica.masterproject.R;

import java.text.ParseException;
import java.util.GregorianCalendar;

public class ReminderDSAlarm extends BroadcastReceiver {

    public ReminderDSAlarm() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("[LOG] Reminder DS Alarm started.");
        if (intent.getAction() != null && intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            // If system rebooted reset alarm
            GregorianCalendar intTime = new GregorianCalendar();
            try {
                intTime.setTime(MotherActivity.FORMAT.parse("2016-02-02T12:00:00.000-0200"));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            MainActivity.reScheduleAlarm(context.getApplicationContext(),
                    intTime.getTimeInMillis(),
                    new Intent(context, ReminderDSAlarm.class));
        }
        else {
            boolean setReminder = false;
            String[] files = new String[2];

            SharedPreferences mSharedPref = context.getSharedPreferences(String.valueOf(R.string.preference_file), Context.MODE_PRIVATE);

            files[0] = context.getString(R.string.scenarios_filename)
                    .substring(0, context.getString(R.string.scenarios_filename).length() - 4);
            files[1] = context.getString(R.string.demographic_filename)
                    .substring(0, context.getString(R.string.demographic_filename).length() - 4);

            for (int i = 0; i < 2; i++) {
                if (!mSharedPref.getBoolean(context.getString(R.string.upload_pending) + files[i], false)
                        && !mSharedPref.getBoolean(context.getString(R.string.upload_done) + files[i], false)) {
                    Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                    long[] pattern = {500, 500, 500, 500, 250, 500, 125, 500};

                    Intent notificationIntent = new Intent(context, MainActivity.class);
                    notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                    PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                            notificationIntent, 0);

                    NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(context)
                                    .setSmallIcon(R.mipmap.ic_launcher)
                                    .setLights(Color.RED, 500, 500)
                                    .setSound(alarmSound)
                                    .setVibrate(pattern)
                                    .setAutoCancel(true)
                                    .setPriority(NotificationCompat.PRIORITY_MAX)
                                    .setCategory(Notification.CATEGORY_ALARM)
                                    .setContentTitle(context.getString(R.string.pending_ds_title))
                                    .setContentText(context.getString(R.string.pending_ds_text))
                                    .setStyle(new NotificationCompat.InboxStyle())
                                    .setContentIntent(pendingIntent);
                    // Sets an ID for the notification
                    int mNotificationId = 1;
                    // Gets an instance of the NotificationManager service
                    NotificationManager notificationManager =
                            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    // Builds the notification and issues it.
                    notificationManager.notify(mNotificationId, mBuilder.build());
                    setReminder = true;
                }
            }

            if (setReminder) {
                // In case it wasn't done, reschedule alarm for the next 30 min
                MainActivity.reScheduleAlarm(context.getApplicationContext(),
                        (new GregorianCalendar()).getTimeInMillis() + MainActivity.HOUR/2,
                        intent);
            }
        }
    }
}