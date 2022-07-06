package com.android.library.baidu.aip.ui;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import com.android.library.baidu.aip.R;

public class HeadZoomScrollView extends ScrollView {
    private float mTouchY = 0f;
    private int mZoomViewWidth = 0;
    private int mZoomViewHeight = 0;
    private boolean mScaling = false;
    private float mScaleRatio = 0.4f;
    private float mScaleTimes = 2f;
    private float mReplyRatio = 0.5f;
    private View mZoomView;

    public HeadZoomScrollView(Context context) {
        super(context);
    }

    public HeadZoomScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HeadZoomScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setOverScrollMode(OVER_SCROLL_NEVER);
        if (getChildAt(0) != null && getChildAt(0) instanceof ViewGroup && mZoomView == null) {
            ViewGroup viewGroup = (ViewGroup) getChildAt(0);
            if (viewGroup.getChildCount() > 0) {
                mZoomView = viewGroup.getChildAt(0);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mZoomViewWidth <= 0 || mZoomViewHeight <= 0) {
            mZoomViewWidth = mZoomView.getMeasuredWidth();
            mZoomViewHeight = mZoomView.getMeasuredHeight();
        }
        if (mZoomView == null || mZoomViewWidth <= 0 || mZoomViewHeight <= 0) {
            return super.onTouchEvent(ev);
        }
        switch (ev.getAction()) {
            case MotionEvent.ACTION_MOVE:
                if (!mScaling) {
                    if (getScrollY() == 0) {
                        mTouchY = ev.getY();
                    } else {
                        break;
                    }
                }
                int distance = (int) ((ev.getY() - mTouchY) * mScaleRatio);
                if (distance < 0) break;
                mScaling = true;
                setZoom(distance);
                return true;
            case MotionEvent.ACTION_UP:
                mScaling = false;
                replyView();
                break;
        }
        return super.onTouchEvent(ev);
    }

    private void setZoom(float s) {
        float scaleTimes = (float) ((mZoomViewWidth + s) / (mZoomViewWidth * 1.0));
        if (scaleTimes > mScaleTimes) return;

        ViewGroup.LayoutParams layoutParams = mZoomView.getLayoutParams();
        layoutParams.width = (int) (mZoomViewWidth + s);
        layoutParams.height = (int) (mZoomViewHeight * ((mZoomViewWidth + s) / mZoomViewWidth));
        final int marginLeft = -(layoutParams.width - mZoomViewWidth) / 2;
        final int marginTop = getResources().getDimensionPixelSize(R.dimen.iko_original_image_top);
        final int marginRight = -(layoutParams.width - mZoomViewWidth) / 2;
        final int marginBottom = 0;
        ((MarginLayoutParams) layoutParams).setMargins(marginLeft, marginTop, marginRight, marginBottom);
        mZoomView.setLayoutParams(layoutParams);
    }

    private void replyView() {
        final float distance = mZoomView.getMeasuredWidth() - mZoomViewWidth;
        ValueAnimator anim = ObjectAnimator.ofFloat(distance, 0.0F).setDuration((long) (distance * mReplyRatio));
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setZoom((Float) animation.getAnimatedValue());
            }
        });
        anim.start();
    }
}