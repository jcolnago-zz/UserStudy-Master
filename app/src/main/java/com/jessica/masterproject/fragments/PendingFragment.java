package com.jessica.masterproject.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
        final ImageView scenarios = (ImageView) mView.findViewById(R.id.scenario_image);
        final ImageView demo = (ImageView) mView.findViewById(R.id.demo_image);
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
                    scenarios.setImageResource(R.drawable.ic_done_black_24dp);
                else if(scenarios_done)
                    scenarios.setImageResource(R.drawable.ic_done_all_black_24dp);
            }
        });

        demo.post(new Runnable() {
            @Override
            public void run() {
                if(demo_pending)
                    demo.setImageResource(R.drawable.ic_done_black_24dp);
                else if(demo_done)
                    demo.setImageResource(R.drawable.ic_done_all_black_24dp);
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
