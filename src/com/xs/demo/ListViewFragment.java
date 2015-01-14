package com.xs.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.example.TestScroll.R;
import com.xs.view.SmoothListView;
import com.xs.view.SmoothScrollView;
import com.xs.view.ViewPagerWithHeader;

import java.util.ArrayList;

/**
 * Created by xs on 15/1/4.
 */
public class ListViewFragment extends Fragment {

    private ViewPager mViewPager;

    private SmoothScrollView mScrollView;

    public SmoothListView mListView;

    private ViewPagerWithHeader mHeader;

    static ArrayList<String> data = new ArrayList<String>();

    static {
        for (int i = 0; i < 30; i++) {
            data.add("here" + String.valueOf(i));
        }
    }

    public ListViewFragment(SmoothScrollView smoothScrollView, ViewPager viewPager, ViewPagerWithHeader header) {
        mScrollView = smoothScrollView;
        mViewPager = viewPager;
        mHeader = header;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mListView = new SmoothListView(getActivity());

        LinearLayout layout = new LinearLayout(getActivity());
        layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        layout.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layout.addView(mListView, params);

        mListView.setAdapter(mAdater);
        mScrollView.setListView(mListView, mViewPager, mHeader);
//        mAdater.notifyDataSetChanged();

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(ListViewFragment.this.getActivity(), DemoActivity.class);
                startActivity(intent);
                Log.v("xs", "item click---->" + position);
            }
        });

        mListView.setFocusable(false);


        return layout;
    }

    class Holder extends GridListVIewAdater.ViewHolder<String> implements Cloneable {

        private TextView title;

        @Override
        public GridListVIewAdater.ViewHolder newInstance() {
            return new Holder();
        }

        @Override
        public View createView(int index, LayoutInflater inflater) {
            View tempView = inflater.inflate(R.layout.list_item, null);
            title = (TextView) tempView.findViewById(R.id.title);
            return tempView;
        }

        @Override
        public void showData(int index, String data) {
            title.setText("--->" + data);
        }
    }

    private GridListVIewAdater mAdater = new GridListVIewAdater(1, data, new Holder()) {
        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }
    };


}
