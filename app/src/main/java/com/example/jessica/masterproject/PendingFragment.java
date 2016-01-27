package com.example.jessica.masterproject;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;


public class PendingFragment extends Fragment {
    private View mView;
    private SharedPreferences mSharedPref;
    private SharedPreferences.Editor mEditor;

    public PendingFragment() {
    }

    public static PendingFragment newInstance() {
        return new PendingFragment();
    }

    private void setupPending () {
        System.out.println("Setting up Pending");
        ProgressBar scenarios = (ProgressBar) mView.findViewById(R.id.pending_scenarios);
        ProgressBar demo = (ProgressBar) mView.findViewById(R.id.pending_demographics);

        //TODO:fix non updating progressbars
        if (mSharedPref.getBoolean(getString(R.string.upload_pending) + "scenarios", false)) {
            System.out.println("scenarios pending");
            scenarios.setProgress(1);
        }
        if (mSharedPref.getBoolean(getString(R.string.upload_done) + "scenarios", false)){
            System.out.println("scenarios done");
            scenarios.setProgress(2);
        }
        if (mSharedPref.getBoolean(getString(R.string.upload_pending) + "demographics", false)){
            System.out.println("demographics pending");
            demo.setProgress(1);
        }
        if (mSharedPref.getBoolean(getString(R.string.upload_done) + "demographics", false)){
            System.out.println("demographics done");
            demo.setProgress(2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mSharedPref = getActivity().getSharedPreferences(String.valueOf(R.string.preference_file), Context.MODE_PRIVATE);
        mEditor = mSharedPref.edit();

        mView  = inflater.inflate(R.layout.pending, container, false);
        setupPending();

        return mView;
    }

    @Override
    public void onStop() {
        super.onStop();

    }
}
