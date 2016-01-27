package com.example.jessica.masterproject;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    // TODO: check if alarms should be with a delay or in a specif time
    // UploadDSAlarm delay of 1 hour after startup, follow instructions in class for rescheduling
    // ReminderDSAlarm day before study begins at 10 am, follow instructions in class for rescheduling
    // UploadInterruptionAlarm first day of the study at 8pm
    // Remember to check if date - current time > 0, if not

    public static final long DAY = 1000*60*60*24;
    public static final long HOUR = 1000*60*60;
    public static final long MINUTE = 1000*60;

    private static final int FILE_PERM = 0x46;
    private static final int REQUEST_PERMISSIONS = 0x50;

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private String mFilename;
    private String[] mData;
    private boolean mAppend;

    // OVERRIDE METHODS FROM MAIN ACTIVITY
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mSectionsPagerAdapter.notifyDataSetChanged();

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        requestMultiplePermissions();

        scheduleAlarm(MainActivity.this, MINUTE, new Intent(MainActivity.this, UploadDSAlarm.class));
        //Not tested: scheduleAlarm(MainActivity.this, MINUTE/2, new Intent(MainActivity.this, UploadInterruptionAlarm.class));
        scheduleAlarm(MainActivity.this, -MINUTE, new Intent(MainActivity.this, ReminderDSAlarm.class));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
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


    // METHODS FROM MAIN ACTIVITY
    public void nextDemographics(View view) {
        mSectionsPagerAdapter.nextDemographics();
    }

    public void nextScenario(View view) {
        mSectionsPagerAdapter.nextScenario();
    }

    public boolean requestFilePermission() {
        String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        int hasPermission = ActivityCompat.checkSelfPermission(this, permission);

        if(hasPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, FILE_PERM);
            return false;
        }
        return true;
    }

    public boolean requestSave(String filename, String[] data, boolean append){
        mFilename = filename;
        mData = data;
        mAppend = append;

        if (requestFilePermission()) {
            return doSave();
        }
        return false;
    }

    public boolean doSave() {
        return FileSaver.save(mFilename, mData, mAppend, this);
    }

    public void requestMultiplePermissions() {
        String locationPermission = Manifest.permission.ACCESS_FINE_LOCATION;
        String storagePermission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        int hasLocPermission = ActivityCompat.checkSelfPermission(this, locationPermission);
        int hasStorePermission = ActivityCompat.checkSelfPermission(this, storagePermission);
        List<String> permissions = new ArrayList<String>();
        if (hasLocPermission != PackageManager.PERMISSION_GRANTED) {
            permissions.add(locationPermission);
        }
        if (hasStorePermission != PackageManager.PERMISSION_GRANTED) {
            permissions.add(storagePermission);
        }
        if (!permissions.isEmpty()) {
            String[] params = permissions.toArray(new String[permissions.size()]);
            ActivityCompat.requestPermissions(this, params, REQUEST_PERMISSIONS);
        } else {
            // We already have permission, so handle as normal
        }
    }

    private static void scheduleAlarm(Context context, long delayMili, Intent intentAlarm) {
        Long time = new GregorianCalendar().getTimeInMillis()+delayMili;

        // create the object
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        //set the alarm for particular time
        alarmManager.set(AlarmManager.RTC_WAKEUP, time, PendingIntent.getBroadcast(context, 1, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
        System.out.println("AlarmSet");
    }

    public static void reScheduleAlarm(Context context, long delayMili, Intent intentAlarm) {
        System.out.println("Rescheduling for: " + intentAlarm.getClass().toString() + " delay of " + delayMili);
        scheduleAlarm(context, delayMili, intentAlarm);
    }

    // SUBCLASSES OF MAIN ACTIVITY

    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        private final FragmentManager mFragmentManager;
        private int mCurrentDemographic = 0;
        private Fragment mDemFragment;
        private Fragment mScenFragment;
        private Fragment mPendingFragment;
        private boolean mUpdateDemographics = false;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            mFragmentManager = fm;

            SharedPreferences SharedPref = getSharedPreferences(String.valueOf(R.string.preference_file), Context.MODE_PRIVATE);

            if ((SharedPref.getBoolean(getString(R.string.upload_pending)
                    + getString(R.string.demographic_filename).substring(0, getString(R.string.demographic_filename).length() - 4), false)
                    || SharedPref.getBoolean(getString(R.string.upload_done)
                    + getString(R.string.demographic_filename).substring(0, getString(R.string.demographic_filename).length() - 4), false))) {
                mDemFragment = DemographicsFragment.newInstance(4);
            }
            else mDemFragment = DemographicsFragment.newInstance(0);

            if ((SharedPref.getBoolean(getString(R.string.upload_pending)
                    + getString(R.string.scenarios_filename).substring(0, getString(R.string.scenarios_filename).length() - 4), false)
                    || SharedPref.getBoolean(getString(R.string.upload_done)
                    + getString(R.string.scenarios_filename).substring(0, getString(R.string.scenarios_filename).length() - 4), false))) {
                mScenFragment = DoneFragment.newInstance();
            }
            else mScenFragment = ScenariosFragment.newInstance();

            mPendingFragment = PendingFragment.newInstance();
        }

        public void nextDemographics() {
            DemographicsFragment fragment = (DemographicsFragment) mDemFragment;
            if(!fragment.saveDemographic())
                return;

            mUpdateDemographics = true;
            mFragmentManager.beginTransaction().remove(mDemFragment).commit();
            mDemFragment = DemographicsFragment.newInstance(++mCurrentDemographic);
            notifyDataSetChanged();
        }

        public void nextScenario() {
            ScenariosFragment fragment = (ScenariosFragment) mScenFragment;
            fragment.nextScenario();
            System.out.println(fragment.isDone());

            if(fragment.isDone()) {
                mFragmentManager.beginTransaction().remove(mScenFragment).commit();
                mScenFragment = DoneFragment.newInstance();
                notifyDataSetChanged();
            }
        }

        @Override
        public Fragment getItem(int position) {
            switch(position)
            {
                case 1:
                    return mDemFragment;
                case 2:
                    return mScenFragment;
                default:
                    return mPendingFragment;
            }
        }

        @Override
        public int getItemPosition(Object object) {
            if(object instanceof DemographicsFragment) {
                if(mUpdateDemographics) {
                    mUpdateDemographics = false;
                    return POSITION_NONE;
                }
            } else if (object instanceof ScenariosFragment) {
                ScenariosFragment fragment = (ScenariosFragment) object;
                if(fragment.isDone())
                    return POSITION_NONE;
            }
            return POSITION_UNCHANGED;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "A Fazer";
                case 1:
                    return "Demog.";
                case 2:
                    return "Cenários";

            }
            return null;
        }
    }

}

