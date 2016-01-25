package com.example.jessica.masterproject;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
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
    private static final int FILE_PERM = 0x46;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private int mCurrentScenario = 0;
    private String mFilename;
    private String[] mData;
    private boolean mAppend;

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
            PlaceholderFragment fragment = new PlaceholderFragment();
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
                    layout = R.layout.welcome;
                    break;
                case 3:
                    layout = R.layout.pending;
                    break;
            }

            return inflater.inflate(layout, container, false);
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        private final FragmentManager mFragmentManager;
        private int mCurrentDemographic = 0;
        private Fragment mDemFragment = DemographicsFragment.newInstance(0);
        private Fragment mScenFragment = ScenariosFragment.newInstance();
        private boolean mUpdateDemographics = false;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            mFragmentManager = fm;
        }

        public void nextDemographics() {
            DemographicsFragment fragment = (DemographicsFragment) mDemFragment;
            if(!fragment.saveDemographic())
                return;

            mUpdateDemographics = true;
            mFragmentManager.beginTransaction().remove(mDemFragment).commit();
            mDemFragment = DemographicsFragment.newInstance(++mCurrentDemographic);
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
                if(mUpdateDemographics) {
                    mUpdateDemographics = false;
                    return POSITION_NONE;
                }
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

    public boolean requestFilePermission() {
        String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        int hasPermission = ActivityCompat.checkSelfPermission(this, permission);

        if(hasPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, FILE_PERM);
            return false;
        }
        return true;
    }

    public boolean requestSave(String filename, String[] data, boolean append){
        mFilename = filename;
        mData = data;
        mAppend = append;

        if (requestFilePermission())
            return doSave();
        return false;
    }

    public boolean doSave() {
        return FileSaver.save(mFilename, mData, mAppend);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        switch (requestCode) {
            case FILE_PERM:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    doSave();
                } else {
                    Toast.makeText(getApplicationContext(), "Você deve dar permissão para o estudo funcionar.", Toast.LENGTH_SHORT).show();
                    requestSave(mFilename, mData, mAppend);
                }
        }
    }
}
