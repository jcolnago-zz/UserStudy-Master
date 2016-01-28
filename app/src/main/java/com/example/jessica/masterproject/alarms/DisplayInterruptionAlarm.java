package com.example.jessica.masterproject.alarms;

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
import android.service.notification.StatusBarNotification;
import android.support.v4.app.NotificationCompat;

import com.example.jessica.masterproject.MainActivity;
import com.example.jessica.masterproject.MotherActivity;
import com.example.jessica.masterproject.ParticipationNotNowActivity;
import com.example.jessica.masterproject.ParticipationYesActivity;
import com.example.jessica.masterproject.R;

import java.text.ParseException;
import java.util.GregorianCalendar;

public class DisplayInterruptionAlarm extends BroadcastReceiver {
    private SharedPreferences mSharedPref;
    private SharedPreferences.Editor mEditor;
    private int mCurrentInterruption;

    public DisplayInterruptionAlarm() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        mSharedPref = context.getSharedPreferences(String.valueOf(R.string.preference_file), Context.MODE_PRIVATE);
        mEditor = mSharedPref.edit();

        if (intent.getAction() != null && intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            int lastInterruption = mSharedPref.getInt(context.getString(R.string.last_interruption), 0);
            if (lastInterruption != MainActivity.INTERRUPTIONS) {
                GregorianCalendar intTime = new GregorianCalendar();
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
            System.out.println("Upload Display Interruption Alarm started!");
            mCurrentInterruption = intent.getIntExtra("current_interruption", 1);

            String filename = context.getString(R.string.interruption_filename);
            filename = mCurrentInterruption + "_" + filename.substring(0, filename.length()-4);

            // Checks if mCurrentInterruption hasn't been seen yet (not pending upload and not done)
            if (!mSharedPref.getBoolean(context.getString(R.string.upload_pending)+filename,false)
                    && !mSharedPref.getBoolean(context.getString(R.string.upload_done)+filename,false)) {

                // Ask if the user can participate now
                Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                long[] pattern = {0, 150, 75, 150, 75, 150};

                // TODO: check flags
                Intent yesIntent = new Intent(context, ParticipationYesActivity.class);
                yesIntent.putExtra("current_interruption", mCurrentInterruption);
                yesIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                // TODO: check flags and change to ParticipationNotNowActivity.class
                Intent notNowIntent = new Intent(context, ParticipationNotNowActivity.class);
                notNowIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                PendingIntent yesPendingIntent = PendingIntent.getActivity(context, 0, yesIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                PendingIntent notNowPendingIntent = PendingIntent.getActivity(context, 1, notNowIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(context)
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .setLights(Color.BLUE, 500, 500)
                                .setSound(alarmSound)
                                .setVibrate(pattern)
                                .setAutoCancel(true)
                                .setPriority(NotificationCompat.PRIORITY_MAX)
                                .setCategory(Notification.CATEGORY_ALARM)
                                .setContentTitle(context.getString(R.string.participation_request_title))
                                .setContentText(context.getString(R.string.participation_request_text))
                                .addAction(R.drawable.not_now_icon, context.getString(R.string.participation_request_not_now), notNowPendingIntent)
                                .addAction(R.drawable.yes_icon, context.getString(R.string.participation_request_yes), yesPendingIntent)
                                .setStyle(new NotificationCompat.InboxStyle())
                                .setContentIntent(yesPendingIntent);
                // Sets an ID for the notification
                int notificationId = mCurrentInterruption;
                // Gets an instance of the NotificationManager service
                NotificationManager notificationManager =
                        (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);

                // Builds the notification and issues it.
                notificationManager.notify(notificationId, mBuilder.build());

                // Updates last_interruption value to avoid having to have both notNow and Yes update it
                    // used in MainActivity
                mEditor.putInt(context.getString(R.string.last_interruption), mCurrentInterruption);

                // Sets up alarm for next interruption if there is one
                if (mCurrentInterruption+1 <= MainActivity.INTERRUPTIONS) {
                    intent.putExtra("current_interruption", mCurrentInterruption + 1);
                    String[] setupValues = context.getResources().getStringArray(R.array.interruptions);
                    GregorianCalendar intTime = new GregorianCalendar();

                    try {
                        intTime.setTime(MotherActivity.FORMAT.parse(setupValues[mCurrentInterruption * 3 + 2]));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    MainActivity.reScheduleAlarm(context.getApplicationContext(),
                            intTime.getTimeInMillis(),
                            intent);

                    System.out.println("Scheduling cancel alarm for: " + filename
                            + " in " + ((new GregorianCalendar()).getTimeInMillis()-intTime.getTimeInMillis()));
                    scheduleCancelAlarm(context, notificationId, filename);
                }
            }
        }
    }

    // Cancel notification and sets interruption as done (no file means N/As)
    private void scheduleCancelAlarm(Context context, int notificationId, String interruption){

        Intent interruptionIntent = new Intent(context.getApplicationContext(), CancelAlarm.class);
        interruptionIntent.putExtra("notificationId", notificationId);
        interruptionIntent.putExtra("interruption", interruption);

        // TODO: change back to Hour/6
        MainActivity.scheduleAlarm(context.getApplicationContext(),
                ((new GregorianCalendar()).getTimeInMillis() + MainActivity.HOUR / 60),
                interruptionIntent);
    }
}
