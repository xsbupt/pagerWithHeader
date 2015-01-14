package com.xs.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.OverScroller;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by xs on 14/12/8.
 */
public class SmoothListView extends ListView {

    private OverScroller mScroller;

    private int mLastY = 0;

    // 用户反射获取motionY
    private int mMotionY = 0;

    private boolean mDisable = false;

    public SmoothListView(Context context) {
        this(context, null);
    }

    public SmoothListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SmoothListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mScroller = new OverScroller(context);
        initParam();
    }

    private void initParam() {
        try {
            Class<AbsListView> cls = AbsListView.class;
            Field field = cls.getDeclaredField("mMotionY");
            field.setAccessible(true);
            Object obj = field.get(this);
            mMotionY = (Integer) obj;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setScroller(OverScroller scroller) {
        if (scroller == null) {
            return;
        }
        mScroller.fling(0, scroller.getCurrY(), 0, (int)scroller.getCurrVelocity(), 0, 0, 0, scroller.getFinalY());
        mLastY = scroller.getCurrY();
        setMotionY(mMotionY);
        post(mRunnable);
    }

    public void trackMotionScroll(int delta) {
        try {
            Class<AbsListView> cls = AbsListView.class;
            Method method = cls.getDeclaredMethod("trackMotionScroll", int.class, int.class);
            method.setAccessible(true);
            boolean result = (Boolean) method.invoke(this, delta, delta);
        } catch (Exception e) {
        }
    }

    public void setMotionY(int motionY) {
        mMotionY = motionY;
        try {
            Class<AbsListView> cls = AbsListView.class;
            Field field = cls.getDeclaredField("mMotionY");
            field.setAccessible(true);
            field.set(this, motionY);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {

            if (mScroller != null) {
                if (mScroller.computeScrollOffset()) {
                    int curY = mScroller.getCurrY();
                    if (curY - mLastY > 0) {
                        trackMotionScroll(mLastY-curY);
                        mLastY = curY;
                    }
                    post(this);
                }
            }
        }
    };

    public void setDisable(boolean disable) {
        mDisable = disable;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        boolean flag = super.onTouchEvent(ev);
        if (mDisable) {
            return true;
        }
        return flag;
    }

    /**
     * Check if the items in the list can be scrolled in a certain direction.
     *
     * @param direction Negative to check scrolling up, positive to check
     *            scrolling down.
     * @return true if the list can be scrolled in the specified direction,
     *         false otherwise.
     */
    public boolean canScrollList(int direction) {
        final int childCount = getChildCount();
        if (childCount == 0) {
            return false;
        }

        final int firstPosition = getFirstVisiblePosition();
        if (direction > 0) {
            final int lastBottom = getChildAt(childCount - 1).getBottom();
            final int lastPosition = firstPosition + childCount;
            return lastBottom < getCount() || lastBottom > getHeight() - getListPaddingBottom();
        } else {
            final int firstTop = getChildAt(0).getTop();
            return firstPosition > 0 || firstTop < getListPaddingTop();
        }
    }
}
