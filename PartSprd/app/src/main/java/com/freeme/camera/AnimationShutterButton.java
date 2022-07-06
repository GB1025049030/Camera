package com.freeme.camera;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.animation.LinearInterpolator;

import com.android.camera.ShutterButton;

public class AnimationShutterButton extends ShutterButton {
    private static final int SIZE = 150;
    private static final int START_POSITION = 270;
    private static final int START_MAX_REPEAT_COUNT = 100;

    private Paint mPaint;
    private RectF mRectF;
    private int mAngle = START_POSITION;
    private ValueAnimator mAngleAnimator;
    private boolean mIsAngleAnimatorEnd;
    private boolean mIsEnableAnimation;
    private boolean mIsNeedAnimation;

    public AnimationShutterButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int diameter = Math.min(w, h);
        float stroke = Math.min(w, h) * 0.15f / 2;
        this.mPaint.setStrokeWidth(stroke);
        this.mRectF.set(stroke, stroke, diameter - stroke, diameter - stroke);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (isEnableAnimation()) {
            mPaint.setColor(Color.WHITE);
            canvas.drawArc(mRectF, mAngle, SIZE, false, mPaint);
            mPaint.setColor(Color.TRANSPARENT);
            canvas.drawArc(mRectF, (mAngle + SIZE) % 360, 360 - SIZE, false, mPaint);
        }
        super.onDraw(canvas);
        if (isEnableAnimation()) {
            if (!mAngleAnimator.isRunning()) {
                mAngleAnimator.start();
            }
        } else {
            if (mAngleAnimator.isRunning()) {
                mAngleAnimator.end();
                invalidate();
            }
        }
    }

    private boolean isEnableAnimation() {
        return mIsNeedAnimation && mIsEnableAnimation && !mIsAngleAnimatorEnd;
    }

    private void initialize() {
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setAntiAlias(true);
        mRectF = new RectF();
        mAngleAnimator = ValueAnimator.ofInt(START_POSITION, START_POSITION + 360);
        mAngleAnimator.setDuration(1000);
        mAngleAnimator.setRepeatCount(START_MAX_REPEAT_COUNT);
        mAngleAnimator.setInterpolator(new LinearInterpolator());
        mAngleAnimator.addUpdateListener(valueAnimator -> {
            mAngle = (int) valueAnimator.getAnimatedValue();
            invalidate();
        });
        mAngleAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mIsAngleAnimatorEnd = true;
            }
        });
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (!enabled) {
            mIsEnableAnimation = true;
            mIsAngleAnimatorEnd = false;
        } else {
            mIsEnableAnimation = false;
        }
        super.setEnabled(enabled);
    }

    public void setNeedAnimation(boolean needAnimation) {
        mIsNeedAnimation = needAnimation;
        invalidate();
    }

    public boolean getNeedAnimation() {
        return mIsNeedAnimation;
    }
}
