package com.jessica.masterproject.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jessica.masterproject.MainActivity;
import com.jessica.masterproject.R;

public class QFinalFragment extends Fragment {
    private static final String ARG_TAB_NUMBER = "tab_number";
    public int mCurrentQuestionnaire = 0;
    private int mCurrentAnswer = 0;
    private String[] mAnswers;
    private View mView;

    public QFinalFragment() {
    }

    public static QFinalFragment newInstance(int tabNumber) {
        QFinalFragment fragment = new QFinalFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TAB_NUMBER, tabNumber);
        fragment.setArguments(args);
        return fragment;
    }

    private boolean readRadio(int viewId, String name) {
        int item = ((MainActivity)getActivity()).readRadio(mView, viewId, name);
        if (item == -1)
            return false;

        mAnswers[mCurrentAnswer++] = Integer.toString(item);
        return true;
    }

    private boolean readTextField(int viewId) {
        String item = ((MainActivity)getActivity()).readTextField(mView, viewId);
        if (item == null) {
            item = "N/A";
        }
        mAnswers[mCurrentAnswer++] = item;
        return true;
    }

    private boolean readSeekBar(int viewId) {
        mAnswers[mCurrentAnswer++] = ((MainActivity)getActivity()).readSeekBar(mView, viewId);
        return true;
    }

    private boolean readAnswers() {
        mCurrentAnswer = 0;
        switch(mCurrentQuestionnaire) {
            case 0:
                mAnswers = new String[14];
                return readSeekBar(R.id.bar_relevance1)
                        && readSeekBar(R.id.bar_relevance2)
                        && readSeekBar(R.id.bar_relevance3)
                        && readSeekBar(R.id.bar_relevance4)
                        && readSeekBar(R.id.bar_relevance5)
                        && readSeekBar(R.id.bar_relevance6)
                        && readSeekBar(R.id.bar_relevance7)
                        && readSeekBar(R.id.bar_relevance8)
                        && readSeekBar(R.id.bar_relevance9)
                        && readSeekBar(R.id.bar_relevance10)
                        && readSeekBar(R.id.bar_relevance11)
                        && readSeekBar(R.id.bar_relevance12)
                        && readSeekBar(R.id.bar_relevance13)
                        && readSeekBar(R.id.bar_relevance14);
            case 1:
                mAnswers = new String[1];
                return readRadio(R.id.missed_group, " sua preferência");
            case 2:
                mAnswers = new String[5];
                return readSeekBar(R.id.bar_study_length)
                        && readRadio(R.id.interruption_group, " sua opinião sobre a quantidade de interrupções")
                        && readSeekBar(R.id.bar_interruption_low)
                        && readSeekBar(R.id.bar_interruption_medium)
                        && readSeekBar(R.id.bar_interruption_high);
            case 3:
                mAnswers = new String[2];
                return readTextField(R.id.opinion_text1)
                        && readTextField(R.id.opinion_text2);
        }
        return false;
    }

    public boolean saveQuestionnaire() {
        if(!readAnswers())
            return false;

        return ((MainActivity)getActivity()).requestSave(MainActivity.QUESTIONNAIRE_FILENAME,
                MainActivity.FILE_FORMAT, mAnswers, mCurrentQuestionnaire != 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        int layout = 0;
        mCurrentQuestionnaire = getArguments().getInt(ARG_TAB_NUMBER);
        SharedPreferences mSharedPref = getActivity().getSharedPreferences(MainActivity.SP_PREFERENCE_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor mEditor = mSharedPref.edit();

        switch(mCurrentQuestionnaire) {
            case 0:
                layout = R.layout.final1;
                break;
            case 1:
                layout = R.layout.final2;
                break;
            case 2:
                layout = R.layout.final3;
                break;
            case 3:
                layout = R.layout.final4;
                break;
            case 4:
                layout = R.layout.done;
                String filename = MainActivity.QUESTIONNAIRE_FILENAME;
                if (!mSharedPref.getBoolean(MainActivity.SP_UPLOAD_DONE + filename, false)) {
                    mEditor.putBoolean(MainActivity.SP_UPLOAD_PENDING + filename, true);
                    mEditor.commit();
                    ((MainActivity) getActivity()).updatePending();
                }
                break;
        }

        mView = inflater.inflate(layout, container, false);

        if (mCurrentQuestionnaire == 1) {
            TextView text = (TextView) mView.findViewById(R.id.missed);
            text.setText(String.format(getString(R.string.missed), mSharedPref.getInt(MainActivity.SP_MISSED_INTERRUPTIONS, 0)));
        }
        return mView;
    }
}