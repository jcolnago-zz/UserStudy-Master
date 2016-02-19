package com.jessica.masterproject;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

public class ParticipationActivity extends MotherActivity {

    private String[] mSensitivity;
    private int mCurrentUserStudy;
    private int mCurrentInterruption;
    private int mCurrentAnswer = 0;
    private String[] mAnswers;
    private String[] setupValues;
    private View mView;
    private String mFilename;
    private String mFormat;
    private boolean mDone;
    private String mChoice;
    private SharedPreferences mSharedPref;
    private SharedPreferences.Editor mEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mCurrentUserStudy = 0;
        setupValues = getResources().getStringArray(R.array.interruptions);

        super.onCreate(savedInstanceState);

        mSharedPref = getSharedPreferences(SP_PREFERENCE_FILE, Context.MODE_PRIVATE);
        mEditor = mSharedPref.edit();

        mCurrentInterruption = getIntent().getIntExtra("current_interruption", -1);

        NotificationManager notificationManager = (NotificationManager) getApplicationContext()
                .getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(mCurrentInterruption);

        mFilename = mCurrentInterruption + "_" + INTERRUPTIONS_FILENAME;
        mFormat = FILE_FORMAT;
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
                startAt = mSharedPref.getInt(SP_LAST_LOW_SENSITIVITY, -1);
                break;
            case "1":
                startAt = mSharedPref.getInt(SP_LAST_MEDIUM_SENSITIVITY, -1);
                break;
            default:
                startAt = mSharedPref.getInt(SP_LAST_HIGH_SENSITIVITY, -1);
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
                mEditor.putInt(SP_LAST_LOW_SENSITIVITY, i);
                break;
            case "1":
                mEditor.putInt(SP_LAST_MEDIUM_SENSITIVITY, i);
                break;
            default:
                mEditor.putInt(SP_LAST_HIGH_SENSITIVITY, i);
                break;
        }

        mEditor.commit();
        return value;
    }

    private String[] readSensitivityFile(){
        String scenariosFile = String.valueOf(Environment.getExternalStorageDirectory())
                + "/annoyme/" + SCENARIOS_FILENAME + FILE_FORMAT;
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

        if(mCurrentUserStudy != 1)
            setupUserStudy(++mCurrentUserStudy);
        else {
            mEditor.putBoolean(SP_UPLOAD_PENDING + mFilename, true);
            mEditor.commit();
            finish();
        }
    }

    public boolean saveUserStudy() {
        if(!readAnswers())
            return false;

        if(mCurrentUserStudy == 1)
            mDone = true;
        else {
            if (mAnswers[0].equals("0"))
                mChoice = getString(R.string.choose).toLowerCase();
            else
                mChoice = getString(R.string.delegate).toLowerCase();
        }

        return requestSave(mFilename, mFormat, mAnswers, mCurrentUserStudy != 0);
    }

    private boolean readRadio(int viewId, String name) {
        int item = readRadio(mView, viewId, name);
        if (item == -1)
            return false;

        mAnswers[mCurrentAnswer++] = Integer.toString(item);
        return true;
    }

    private boolean readCheckBox(int viewId) {
        String answer = readCheckBox(mView, viewId);
        mAnswers[mCurrentAnswer++] = answer;
        return Boolean.parseBoolean(answer);
    }

    private boolean readCheckboxGroup(int[] checks) {
        boolean atLeastOne = false;
        for (int checkbox : checks) {
            if (readCheckBox(checkbox))
                atLeastOne = true;
        }
        return atLeastOne;
    }

    private boolean readTextField(int viewId) {
        String item = readTextField(mView, viewId);
        if (item == null) {
            mAnswers[mCurrentAnswer++] = "N/A";
            return false;
        }
        mAnswers[mCurrentAnswer++] = item;
        return true;
    }

    private boolean readReason(int[] checks, int text) {
        boolean checkOption = readCheckboxGroup(checks);
        boolean checkText = readTextField(text);

        if (!checkOption && !checkText){
            Toast.makeText(getApplicationContext(), R.string.missing_user_study_reason, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean readAnswers() {
        mCurrentAnswer = 0;
        switch(mCurrentUserStudy) {
            case 0:
                mAnswers = new String[1];
                Arrays.fill(mAnswers, "N/A");
                return readRadio(R.id.user_study_choice_group, "Escolha");
            case 1:
                mAnswers = new String[10];
                Arrays.fill(mAnswers, "N/A");
                int[] checks = {R.id.user_study_data, R.id.user_study_requester, R.id.user_study_motive, R.id.user_study_certainty};
                return readRadio(R.id.user_study_mood_group, "Humor")
                        && readRadio(R.id.user_study_activity_group, "Ocupado")
                        && readRadio(R.id.user_study_engagement_group, "Interação")
                        && readRadio(R.id.user_study_social_acceptance_group, "Aceitação")
                        && readRadio(R.id.user_study_frequency_group, "Frequência")
                        && readReason(checks, R.id.user_study_other_text);
        }
        return false;
    }

    public void setupUserStudy(int page){
        if (page == 0) {
            mDone = false;
            setContentView(R.layout.lets_see1);

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
            setContentView(R.layout.lets_see2);
            ((TextView) findViewById(R.id.user_study_question)).setText(
                    String.format(getString(R.string.user_study_question), mChoice));
        }
    }

    @Override
    public void onStop() {
        if (!mDone) {
            String[] temp = new String[11];
            Arrays.fill(temp, "N/A");
            if (mCurrentUserStudy == 1) {
                temp[0] = mAnswers[0];
            }
            mEditor.putBoolean(SP_UPLOAD_PENDING + mFilename, true);
            requestSave(mFilename, mFormat, temp, mCurrentUserStudy != 0);
            mEditor.commit();
        }
        super.onStop();
    }
}
