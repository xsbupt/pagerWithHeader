package com.xs.demo;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import com.example.TestScroll.R;
import com.xs.view.ViewPagerWithHeader;

/**
 * Created by xs on 14/12/11.
 */
public class DemoActivity extends FragmentActivity {

    private static final String[] CONTENT = new String[]{"A", "BBBBBB", "C", "DDDDDDD", "E", "FFFFFFF", "G", "HHHHHHHH"};

    private ViewPager mViewPager;

    private ViewPagerWithHeader mHeader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.demo);

        FragmentPagerAdapter adapter = new GoogleMusicAdapter(getSupportFragmentManager());

        mHeader = (ViewPagerWithHeader) findViewById(R.id.header);
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(adapter);

        mHeader.setIndicatorMargin(0);
        mHeader.setHeaderData(CONTENT);
        mHeader.setViewPager(mViewPager, null);
    }

    class GoogleMusicAdapter extends FragmentPagerAdapter {
        public GoogleMusicAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = TestFragment.newInstance(CONTENT[position % CONTENT.length]);
            return fragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return CONTENT[position % CONTENT.length].toUpperCase();
        }

        @Override
        public int getCount() {
            return CONTENT.length;
        }
    }


}
