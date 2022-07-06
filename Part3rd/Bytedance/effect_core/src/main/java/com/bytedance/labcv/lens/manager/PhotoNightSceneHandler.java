package com.bytedance.labcv.lens.manager;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import com.bytedance.labcv.core.lens.ImageQualityResourceProvider;
import com.bytedance.labcv.core.lens.PhotoImageQualityManager;
import com.bytedance.labcv.core.util.LogUtils;
import com.bytedance.labcv.core.util.timer_record.LogTimerRecord;
import com.bytedance.labcv.effectsdk.BytedEffectConstants;
import com.bytedance.labcv.effectwork.common.utils.BitmapUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

public class PhotoNightSceneHandler extends Handler {
    public static final int ADD_BUFFER = 3001;
    public static final int PhotoNightScenePicCnt = 4;

    private HandlerThread mHandlerThread;
    private Handler workHandler;
    private PhotoImageQualityManager mPhotoImageQualityManager;
    private ArrayList<byte[]> mInputBuffers;
    private PhotoNightSceneCallback mCallback;

    private Context mContext;
    private ImageQualityResourceProvider mImageQualityResourceProvider;
    private int mImageWidth;
    private int mImageHeight;
    private boolean isAlgoOn;

    public PhotoNightSceneHandler(Context context, ImageQualityResourceProvider provider) {
        mHandlerThread = new HandlerThread("Photo_night_scene");
        mHandlerThread.start();

        workHandler = new Handler(mHandlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Messenger messenger = msg.replyTo;
                switch (msg.what) {
                    case ADD_BUFFER:
                        Payload payload = (Payload) msg.obj;
                        LogUtils.e(payload.toString());
                        byte[] buffer = addInputBuffer(payload);
                        if (mCallback != null) {
                            mCallback.onProcessFinished(buffer);
                        }
                        if (mInputBuffers.size() == PhotoNightScenePicCnt) {
                            long cur = System.currentTimeMillis();

//                            for (int i )
                            buffer = process(isAlgoOn);
                            long end = System.currentTimeMillis();
                            double cost = (double) (end - cur);
                            mCallback.onProcessFinished(buffer, mImageWidth, mImageHeight, cost);
                            mInputBuffers.clear();
                        }
                        break;
                }
            }
        };

        mContext = context;
        mImageQualityResourceProvider = provider;
    }

    public void setCallback (PhotoNightSceneCallback callback) {
        mCallback = callback;
    }

    private byte[] addInputBuffer(Payload payload) {
        byte[] data = payload.buffer;
        int width = payload.width, height = payload.height;
        byte[] nv21Bytes = BitmapUtils.rotateNV21Degree90(data, width, height);
        if (mInputBuffers == null) {
            mInputBuffers = new ArrayList<byte[]>();
        }
        mInputBuffers.add(nv21Bytes);
        mImageWidth = height;
        mImageHeight = width;
        LogUtils.e("receive "+Thread.currentThread().getId());
        return nv21Bytes;
    }

    private byte[] process(boolean on){
        if (mPhotoImageQualityManager == null) {
            mPhotoImageQualityManager = new PhotoImageQualityManager(mContext, mImageQualityResourceProvider);
            mPhotoImageQualityManager.mPhotoNightSceneImageNumber = PhotoNightScenePicCnt;
            mPhotoImageQualityManager.mPhotoNightSceneType = BytedEffectConstants.YUV420Type.YUV_420_TYPE_NV12;
            mPhotoImageQualityManager.mPhotoNightSceneWidth = mImageWidth;
            mPhotoImageQualityManager.mPhotoNightSceneHeight = mImageHeight;
            mPhotoImageQualityManager.setImageQuality(BytedEffectConstants.PhotoQualityType.PHOTO_QUALITY_TYPE_NIGNT_SCENE, on);
        }

        if (!on) {
            return mInputBuffers.get(mInputBuffers.size() - 1);
        }
        if (mInputBuffers.size() == mPhotoImageQualityManager.mPhotoNightSceneImageNumber) {
            ByteBuffer[] inputs = new ByteBuffer[mPhotoImageQualityManager.mPhotoNightSceneImageNumber];
            int width = mPhotoImageQualityManager.mPhotoNightSceneWidth;
            int height = mPhotoImageQualityManager.mPhotoNightSceneHeight;
            int allocSize = (int) (width * height * 1.5f);
            for (int i = 0; i < mPhotoImageQualityManager.mPhotoNightSceneImageNumber; i ++) {
                inputs[i] = ByteBuffer.allocateDirect(allocSize).order(ByteOrder.nativeOrder());
                inputs[i].put(mInputBuffers.get(i));
                inputs[i].position(0);
            }

            LogTimerRecord.RECORD("photo_night_scene");
            ByteBuffer result = null;
            try {
                result = mPhotoImageQualityManager.processBuffer(inputs);
            } catch (Exception e) {
                LogUtils.e("exception");
            }

            if (result == null) {
                return null;
            }

            LogTimerRecord.STOP("photo_night_scene");
            result.position(0);
            LogTimerRecord.STOP("total process");
            byte[] buffer = new byte[allocSize];
            result.get(buffer, 0, allocSize);
            return buffer;
        }
        return null;
    }

    @Override
    public void handleMessage(Message msg) {
        Messenger messenger = msg.replyTo;
        switch (msg.what) {
            case ADD_BUFFER:
                Message message = Message.obtain();
                message.what = msg.what;
                message.obj = msg.obj;
                workHandler.sendMessage(message);
                Payload payload = (Payload) msg.obj;
                isAlgoOn = payload.isOn;
                break;
        }
    }

    public void destroy() {
        if (mPhotoImageQualityManager != null) {
            mPhotoImageQualityManager.destroy();
        }
//        workHandler.
        mHandlerThread.quit();
    }


    static public class Payload {
        public byte[] buffer;
        public int width;
        public int height;
        public boolean isOn;
        public Payload(byte[] b, int w, int h, boolean on) {
            buffer = b;
            width = w;
            height = h;
            isOn = on;
        }
    }

    public interface PhotoNightSceneCallback{
        void onProcessFinished(byte buffer[]);
        void onProcessFinished(byte[] bytes, int width, int height, double time);
    }
}
