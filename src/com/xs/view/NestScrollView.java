package com.xs.view;

import android.content.Context;
import android.os.SystemClock;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ScrollView;
import com.example.TestScroll.R;

/**
 * NestScrollView用于研究scrollview嵌套listview之类的问题
 * <p/>
 * Created by xs on 14/12/19.
 */
public class NestScrollView extends ScrollView {

    private View mInner;

    private ListView mListView;

    private ViewPager mViewPager;

    private ViewGroup mViewGroup;

    private int mTouchSlop;

    private int mLastMotionY;

    private int mActivePointerId = -1;

    private boolean mFlag = true;

    public NestScrollView(Context context) {
        this(context, null);
    }

    public NestScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NestScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
    }

    private void initView() {
        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
    }

    @Override
    protected void onFinishInflate() {
        if (getChildCount() > 0) {
            mInner = getChildAt(0);
            mViewGroup = (ViewGroup) findViewById(R.id.pager);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mLastMotionY = (int) ev.getY();
                mActivePointerId = ev.getPointerId(0);
                break;
            case MotionEvent.ACTION_MOVE:
                final int activePointerIndex = ev.findPointerIndex(mActivePointerId);
                if (activePointerIndex == -1) {
                    break;
                }
                final int y = (int) ev.getY(activePointerIndex);
                int deltaY = mLastMotionY - y;
                boolean isSrollDown = false;
                if (deltaY > 0) {
                    isSrollDown = true;
                } else {
                    isSrollDown = false;
                }
                mLastMotionY = y;
                if (isSrollDown && !canScrollVertically1(1)) {
                    if (mFlag) {
                        MotionEvent event = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, ev.getX(), ev.getY(), 0);
                        mViewGroup.dispatchTouchEvent(event);
                    }

                    mFlag = false;
                    mViewGroup.dispatchTouchEvent(ev);
                } else {
                    mFlag = true;
                }
        }

        return super.dispatchTouchEvent(ev);
    }

    public boolean canScrollVertically1(int direction) {
        final int offset = computeVerticalScrollOffset();
        final int range = computeVerticalScrollRange() - computeVerticalScrollExtent();
        if (range == 0) return false;
        if (direction < 0) {
            return offset >= 0;
        } else {
            return offset < range;
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        if (!mFlag) {
            return false;
        } else {
            return super.onTouchEvent(ev);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        return true;
//        switch (action &  MotionEvent.ACTION_MASK) {
//            case MotionEvent.ACTION_DOWN:
//                mLastMotionY = (int) ev.getY();
//                mActivePointerId = ev.getPointerId(0);
//                break;
//            case MotionEvent.ACTION_MOVE:
//                final int activePointerIndex = ev.findPointerIndex(mActivePointerId);
//                Log.v("xs", "index--->" + activePointerIndex);
//                if (activePointerIndex == -1) {
//                    break;
//                }
//                final int y = (int) ev.getY(activePointerIndex);
//                int deltaY = mLastMotionY-y;
//                Log.v("xs", "move---->" + mLastMotionY + "--->" + y);
//                if (Math.abs(deltaY) > mTouchSlop) {
//                    if (deltaY > 0) {
//                        deltaY -= mTouchSlop;
//                    } else {
//                        deltaY += mTouchSlop;
//                    }
//                    Log.v("xs", "deltaY--->" + deltaY );
//                }
//                mLastMotionY = y;
//        }

//        Log.v("xs", "onInterceptTouchEvent--->" + ev.getAction());
        return super.onInterceptTouchEvent(ev);
    }
}



