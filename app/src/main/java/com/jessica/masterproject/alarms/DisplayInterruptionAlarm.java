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
        SharedPreferences mSharedPref = context.getSharedPreferences(MainActivity.SP_PREFERENCE_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor mEditor = mSharedPref.edit();

        GregorianCalendar intTime = new GregorianCalendar();

        if (intent.getAction() != null && intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            int lastInterruption = mSharedPref.getInt(MainActivity.SP_LAST_INTERRUPTION, 0);
            if (lastInterruption != MainActivity.INTERRUPTIONS) {
                Intent interruptionIntent = new Intent(context, DisplayInterruptionAlarm.class);
                String[] setupValues = context.getResources().getStringArray(R.array.interruptions);

                try {
                    intTime.setTime(MainActivity.FORMAT.parse(setupValues[lastInterruption * 3 + 2]));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                interruptionIntent.putExtra("current_interruption", lastInterruption + 1);
                long elapsed_time = intTime.getTimeInMillis() - new GregorianCalendar().getTimeInMillis();
                interruptionIntent.putExtra("lost_time", elapsed_time < 0 ? -elapsed_time : 0);

                MainActivity.reScheduleAlarm(context.getApplicationContext(),
                        intTime.getTimeInMillis(),
                        interruptionIntent);
            }
        }
        else {
            int mCurrentInterruption = intent.getIntExtra("current_interruption", 1);
            String filename = mCurrentInterruption + "_" + MainActivity.INTERRUPTIONS_FILENAME;
            long lost_time = intent.getLongExtra("lost_time", 0);

            // Safeguard. Shouldn't be needed in our code because timeout is smaller than interruption
            // interval
            if (mCurrentInterruption > 1) {
                Intent cancelIntent = new Intent(context.getApplicationContext(), CancelAlarm.class);
                cancelIntent.putExtra("notificationId", (mCurrentInterruption-1));
                cancelIntent.putExtra("interruption", (mCurrentInterruption-1) + "_interruption");
                scheduleCancelAlarm(context, cancelIntent, 0);
            }

            // Updates last_interruption value to avoid having to have both notNow and Yes update it
            // used in MainActivity
            mEditor.putInt(MainActivity.SP_LAST_INTERRUPTION, mCurrentInterruption);
            mEditor.commit();

            // Sets up alarm for next interruption if there is one
            if (mCurrentInterruption+1 <= MainActivity.INTERRUPTIONS) {
                String[] setupValues = context.getResources().getStringArray(R.array.interruptions);

                try {
                    intTime.setTime(MainActivity.FORMAT.parse(setupValues[mCurrentInterruption * 3 + 2]));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                intent.putExtra("current_interruption", mCurrentInterruption + 1);
                long elapsed_time = intTime.getTimeInMillis() - new GregorianCalendar().getTimeInMillis();
                intent.putExtra("lost_time", elapsed_time < 0 ? -elapsed_time : 0);

                MainActivity.reScheduleAlarm(context.getApplicationContext(),
                        intTime.getTimeInMillis(),
                        intent);
            }

            // Checks if mCurrentInterruption hasn't been seen yet (not pending upload and not done)
            if (!mSharedPref.getBoolean(MainActivity.SP_UPLOAD_PENDING + filename, false)
                    && !mSharedPref.getBoolean(MainActivity.SP_UPLOAD_DONE + filename, false)) {

                // Check if it hasn't been missed (less than 15minutes of its display time)
                if (lost_time  < MainActivity.HOUR/4) {
                    Intent cancelIntent = new Intent(context.getApplicationContext(), CancelAlarm.class);
                    cancelIntent.putExtra("notificationId", mCurrentInterruption);
                    cancelIntent.putExtra("interruption", filename);
                    PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(context, mCurrentInterruption, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                    // Ask if the user can participate now --
                    Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    long[] pattern = {0, 150, 75, 150, 75, 150};
                    NotificationManager notificationManager =
                            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);  // Gets an instance of the NotificationManager service

                    Intent yesIntent = new Intent(context, ParticipationActivity.class);
                    yesIntent.putExtra("current_interruption", mCurrentInterruption);
                    yesIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                    Intent notNowIntent = new Intent(context, ParticipationNotNowActivity.class);
                    notNowIntent.putExtra("current_interruption", mCurrentInterruption);
                    notNowIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                    PendingIntent yesPendingIntent = PendingIntent.getActivity(context, mCurrentInterruption, yesIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    PendingIntent notNowPendingIntent = PendingIntent.getActivity(context, mCurrentInterruption, notNowIntent, PendingIntent.FLAG_UPDATE_CURRENT);

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

                    // Builds the notification and issues it.
                    notificationManager.notify(mCurrentInterruption, notification);

                    //Schedule alarm for 15 min after this notification is dispatched
                    // minus time it might have lost already
                    scheduleCancelAlarm(context, cancelIntent, MainActivity.HOUR / 4 - lost_time);
                } else {
                    // Interruption lost
                    // Increment missed counter
                    int missed = mSharedPref.getInt(MainActivity.SP_MISSED_INTERRUPTIONS, 0);
                    mEditor.putInt(MainActivity.SP_MISSED_INTERRUPTIONS, ++missed);

                    // set it as done, so it won't try to upload an non-existing file.
                    mEditor.putBoolean(MainActivity.SP_UPLOAD_DONE + filename, true);
                    mEditor.commit();
                }
            }
        }
    }

    private void scheduleCancelAlarm(Context context, Intent intent, long time){
        MainActivity.scheduleAlarm(context.getApplicationContext(),
                ((new GregorianCalendar()).getTimeInMillis() + time),
                intent);
    }
}