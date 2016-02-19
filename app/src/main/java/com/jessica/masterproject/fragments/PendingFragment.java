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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jessica.masterproject.MainActivity;
import com.jessica.masterproject.R;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.GregorianCalendar;
import java.util.HashSet;


public class PendingFragment extends Fragment {
    private View mView;
    private HashSet<String> mInterruptionsPending;
    private String mCurrentInterruption;
    private SharedPreferences mSharedPref;
    private SharedPreferences.Editor mEditor;

    public PendingFragment() { }

    public static PendingFragment newInstance() {
        return new PendingFragment();
    }

    public void setupPending () {
        //Overall Information
        final ProgressBar initialProgress = (ProgressBar) mView.findViewById(R.id.initialProgress);
        final ProgressBar studyProgress   = (ProgressBar) mView.findViewById(R.id.studyProgress);
        final ProgressBar finalProgress   = (ProgressBar) mView.findViewById(R.id.finalProgress);
        final TextView missed             = (TextView) mView.findViewById(R.id.missedInterruptions);

        final int scenariosPending = mSharedPref.getBoolean(MainActivity.SP_UPLOAD_PENDING + MainActivity.SCENARIOS_FILENAME, false) ? 1 : 0;
        final int scenariosDone    = mSharedPref.getBoolean(MainActivity.SP_UPLOAD_DONE + MainActivity.SCENARIOS_FILENAME, false)? 1 : 0;
        final int questPending     = mSharedPref.getBoolean(MainActivity.SP_UPLOAD_PENDING + MainActivity.QUESTIONNAIRE_FILENAME, false)? 1 : 0;
        final int questDone        = mSharedPref.getBoolean(MainActivity.SP_UPLOAD_DONE + MainActivity.QUESTIONNAIRE_FILENAME, false)? 1 : 0;

        try {
            if (new GregorianCalendar().getTime().before(MainActivity.FORMAT.parse(MainActivity.INTERRUPTIONS_END))) {
                initialProgress.post(new Runnable() {
                    @Override
                    public void run() {
                        initialProgress.setMax(MainActivity.ACTIVITIES_PROGRESS);
                        initialProgress.setProgress(scenariosPending + 2*scenariosDone + questPending + 2*questDone);
                    }
                });
            } else {
                // There's nothing that can be done, so tell the user it was all done anyway
                initialProgress.post(new Runnable() {
                    @Override
                    public void run() {
                        initialProgress.setMax(MainActivity.ACTIVITIES_PROGRESS);
                        initialProgress.setProgress(MainActivity.ACTIVITIES_PROGRESS);
                    }
                });
                finalProgress.post(new Runnable() {
                    @Override
                    public void run() {
                        finalProgress.setMax(MainActivity.ACTIVITIES_PROGRESS);
                        finalProgress.setProgress(scenariosPending + 2*scenariosDone + questPending + 2*questDone);
                    }
                });
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        studyProgress.post(new Runnable() {
            @Override
            public void run() {
                studyProgress.setMax(MainActivity.INTERRUPTIONS);
                studyProgress.setProgress(mSharedPref.getInt(
                        MainActivity.SP_LAST_INTERRUPTION, 0));
            }
        });

        missed.post(new Runnable() {
            @Override
            public void run() {
                missed.setText(String.valueOf(mSharedPref.getInt(
                        MainActivity.SP_MISSED_INTERRUPTIONS, 0)));
            }
        });


        // Pending interruptions
        mInterruptionsPending = new HashSet<>(mSharedPref.getStringSet(MainActivity.SP_MORE_INFORMATION_INTERRUPTIONS, new HashSet<String>()));
        TextView text         = (TextView) mView.findViewById(R.id.pending_question);
        TextView helpText     = (TextView) mView.findViewById(R.id.pending_help);
        EditText answer       = (EditText) mView.findViewById(R.id.editText);
        Button next           = (Button) mView.findViewById(R.id.pending_next);

        answer.setText("");
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
            else System.err.println(String.format("[ERROR] Interruption file for %s is empty ", mCurrentInterruption));
        } else {
            text.setText(getString(R.string.pending_done));
            helpText.setVisibility(View.GONE);
            answer.setVisibility(View.GONE);
            next.setVisibility(View.GONE);
        }
    }

    private String[] readInterruptionFile(){
        String interruptionFile = String.valueOf(Environment.getExternalStorageDirectory())
                + "/annoyme/" + mCurrentInterruption + "_" 
                + MainActivity.INTERRUPTIONS_FILENAME + MainActivity.FILE_FORMAT;
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

        String filename = mCurrentInterruption + "_" + MainActivity.INTERRUPTIONS_FILENAME;
        if (((MainActivity)getActivity()).requestSave(filename, MainActivity.FILE_FORMAT, answer, true)) {
            mEditor.putBoolean(MainActivity.SP_UPLOAD_PENDING + filename, true);
            mInterruptionsPending.remove(mCurrentInterruption);
            mEditor.putStringSet(MainActivity.SP_MORE_INFORMATION_INTERRUPTIONS, (HashSet) mInterruptionsPending.clone());
            mEditor.commit();
        }

        setupPending();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mSharedPref = getContext().getSharedPreferences(MainActivity.SP_PREFERENCE_FILE, Context.MODE_PRIVATE);
        mEditor = mSharedPref.edit();

        mView = inflater.inflate(R.layout.pending, container, false);

        setupPending();
        return mView;
    }
}
