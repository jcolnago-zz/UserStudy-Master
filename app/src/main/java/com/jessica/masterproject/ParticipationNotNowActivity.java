package com.jessica.masterproject;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ParticipationNotNowActivity extends MotherActivity {
    private List<String> mAnswers;
    private boolean mDone;
    private View mView;
    private SharedPreferences mSharedPref;
    private SharedPreferences.Editor mEditor;
    private String mFilename;
    private int mCurrentInterruption;
    private SimpleDateFormat mFormat = new SimpleDateFormat("dd MMMM 'às' HH:mm");;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_participation_not_now);

        mSharedPref = getSharedPreferences(String.valueOf(R.string.preference_file), Context.MODE_PRIVATE);
        mEditor = mSharedPref.edit();

        mCurrentInterruption = getIntent().getIntExtra("current_interruption", -1);

        NotificationManager notificationManager = (NotificationManager) getApplicationContext()
                .getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(mCurrentInterruption);

        mFilename = mCurrentInterruption + "_" + getString(R.string.interruption_filename);
        mAnswers = new ArrayList<>();
    }

    private boolean readCheckBox(int viewId) {
        boolean checked = false;
        if (((CheckBox) mView.findViewById(viewId)).isChecked()){
            checked = true;
            mAnswers.add(((CheckBox) mView.findViewById(viewId)).getText().toString());
        }
        return checked;
    }

    private boolean readCheckboxGroup(int[] checks) {
        boolean atLeastOne = false;
        for (int checkbox : checks) {
            if (readCheckBox(checkbox))
                atLeastOne = true;
        }

        if (!atLeastOne)
            Toast.makeText(getApplicationContext(), getString(R.string.missing_user_study_reason), Toast.LENGTH_SHORT).show();

        return atLeastOne;
    }

    private boolean readAnswers() {
        int[] checks = {R.id.not_now_social_expectation,
                R.id.not_now_activity_engagement,
                R.id.not_now_mood,
                R.id.not_now_frequency,
                R.id.not_now_other};

        if (Boolean.parseBoolean(readCheckBox(mView, R.id.not_now_other))){
            // Updates the list of dismissed interruptions
            Set<String> moreInfo = mSharedPref.getStringSet(getString(R.string.more_information_interruptions), new HashSet<String>());
            moreInfo.add(Integer.toString(mCurrentInterruption));
            mEditor.putStringSet(getString(R.string.more_information_interruptions), moreInfo);
        }

        return readCheckboxGroup(checks);
    }

    public void saveNotNow(View view) {
        String[] notNow = new String[2];
        Arrays.fill(notNow, "N/A");

        mView = view.getRootView();

        if (!readAnswers())
            return;
        mDone = true;

        notNow[0] = mFormat.format(new GregorianCalendar().getTime());
        if (!mAnswers.isEmpty())
            notNow[1] = TextUtils.join("; ", mAnswers);

        if (requestSave(mFilename, notNow, false)) {
            mEditor.putBoolean(getString(R.string.upload_pending)
                    + mFilename.substring(0, mFilename.length() - 4), true);
            mEditor.commit();
            finish();
        }
    }

    @Override
    public void onStop() {
        if (!mDone) {
            String[] notNow = new String[2];
            Arrays.fill(notNow, "N/A");

            notNow[0] = mFormat.format(new GregorianCalendar().getTime());

            if (requestSave(mFilename, notNow, false)) {
                mEditor.putBoolean(getString(R.string.upload_pending)
                        + mFilename.substring(0, mFilename.length() - 4), true);
                mEditor.commit();
            }
        }
        super.onStop();
    }
}
