package com.example.jessica.masterproject;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class ScenariosFragment extends Fragment {
    private boolean mDone = false;
    private int mCurrentScenario = 0;
    private String[] mAnswers;
    private String[] mQuestions;
    private View mView;

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
            mQuestions = getResources().getStringArray(R.array.coletaPerso1_p1_r);
            mAnswers = new String[mQuestions.length];
            progress.setMax(mQuestions.length);
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
        if(mCurrentScenario == 3) {//mQuestions.length) {
            mDone = true;
            ((MainActivity)getActivity()).requestSave("scenarios.csv", mAnswers, false);
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
        if(!mDone) {
            mView = inflater.inflate(R.layout.scenarios, container, false);
            setupScenario();
        } else {
            mView = inflater.inflate(R.layout.scenarios, container, false);
        }
        return mView;
    }
}
