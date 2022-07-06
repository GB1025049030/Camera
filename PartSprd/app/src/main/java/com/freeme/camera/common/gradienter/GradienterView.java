package com.freeme.camera.common.gradienter;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;

import com.android.camera.debug.Log;

import java.util.concurrent.TimeUnit;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class GradienterView extends View implements SensorEventListener {
    private static final Log.Tag TAG = new Log.Tag("GradienterView");

    private static final int DEFAULT_DEVIATION = 2;
    private static final float CIRCLE_PERCENT = 0.2f;
    private static final float Line_PERCENT = 0.4f;

    private final ValueAnimator mValueAnimator;
    private int mCurrentDegrees;
    private int mCurrentPosition;

    private float mCenterX;
    private float mCenterY;
    private float mCircleRadius;
    private int mLineLength;
    private final Paint mDottedLinePaint;
    private final Path mDottedLinePath;
    private final Paint mSolidLinePaint;
    private int mDrawDegrees;
    private boolean mIsStart;

    private final SensorManager mSensorManager;
    private final DegreeInfo mDegreeInfo;
    private float[] mAccelerometerValues;
    private float[] mMagnetometerValues;

    public GradienterView(Context context) {
        this(context, null);
    }

    public GradienterView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GradienterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mDottedLinePaint = new Paint();
        mDottedLinePaint.setStyle(Paint.Style.STROKE);
        mDottedLinePaint.setAntiAlias(true);
        mDottedLinePaint.setStrokeWidth(2);
        mDottedLinePaint.setColor(Color.parseColor("#FFB8B9BC"));
        mDottedLinePaint.setPathEffect(new DashPathEffect(new float[]{2, 8}, 0));
        mDottedLinePath = new Path();
        mSolidLinePaint = new Paint();
        mSolidLinePaint.setStyle(Paint.Style.STROKE);
        mSolidLinePaint.setAntiAlias(true);
        mSolidLinePaint.setStrokeWidth(2);
        mSolidLinePaint.setColor(Color.WHITE);

        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mDegreeInfo = new DegreeInfo(mSensorManager);
        if (mDegreeInfo.isSupportAccelerometer()) {
            this.mAccelerometerValues = new float[3];
        }
        if (mDegreeInfo.isSupportMagnetic()) {
            this.mMagnetometerValues = new float[3];
        }

        mValueAnimator = new ValueAnimator();
        mValueAnimator.setDuration(35);
        mValueAnimator.addUpdateListener(animation -> {
            mDrawDegrees = (int) animation.getAnimatedValue();
            invalidate();
        });
    }

    private int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }

    private void init(final int size) {
        mCircleRadius = Math.round(size * GradienterView.CIRCLE_PERCENT / 2);
        mLineLength = Math.round(size * GradienterView.Line_PERCENT);
        mCenterX = Math.round(size / 2f);
        mCenterY = Math.round(size / 2f);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int size = measureSize(widthMeasureSpec);
        setMeasuredDimension(size, (int) (size * 1.1f));
        init(size);
    }

    private int measureSize(int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        return specMode == MeasureSpec.EXACTLY ? specSize : (int) (getScreenWidth(mContext) * 0.67f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        draw(canvas, mDrawDegrees % 360, mCenterX, mCenterY, mCircleRadius, mLineLength);
    }

    private void draw(Canvas canvas, float degrees, float px, float py, float r, float d) {
        int deg = (int) degrees;
        int p1 = deg / 90;
        int p2 = p1 % 2;
        boolean start = (deg - p1 * 90) < DEFAULT_DEVIATION;
        boolean end = ((p1 + 1) * 90 - deg) < DEFAULT_DEVIATION;
        if (p2 == 0 && start || p2 != 0 && end) {
            mSolidLinePaint.setColor(Color.WHITE);
            canvas.drawCircle(px, py, r, mSolidLinePaint);
            canvas.drawLine(px - (r + d), py, px + (r + d), py, mSolidLinePaint);
        } else if (p2 == 0 && end || p2 != 0 && start) {
            mSolidLinePaint.setColor(Color.WHITE);
            canvas.drawCircle(px, py, r, mSolidLinePaint);
            canvas.drawLine(px, py - (r + d), px, py + (r + d), mSolidLinePaint);
        } else {
            mSolidLinePaint.setColor(Color.parseColor("#FFB8B9BC"));
            canvas.drawCircle(px, py, r, mSolidLinePaint);
            canvas.save();
            canvas.rotate(degrees, px, py);
            canvas.drawLine(px - (r + d), py, px - r, py, mSolidLinePaint);
            canvas.drawLine(px + r, py, px + (r + d), py, mSolidLinePaint);
            mDottedLinePath.moveTo(px - r, py);
            mDottedLinePath.lineTo(px + r, py);
            canvas.drawPath(mDottedLinePath, mDottedLinePaint);
            canvas.restore();
        }
    }

    public void start() {
        if (!mIsStart) {
            if (mDegreeInfo.isSupportAccelerometer()) {
                mSensorManager.registerListener(this,
                        mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
                mIsStart = true;
                getDegreeByTime();
            }
            if (mDegreeInfo.isSupportMagnetic()) {
                mSensorManager.registerListener(this,
                        mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_NORMAL);
            }
        }
    }

    public void stop() {
        if (mIsStart) {
            mSensorManager.unregisterListener(this);
            mIsStart = false;
        }
    }

    public void getDegreeByTime() {
        Flowable.interval(40, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .takeWhile(aLong -> mIsStart)
                .subscribe(aLong -> {
                    if (!mValueAnimator.isRunning()) {
                        int degree = mDegreeInfo.getDegree(mAccelerometerValues, mMagnetometerValues);
                        if (degree != -1 && degree % 360 != mCurrentDegrees % 360) {
                            int position = degree / 90;
                            int diffDegree = Math.abs(degree - mCurrentDegrees);
                            if (diffDegree > 30 || 360 - diffDegree > 30) {
                                int start = mCurrentDegrees;
                                int stop = degree;
                                if (position * mCurrentPosition == 0 && position + mCurrentPosition == 3) {
                                    if (position == 0) {
                                        stop = degree + 360;
                                    } else {
                                        start = mCurrentDegrees + 360;
                                    }
                                }
                                mValueAnimator.setIntValues(start, stop);
                                mValueAnimator.start();
                            } else {
                                mDrawDegrees = degree;
                                invalidate();
                            }
                            mCurrentDegrees = degree;
                            mCurrentPosition = position;
                        }
                    }
                });
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                System.arraycopy(event.values, 0, mAccelerometerValues, 0, mAccelerometerValues.length);
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                System.arraycopy(event.values, 0, mMagnetometerValues, 0, mMagnetometerValues.length);
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
