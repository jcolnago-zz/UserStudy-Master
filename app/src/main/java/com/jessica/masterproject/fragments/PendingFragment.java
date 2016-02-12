package com.jessica.masterproject.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jessica.masterproject.MainActivity;
import com.jessica.masterproject.R;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


public class PendingFragment extends Fragment {
    private View mView;
    private SharedPreferences mSharedPref;
    private SharedPreferences.Editor mEditor;
    private Set<String> mInterruptionsPending;
    private String mCurrentInterruption;

    public PendingFragment() {
    }

    public static PendingFragment newInstance() {
        return new PendingFragment();
    }

    public void setupPending () {
        final ImageView scenarios = (ImageView) mView.findViewById(R.id.scenarioImage);
        final ImageView demo = (ImageView) mView.findViewById(R.id.demoImage);

        final boolean scenarios_pending = mSharedPref.getBoolean(getString(R.string.upload_pending) + "scenarios", false);
        final boolean scenarios_done    = mSharedPref.getBoolean(getString(R.string.upload_done) + "scenarios", false);
        final boolean demo_pending      = mSharedPref.getBoolean(getString(R.string.upload_pending) + "demographics", false);
        final boolean demo_done         = mSharedPref.getBoolean(getString(R.string.upload_done) + "demographics", false);

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

        mInterruptionsPending = mSharedPref.getStringSet(getString(R.string.dismissed_interruptions), new HashSet<String>());
        TextView text = (TextView) mView.findViewById(R.id.pending_question);
        TextView helpText = (TextView) mView.findViewById(R.id.pending_help);
        EditText answer = (EditText) mView.findViewById(R.id.editText);
        Button next = (Button) mView.findViewById(R.id.pending_next);

        if (!mInterruptionsPending.isEmpty()) {
            mCurrentInterruption = mInterruptionsPending.iterator().next();
            String[] data = readInterruptionFile();

            if (data != null) {
                final ProgressBar progress = (ProgressBar) mView.findViewById(R.id.progressBar);
                progress.post(new Runnable() {
                    @Override
                    public void run() {
                        progress.setMax(mInterruptionsPending.size());
                        progress.setProgress(1);
                    }
                });

                helpText.setVisibility(View.VISIBLE);
                answer.setVisibility(View.VISIBLE);
                next.setVisibility(View.VISIBLE);

                String date = data[0];
                text.setText(String.format(getString(R.string.pending_question), date));

                if (!data[1].equals("N/A")) {
                    helpText.setText(String.format(getString(R.string.pending_help), data[1]));
                } else helpText.setVisibility(View.GONE);
            }
        } else {
            text.setText(getString(R.string.pending_done));
            helpText.setVisibility(View.GONE);
            answer.setVisibility(View.GONE);
            next.setVisibility(View.GONE);
        }
    }

    private String[] readInterruptionFile(){
        String interruptionFile = String.valueOf(Environment.getExternalStorageDirectory())
                + "/annoyme/" + mCurrentInterruption + "_" + getString(R.string.interruption_filename);
        BufferedReader bufferedReader;
        String line;
        String[] info = null;

        try {
            bufferedReader = new BufferedReader(new FileReader(interruptionFile));
            while ((line = bufferedReader.readLine()) != null) {
                info = line.split(",");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return info;
    }

    public void nextPending () {
        String[] answer = new String[1];
        answer[0] = ((MainActivity)getActivity()).readTextField(mView, R.id.editText);

        // Do not accept empty answer
        if (answer[0] == null) {
            Toast.makeText(getActivity(), getString(R.string.missing_answer), Toast.LENGTH_SHORT).show();
            return;
        }

        String filename = mCurrentInterruption + "_" + getString(R.string.interruption_filename);
        if (((MainActivity)getActivity()).requestSave(filename,answer, true)) {
            mEditor.putBoolean(getString(R.string.upload_pending)
                    + filename.substring(0, filename.length() - 4), true);
            mInterruptionsPending.remove(mCurrentInterruption);
            mEditor.putStringSet(getString(R.string.dismissed_interruptions), mInterruptionsPending);
            mEditor.commit();
        }

        setupPending();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mSharedPref = getActivity().getSharedPreferences(String.valueOf(R.string.preference_file), Context.MODE_PRIVATE);
        mEditor = mSharedPref.edit();
        mView = inflater.inflate(R.layout.pending, container, false);

        setupPending();
        return mView;
    }

    @Override
    public void onResume() {
        setupPending();
        super.onResume();
    }
}
