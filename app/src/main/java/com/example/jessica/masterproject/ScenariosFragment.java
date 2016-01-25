package com.example.jessica.masterproject;

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
import android.widget.Toast;

import java.util.Arrays;

public class ScenariosFragment extends Fragment {
    private boolean mDone = false;
    private int mCurrentScenario = 0;
    private int mLastAnswered = 0;
    private String[] mAnswers;
    private String[] mQuestions;
    private View mView;
    private SharedPreferences mSharedPref;
    private SharedPreferences.Editor mEditor;

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
            mAnswers = new String[mQuestions.length];
            progress.setMax(mQuestions.length);
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

        RadioGroup choices = (RadioGroup) mView.findViewById(R.id.choices);
        int selectedId = choices.getCheckedRadioButtonId();

        if(selectedId < 0) {
            //TODO: put string into resource
            Toast.makeText(getContext(), "Você deve selecionar uma opção", Toast.LENGTH_SHORT).show();
            return;
        }

        int choice = choices.indexOfChild(mView.findViewById(selectedId));
        mAnswers[mCurrentScenario] = Integer.toString(choice);

        mCurrentScenario++;

        mEditor.putInt(getString(R.string.current_scenario), mCurrentScenario);
        mEditor.commit();

        if(mCurrentScenario == 3) {//mQuestions.length) {
            ((MainActivity)getActivity()).requestSave(getString(R.string.scenarios_filename), Arrays.copyOfRange(mAnswers, mLastAnswered, mCurrentScenario), mLastAnswered!=0);
            mDone = true;
        } else {
            setupScenario();
        }
    }

    public boolean isDone() {
        return mDone;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mSharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        mEditor = mSharedPref.edit();
        if(!mDone) {
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