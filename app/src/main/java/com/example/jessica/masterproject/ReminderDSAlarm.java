package com.example.jessica.masterproject;

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

public class ReminderDSAlarm extends BroadcastReceiver {
    private SharedPreferences mSharedPref;

    public ReminderDSAlarm() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            MainActivity.reScheduleAlarm(context.getApplicationContext(), MainActivity.MINUTE/2, new Intent(context, ReminderDSAlarm.class));
        }
        else {
            boolean setReminder = false;
            String[] files = new String[2];

            mSharedPref = context.getSharedPreferences(String.valueOf(R.string.preference_file), Context.MODE_PRIVATE);

            files[0] = context.getString(R.string.scenarios_filename)
                    .substring(0, context.getString(R.string.scenarios_filename).length() - 4);
            files[1] = context.getString(R.string.demographic_filename)
                    .substring(0, context.getString(R.string.demographic_filename).length() - 4);

            for (int i = 0; i < 2; i++) {
                if (!mSharedPref.getBoolean(context.getString(R.string.upload_pending)+files[i],false)
                        && !mSharedPref.getBoolean(context.getString(R.string.upload_done)+files[i],false)) {
                    System.out.println(files[i] + " not done yet.");
                    Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                    long[] pattern = {500,500,500,500,250,500,125,500};

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
                                    .setPriority(NotificationCompat.PRIORITY_MAX)
                                    .setCategory(Notification.CATEGORY_ALARM)
                                    .setContentTitle(context.getString(R.string.pending_ds_title))
                                    .setContentText(context.getString(R.string.pending_ds_text))
                                    .setStyle(new NotificationCompat.InboxStyle())
                                    .setContentIntent(pendingIntent);
                    // Sets an ID for the notification
                    int mNotificationId = 001;
                    // Gets an instance of the NotificationManager service
                    NotificationManager mNotifyMgr =
                            (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
                    // Builds the notification and issues it.
                    mNotifyMgr.notify(mNotificationId, mBuilder.build());
                    setReminder = true;
                }
            }

            if (setReminder) {
                //TODO: Change this back to HOUR/2
                System.out.println("...rescheduling...");
                MainActivity.reScheduleAlarm(context.getApplicationContext(), MainActivity.MINUTE/2, intent);
            }
        }
    }
}