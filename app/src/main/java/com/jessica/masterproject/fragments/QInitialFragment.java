package com.jessica.masterproject.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jessica.masterproject.MainActivity;
import com.jessica.masterproject.R;

public class QInitialFragment extends Fragment {
    private static final String ARG_TAB_NUMBER = "tab_number";
    public int mCurrentQuestionnaire = 0;
    private int mCurrentAnswer = 0;
    private String[] mAnswers;
    private View mView;

    public QInitialFragment() {
    }

    public static QInitialFragment newInstance(int tabNumber) {
        QInitialFragment fragment = new QInitialFragment();
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

    private boolean readCheckBox(int viewId) {
        mAnswers[mCurrentAnswer++] = ((MainActivity)getActivity()).readCheckBox(mView, viewId);
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
                mAnswers = new String[13];
                return readRadio(R.id.gender_group, "Sexo")
                        && readRadio(R.id.age_group, "Faixa etária")
                        && readRadio(R.id.comp_know_group, "Conhecimento de computadores")
                        && readCheckBox(R.id.fun)
                        && readCheckBox(R.id.work)
                        && readCheckBox(R.id.banking)
                        && readCheckBox(R.id.SN)
                        && readCheckBox(R.id.shopping)
                        && readCheckBox(R.id.emails)
                        && readCheckBox(R.id.news)
                        && readCheckBox(R.id.study)
                        && readCheckBox(R.id.other)
                        && readRadio(R.id.time_group, "Tempo de uso de computador");
            case 1:
                mAnswers = new String[6];
                return readSeekBar(R.id.bar_inttrust1)
                        && readSeekBar(R.id.bar_inttrust2)
                        && readSeekBar(R.id.bar_inttrust3)
                        && readSeekBar(R.id.bar_inttrust4)
                        && readSeekBar(R.id.bar_inttrust5)
                        && readSeekBar(R.id.bar_inttrust6);
            case 2:
                mAnswers = new String[8];
                return readCheckBox(R.id.pb1)
                        && readCheckBox(R.id.pb2)
                        && readCheckBox(R.id.pb3)
                        && readCheckBox(R.id.pb4)
                        && readCheckBox(R.id.pb5)
                        && readSeekBar(R.id.westin1)
                        && readSeekBar(R.id.westin2)
                        && readSeekBar(R.id.westin3);
            case 3:
                mAnswers = new String[10];
                return readSeekBar(R.id.control1)
                        && readSeekBar(R.id.control2)
                        && readSeekBar(R.id.control3)
                        && readSeekBar(R.id.control4)
                        && readSeekBar(R.id.control5)
                        && readSeekBar(R.id.control6)
                        && readSeekBar(R.id.control7)
                        && readSeekBar(R.id.control8)
                        && readSeekBar(R.id.control9)
                        && readSeekBar(R.id.control10);
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

        switch(mCurrentQuestionnaire) {
            case 0:
                layout = R.layout.initial1;
                break;
            case 1:
                layout = R.layout.initial2;
                break;
            case 2:
                layout = R.layout.initial3;
                break;
            case 3:
                layout = R.layout.initial4;
                break;
            case 4:
                layout = R.layout.done;
                SharedPreferences mSharedPref = getActivity().getSharedPreferences(MainActivity.SP_PREFERENCE_FILE, Context.MODE_PRIVATE);
                SharedPreferences.Editor mEditor = mSharedPref.edit();
                String filename = MainActivity.QUESTIONNAIRE_FILENAME;
                if (!mSharedPref.getBoolean(MainActivity.SP_UPLOAD_DONE + filename, false)) {
                    mEditor.putBoolean(MainActivity.SP_UPLOAD_PENDING + filename, true);
                    mEditor.commit();
                    ((MainActivity) getActivity()).updatePending();
                }
                break;
        }

        mView = inflater.inflate(layout, container, false);
        return mView;
    }
}