package com.example.jessica.masterproject;

import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private int mCurrentScenario = 0;

    public void nextDemographics(View view) {
        mSectionsPagerAdapter.nextDemographics();
    }

    public void nextScenario(View view) {
        mSectionsPagerAdapter.nextScenario();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mSectionsPagerAdapter.notifyDataSetChanged();

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    public static class PlaceholderFragment extends Fragment {
        private static final String ARG_TAB_NUMBER = "tab_number";
        public PlaceholderFragment() {
        }

        public static PlaceholderFragment newInstance(int tabNumber) {
            System.out.println("newInstance "+tabNumber);
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_TAB_NUMBER, tabNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            System.out.println("onCreateView");
            int layout = 0;
            Bundle args = getArguments();
            switch(args.getInt(ARG_TAB_NUMBER)) {
                case 0:
                    layout = R.layout.welcome;
                    break;
                case 3:
                    layout = R.layout.pending;
                    break;
            }

            return inflater.inflate(layout, container, false);
        }
    }

    public static class DemographicsFragment extends Fragment {
        private static final String ARG_TAB_NUMBER = "tab_number";
        public int mTabNumber = 0;
        public DemographicsFragment() {
        }

        public static DemographicsFragment newInstance(int tabNumber) {
            System.out.println("newInstance "+tabNumber);
            DemographicsFragment fragment = new DemographicsFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_TAB_NUMBER, tabNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            int layout = 0;
            Bundle args = getArguments();
            switch(args.getInt(ARG_TAB_NUMBER)) {
                case 0:
                    layout = R.layout.demographics0;
                    break;
                case 1:
                    layout = R.layout.demographics1;
                    break;
                case 2:
                    layout = R.layout.demographics2;
                    break;
                // TODO: return proper fragments below
                // TODO: maybe make a mDone flag like with Scenarios?
                case 3:
                    //layout = R.layout.demographics3;
                    break;
                case 4:
                    //layout = R.layout.demographics4;
                    break;
                case 5:
                    //layout = R.layout.done;
                    break;
            }

            return inflater.inflate(layout, container, false);
        }
    }

    public static class ScenariosFragment extends Fragment {
        private boolean mDone = false;
        private int mCurrentScenario = 0;
        private int[] mAnswers;
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
                mAnswers = new int[mQuestions.length];
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
                Toast.makeText(getContext(), "Você deve selecionar uma opção (CHANGE ME)", Toast.LENGTH_SHORT).show();
                return;
            }

            int choice = choices.indexOfChild(mView.findViewById(selectedId));
            mAnswers[mCurrentScenario] = choice;

            mCurrentScenario++;
            if(mCurrentScenario == mQuestions.length) {
                mDone = true;
                // TODO: save file
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

    public static class DoneFragment extends Fragment {

        public DoneFragment() {
        }

        public static DoneFragment newInstance() {
            return new DoneFragment();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // TODO: return proper done fragment
            return null;//inflater.inflate(R.layout.done, container, false);
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        private final FragmentManager mFragmentManager;
        private int mCurrentDemographic = 0;
        private Fragment mDemFragment = DemographicsFragment.newInstance(mCurrentDemographic);
        private Fragment mScenFragment = ScenariosFragment.newInstance();

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            mFragmentManager = fm;
        }

        public void nextDemographics() {
            mCurrentDemographic++;
            mFragmentManager.beginTransaction().remove(mDemFragment).commit();
            mDemFragment = DemographicsFragment.newInstance(mCurrentDemographic);
            notifyDataSetChanged();
        }

        public void nextScenario() {
            ScenariosFragment fragment = (ScenariosFragment) mScenFragment;
            fragment.nextScenario();

            if(fragment.isDone()) {
                mFragmentManager.beginTransaction().remove(mScenFragment).commit();
                mScenFragment = DoneFragment.newInstance();
                notifyDataSetChanged();
            }
        }

        @Override
        public Fragment getItem(int position) {
            switch(position)
            {
                case 1:
                    return mDemFragment;
                case 2:
                    return mScenFragment;
                default:
                    return PlaceholderFragment.newInstance(position);
            }
        }

        @Override
        public int getItemPosition(Object object) {
            if(object instanceof DemographicsFragment) {
                DemographicsFragment fragment = (DemographicsFragment) object;
                if (fragment.mTabNumber != mCurrentDemographic)
                    return POSITION_NONE;
            } else if (object instanceof ScenariosFragment) {
                ScenariosFragment fragment = (ScenariosFragment) object;
                if(fragment.isDone())
                    return POSITION_NONE;
            }
            return POSITION_UNCHANGED;
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Bem Vindo";
                case 1:
                    return "Demog.";
                case 2:
                    return "Cenários";
                case 3:
                    return "A Fazer";
            }
            return null;
        }
    }
}
