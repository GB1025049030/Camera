package com.freeme.camera.modules.ikophoto;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.Optional;

public class SensorManagerClient {
    private Context mContext;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private SensorEventListener mSensorEventListener;
    private OnShakeListener mOnShakeListener;

    public SensorManagerClient(Context context) {
        this.mContext = context;
    }

    private void registerSensorListener() {
        Optional.ofNullable(mSensorManager).orElseGet(() -> {
            mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
            return mSensorManager;
        }).registerListener(Optional.ofNullable(mSensorEventListener).orElseGet(() -> {
            mSensorEventListener = new IkoSensorEventListener();
            return mSensorEventListener;
        }), Optional.ofNullable(mSensor).orElseGet(() -> {
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            return mSensor;
        }), 3);
    }

    private void unRegisterSensorListener() {
        Optional.ofNullable(mSensorManager).ifPresent(sensorManager -> {
            Optional.ofNullable(mSensorEventListener).ifPresent(sensorManager::unregisterListener);
        });
    }

    public void resume(OnShakeListener onShakeListener) {
        registerSensorListener();
        this.mOnShakeListener = onShakeListener;
    }

    public void pause() {
        unRegisterSensorListener();
    }

    public interface OnShakeListener {
        void onSilent(boolean isSilent);
    }

    private class IkoSensorEventListener implements SensorEventListener {
        private static final float MAX_SHAKE_EVENT_VALUE = 1.0f;

        private float mHorizontalValue;
        private float mVerticalValue;
        private float mLateralValue;
        private int mSensorValue;
        private long mInitTime;
        private long mStartTimeMills;

        private IkoSensorEventListener() {
            this.mSensorValue = 1;
        }

        public void onAccuracyChanged(Sensor sensor, int i) {
        }

        public void onSensorChanged(SensorEvent sensorEvent) {

            long currentTime = System.currentTimeMillis();
            if (currentTime - mStartTimeMills >= 100) {
                mStartTimeMills = currentTime;
                float x_Value = sensorEvent.values[SensorManager.DATA_X];
                float y_Value = sensorEvent.values[SensorManager.DATA_Y];
                float z_Value = sensorEvent.values[SensorManager.DATA_Z];
                long mUnInitTime = System.currentTimeMillis();
                float xAbs = Math.abs(mHorizontalValue - x_Value);
                float yAbs = Math.abs(mVerticalValue - y_Value);
                float zAbs = Math.abs(mLateralValue - z_Value);
                if (xAbs > MAX_SHAKE_EVENT_VALUE || yAbs > MAX_SHAKE_EVENT_VALUE || zAbs > MAX_SHAKE_EVENT_VALUE) {
                    if (mSensorValue != 1) {
                        if (mOnShakeListener != null) {
                            mOnShakeListener.onSilent(false);
                        }
                    }
                    mSensorValue = 1;
                    mInitTime = 0;
                } else {
                    if (mSensorValue == 1) {
                        mInitTime = System.currentTimeMillis();
                    }
                    if (mUnInitTime - mInitTime > 1000) {
                        if (mOnShakeListener != null) {
                            mOnShakeListener.onSilent(true);
                        }
                    }
                    mSensorValue = 0;
                }
                mHorizontalValue = x_Value;
                mVerticalValue = y_Value;
                mLateralValue = z_Value;
            }
        }
    }
}
