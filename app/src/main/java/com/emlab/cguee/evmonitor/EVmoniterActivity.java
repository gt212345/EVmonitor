package com.emlab.cguee.evmonitor;

import android.app.FragmentTransaction;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;

import com.google.android.gms.common.GooglePlayServicesUtil;


public class EVmoniterActivity extends FragmentActivity implements SurfaceHolder.Callback, DisplayFragment.OnFragmentInteractionListener {
    FragmentTransaction mFragmentTransaction = getFragmentManager()
            .beginTransaction();

    SectionsPagerAdapter mSectionsPagerAdapter;
    public static FragmentManager fragmentManager;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            switch (position) {
                case 0:
                    fragment = DisplayFragment.newInstance("test1","test2");
                    mFragmentTransaction.addToBackStack(null);
                    break;
                case 1:
                    fragment = DisplayFragment.newInstance("test1","test2");
                    mFragmentTransaction.addToBackStack(null);
                    break;
                case 2:
//                    mFragmentTransaction.addToBackStack(null);
                    break;
                case 3:
//                    mFragmentTransaction.addToBackStack(null);
                    break;
                case 4:
//                    mFragmentTransaction.addToBackStack(null);
                    break;
            }

            return fragment;
        }

        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return null;
                case 1:
                    return new String("Step 1");
//                case 2:
//                    return new String("Step 2");
//                case 3:
//                    return new String("Step 3");
//                case 4:
//                    return new String("Step 4");
                default:
                    return null;
            }
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evmoniter);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());
        Log.w("googleplayservice",""+GooglePlayServicesUtil.isGooglePlayServicesAvailable(this));

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.evmoniter, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

}
