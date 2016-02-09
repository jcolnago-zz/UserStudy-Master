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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class MotherActivity extends AppCompatActivity {

    private static final int FILE_PERM = 0x46;
    private static final int REQUEST_PERMISSIONS = 0x50;

    public static final DateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    private String mFilename;
    private String[] mData;
    private boolean mAppend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case FILE_PERM:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    doSave();
                } else {
                    Toast.makeText(getApplicationContext(), "Você deve dar permissão para o estudo funcionar.", Toast.LENGTH_SHORT).show();
                    requestSave(mFilename, mData, mAppend);
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

    public boolean requestSave(String filename, String[] data, boolean append) {
        mFilename = filename;
        mData = data;
        mAppend = append;

        if (requestFilePermission()) {
            return doSave();
        }
        return false;
    }

    protected boolean doSave() {
        return FileSaver.save(mFilename, mData, mAppend);
    }

    protected void requestMultiplePermissions() {
        String locationPermission = Manifest.permission.ACCESS_COARSE_LOCATION;
        String storagePermission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        int hasLocPermission = ActivityCompat.checkSelfPermission(this, locationPermission);
        int hasStorePermission = ActivityCompat.checkSelfPermission(this, storagePermission);
        List<String> permissions = new ArrayList<>();
        if (hasLocPermission != PackageManager.PERMISSION_GRANTED) {
            permissions.add(locationPermission);
        }
        if (hasStorePermission != PackageManager.PERMISSION_GRANTED) {
            permissions.add(storagePermission);
        }
        if (!permissions.isEmpty()) {
            String[] params = permissions.toArray(new String[permissions.size()]);
            ActivityCompat.requestPermissions(this, params, REQUEST_PERMISSIONS);
        }
    }

    public static void scheduleAlarm(Context context, long time, Intent intentAlarm) {
        System.out.println("[LOG] scheduleAlarm initiated: " + time);
        // create the object
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        //set the alarm for particular time
        alarmManager.set(AlarmManager.RTC_WAKEUP, time, PendingIntent.getBroadcast(context, 1, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
    }

    public static void reScheduleAlarm(Context context, long time, Intent intentAlarm) {
        System.out.println("  [LOG] reScheduleAlarm initiated: " + time);
        scheduleAlarm(context, time, intentAlarm);
    }

    public int readRadio(View view, int viewId, String name) {
        RadioGroup group = (RadioGroup) view.findViewById(viewId);
        int id = group.getCheckedRadioButtonId();
        if (id < 0) {
            Toast.makeText(this, getString(R.string.missing_option) + name, Toast.LENGTH_SHORT).show();
            return -1;
        }

        return group.indexOfChild(view.findViewById(id));
    }

    public String readTextField(View view, int viewId) {
        EditText editText = (EditText) view.findViewById(viewId);
        String text = editText.getText().toString();
        if (text.equalsIgnoreCase("")) {
            Toast.makeText(this, getString(R.string.missing_answer), Toast.LENGTH_SHORT).show();
            return null;
        }
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