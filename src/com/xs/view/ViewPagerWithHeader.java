package com.xs.view;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.*;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.example.TestScroll.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xs on 14/12/7.
 */
public class ViewPagerWithHeader extends LinearLayout{

    private ViewPager mViewPager;

    private ViewPager.OnPageChangeListener mListener;

    private String[] mData;

    private HorizontalScrollView mHorizonView;

    private LinearLayout mLayout;

    private IndicatorLineView mLine;

    private List<TextView> mAllTitle = new ArrayList<TextView>();

    private final int INVALID_WIDTH = Integer.MIN_VALUE;

    private boolean mSelect = false;

    private int mDesirePos = 0;

    /**
     * the text size(using sp)
     */
    private int mTitleTextSize = 14;

    /**
     * the text padding
     */
    private int mTextPadding = 0;

    /**
     * select color
     */
    private int mTitleSelectColor = 0xff377bee;

    /**
     * not select color
     */
    private int mTitleNotSelectColor = 0xffcccccc;

    /**
     * the textview min width
     */
    private int mMinWidth = INVALID_WIDTH;

    /**
     * the tip margin to its parent
     */
    private int mIndicatorMargin = 0;

    public ViewPagerWithHeader(Context context) {
        this(context, null);
    }

    public ViewPagerWithHeader(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ViewPagerWithHeader(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
    }

    private void initView() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.pager_with_header, this);
        mHorizonView = (HorizontalScrollView) findViewById(R.id.horizon_view);
        mLayout = (LinearLayout) findViewById(R.id.title_layout);
        mLine = (IndicatorLineView) findViewById(R.id.line);

        // 字体的大小
        Resources r = getResources();
        // title padding
        mTextPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, r.getDisplayMetrics());

        mHorizonView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
    }

    /**
     * set min width for the textview
     *
     * @param minWidth min width
     */
    public void setMinWidth(int minWidth) {
        if (mMinWidth >= 0) {
            mMinWidth = minWidth;
        } else {
            mMinWidth = INVALID_WIDTH;
        }
    }

    /**
     * set tip margin to its parent
     *
     * @param margin the tip margin to its parent
     */
    public void setIndicatorMargin(int margin) {
        mIndicatorMargin = margin;
    }

    /**
     * set the select color
     *
     * @param color
     */
    public void setTitleSelectColor(int color) {
        mTitleSelectColor = color;
    }

    /**
     * set the not select color
     *
     * @param color
     */
    public void setTitleNotSelectColor(int color) {
        mTitleNotSelectColor = color;
    }

    /**
     * set the color of indicator line
     *
     * @param color the color
     */
    public void setIndicatorColor(int color) {
        mLine.setBarColor(color);
    }

    public void setHeaderData(String[] data) {
        mData = data;
        int index = 0;
        for (String temp : mData) {
            TextView textView = new TextView(getContext());
            textView.setText(temp);
            textView.setTextSize(mTitleTextSize);
            textView.setGravity(Gravity.CENTER);
            textView.setTag(index);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            textView.setPadding(mTextPadding, 0, mTextPadding, 0);
            if (mMinWidth != INVALID_WIDTH) {
                textView.setMinWidth(mMinWidth);
                textView.setTextColor(mTitleNotSelectColor);
            }
            mLayout.addView(textView, params);
            mAllTitle.add(textView);
            if (index == 0) {
                textView.setTextColor(mTitleSelectColor);
                ViewTreeObserver viewTreeObserver = textView.getViewTreeObserver();
                if (viewTreeObserver.isAlive()) {
                    viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            mAllTitle.get(0).getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            int viewWidth = mAllTitle.get(0).getWidth();
                            mLine.setLine(mIndicatorMargin, viewWidth - mIndicatorMargin);
                        }
                    });
                }

            }

            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int index = (Integer) v.getTag();
                    int total = 0;
                    for (int i=0; i<index; i++) {
                        total += mAllTitle.get(i).getWidth();
                    }
                    mDesirePos = index;
                    mSelect = true;
                    mHorizonView.smoothScrollTo(total, 0);
                    mViewPager.setCurrentItem(index);
                }
            });

            index++;
        }
    }

    public void setViewPager(ViewPager viewPager, ViewPager.OnPageChangeListener listener) {
        mViewPager = viewPager;
        mListener = listener;
        mViewPager.setOnPageChangeListener(mPageListener);
    }

    private ViewPager.OnPageChangeListener mPageListener = new ViewPager.OnPageChangeListener() {

        // the pull direction
        private boolean mPullLeft = true;

        // use to judge is pull left or right
        private boolean mDetect = true;

        // remember the last positionOffset
        private float mLastOffset = 0;

        // the remain length to move
        private int mRemain = 0;

        // the total length to move
        private int mTotaloffsetX = 0;

        // my current select index
        private int mCurIndex = 0;

        // need to move header
        private boolean isMovehead = true;

        // the last scroll pos
        private int mLastPos = 0;

        @Override
        public void onPageScrolled(int pos, float positionOffset, int positionOffsetPixels) {
            if (!mLine.mRenderFinsh) {
                return;
            }

            if (mDetect && !mSelect) {
                if (mHorizonView.getScrollX() != mAllTitle.get(mLastPos).getLeft()) {
                    mHorizonView.scrollTo(mAllTitle.get(mLastPos).getLeft(), 0);
                }
            }

            if (mLastPos > pos) {
                mPullLeft = false;
                mLastOffset = 1.0f;
                mLine.setmLastPercent(1.0f);
                TextView now = mAllTitle.get(mLastPos);
                if (mLastPos - 1 < mAllTitle.size() && mLastPos-1 >= 0) {
                    TextView next = mAllTitle.get(mLastPos - 1);
                    boolean space = leftHaveEnougSpace(mLastPos);
                    mRemain = mTotaloffsetX = now.getLeft() - next.getLeft() - mIndicatorMargin + (mLine.mLeft - now.getLeft());
                    if (space) {
                        isMovehead = true;
                    } else {
                        isMovehead = false;
                    }
                    int nextTipLength = next.getWidth() - mIndicatorMargin * 2;
                    mLine.setNextLength(nextTipLength, mPullLeft, mTotaloffsetX);
                } else {
                    mTotaloffsetX = 0;
                }
                mDetect = false;
            } else if (pos > mLastPos || mDetect) {
                if (pos > mLastPos) {
                    stepMove(1.0f);
                    mLastOffset = 0f;
                } else {
                    stepMove(0);
                    mLastOffset = 0f;
                }
                mPullLeft = true;
                mLine.setmLastPercent(0);
                if (pos+1 < mAllTitle.size()) {
                    TextView next = mAllTitle.get(pos+1);
                    mRemain = mTotaloffsetX = next.getLeft() - (mLine.mLeft-mIndicatorMargin);
                    int nextTipLength = next.getWidth() - mIndicatorMargin * 2;
                    mLine.setNextLength(nextTipLength, mPullLeft, mTotaloffsetX);
                    isMovehead = true;
                    mDetect = false;
                    if (mTotaloffsetX == 0) {
                        mLastOffset = 1.0f;
                    }
                } else {
                    mTotaloffsetX = 0;
                }
            }

            mLastPos = pos;
            stepMove(positionOffset);
            if (mListener != null) {
                mListener.onPageScrolled(pos, positionOffset, positionOffsetPixels);
            }
        }

        private void stepMove(float positionOffset) {
            int tempTotaloffsetX = mTotaloffsetX == 0 ? 0 : mTotaloffsetX+mTotaloffsetX/3;
            int temp = (int) ((tempTotaloffsetX) * (positionOffset - mLastOffset));
            isMovehead = mSelect ? false : isMovehead;
            if (Math.abs(temp) > 0) {
                int tempRemain = mRemain;
                mRemain = mPullLeft ? mRemain - temp : mRemain + temp;
                int tempHeadMove = mRemain >=0 ? temp : (mPullLeft ? tempRemain : -tempRemain);
                tempHeadMove = tempRemain <0 ? 0: tempHeadMove;
                if (isMovehead && tempRemain>0) {
                    mHorizonView.scrollBy(tempHeadMove, 0);
                }
                mLine.scrollBy(tempHeadMove, positionOffset-mLastOffset);
                mLastOffset = positionOffset;
            }
        }

        @Override
        public void onPageSelected(int page) {
            mAllTitle.get(mCurIndex).setTextColor(mTitleNotSelectColor);
            mAllTitle.get(page).setTextColor(mTitleSelectColor);
            mCurIndex = page;
            if (mListener != null) {
                mListener.onPageSelected(page);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            if (state == ViewPager.SCROLL_STATE_IDLE) {
                mLastPos = mViewPager.getCurrentItem();
                mDetect = true;
                mSelect = false;
                int left = mAllTitle.get(mLastPos).getLeft();
                int width = mAllTitle.get(mLastPos).getWidth();
                mLine.endMove(left+mIndicatorMargin, left+width-mIndicatorMargin);
            }
            if (mListener != null) {
                mListener.onPageScrollStateChanged(state);
            }
        }
    };

    private boolean leftHaveEnougSpace(int mCurIndex) {
        int tempTotal = 0;
        for (int i = mCurIndex - 1; i < mAllTitle.size(); i++) {
            if (i >= 0) {
                tempTotal += mAllTitle.get(i).getWidth();
                if (tempTotal > mHorizonView.getWidth()) {
                    return true;
                }
            }
        }
        if (tempTotal > mHorizonView.getWidth()) {
            return true;
        }
        return false;
    }
}
