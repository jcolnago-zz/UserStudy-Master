package com.jessica.masterproject;

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

import com.jessica.masterproject.alarms.DisplayInterruptionAlarm;
import com.jessica.masterproject.alarms.ActivityReminderAlarm;
import com.jessica.masterproject.alarms.ResetAlarm;
import com.jessica.masterproject.alarms.UploadQSAlarm;
import com.jessica.masterproject.alarms.UploadInterruptionAlarm;
import com.jessica.masterproject.fragments.QFinalFragment;
import com.jessica.masterproject.fragments.QInitialFragment;
import com.jessica.masterproject.fragments.DoneFragment;
import com.jessica.masterproject.fragments.PendingFragment;
import com.jessica.masterproject.fragments.ScenariosFragment;

import java.text.ParseException;

import java.util.Date;
import java.util.GregorianCalendar;

public class MainActivity extends MotherActivity {

    private SharedPreferences mSharedPreferences;
    private SectionsPagerAdapter mSectionsPagerAdapter;

    // OVERRIDE METHODS FROM MAIN ACTIVITY
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSharedPreferences = getSharedPreferences(SP_PREFERENCE_FILE, Context.MODE_PRIVATE);
        setContentView(R.layout.main_act);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mSectionsPagerAdapter.notifyDataSetChanged();

        // Set up the ViewPager with the sections adapter.
        ViewPager mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        requestMultiplePermissions();

        // Try to upload Questionnaire and Scenarios
          // Check to see if upload is necessary is within alarm's code
        scheduleAlarm(MainActivity.this,
                ((new GregorianCalendar()).getTimeInMillis()),
                new Intent(MainActivity.this, UploadQSAlarm.class));

        // Try to upload Interruptions
          // Check to see if upload is necessary is within alarm's code
        scheduleAlarm(MainActivity.this,
                ((new GregorianCalendar()).getTimeInMillis()),
                new Intent(MainActivity.this, UploadInterruptionAlarm.class));

        // Remind participant of finishing activities
        GregorianCalendar intTime = new GregorianCalendar();
        Intent reminderIntent = new Intent(MainActivity.this, ActivityReminderAlarm.class);
        try {
            // If before interruptions start, set time to remind users of initial activities
            if (new GregorianCalendar().getTime().before(FORMAT.parse(INTERRUPTIONS_START))) {
                intTime.setTime(FORMAT.parse(SCENARIO_ONE_START));
                if (new GregorianCalendar().getTime().before(FORMAT.parse(SCENARIO_ONE_REMINDER))) {
                    reminderIntent.putExtra("interval", 2 * HOUR);
                }
                else reminderIntent.putExtra("interval", HOUR / 2);
                if (intTime.getTimeInMillis()-(new GregorianCalendar()).getTimeInMillis() < 0) {
                    scheduleAlarm(MainActivity.this, (new GregorianCalendar()).getTimeInMillis() + HOUR / 6, reminderIntent);
                } else scheduleAlarm(MainActivity.this, intTime.getTimeInMillis(), reminderIntent);
            }
            // If after interruptions end, set time to remind users of finishing final activities
            else {
                intTime.setTime(FORMAT.parse(INTERRUPTIONS_END));
                if (new GregorianCalendar().getTime().before(FORMAT.parse(SCENARIO_TWO_REMINDER))) {
                    reminderIntent.putExtra("interval", 2 * HOUR);
                }
                else reminderIntent.putExtra("interval", HOUR / 2);
                if (intTime.getTimeInMillis()-(new GregorianCalendar()).getTimeInMillis() < 0) {
                    scheduleAlarm(MainActivity.this, (new GregorianCalendar()).getTimeInMillis() + HOUR / 6, reminderIntent);
                } else scheduleAlarm(MainActivity.this, intTime.getTimeInMillis(), reminderIntent);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //Set up alarm for resetting values for final activities
        try {
            intTime.setTime(FORMAT.parse(INTERRUPTIONS_END));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        scheduleAlarm(MainActivity.this,
                intTime.getTimeInMillis(),
                new Intent(MainActivity.this, ResetAlarm.class));

        // Set up alarm for the first interruption
        int lastInterruption = mSharedPreferences.getInt(SP_LAST_INTERRUPTION, 0);
        if (lastInterruption != INTERRUPTIONS) {
            Intent interruptionIntent = new Intent(MainActivity.this, DisplayInterruptionAlarm.class);
            String[] setupValues = getResources().getStringArray(R.array.interruptions);

            try {
                intTime.setTime(FORMAT.parse(setupValues[lastInterruption * 3 + 2]));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            interruptionIntent.putExtra("current_interruption", lastInterruption + 1);
            long elapsed_time = intTime.getTimeInMillis() - new GregorianCalendar().getTimeInMillis();
            interruptionIntent.putExtra("lost_time", elapsed_time < 0 ? -elapsed_time : 0);
            scheduleAlarm(MainActivity.this,
                    intTime.getTimeInMillis(),
                    interruptionIntent);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item);
    }

    // METHODS FROM MAIN ACTIVITY
    public void nextInitial (View view) {
        mSectionsPagerAdapter.nextQInitial();
    }

    public void nextFinal (View view) {
        mSectionsPagerAdapter.nextQFinal();
    }

    public void nextScenario(View view) {
        mSectionsPagerAdapter.nextScenario();
    }

    public void nextPending(View view) {
        mSectionsPagerAdapter.nextPending();
    }

    public void updatePending() {
        mSectionsPagerAdapter.updatePending();
    }

    // SUBCLASSES OF MAIN ACTIVITY

    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        private final FragmentManager mFragmentManager;
        private int mCurrentQuest = 0;
        private Fragment mQuestFragment;
        private Fragment mScenFragment;
        private Fragment mPendingFragment;
        private boolean mUpdateInitial = false;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            mFragmentManager = fm;

            if ((mSharedPreferences.getBoolean(SP_UPLOAD_PENDING + QUESTIONNAIRE_FILENAME, false)
                    || mSharedPreferences.getBoolean(SP_UPLOAD_DONE + QUESTIONNAIRE_FILENAME, false)))
                mQuestFragment =  DoneFragment.newInstance();
            else {
                try {
                    Date date = new GregorianCalendar().getTime();
                    if (date.before(FORMAT.parse(INTERRUPTIONS_END)))
                        mQuestFragment = QInitialFragment.newInstance(0);
                    else
                        mQuestFragment = QFinalFragment.newInstance(0);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            if ((mSharedPreferences.getBoolean(SP_UPLOAD_PENDING + SCENARIOS_FILENAME, false)
                    || mSharedPreferences.getBoolean(SP_UPLOAD_DONE + SCENARIOS_FILENAME, false)))
                mScenFragment = DoneFragment.newInstance();
            else {
                try {
                    Date date = new GregorianCalendar().getTime();
                    if ((date.after(FORMAT.parse(SCENARIO_ONE_START))
                            && date.before(FORMAT.parse(INTERRUPTIONS_START)))
                        || date.after(FORMAT.parse(INTERRUPTIONS_END)))
                        mScenFragment = ScenariosFragment.newInstance();
                    else mScenFragment = DoneFragment.newInstance();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            mPendingFragment = PendingFragment.newInstance();
        }

        public void nextQInitial() {
            QInitialFragment fragment = (QInitialFragment) mQuestFragment;
            if(!fragment.saveQuestionnaire())
                return;

            mUpdateInitial = true;
            mFragmentManager.beginTransaction().remove(mQuestFragment).commit();
            mQuestFragment = QInitialFragment.newInstance(++mCurrentQuest);
            notifyDataSetChanged();
        }

        public void nextQFinal() {
            QFinalFragment fragment = (QFinalFragment) mQuestFragment;
            if(!fragment.saveQuestionnaire())
                return;

            mUpdateInitial = true;
            mFragmentManager.beginTransaction().remove(mQuestFragment).commit();
            mQuestFragment = QFinalFragment.newInstance(++mCurrentQuest);
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

        public void nextPending() {
            ((PendingFragment) mPendingFragment).nextPending();
        }

        public void updatePending() {
            ((PendingFragment) mPendingFragment).setupPending();
        }

        @Override
        public Fragment getItem(int position) {
            switch(position)
            {
                case 1:
                    return mQuestFragment;
                case 2:
                    return mScenFragment;
                default:
                    return mPendingFragment;
            }
        }

        @Override
        public int getItemPosition(Object object) {
            if(object instanceof QInitialFragment) {
                if(mUpdateInitial) {
                    mUpdateInitial = false;
                    return POSITION_NONE;
                }
            } else if (object instanceof ScenariosFragment) {
                ScenariosFragment fragment = (ScenariosFragment) object;
                if(fragment.isDone())
                    return POSITION_NONE;
            } else if(object instanceof QFinalFragment) {
                if(mUpdateInitial) {
                    mUpdateInitial = false;
                    return POSITION_NONE;
                }
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
                    return "Questionário";
                case 2:
                    return "Cenários";
            }
            return null;
        }
    }
}

