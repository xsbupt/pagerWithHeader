package com.xs.demo;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.ViewTreeObserver;
import com.example.TestScroll.R;
import com.xs.view.SmoothScrollView;
import com.xs.view.ViewPagerWithHeader;

/**
 * Created by xs on 15/1/4.
 */
public class NestActivity extends FragmentActivity {

    private static final String[] CONTENT = new String[]{"A", "BBBBBB", "C", "DDDDDDD", "E", "FFFFFFF", "G", "HHHHHHHH"};

    private SmoothScrollView mScrollView;

    private ViewPagerWithHeader mHeader;

    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test);

        mScrollView = (SmoothScrollView) findViewById(R.id.scrollView);
        mHeader = (ViewPagerWithHeader) findViewById(R.id.header);
        mViewPager = (ViewPager) findViewById(R.id.pager);

        ViewTreeObserver viewTreeObserver = mScrollView.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mScrollView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    Log.v("xs", "height---->" + (mScrollView.getHeight() - 60));
                    //mViewPager.getLayoutParams().height = mScrollView.getHeight() - 60;
                }
            });
        }

        mHeader.setIndicatorMargin(0);
        mHeader.setHeaderData(CONTENT);
        mHeader.setViewPager(mViewPager, null);

        FragmentPagerAdapter adapter = new GoogleMusicAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(adapter);

//        mScrollView.smoothScrollTo(0, 0);
    }


    class GoogleMusicAdapter extends FragmentPagerAdapter {
        public GoogleMusicAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                ListViewFragment fragment = new ListViewFragment(mScrollView, mViewPager, mHeader);
                mScrollView.scrollTo(0, 0);
                return fragment;
            }

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
