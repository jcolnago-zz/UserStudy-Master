package com.jessica.masterproject.fragments;

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

import com.jessica.masterproject.MainActivity;
import com.jessica.masterproject.R;

import java.util.Arrays;

public class ScenariosFragment extends Fragment {
    private int mCurrentScenario = 0;
    private int mLastAnswered = 0;
    private String[] mAnswersChoices;
    private String[] mAnswersDecisions;
    private String[] mQuestions;
    private View mView;
    private SharedPreferences mSharedPref;
    private SharedPreferences.Editor mEditor;

    public ScenariosFragment() { }

    public static ScenariosFragment newInstance() {
        return new ScenariosFragment();
    }

    private void setupScenario () {
        final ProgressBar progress = (ProgressBar) mView.findViewById(R.id.progress);
        TextView scenario          = (TextView) mView.findViewById(R.id.scenario);
        RadioGroup choices         = (RadioGroup) mView.findViewById(R.id.choices);
        RadioGroup decision        = (RadioGroup) mView.findViewById(R.id.decision);

        if(mQuestions == null)
            mQuestions = getResources().getStringArray(R.array.scenarios);

        if(mAnswersChoices == null)
            mAnswersChoices = new String[mQuestions.length];

        if(mAnswersDecisions == null)
            mAnswersDecisions = new String[mQuestions.length];
        

        // If fragment has been newly initialized
        if (mCurrentScenario == 0) {
            mCurrentScenario = mSharedPref.getInt(MainActivity.SP_CURRENT_SCENARIO, mCurrentScenario);
            mLastAnswered = mCurrentScenario;
        }

        progress.post(new Runnable() {
            @Override
            public void run() {
                progress.setMax(mQuestions.length);
                progress.setProgress(mCurrentScenario+1);
            }
        });

        scenario.setText(mQuestions[mCurrentScenario]);
        choices.clearCheck();
        decision.clearCheck();
    }

    public void nextScenario () {
        int choice = ((MainActivity)getActivity()).readRadio(mView, R.id.choices, " o seu comportamento");
        int decision = ((MainActivity)getActivity()).readRadio(mView, R.id.decision, " a sua escolha");
        if (choice == -1 || decision == -1)
            return;

        mAnswersChoices[mCurrentScenario] = Integer.toString(choice);
        mAnswersDecisions[mCurrentScenario++] = Integer.toString(decision);
        if(mCurrentScenario == mQuestions.length) {
            if (((MainActivity)getActivity()).requestSave(MainActivity.SCENARIOS_FILENAME, MainActivity.FILE_FORMAT,
                    Arrays.copyOfRange(mAnswersChoices, mLastAnswered, mCurrentScenario), mLastAnswered!=0)
                    && ((MainActivity)getActivity()).requestSave(MainActivity.SCENARIOS_DECISIONS_FILENAME, MainActivity.FILE_FORMAT,
                        Arrays.copyOfRange(mAnswersDecisions, mLastAnswered, mCurrentScenario), mLastAnswered!=0)) {
                mEditor.putBoolean(MainActivity.SP_UPLOAD_PENDING + MainActivity.SCENARIOS_FILENAME, true);
                mEditor.putBoolean(MainActivity.SP_UPLOAD_PENDING + MainActivity.SCENARIOS_DECISIONS_FILENAME, true);
                mEditor.commit();
                ((MainActivity) getActivity()).updatePending();
            } else mCurrentScenario--;

        } else {
            mEditor.putInt(MainActivity.SP_CURRENT_SCENARIO, mCurrentScenario);
            mEditor.commit();
            setupScenario();
        }
    }

    public boolean isDone() {
        return (mSharedPref.getBoolean(MainActivity.SP_UPLOAD_PENDING + MainActivity.SCENARIOS_FILENAME, false)
                || mSharedPref.getBoolean(MainActivity.SP_UPLOAD_DONE+ MainActivity.SCENARIOS_FILENAME,false));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mSharedPref = getActivity().getSharedPreferences(MainActivity.SP_PREFERENCE_FILE, Context.MODE_PRIVATE);
        mEditor = mSharedPref.edit();

        if(!isDone()) {
            mView = inflater.inflate(R.layout.scenarios, container, false);
            setupScenario();
        } else  mView = inflater.inflate(R.layout.done, container, false);

        return mView;
    }

    @Override
    public void onStop() {
        if (mAnswersChoices!=null && mCurrentScenario < mAnswersChoices.length && mLastAnswered != mCurrentScenario) {
            if (((MainActivity)getActivity()).requestSave(MainActivity.SCENARIOS_FILENAME, MainActivity.FILE_FORMAT,
                    Arrays.copyOfRange(mAnswersChoices, mLastAnswered, mCurrentScenario), mLastAnswered!=0)
                    && ((MainActivity)getActivity()).requestSave(MainActivity.SCENARIOS_DECISIONS_FILENAME, MainActivity.FILE_FORMAT,
                    Arrays.copyOfRange(mAnswersDecisions, mLastAnswered, mCurrentScenario), mLastAnswered!=0)) {
                mLastAnswered = mCurrentScenario;
                mEditor.putInt(MainActivity.SP_CURRENT_SCENARIO, mCurrentScenario);
                mEditor.commit();
            }
        }
        super.onStop();
    }
}
