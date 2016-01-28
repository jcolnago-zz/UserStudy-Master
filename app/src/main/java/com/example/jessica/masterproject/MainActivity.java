package com.example.jessica.masterproject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.example.jessica.masterproject.alarms.DisplayInterruptionAlarm;
import com.example.jessica.masterproject.alarms.ReminderDSAlarm;
import com.example.jessica.masterproject.alarms.UploadDSAlarm;
import com.example.jessica.masterproject.alarms.UploadInterruptionAlarm;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

public class MainActivity extends MotherActivity {
    // UploadDSAlarm delay of 1 hour after startup, follow instructions in class for rescheduling
    // ReminderDSAlarm day before study begins at 10 am, follow instructions in class for rescheduling
    // UploadInterruptionAlarm first day of the study at 8pm
    // Remember to check if date - current time > 0, if not

    public static final long DAY = 1000*60*60*24;
    public static final long HOUR = 1000*60*60;
    public static final long MINUTE = 1000*60;
    public static final int INTERRUPTIONS = 2;

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private SharedPreferences mSharedPref;

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
        mSharedPref = getSharedPreferences(String.valueOf(R.string.preference_file), Context.MODE_PRIVATE);

        // Try to upload Demographics and Scenarios after 2 hours of starting the activity
        // TODO: set this back to 2*HOUR
        System.out.println("Setting Upload DS Alarm: " + (new GregorianCalendar()).getTimeInMillis()+HOUR/60);
        scheduleAlarm(MainActivity.this,
                (new GregorianCalendar()).getTimeInMillis()+HOUR/60,
                new Intent(MainActivity.this, UploadDSAlarm.class));

        // Remind participant of finishing Demographics and Scenario the day before the start of the study
        GregorianCalendar intTime = new GregorianCalendar();
        try {
            intTime.setTime(MotherActivity.FORMAT.parse("2016-01-31T11:00:00.000-0300"));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        System.out.println("Setting Reminder DS Alarm: " + intTime.getTimeInMillis());
        scheduleAlarm(MainActivity.this,
                intTime.getTimeInMillis(),
                new Intent(MainActivity.this, ReminderDSAlarm.class));

        // Set up alarm for the first interruption
        int lastInterruption = mSharedPref.getInt(getString(R.string.last_interruption), 0);
        if (lastInterruption != INTERRUPTIONS) {
            Intent interruptionIntent = new Intent(MainActivity.this, DisplayInterruptionAlarm.class);
            interruptionIntent.putExtra("current_interruption", lastInterruption + 1);

            String[] setupValues = getResources().getStringArray(R.array.interruptions);

            try {
                intTime.setTime(MotherActivity.FORMAT.parse(setupValues[lastInterruption * 3 + 2]));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            System.out.println("Setting Display Interruption Alarm: " + intTime.getTimeInMillis());
            scheduleAlarm(MainActivity.this,
                    intTime.getTimeInMillis(),
                    interruptionIntent);
        }

        // Set alarm to upload interruptions 30s from start of first interruption
        System.out.println("Setting Upload Interruption Alarm: " + (intTime.getTimeInMillis() + MINUTE / 2));
        scheduleAlarm(MainActivity.this,
                intTime.getTimeInMillis() + MINUTE / 2,
                new Intent(MainActivity.this, UploadInterruptionAlarm.class));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    // METHODS FROM MAIN ACTIVITY
    public void nextDemographics(View view) {
        mSectionsPagerAdapter.nextDemographics();
    }

    public void nextScenario(View view) {
        mSectionsPagerAdapter.nextScenario();
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

