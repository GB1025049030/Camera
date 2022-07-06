package com.freeme.camera.common.gradienter;

import android.hardware.Sensor;
import android.hardware.SensorManager;

import com.android.camera.debug.Log;

public class DegreeInfo {
    private static final Log.Tag TAG = new Log.Tag("DegreeInfo");
    private static final int MODE_ONLY_ACCELEROMETER = 1;
    private static final int MODE_ONLY_MAGNETIC = 2;

    private final SensorManager mSensorManager;
    private final int mCurrentMode;

    private float[] mRotationMatrix;
    private float[] mOrientationValues;

    protected DegreeInfo(SensorManager manager) {
        this.mSensorManager = manager;
        this.mCurrentMode = getSupportMode();
        if ((mCurrentMode & MODE_ONLY_MAGNETIC) != 0) {
            this.mRotationMatrix = new float[9];
            this.mOrientationValues = new float[3];
        }
    }

    public int getDegree(float[] accelerometer, float[] magnetometer) {
        float degree = 0;
        if (mCurrentMode == (MODE_ONLY_ACCELEROMETER | MODE_ONLY_MAGNETIC)) {
            SensorManager.getRotationMatrix(mRotationMatrix, null, accelerometer, magnetometer);
            SensorManager.getOrientation(mRotationMatrix, mOrientationValues);
            degree = getDegree(mOrientationValues[2]);
        } else if (mCurrentMode == MODE_ONLY_ACCELEROMETER) {
            if (accelerometer[2] < 9) {
                degree = getDegree(accelerometer[0], accelerometer[1]);
            } else {
                return -1;
            }
        }
        return ((int) degree) % 360;
    }

    private int getSupportMode() {
        int mode = 0;
        if (mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER).size() != 0) {
            mode = mode | MODE_ONLY_ACCELEROMETER;
        }
        if (mSensorManager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD).size() != 0) {
            mode = mode | MODE_ONLY_MAGNETIC;
        }
        return mode;
    }

    public boolean isSupportAccelerometer() {
        return (mCurrentMode & MODE_ONLY_ACCELEROMETER) != 0;
    }

    public boolean isSupportMagnetic() {
        return (mCurrentMode & MODE_ONLY_MAGNETIC) != 0;
    }

    private float getDegree(float values) {
        int degrees = (int) Math.round((360f + values * 180f / Math.PI) % 360);
        return degrees > 270 ? degrees - 360 : degrees;
    }

    private float getDegree(float x, float y) {
        double r = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
        double sin = x / r;
        int degrees = (int) Math.round((360f + Math.asin(sin) * 180f / Math.PI) % 360);
        if (y < 0) {
            degrees = x > 0 ? 180 - degrees : 540 - degrees;
        }
        return degrees;
    }
}
