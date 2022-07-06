// Copyright (C) 2018 Beijing Bytedance Network Technology Co., Ltd.
package com.dream.camera.effect;

import android.content.Context;
import android.hardware.SensorManager;
import android.view.OrientationEventListener;


public class OrientationSensor {

    private static OrientationEventListener mOrientationListener;
    private static int mOrientation;

    public static void start(Context context) {
        if (mOrientationListener != null) {
            return;
        }
        mOrientationListener = new OrientationEventListener(context, SensorManager.SENSOR_DELAY_NORMAL) {
            @Override
            public void onOrientationChanged(int orientation) {
                if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
                    return;
                }

                int newOrientation = ((orientation + 45) / 90 * 90) % 360;
                if (newOrientation != mOrientation) {
                    mOrientation = newOrientation;
                }
            }
        };

        if (mOrientationListener.canDetectOrientation()) {
            mOrientationListener.enable();
        } else {
            mOrientationListener = null;
        }
    }

    public static void stop() {
        if (mOrientationListener != null) {
            mOrientationListener.disable();
        }
        mOrientationListener = null;
    }

    public static int getSensorOrientation(){
        return mOrientation;
    }

    public static Rotation getOrientation() {
        switch (mOrientation) {
            case 90:
                return Rotation.CLOCKWISE_ROTATE_90;
            case 180:
                return Rotation.CLOCKWISE_ROTATE_180;
            case 270:
                return Rotation.CLOCKWISE_ROTATE_270;
            default:
                return Rotation.CLOCKWISE_ROTATE_0;
        }
    }

    /**
     * 图像旋转角
     * Image rotation
     */
    public enum Rotation {
        /**
         * 图像不需要旋转，图像中的人脸为正脸
         * The image does not need to be rotated. The face in the image is positive
         */
        CLOCKWISE_ROTATE_0(0),
        /**
         * 图像需要顺时针旋转90度，使图像中的人脸为正
         * The image needs to be rotated 90 degrees clockwise so that the face in the image is positive
         */
        CLOCKWISE_ROTATE_90(1),
        /**
         * 图像需要顺时针旋转180度，使图像中的人脸为正
         * The image needs to be rotated 180 degrees clockwise so that the face in the image is positive
         */
        CLOCKWISE_ROTATE_180(2),
        /**
         * 图像需要顺时针旋转270度，使图像中的人脸为正
         * The image needs to be rotated 270 degrees clockwise so that the face in the image is positive
         */
        CLOCKWISE_ROTATE_270(3);

        public int id = 0;

        Rotation(int id) {
            this.id = id;
        }
    }
}
