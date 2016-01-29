package com.jessica.masterproject.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jessica.masterproject.R;


public class PendingFragment extends Fragment {
    private View mView;
    private SharedPreferences mSharedPref;

    public PendingFragment() {
    }

    public static PendingFragment newInstance() {
        return new PendingFragment();
    }

    public void setupPending () {
        final ProgressBar scenarios = (ProgressBar) mView.findViewById(R.id.pending_scenarios);
        final ProgressBar demo = (ProgressBar) mView.findViewById(R.id.pending_demographics);
        final TextView missed = (TextView) mView.findViewById(R.id.missed);

        final boolean scenarios_pending = mSharedPref.getBoolean(getString(R.string.upload_pending) + "scenarios", false);
        final boolean scenarios_done    = mSharedPref.getBoolean(getString(R.string.upload_done) + "scenarios", false);
        final boolean demo_pending      = mSharedPref.getBoolean(getString(R.string.upload_pending) + "demographics", false);
        final boolean demo_done         = mSharedPref.getBoolean(getString(R.string.upload_done) + "demographics", false);
        final int missed_interruptions  =  mSharedPref.getInt(getString(R.string.missed_interruptions), 0);

        missed.setText(String.valueOf(missed_interruptions));

        scenarios.post(new Runnable() {
            @Override
            public void run() {
                if(scenarios_pending)
                    scenarios.setProgress(1);
                else if(scenarios_done)
                    scenarios.setProgress(2);
            }
        });

        demo.post(new Runnable() {
            @Override
            public void run() {
                if(demo_pending)
                    demo.setProgress(1);
                else if(demo_done)
                    demo.setProgress(2);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mSharedPref = getActivity().getSharedPreferences(String.valueOf(R.string.preference_file), Context.MODE_PRIVATE);

        mView = inflater.inflate(R.layout.pending, container, false);

        setupPending();
        return mView;
    }

    @Override
    public void onStop() {
        super.onStop();

    }
}
