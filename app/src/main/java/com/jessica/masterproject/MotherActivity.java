package com.jessica.masterproject;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import com.jessica.masterproject.helpers.FileSaver;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

public class MotherActivity extends AppCompatActivity {

    private static final int FILE_PERM = 0x46;
    private static final int REQUEST_PERMISSIONS = 0x50;

    public static final long DAY = 1000 * 60 * 60 * 24;
    public static final long HOUR = 1000 * 60 * 60;
    public static final int INTERRUPTIONS = 90;

    // related to the pending overallProgressBar. Do not change!
    public static final int ACTIVITIES_PROGRESS = 4;

    public static final String SCENARIO_ONE_START = "2016-02-27T08:00:00.000-0300";
    public static final String SCENARIO_ONE_REMINDER = "2016-02-28T12:00:00.000-0300";
    public static final String INTERRUPTIONS_START = "2016-02-29T08:00:00.000-0300";
    public static final String INTERRUPTIONS_END = "2016-03-10T08:00:00.000-0300";
    public static final String SCENARIO_TWO_REMINDER = "2016-03-11T12:00:00.000-0300";
    public static final DateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    public static final String SP_MORE_INFORMATION_INTERRUPTIONS = "com.annoyme.app.more_information_interruptions";
    public static final String SP_LAST_INTERRUPTION_UPLOADED = "com.annoyme.app.last_interruption_uploaded";
    public static final String SP_LAST_MEDIUM_SENSITIVITY = "com.annoyme.app.last_medium_sensitivity";
    public static final String SP_LAST_HIGH_SENSITIVITY = "com.annoyme.app.last_high_sensitivity";
    public static final String SP_LAST_LOW_SENSITIVITY = "com.annoyme.app.last_low_sensitivity";
    public static final String SP_MISSED_INTERRUPTIONS = "com.annoyme.app.missed_interruptions";
    public static final String SP_LAST_INTERRUPTION = "com.annoyme.app.last_interruption";
    public static final String SP_CURRENT_SCENARIO = "com.annoyme.app.current_scenario";
    public static final String SP_PREFERENCE_FILE = "com.annoyme.app.preference_file";
    public static final String SP_UPLOAD_PENDING = "com.annoyme.app.upload_pending_";
    public static final String SP_UPLOAD_DONE = "com.annoyme.app.upload_done_";
    public static final String SP_RESET = "com.annoyme.app.reset";

    public static final String INTERRUPTIONS_FILENAME = "interruption";
    public static final String FILE_FORMAT = ".csv";

    public static String SCENARIOS_DECISIONS_FILENAME = "scenarios_decisions";
    public static String QUESTIONNAIRE_FILENAME = "questionnaire";
    public static String SCENARIOS_FILENAME = "scenarios";

    private String mFilename;
    private String mFormat;
    private String[] mData;
    private boolean mAppend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            if (new GregorianCalendar().getTime().after(FORMAT.parse(INTERRUPTIONS_END))) {
                // This is the most stupid check I have ever had to use, but for some reason
                // it has happened that it did not re attributed the value to the variable when
                // class was instantiated. So this is a "better safe than sorry check"
                if (MainActivity.SCENARIOS_FILENAME.equals("scenarios")) {
                    MainActivity.SCENARIOS_DECISIONS_FILENAME = MainActivity.SCENARIOS_DECISIONS_FILENAME + "_final";
                    MainActivity.SCENARIOS_FILENAME = MainActivity.SCENARIOS_FILENAME + "_final";
                    MainActivity.QUESTIONNAIRE_FILENAME = MainActivity.QUESTIONNAIRE_FILENAME + "_final";
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case FILE_PERM:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    doSave();
                else {
                    Toast.makeText(getApplicationContext(), "Você deve dar permissão para o estudo funcionar.", Toast.LENGTH_SHORT).show();
                    requestSave(mFilename, mFormat, mData, mAppend);
                }
                break;
            case REQUEST_PERMISSIONS:
                break;
        }
    }

    protected boolean requestFilePermission() {
        String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        int hasPermission = ActivityCompat.checkSelfPermission(this, permission);

        if (hasPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, FILE_PERM);
            return false;
        }
        return true;
    }

    public boolean requestSave(String filename, String format, String[] data, boolean append) {
        mFilename = filename;
        mFormat = format;
        mData = data;
        mAppend = append;

        if (requestFilePermission())
            return doSave();
        return false;
    }

    protected boolean doSave() {
        return FileSaver.save(mFilename, mFormat, mData, mAppend);
    }

    protected void requestMultiplePermissions() {
        String storagePermission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        int hasStorePermission = ActivityCompat.checkSelfPermission(this, storagePermission);
        List<String> permissions = new ArrayList<>();
        if (hasStorePermission != PackageManager.PERMISSION_GRANTED)
            permissions.add(storagePermission);

        if (!permissions.isEmpty()) {
            String[] params = permissions.toArray(new String[permissions.size()]);
            ActivityCompat.requestPermissions(this, params, REQUEST_PERMISSIONS);
        }
    }

    public static void scheduleAlarm(Context context, long time, Intent intentAlarm) {
        // create the object
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        //set the alarm for particular time
        alarmManager.set(AlarmManager.RTC_WAKEUP, time, PendingIntent.getBroadcast(context, 1, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
    }

    public static void reScheduleAlarm(Context context, long time, Intent intentAlarm) {
        scheduleAlarm(context, time, intentAlarm);
    }

    public int readRadio(View view, int viewId, String name) {
        RadioGroup group = (RadioGroup) view.findViewById(viewId);
        int id = group.getCheckedRadioButtonId();
        if (id < 0) {
            Toast.makeText(getApplicationContext(), getString(R.string.missing_option) + name, Toast.LENGTH_SHORT).show();
            return -1;
        }
        return group.indexOfChild(view.findViewById(id));
    }

    public String readTextField(View view, int viewId) {
        EditText editText = (EditText) view.findViewById(viewId);
        String text = editText.getText().toString();
        if (text.equalsIgnoreCase(""))
            return null;
        return text;
    }

    public String readCheckBox(View view, int viewId) {
        CheckBox box = (CheckBox) view.findViewById(viewId);
        return Boolean.toString(box.isChecked());
    }

    public String readSeekBar(View view, int viewId) {
        SeekBar bar = (SeekBar) view.findViewById(viewId);
        return Integer.toString(bar.getProgress());
    }
}