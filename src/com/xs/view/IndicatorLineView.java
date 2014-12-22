package com.xs.view;

/**
 * Created by xs on 14/12/1.
 */
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class IndicatorLineView extends View {

    public int mLeft = 0;

    private int mRight = 0;

    private int mDesireLeft = 0;

    private int mDesireRight = 0;

    private int mOffset = 0;

    private float mLastPercent;

    private boolean mPullLeft = false;

    /**
     * the bar color
     */
    private int mBarColor = 0xffff3800;

    public IndicatorLineView(Context context) {
        this(context, null);
    }

    public IndicatorLineView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IndicatorLineView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setBarColor(int barColor) {
        mBarColor = barColor;
    }

    public void setLine(int start, int end) {
        mLeft = start;
        mRight = end;
        invalidate();
    }

    public void setNextLength(int nextLength, boolean isPullLeft, int total) {
        int mCurrentLength = mRight - mLeft;
        mOffset = isPullLeft ? nextLength - mCurrentLength : mCurrentLength - nextLength;
        mDesireLeft = isPullLeft ? mLeft + total : mLeft - total;
        mDesireRight = isPullLeft ? mRight+total+mOffset : mRight-total-mOffset;
        mPullLeft = isPullLeft;
    }

    public void setmLastPercent(float lastPercent) {
        this.mLastPercent = lastPercent;
    }

    public void scrollBy(int by, float nowPercent) {
        mLeft += by;
        int temp = (int)((nowPercent-mLastPercent)* mOffset);
        if (Math.abs(temp) > 0) {
            mLastPercent = nowPercent;
        }
        mRight = mRight + by + temp;
        if (mPullLeft) {
            mLeft = mLeft < mDesireLeft ? mLeft : mDesireLeft;
            mRight = mRight < mDesireRight ? mRight : mDesireRight;
        } else {
            mLeft = mLeft > mDesireLeft ? mLeft : mDesireLeft;
            mRight = mRight > mDesireRight ? mRight : mDesireRight;
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 蓝色
        Paint paint = new Paint();
        paint.setColor(mBarColor);
        paint.setStyle(Paint.Style.FILL);
        // 灰色
        Paint gray = new Paint();
        gray.setColor(Color.parseColor("#cccccc"));
        gray.setStyle(Paint.Style.FILL);

        canvas.drawRect(new Rect(0, 0, mLeft, getHeight()), gray);
        canvas.drawRect(new Rect(mLeft, 0, mRight, getHeight()), paint);
        canvas.drawRect(new Rect(mRight, 0, getWidth(), getHeight()), gray);
    }
}
