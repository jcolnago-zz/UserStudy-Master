package com.example.jessica.masterproject;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Toast;

public class DemographicsFragment extends Fragment {
    private static final String ARG_TAB_NUMBER = "tab_number";
    public int mCurrentDemographic = 0;
    private int mCurrentAnswer = 0;
    private String[] mAnswers;
    private View mView;

    public DemographicsFragment() {
    }

    public static DemographicsFragment newInstance(int tabNumber) {
        DemographicsFragment fragment = new DemographicsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TAB_NUMBER, tabNumber);
        fragment.setArguments(args);
        return fragment;
    }

    private boolean readRadio(int viewId, String name) {
        RadioGroup group = (RadioGroup) mView.findViewById(viewId);
        int id = group.getCheckedRadioButtonId();
        if(id < 0) {
            Toast.makeText(getContext(), "Você deve selecionar uma opção para "+name, Toast.LENGTH_SHORT).show();
            return false;
        }

        int item = group.indexOfChild(mView.findViewById(id));
        mAnswers[mCurrentAnswer++] = Integer.toString(item);
        return true;
    }

    private boolean readCheckBox(int viewId) {
        CheckBox box = (CheckBox) mView.findViewById(viewId);
        mAnswers[mCurrentAnswer++] = Boolean.toString(box.isChecked());
        return true;
    }

    private boolean readSeekBar(int viewId) {
        SeekBar bar = (SeekBar) mView.findViewById(viewId);
        mAnswers[mCurrentAnswer++] = Integer.toString(bar.getProgress());
        return true;
    }

    private boolean readAnswers() {
        switch(mCurrentDemographic) {
            case 0:
                mAnswers = new String[13];
                return readRadio(R.id.sex, "Sexo")
                        && readRadio(R.id.age, "Faixa etária")
                        && readRadio(R.id.compKnow, "Conhecimento de Computadores")
                        && readCheckBox(R.id.fun)
                        && readCheckBox(R.id.work)
                        && readCheckBox(R.id.bank)
                        && readCheckBox(R.id.sn)
                        && readCheckBox(R.id.shop)
                        && readCheckBox(R.id.emails)
                        && readCheckBox(R.id.news)
                        && readCheckBox(R.id.study)
                        && readCheckBox(R.id.other)
                        && readRadio(R.id.time, "Tempo de Uso de Computador");
            case 1:
                mAnswers = new String[6];
                return readSeekBar(R.id.inttrust_1)
                        && readSeekBar(R.id.inttrust_2)
                        && readSeekBar(R.id.inttrust_3)
                        && readSeekBar(R.id.inttrust_4)
                        && readSeekBar(R.id.inttrust_5)
                        && readSeekBar(R.id.inttrust_6);
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

    public boolean saveDemographic() {
        if(!readAnswers())
            return false;

        ((MainActivity)getActivity()).requestSave(getString(R.string.demographic_filename), mAnswers, mCurrentDemographic != 0);
        return true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        int layout = 0;
        mCurrentDemographic = getArguments().getInt(ARG_TAB_NUMBER);

        switch(mCurrentDemographic) {
            case 0:
                layout = R.layout.demographics0;
                break;
            case 1:
                layout = R.layout.demographics1;
                break;
            case 2:
                layout = R.layout.demographics2;
                break;
            case 3:
                layout = R.layout.demographics3;
                break;
            case 4:
                layout = R.layout.done;
                break;
        }

        mView = inflater.inflate(layout, container, false);
        return mView;
    }
}