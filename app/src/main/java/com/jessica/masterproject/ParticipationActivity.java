package com.jessica.masterproject;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

public class ParticipationActivity extends MotherActivity {

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

        mFilename = mCurrentInterruption + "_" + getString(R.string.interruption_filename);
        setupUserStudy(mCurrentUserStudy);

    }

    private String getNextSensitivity(String[] sensitivities, String sensitivityLevel) {
        int startAt;
        int i;
        String value = null;
        boolean fromStart = true;

        // Fetch right last sensitivity used
        switch (sensitivityLevel) {
            case "0":
                startAt = mSharedPref.getInt(getString(R.string.last_low_sensitivity), -1);
                break;
            case "1":
                startAt = mSharedPref.getInt(getString(R.string.last_medium_sensitivity), -1);
                break;
            default:
                startAt = mSharedPref.getInt(getString(R.string.last_high_sensitivity), -1);
                break;
        }

        // If we tried once and couldn't find a scenario, return default
        // or if scenarios file wasn't created
        if (startAt == -2 || sensitivities == null) {
            if (sensitivityLevel.equals("0"))
                return getString(R.string.default_low_sensitivity);
            if (sensitivityLevel.equals("1"))
                return getString(R.string.default_medium_sensitivity);
            return getString(R.string.default_high_sensitivity);
        }

        i = startAt;

        // Try from last point
        for (i = i+1; i < sensitivities.length; i++) {
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
            switch (sensitivityLevel) {
                case "0":
                    value = getString(R.string.default_low_sensitivity);
                    break;
                case "1":
                    value = getString(R.string.default_medium_sensitivity);
                    break;
                default:
                    value = getString(R.string.default_high_sensitivity);
                    break;
            }
        }

        switch (sensitivityLevel) {
            case "0":
                mEditor.putInt(getString(R.string.last_low_sensitivity), i);
                break;
            case "1":
                mEditor.putInt(getString(R.string.last_medium_sensitivity), i);
                break;
            default:
                mEditor.putInt(getString(R.string.last_high_sensitivity), i);
                break;
        }

        mEditor.commit();
        return value;
    }

    private String[] readSensitivityFile(){
        String scenariosFile = String.valueOf(Environment.getExternalStorageDirectory())
                + "/annoyme/" + getString(R.string.scenarios_filename);
        BufferedReader bufferedReader;
        String line;
        String[] sensitivity = null;

        try {
            bufferedReader = new BufferedReader(new FileReader(scenariosFile));
            while ((line = bufferedReader.readLine()) != null) {
                sensitivity = line.split(",");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
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
            System.out.println("[LOG] ParticipationActivity: setting as pending: " + mFilename.substring(0, mFilename.length() - 4));
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
        if (item == null)
            item = "N/A";
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
            setContentView(R.layout.activity_participation);

            if (mCurrentInterruption != -1) {
                if (setupValues != null) {
                    if (mSensitivity == null)
                        mSensitivity = readSensitivityFile();
                    ((TextView) findViewById(R.id.sensitivity)).setText(getNextSensitivity(
                            mSensitivity, setupValues[(mCurrentInterruption - 1) * 3]));

                    String mCertainty = setupValues[1 + (mCurrentInterruption - 1) * 3];

                    ((TextView) findViewById(R.id.certainty)).setText(String.format(getString(R.string.certainty), mCertainty));

                } else System.err.println("Problem reading the interruptions file.");
            } else System.err.println("Current interruption extra was not added to intent.");
        } else {
            setContentView(R.layout.activity_participation_1);
        }
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
}
