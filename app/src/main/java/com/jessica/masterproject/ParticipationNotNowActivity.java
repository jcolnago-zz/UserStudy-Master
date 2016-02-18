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
    private String mFormat;
    private int mCurrentInterruption;
    private SimpleDateFormat mDataFormat = new SimpleDateFormat("dd MMMM 'às' HH:mm");;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.not_now);

        mSharedPref = getSharedPreferences(SP_PREFERENCE_FILE, Context.MODE_PRIVATE);
        mEditor = mSharedPref.edit();

        mCurrentInterruption = getIntent().getIntExtra("current_interruption", -1);

        NotificationManager notificationManager = (NotificationManager) getApplicationContext()
                .getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(mCurrentInterruption);

        mFilename = mCurrentInterruption + "_" + INTERRUPTIONS_FILENAME;
        mFormat = FILE_FORMAT;
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
            Set<String> moreInfo = new HashSet<>(mSharedPref.getStringSet(SP_MORE_INFORMATION_INTERRUPTIONS, new HashSet<String>()));
            moreInfo.add(Integer.toString(mCurrentInterruption));
            mEditor.putStringSet(SP_MORE_INFORMATION_INTERRUPTIONS, moreInfo);
            mEditor.commit();
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

        notNow[0] = mDataFormat.format(new GregorianCalendar().getTime());
        if (!mAnswers.isEmpty())
            notNow[1] = TextUtils.join("; ", mAnswers);

        if (requestSave(mFilename, mFormat, notNow, false)) {
            mEditor.putBoolean(SP_UPLOAD_PENDING + mFilename, true);
            mEditor.commit();
            finish();
        }
    }

    @Override
    public void onStop() {
        if (!mDone) {
            String[] notNow = new String[2];
            Arrays.fill(notNow, "N/A");

            notNow[0] = mDataFormat.format(new GregorianCalendar().getTime());

            if (requestSave(mFilename, mFormat, notNow, false)) {
                mEditor.putBoolean(SP_UPLOAD_PENDING + mFilename, true);
                mEditor.commit();
            }
        }
        super.onStop();
    }
}
