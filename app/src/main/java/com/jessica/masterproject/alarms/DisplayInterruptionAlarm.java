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
import com.jessica.masterproject.ParticipationActivity;
import com.jessica.masterproject.ParticipationNotNowActivity;
import com.jessica.masterproject.R;

import java.text.ParseException;
import java.util.GregorianCalendar;

public class DisplayInterruptionAlarm extends BroadcastReceiver {

    public DisplayInterruptionAlarm() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("[LOG] DisplayInterruptionAlarm started.");
        SharedPreferences mSharedPref = context.getSharedPreferences(String.valueOf(R.string.preference_file), Context.MODE_PRIVATE);
        SharedPreferences.Editor mEditor = mSharedPref.edit();

        GregorianCalendar intTime = new GregorianCalendar();
        String filename = context.getString(R.string.interruption_filename);

        if (intent.getAction() != null && intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            int lastInterruption = mSharedPref.getInt(context.getString(R.string.last_interruption), 0);
            if (lastInterruption != MainActivity.INTERRUPTIONS) {
                Intent interruptionIntent = new Intent(context, DisplayInterruptionAlarm.class);
                interruptionIntent.putExtra("current_interruption", lastInterruption + 1);

                String[] setupValues = context.getResources().getStringArray(R.array.interruptions);

                try {
                    intTime.setTime(MotherActivity.FORMAT.parse(setupValues[lastInterruption * 3 + 2]));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                MainActivity.reScheduleAlarm(context.getApplicationContext(),
                        intTime.getTimeInMillis(),
                        interruptionIntent);
            }
        }
        else {
            int mCurrentInterruption = intent.getIntExtra("current_interruption", 1);
            System.out.println("[LOG] DisplayInterruptionAlarm for interruption: " + mCurrentInterruption);
            filename = mCurrentInterruption + "_" + filename.substring(0, filename.length()-4);

            // Checks if mCurrentInterruption hasn't been seen yet (not pending upload and not done)
            if (!mSharedPref.getBoolean(context.getString(R.string.upload_pending) + filename, false)
                    && !mSharedPref.getBoolean(context.getString(R.string.upload_done) + filename, false)) {

                // Ask if the user can participate now --
                Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                long[] pattern = {0, 150, 75, 150, 75, 150};
                NotificationManager notificationManager =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);  // Gets an instance of the NotificationManager service

                // If the timeout of 10 minutes doesn't do the trick, cancel previous one before creating a new one
                // Ideally this would check if the notification is active getActiveNotifications()
                // But this can only be done in android 4.3+
                if (mCurrentInterruption > 1) {
                    Intent cancelIntent = new Intent(context.getApplicationContext(), CancelAlarm.class);
                    cancelIntent.putExtra("notificationId", (mCurrentInterruption-1));
                    cancelIntent.putExtra("interruption", (mCurrentInterruption-1) + "_interruption");

                    scheduleCancelAlarm(context, cancelIntent, 0);
                }

                Intent yesIntent = new Intent(context, ParticipationActivity.class);
                yesIntent.putExtra("current_interruption", mCurrentInterruption);
                yesIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                Intent notNowIntent = new Intent(context, ParticipationNotNowActivity.class);
                notNowIntent.putExtra("current_interruption", mCurrentInterruption);
                notNowIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                Intent cancelIntent = new Intent(context.getApplicationContext(), CancelAlarm.class);
                cancelIntent.putExtra("notificationId", mCurrentInterruption);
                cancelIntent.putExtra("interruption", filename);

                PendingIntent yesPendingIntent = PendingIntent.getActivity(context, mCurrentInterruption, yesIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                PendingIntent notNowPendingIntent = PendingIntent.getActivity(context, mCurrentInterruption, notNowIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(context, mCurrentInterruption, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(context)
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .setLights(Color.BLUE, 500, 500)
                                .setSound(alarmSound)
                                .setVibrate(pattern)
                                .setAutoCancel(true)
                                .setDeleteIntent(cancelPendingIntent)
                                .setPriority(NotificationCompat.PRIORITY_MAX)
                                .setCategory(Notification.CATEGORY_ALARM)
                                .setContentTitle(context.getString(R.string.participation_request_title))
                                .setContentText(context.getString(R.string.participation_request_text))
                                .addAction(R.drawable.not_now_icon, context.getString(R.string.participation_request_not_now), notNowPendingIntent)
                                .addAction(R.drawable.yes_icon, context.getString(R.string.participation_request_yes), yesPendingIntent)
                                .setContentIntent(yesPendingIntent);

                Notification notification = mBuilder.build();

                // Updates last_interruption value to avoid having to have both notNow and Yes update it
                    // used in MainActivity
                mEditor.putInt(context.getString(R.string.last_interruption), mCurrentInterruption);
                mEditor.commit();

                // Sets up alarm for next interruption if there is one
                if (mCurrentInterruption +1 <= MainActivity.INTERRUPTIONS) {
                    intent.putExtra("current_interruption", mCurrentInterruption + 1);
                    String[] setupValues = context.getResources().getStringArray(R.array.interruptions);

                    try {
                        intTime.setTime(MotherActivity.FORMAT.parse(setupValues[mCurrentInterruption * 3 + 2]));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    System.out.println("[LOG] DisplayInterruptionAlarm: setting up next interruption["+(mCurrentInterruption +1)+"] alarm");
                    MainActivity.reScheduleAlarm(context.getApplicationContext(),
                            intTime.getTimeInMillis(),
                            intent);
                }

                //Schedule alarm for 10 min after this notification is dispatched
                System.out.println("[LOG] DisplayInterruptionAlarm: scheduling cancel for 10min");
                scheduleCancelAlarm(context, cancelIntent, (MainActivity.HOUR / 6));

                // Builds the notification and issues it.
                notificationManager.notify(mCurrentInterruption, notification);
            }
        }
    }

    private void scheduleCancelAlarm(Context context, Intent intent, long time){
        MainActivity.scheduleAlarm(context.getApplicationContext(),
                ((new GregorianCalendar()).getTimeInMillis() + time),
                intent);
    }
}
