package com.jessica.masterproject;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import java.util.Arrays;

public class ParticipationNotNowActivity extends MotherActivity {
    private String[] mAnswers;
    private int mCurrentAnswer;
    private boolean mDone;
    private View mView;
    private SharedPreferences.Editor mEditor;
    private String mFilename;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_participation_not_now);

        SharedPreferences mSharedPref = getSharedPreferences(String.valueOf(R.string.preference_file), Context.MODE_PRIVATE);
        mEditor = mSharedPref.edit();

        int mCurrentInterruption = getIntent().getIntExtra("current_interruption", -1);

        NotificationManager notificationManager = (NotificationManager) getApplicationContext()
                .getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(mCurrentInterruption);

        mFilename = mCurrentInterruption + "_" + getString(R.string.interruption_filename);
    }

    private boolean readCheckBox(int viewId) {
        mAnswers[mCurrentAnswer++] = readCheckBox(mView, viewId);
        return true;
    }

    private boolean readAnswers() {
        mCurrentAnswer = 0;
        mAnswers = new String[4];
        Arrays.fill(mAnswers, "N/A");
        return  readCheckBox(R.id.notNow_situation)
                && readCheckBox(R.id.notNow_tired)
                && readCheckBox(R.id.notNow_too_much)
                && readCheckBox(R.id.notNow_will);
    }

    public void saveNotNow(View view) {
        mView = view.getRootView();

        readAnswers();
        mDone = true;

        requestSave(mFilename, mAnswers, false);
        mEditor.putBoolean(getString(R.string.upload_pending)
                + mFilename.substring(0, mFilename.length() - 4), true);
        mEditor.commit();
        finish();
    }

    @Override
    public void onStop() {
        if (!mDone) {
            String[] temp = new String[4];
            Arrays.fill(temp, "N/A");
            mEditor.putBoolean(getString(R.string.upload_pending)
                    + mFilename.substring(0, mFilename.length() - 4), true);
            mEditor.commit();
            requestSave(mFilename, temp, false);
        }
        super.onStop();
    }
}
