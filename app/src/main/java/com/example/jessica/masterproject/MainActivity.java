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

    public void nextDemographics(View view) {

        mSectionsPagerAdapter.nextDemographics();
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
        public int mDemTab = 0;
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
                case 1:
                    System.out.println("Placeholder demographics0");
                    layout = R.layout.demographics0;
                    break;
                case 2:
                    layout = R.layout.scenarios;
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

    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        private final FragmentManager mFragmentManager;
        private int mCurrentDemographic = 0;
        private Fragment mDemFragment = DemographicsFragment.newInstance(mCurrentDemographic);
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

        @Override
        public Fragment getItem(int position) {
            System.out.println("getItem " + position);
            if(position == 1) {
                return mDemFragment;
            }
            return PlaceholderFragment.newInstance(position);
        }

        @Override
        public int getItemPosition(Object object) {
            if(object instanceof DemographicsFragment) {
                DemographicsFragment fragment = (DemographicsFragment) object;
                if (fragment.mTabNumber != mCurrentDemographic)
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
