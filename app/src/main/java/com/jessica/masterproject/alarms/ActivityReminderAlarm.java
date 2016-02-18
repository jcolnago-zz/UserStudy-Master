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
import com.jessica.masterproject.R;

import java.text.ParseException;
import java.util.GregorianCalendar;

public class ActivityReminderAlarm extends BroadcastReceiver {

    public ActivityReminderAlarm() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            // If system rebooted reset alarm
            GregorianCalendar intTime = new GregorianCalendar();
            Intent reminderIntent = new Intent(context, ActivityReminderAlarm.class);
            try {
                // If before interruptions start, set time to remind users of initial activities
                if (new GregorianCalendar().getTime().before(MainActivity.FORMAT.parse(MainActivity.INTERRUPTIONS_START))) {
                    intTime.setTime(MainActivity.FORMAT.parse(MainActivity.SCENARIO_ONE_START));
                    if (new GregorianCalendar().getTime().before(MainActivity.FORMAT.parse(MainActivity.SCENARIO_ONE_REMINDER))) {
                        reminderIntent.putExtra("interval", 2 * MainActivity.HOUR);
                    }
                    else reminderIntent.putExtra("interval", MainActivity.HOUR / 2);
                    MainActivity.reScheduleAlarm(context, intTime.getTimeInMillis(), reminderIntent);
                }
                // If after interruptions end, set time to remind users of finishing final activities
                else {
                    intTime.setTime(MainActivity.FORMAT.parse(MainActivity.INTERRUPTIONS_END));
                    if (new GregorianCalendar().getTime().before(MainActivity.FORMAT.parse(MainActivity.SCENARIO_TWO_REMINDER))) {
                        reminderIntent.putExtra("interval", 2 * MainActivity.HOUR);
                    }
                    else reminderIntent.putExtra("interval", MainActivity.HOUR / 2);
                    MainActivity.reScheduleAlarm(context, intTime.getTimeInMillis(), reminderIntent);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        else {
            boolean setReminder = true;
            String[] files = new String[2];

            SharedPreferences mSharedPref = context.getSharedPreferences(MainActivity.SP_PREFERENCE_FILE, Context.MODE_PRIVATE);

            files[0] = MainActivity.SCENARIOS_FILENAME;
            files[1] = MainActivity.QUESTIONNAIRE_FILENAME;

            for (int i = 0; i < 2; i++) {
                if (!mSharedPref.getBoolean(MainActivity.SP_UPLOAD_PENDING + files[i], false)
                        && !mSharedPref.getBoolean(MainActivity.SP_UPLOAD_DONE + files[i], false)) {
                    Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                    long[] pattern = {500, 500, 500, 500, 250, 500, 125, 500};

                    Intent notificationIntent = new Intent(context, MainActivity.class);
                    notificationIntent.putExtra("clicked", true);
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
                                    .setContentTitle(context.getString(R.string.pending_qs_title))
                                    .setContentText(context.getString(R.string.pending_qs_text))
                                    .setContentIntent(pendingIntent);
                    // Sets an ID for the notification
                    int mNotificationId = 1;
                    // Gets an instance of the NotificationManager service
                    NotificationManager notificationManager =
                            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    // Builds the notification and issues it.
                    notificationManager.notify(mNotificationId, mBuilder.build());
                }
                else setReminder = false;
            }

            if (setReminder) {
                // In case it wasn't done, reschedule alarm for the interval defined or hour if none defined.
                MainActivity.reScheduleAlarm(context.getApplicationContext(),
                        (new GregorianCalendar()).getTimeInMillis() + intent.getLongExtra("interval", MainActivity.HOUR),
                        intent);
            }
        }
    }
}