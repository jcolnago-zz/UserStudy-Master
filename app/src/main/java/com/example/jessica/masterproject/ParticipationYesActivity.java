package com.example.jessica.masterproject;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;

public class ParticipationYesActivity extends MotherActivity {

    // Fullscreen activity variables
    private static final boolean AUTO_HIDE = true;
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    // Activity variables
    private String mCertainty;
    private SharedPreferences mSharedPref;
    private SharedPreferences.Editor mEditor;
    private String[] mSensitivity;
    private int mCurrentUserStudy;
    private int mCurrentInterruption;
    private int mCurrentAnswer = 0;
    private String[] mAnswers;
    private String[] setupValues;
    private View mView;
    private String mFilename;
    private boolean mDone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mCurrentUserStudy = 0;
        setupValues = getResources().getStringArray(R.array.interruptions);

        super.onCreate(savedInstanceState);

        mSharedPref = getSharedPreferences(String.valueOf(R.string.preference_file), Context.MODE_PRIVATE);
        mEditor = mSharedPref.edit();

        mCurrentInterruption = getIntent().getIntExtra("current_interruption", -1);

        NotificationManager notificationManager = (NotificationManager) getApplicationContext()
                .getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(mCurrentInterruption);

        mVisible = true;
        mFilename = mCurrentInterruption + "_" + getString(R.string.interruption_filename);
        setupUserStudy(mCurrentUserStudy);

    }

    private String getNextSensitivity(String[] sensitivities, String sensitivityLevel) {
        int startAt = -1;
        int i = 0;
        String value = null;
        boolean fromStart = true;

        // Fetch right last sensitivity used
        if (sensitivityLevel.equals("0"))
            startAt = mSharedPref.getInt(getString(R.string.last_low_sensitivity),-1);
        else if (sensitivityLevel.equals("1"))
            startAt = mSharedPref.getInt(getString(R.string.last_medium_sensitivity),-1);
        else
            startAt = mSharedPref.getInt(getString(R.string.last_high_sensitivity),-1);

        // If we tried once and couldn't find a scenario, return default
        if (startAt == -2) {
            if (sensitivityLevel.equals("0"))
                return getString(R.string.default_low_sensitivity);
            if (sensitivityLevel.equals("1"))
                return getString(R.string.default_medium_sensitivity);
            return getString(R.string.default_high_sensitivity);
        }

        // Try from last point
        for (i = startAt+1; i < sensitivities.length; i++) {
            // Set right last sensitivity used
            if (sensitivities[i].equals(sensitivityLevel)) {
                value = getResources().getStringArray(R.array.scenarios)[i];
                fromStart = false;
                break;
            }
        }

        // If unsuccessful, try from the beginning
        if (fromStart) {
            for (i = 0; i < startAt; i++) {
                // Set right last sensitivity used
                if (sensitivities[i].equals(sensitivityLevel)) {
                    value = getResources().getStringArray(R.array.scenarios)[i];
                    break;
                }
            }
        }

        // If no scenario was found, set default scenario and make sure we won't have to check again
        if (value == null) {
            i = -2;
            if (sensitivityLevel.equals("0"))
                value = getString(R.string.default_low_sensitivity);
            else if (sensitivityLevel.equals("1"))
                value = getString(R.string.default_medium_sensitivity);
            else
                value = getString(R.string.default_high_sensitivity);
        }

        if (sensitivityLevel.equals("0"))
            mEditor.putInt(getString(R.string.last_low_sensitivity),i);
        else if (sensitivityLevel.equals("1"))
            mEditor.putInt(getString(R.string.last_medium_sensitivity),i);
        else
            mEditor.putInt(getString(R.string.last_high_sensitivity),i);

        return value;
    }

    private String[] readSensitivityFile(){
        String scenariosFile = String.valueOf(Environment.getExternalStorageDirectory())
                + "/annoyme/" + getString(R.string.scenarios_filename);
        BufferedReader bufferedReader = null;
        String line = "";
        String[] sensitivity = null;

        try {
            bufferedReader = new BufferedReader(new FileReader(scenariosFile));
            while ((line = bufferedReader.readLine()) != null) {
                sensitivity = line.split(",");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return sensitivity;
    }

    public void nextUserStudy(View view) {
        mView = view.getRootView();

        if(!saveUserStudy())
            return;

        if(mCurrentUserStudy != 1) {
            setupUserStudy(++mCurrentUserStudy);
        } else {
            mEditor.putBoolean(getString(R.string.upload_pending)
                    + mFilename.substring(0, mFilename.length() - 4), true);
            mEditor.commit();
            finish();
        }
    }

    public boolean saveUserStudy() {
        if(!readAnswers())
            return false;

        if(mCurrentUserStudy == 1)
            mDone = true;

        return requestSave(mFilename, mAnswers, mCurrentUserStudy != 0);
    }

    private boolean readRadio(int viewId, String name) {
        int item = readRadio(mView, viewId, name);
        if (item == -1)
            return false;

        mAnswers[mCurrentAnswer++] = Integer.toString(item);
        return true;
    }

    private boolean readCheckBox(int viewId) {
        mAnswers[mCurrentAnswer++] = readCheckBox(mView, viewId);
        return true;
    }

    private boolean readTextField(int viewId) {
        String item = readTextField(mView, viewId);
        mAnswers[mCurrentAnswer++] = item;
        return true;
    }

    private boolean readAnswers() {
        mCurrentAnswer = 0;
        switch(mCurrentUserStudy) {
            case 0:
                mAnswers = new String[1];
                Arrays.fill(mAnswers, "N/A");
                return readRadio(R.id.us_choice, "Escolha");
            case 1:
                mAnswers = new String[19];
                Arrays.fill(mAnswers, "N/A");
                return readRadio(R.id.mood_group, "Humor")
                        && readRadio(R.id.busy_group, "Ocupado")
                        && readRadio(R.id.interacting_group, "Interação")
                        && readRadio(R.id.acceptance_group, "Aceitação")
                        && readCheckBox(R.id.do_not_care)
                        && readRadio(R.id.where_group, "Onde")
                        && readTextField(R.id.where_other_text)
                        && readCheckBox(R.id.what_studying)
                        && readCheckBox(R.id.what_shower)
                        && readCheckBox(R.id.what_in_class)
                        && readCheckBox(R.id.what_driving)
                        && readCheckBox(R.id.what_meeting)
                        && readCheckBox(R.id.what_shopping)
                        && readCheckBox(R.id.what_watching)
                        && readCheckBox(R.id.what_eating)
                        && readCheckBox(R.id.what_cooking)
                        && readCheckBox(R.id.what_talking)
                        && readCheckBox(R.id.what_other)
                        && readTextField(R.id.what_other_text);

        }
        return false;
    }

    public void setupUserStudy(int page){
        if (page == 0) {
            mDone = false;
            setContentView(R.layout.activity_participation_yes);
            mControlsView = findViewById(R.id.fullscreen_content_controls);
            mContentView = findViewById(R.id.fullscreen_content);

            if (mCurrentInterruption != -1) {
                if (setupValues != null) {
                    if (mSensitivity == null)
                        mSensitivity = readSensitivityFile();
                    ((TextView) findViewById(R.id.sensitivity)).setText(getNextSensitivity(
                            mSensitivity, setupValues[(mCurrentInterruption - 1) * 3]));

                    mCertainty = setupValues[1 + (mCurrentInterruption - 1) * 3];

                    ((TextView) findViewById(R.id.certainty)).setText(String.format(getString(R.string.certainty), mCertainty));

                } else System.err.println("Problem reading the interruptions file.");
            } else System.err.println("Current interruption extra was not added to intent.");
        } else {
            setContentView(R.layout.activity_participation_1);
            mControlsView = findViewById(R.id.fullscreen_content_controls_1);
            mContentView = findViewById(R.id.fullscreen_content_1);
            hide();
        }

        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.us_button).setOnTouchListener(mDelayHideTouchListener);
    }

    @Override
    public void onStop() {
        if (!mDone) {
            String[] temp = new String[20];
            Arrays.fill(temp, "N/A");
            if (mCurrentUserStudy == 1) {
                temp[0] = mAnswers[0];
            }
            mEditor.putBoolean(getString(R.string.upload_pending)
                    + mFilename.substring(0, mFilename.length() - 4), true);
            mEditor.commit();
            requestSave(mFilename, temp, mCurrentUserStudy != 0);
        }
        super.onStop();
    }

    // Fullscreen activity methods
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
}
