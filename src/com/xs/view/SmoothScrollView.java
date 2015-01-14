package com.xs.view;

import android.content.Context;
import android.os.SystemClock;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.OverScroller;
import android.widget.ScrollView;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by xs on 14/12/5.
 */
public class SmoothScrollView extends ScrollView {

    private ViewPager mViewPager;

    private SmoothListView mListView;

    private ViewPagerWithHeader mHeader;

    private boolean mOriInterupt = true;

    private boolean mInterupt = true;

    private int mPaddingBottom;

    private int mPaddingTop;

    private OverScroller mScroller;

    private OverScroller mSelftScroller;

    private boolean isOver = false;

    // enable list view scroller
    private boolean mEnableListViewScroller = false;

    // use to imitate the clicked
    private boolean mImitateClicked = false;

    // diable scrollview
    private boolean mDisable = false;

    private boolean mHorizontalEnable = false;

    private int mLastMotionX;

    private int mLastMotionY;

    private int mTouchSlop;

    private boolean mIsBeingDragged = false;

    private boolean mInsideHeader = false;

    private boolean mInsideViewPager = false;

    // 模拟viewpager的滑动
    private boolean mViewPagerImitate = false;

    private float mImitateY = 0;

    // 表示模拟了touch事件
    private boolean mImitateTouch = true;

    public SmoothScrollView(Context context) {
        this(context, null);
    }

    public SmoothScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SmoothScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
    }

    public void setListView(SmoothListView view, ViewPager viewPager, ViewPagerWithHeader header) {
        this.mListView = view;
        mViewPager = viewPager;
        mHeader = header;
    }

    private void initView() {
        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();

        mSelftScroller = new OverScroller(getContext());
        try {
            Class<ScrollView> cls = ScrollView.class;

            Field field = cls.getDeclaredField("mScroller");
            field.setAccessible(true);
            Object obj = field.get(this);
            mScroller = (OverScroller) obj;

            field = cls.getDeclaredField("mPaddingBottom");
            field.setAccessible(true);
            mPaddingBottom = (Integer) field.get(this);

            field = cls.getDeclaredField("mPaddingTop");
            field.setAccessible(true);
            mPaddingTop = (Integer) field.get(this);

        } catch (Exception e) {
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mLastMotionX = (int) ev.getX();
                mLastMotionY = (int) ev.getY();
                mOriInterupt = mInterupt;
                mHorizontalEnable = false;
                mIsBeingDragged = false;
                mViewPagerImitate = false;

                int[] location = new int[2];
                this.getLocationOnScreen(location);

                int[] temp = new int[2];
                mHeader.getLocationOnScreen(temp);

                int[] tempLoc = new int[2];
                mViewPager.getLocationOnScreen(tempLoc);

                if (ev.getY() > temp[1]-location[1]+mHeader.getHeight() && canScrollDown(1)) {
                    mInsideViewPager = true;
                    mInsideHeader = false;
                    mImitateTouch = true;
                    mImitateY = ev.getY()-(tempLoc[1]-location[1]);
                    MotionEvent event = imitateMotionEvent(MotionEvent.ACTION_DOWN, ev.getX(), mImitateY);
                    mListView.onTouchEvent(event);
                } else if (ev.getY() > temp[1]-location[1] && canScrollDown(1)) {
                    mInsideViewPager = false;
                    mInsideHeader = true;
                    mImitateTouch = true;
                    mImitateY = ev.getY()-(temp[1]-location[1]);
                    MotionEvent event = imitateMotionEvent(MotionEvent.ACTION_DOWN, ev.getX(), mImitateY);
                    mHeader.dispatchTouchEvent(event);
                } else {
                    mInsideViewPager = false;
                    mInsideHeader = false;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                final int x = (int) ev.getX();
                final int y = (int) ev.getY();
                int deltaX = mLastMotionX - x;
                int deltaY = mLastMotionY - y;

                // 判断是向左还是向右移动
                if (!mIsBeingDragged && (Math.abs(deltaX) > mTouchSlop || Math.abs(deltaY) > mTouchSlop)) {
                    mIsBeingDragged = true;
                    // 左右的位移大于上下的位移，且view
                    if (Math.abs(deltaX) > Math.abs(deltaY) && canScrollDown(1)) {
                        mHorizontalEnable = true;
                        if (deltaX > 0) {
                            deltaX -= mTouchSlop;
                        } else {
                            deltaX += mTouchSlop;
                        }
                    } else {
                        if (deltaY > 0) {
                            deltaY -= mTouchSlop;
                        } else {
                            deltaY += mTouchSlop;
                        }
                    }
                }

                if (mIsBeingDragged) {
                    // 如果有模拟action down的事件，在move事件中，当在viewpager中时，需要模拟cancel事件
                    if (mImitateTouch) {
                        if (mInsideViewPager) {
                            MotionEvent event = imitateMotionEvent(MotionEvent.ACTION_CANCEL, ev.getX(), mImitateY);
                            mListView.onTouchEvent(event);
                        }
                        mImitateTouch = false;
                    }

                    boolean isSrollDown;
                    if (deltaY > 0) {
                        isSrollDown = true;
                    } else {
                        isSrollDown = false;
                    }

                    Log.v("xs", "isSrollDown--->" + isSrollDown);

                    if (mInsideHeader && mHorizontalEnable) {
                        // 不能向下滑动了
                        if (canScrollDown(1)) {
                            mDisable = true;
                            MotionEvent event = imitateMotionEvent(MotionEvent.ACTION_MOVE, ev.getX(), mImitateY);
                            mHeader.dispatchTouchEvent(event);
                            mLastMotionX = x;
                        }
                        break;
                    }

                    if (mInsideViewPager && mHorizontalEnable) {
                        // 不能向下滑动了
                        if (canScrollDown(1)) {
                            mDisable = true;
                            if (!mViewPagerImitate) {
                                mViewPager.beginFakeDrag();
                                mViewPagerImitate = true;
                            }
                            mViewPager.fakeDragBy(-deltaX);
                            mLastMotionX = x;
                        }
                        break;
                    }


                    if (!isSrollDown) {
                        // 从上往下滑，同时listview不能继续滑动
                        if (!mListView.canScrollList(-1)) {
                            mInterupt = true;
                            mDisable = false;
                            if (!mOriInterupt) {
                                scrollBy(0, deltaY);
                            }
                            mListView.setDisable(true);
                        } else {
                            mDisable = true;
                            mListView.setDisable(false);
                            if (mImitateClicked) {
                                mListView.dispatchTouchEvent(ev);
                            }
                        }
                    } else {
                        // 从下往上滑，同时scrollview不能继续滑动
                        if (!canScrollDown(1)) {
                            mListView.setDisable(false);
                            if (mOriInterupt && mInterupt) {
                                MotionEvent event = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, ev.getX(), ev.getY(), 0);
                                mListView.dispatchTouchEvent(event);
                                mImitateClicked = true;
                            }
                            if (mOriInterupt) {
                                mListView.dispatchTouchEvent(ev);
                            }
                            mInterupt = false;
                            mDisable = true;
                        } else {
                            mInterupt = true;
                            mDisable = false;
                            if (canScrollDown(-1) && !mOriInterupt) {
                                scrollBy(0, deltaY);
                            }
                            mListView.setDisable(true);
                        }
                    }
                    mLastMotionY = y;
                    mLastMotionX = x;
                }
                break;
            case MotionEvent.ACTION_UP:
                mOriInterupt = mInterupt;

                int[] temp1 = new int[2];
                this.getLocationOnScreen(temp1);

                int[] temp2 = new int[2];
                mHeader.getLocationOnScreen(temp2);

                int[] temp3 = new int[2];
                mViewPager.getLocationOnScreen(temp3);

                if (mImitateClicked) {
                    mListView.dispatchTouchEvent(ev);
                    mImitateClicked = false;
                }
                // 如果是在header部分，都需要最后模拟一个up动作
                if (mInsideHeader) {
                    MotionEvent event = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, ev.getX(), mImitateY, 0);
                    mHeader.dispatchTouchEvent(event);
                }
                if (mInsideViewPager && !mIsBeingDragged) {
                    Log.v("xs", "I am here---->" + mIsBeingDragged);
                    MotionEvent event = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, ev.getX(), ev.getY()-(temp3[1]-temp1[1]), 0);
                    mListView.onTouchEvent(event);
                }
                if (mViewPagerImitate) {
                    mViewPager.endFakeDrag();
                    mViewPagerImitate = false;
                }
                break;
        }

        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mDisable) {
            return true;
        } else {
            return super.onTouchEvent(ev);
        }
    }

    /**
     * direction < 0 判断是否能向上滑动
     * direction > 0 判断是否能向下滑动
     */
    private boolean canScrollDown(int direction) {
        final int offset = computeVerticalScrollOffset();
        final int range = computeVerticalScrollRange() - computeVerticalScrollExtent();
        if (range == 0) return false;
        if (direction < 0) {
            return offset >= 0;
        } else {
            return offset < range;
        }
    }

    private MotionEvent imitateMotionEvent(int action, float x, float y) {
        MotionEvent event = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), action, x, y, 0);
        return event;
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mInterupt;
    }

    public void fling(int velocityY) {
        if (getChildCount() > 0) {
            int height = getHeight() - mPaddingBottom - mPaddingTop;
            int bottom = getChildAt(0).getHeight();

            mScroller.fling(getScrollX(), getScrollY(), 0, velocityY, 0, 0, 0,
                    Math.max(0, (bottom - height) * 2), 0, height / 2);

            mSelftScroller.fling(getScrollX(), getScrollY(), 0, velocityY, 0, 0, 0,
                    Math.max(0, (bottom - height) * 2), 0, height / 2);

            isOver = false;
            mEnableListViewScroller = true;
//            postInvalidateOnAnimation();
            postInvalidate();
        }
    }


    @Override
    public void computeScroll() {
        try {
            Class<ScrollView> cls = ScrollView.class;
            Field field = cls.getDeclaredField("mScroller");
            field.setAccessible(true);
            Object obj = field.get(this);

            int mOverflingDistance = 0;
            field = cls.getDeclaredField("mOverflingDistance");
            field.setAccessible(true);
            mOverflingDistance = (Integer) field.get(this);

            if (obj instanceof OverScroller) {
                OverScroller mScroller = (OverScroller) obj;
                if (mScroller.computeScrollOffset()) {
                    mSelftScroller.computeScrollOffset();

                    int oldX = this.getScrollX();
                    int oldY = this.getScrollY();
                    int x = mScroller.getCurrX();
                    int y = mScroller.getCurrY();

                    if (oldX != x || oldY != y) {
                        Method method = cls.getDeclaredMethod("getScrollRange");
                        method.setAccessible(true);
                        final int range = (Integer) method.invoke(this);
                        final int overscrollMode = getOverScrollMode();
                        final boolean canOverscroll = overscrollMode == OVER_SCROLL_ALWAYS ||
                                (overscrollMode == OVER_SCROLL_IF_CONTENT_SCROLLS && range > 0);

                        overScrollBy(x - oldX, y - oldY, oldX, oldY, 0, range,
                                0, mOverflingDistance, false);
                        onScrollChanged(getScrollX(), getScrollY(), oldX, oldY);

                        if (getScrollY() >= range) {
                            isOver = true;
                        }
                    }

                    if (!awakenScrollBars()) {
                        postInvalidateOnAnimation();
                    }
                }

                if (mSelftScroller.computeScrollOffset() && isOver && mEnableListViewScroller) {
                    mEnableListViewScroller = false;
                    if (mInterupt) {
                        mListView.setScroller(mSelftScroller);
                        Log.v("xs", "scroll view over scroller");
                        mInterupt = false;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
