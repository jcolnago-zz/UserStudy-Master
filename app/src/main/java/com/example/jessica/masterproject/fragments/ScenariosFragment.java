package com.example.jessica.masterproject.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.jessica.masterproject.MainActivity;
import com.example.jessica.masterproject.R;

import java.util.Arrays;

//TODO: uncomment mQuestions
public class ScenariosFragment extends Fragment {
    private int mCurrentScenario = 0;
    private int mLastAnswered = 0;
    private String[] mAnswers;
    private String[] mQuestions;
    private View mView;
    private SharedPreferences mSharedPref;
    private SharedPreferences.Editor mEditor;
    private String mFilename;

    public ScenariosFragment() {
    }

    public static ScenariosFragment newInstance() {
        return new ScenariosFragment();
    }

    private void setupScenario () {
        ProgressBar progress = (ProgressBar) mView.findViewById(R.id.progress);
        TextView question = (TextView) mView.findViewById(R.id.question);
        RadioGroup choices = (RadioGroup) mView.findViewById(R.id.choices);

        if(mQuestions == null){
            mQuestions = getResources().getStringArray(R.array.scenarios);
            mAnswers = new String[3];//mQuestions.length];
            progress.setMax(3);//mQuestions.length);
        }

        // Se tiver SharedPref for current_scenario use that value, else use default mCurrentScenario
        int temp = mSharedPref.getInt(getString(R.string.current_scenario), mCurrentScenario);
        if (temp != mCurrentScenario) {
            mLastAnswered = mCurrentScenario = temp;
        }

        progress.setProgress(mCurrentScenario);
        question.setText(mQuestions[mCurrentScenario]);
        choices.clearCheck();
    }

    public void nextScenario () {
        int choice = ((MainActivity)getActivity()).readRadio(mView, R.id.choices, "esse cenário");
        if (choice == -1)
            return;

        if(mCurrentScenario == 3) {//mQuestions.length) {
            if (((MainActivity)getActivity()).requestSave(getString(R.string.scenarios_filename),
                    Arrays.copyOfRange(mAnswers, mLastAnswered, mCurrentScenario), mLastAnswered!=0)) {
                mEditor.putBoolean(getString(R.string.upload_pending)
                        + mFilename.substring(0, mFilename.length() - 4), true);
                mEditor.commit();
            }
        } else {
            mAnswers[mCurrentScenario++] = Integer.toString(choice);
            mEditor.putInt(getString(R.string.current_scenario), mCurrentScenario);
            mEditor.commit();

            setupScenario();
        }
    }

    public boolean isDone() {
        return (mSharedPref.getBoolean(getString(R.string.upload_pending) + mFilename.substring(0, mFilename.length() - 4), false)
                || mSharedPref.getBoolean(getString(R.string.upload_done)+ mFilename.substring(0, mFilename.length() - 4),false));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mFilename = getString(R.string.scenarios_filename);
        mSharedPref = getActivity().getSharedPreferences(String.valueOf(R.string.preference_file), Context.MODE_PRIVATE);
        mEditor = mSharedPref.edit();
        if(!(mSharedPref.getBoolean(getString(R.string.upload_pending) + mFilename.substring(0, mFilename.length() - 4), false)
                || mSharedPref.getBoolean(getString(R.string.upload_done)+ mFilename.substring(0, mFilename.length() - 4),false))) {
            mView = inflater.inflate(R.layout.scenarios, container, false);
            setupScenario();
        } else {
            mView = inflater.inflate(R.layout.scenarios, container, false);
        }
        return mView;
    }

    @Override
    public void onStop() {
        if (mCurrentScenario != 3 && mLastAnswered!=mCurrentScenario) { //mQuestions.length)  {
            ((MainActivity)getActivity()).requestSave("scenarios.csv", Arrays.copyOfRange(mAnswers, mLastAnswered, mCurrentScenario), mLastAnswered!=0);
        }
        super.onStop();
    }
}
