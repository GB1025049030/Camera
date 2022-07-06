package com.freeme.camera.modules.nightphoto;

import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.os.Message;
import android.util.Range;
import android.util.Rational;

import androidx.annotation.NonNull;

import com.android.camera.CameraActivity;
import com.android.camera.PhotoUI;
import com.android.camera.app.AppController;
import com.android.camera.debug.Log;
import com.android.camera.util.CameraUtil;
import com.android.ex.camera2.portability.CameraAgent;
import com.bytedance.labcv.core.lens.ImageQualityResourceHelper;
import com.bytedance.labcv.core.util.LogUtils;
import com.bytedance.labcv.effectwork.common.utils.BitmapUtils;
import com.bytedance.labcv.lens.manager.PhotoNightSceneHandler;
import com.freeme.camera.modules.CompositePhotoModule;

import java.util.ArrayList;
import java.util.List;

public class NightPhotoModule extends CompositePhotoModule implements PhotoNightSceneHandler.PhotoNightSceneCallback {
    private static final Log.Tag TAG = new Log.Tag("NightPhotoModule");

    private CameraNightRequest mCameraNightRequest;
    protected PhotoNightSceneHandler mPhotoNightSceneHandler;

    public NightPhotoModule(AppController app) {
        super(app);
    }

    @Override
    public void init(CameraActivity activity, boolean isSecureCamera, boolean isCaptureIntent) {
        super.init(activity, isSecureCamera, isCaptureIntent);
        mCameraNightRequest = new CameraNightRequest();
    }

    @Override
    public PhotoUI createUI(CameraActivity activity) {
        mActivity = activity;
        return new NightPhotoUI(activity, this, activity.getModuleLayoutRoot());
    }

    @Override
    public void resume() {
        super.resume();
        mPhotoNightSceneHandler = new PhotoNightSceneHandler(mActivity.getAndroidContext(),
                new ImageQualityResourceHelper(mActivity.getAndroidContext().getApplicationContext()));
        mPhotoNightSceneHandler.setCallback(this);
    }

    @Override
    public void pause() {
        super.pause();
        if (mPhotoNightSceneHandler != null) {
            mPhotoNightSceneHandler.destroy();
            mPhotoNightSceneHandler = null;
        }
    }

    @Override
    public void onImageAvailable(ImageReader reader) {
        Image image = reader.acquireNextImage();
        if (image != null) {
            int width = image.getWidth();
            int height = image.getHeight();
            byte[] data = BitmapUtils.getDataFromImage(image, 0);
            image.close();
            PhotoNightSceneHandler.Payload payload = new PhotoNightSceneHandler.Payload(data, width, height, true);
            Message message = Message.obtain();
            message.obj = payload;
            message.what = PhotoNightSceneHandler.ADD_BUFFER;
            if (mPhotoNightSceneHandler != null) {
                mPhotoNightSceneHandler.sendMessage(message);
            }
        }
        Log.d(TAG, "onImageAvailable: ==================================================================");
    }

    @NonNull
    @Override
    public CameraAgent.CameraCompositeRequest getCameraCompositeRequest() {
        return mCameraNightRequest;
    }

    @Override
    public void onProcessFinished(byte[] buffer) {
        CameraUtil.saveYUVToFile(buffer, 4000, 3000);
    }

    @Override
    public void onProcessFinished(byte[] bytes, int width, int height, double time) {
        if (bytes != null) {
            byte[] jpeg = BitmapUtils.getJpegFromYUV(bytes, width, height);
            getJpegPictureCallback().onPictureTaken(jpeg, getCurrentExif(), null);
            Log.d(TAG, "onProcessFinished: algorithm time : " + time + ", total time : " + (System.currentTimeMillis() - mStartTime));
        }
    }

    public class CameraNightRequest implements CameraAgent.CameraCompositeRequest {
        @Override
        public List<CaptureRequest> create(CameraDevice camera, CameraCharacteristics characteristics) {
            Range<Integer> aeRange = characteristics.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE);
            Rational rational = characteristics.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_STEP);

            List<Integer> evs = getAES();
            List<CaptureRequest> requests = new ArrayList<>(evs.size());
            for (Integer ev : evs) {
                try {
                    CaptureRequest.Builder builder = camera.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                    builder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON);
                    double ae = ev / rational.doubleValue();
                    if (ae < aeRange.getLower()) {
                        ae = aeRange.getLower();
                    } else if (ae > aeRange.getUpper()) {
                        ae = aeRange.getUpper();
                    }
                    builder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, new Integer((int) ae));
                    builder.set(CaptureRequest.CONTROL_AE_LOCK, true);
                    builder.addTarget(getCompositeSurface());
                    requests.add(builder.build());
                } catch (Exception e) {
                    e.printStackTrace();
                    LogUtils.e("can't not change the param " + e.toString());
                }
            }
            return requests;
        }

        List<Integer> getAES() {
            List<Integer> aes = new ArrayList<>();
            for (int i = 0; i < PhotoNightSceneHandler.PhotoNightScenePicCnt - 1; i++) {
                aes.add(0);
            }
            aes.add(-2);
            return aes;
        }
    }
}
