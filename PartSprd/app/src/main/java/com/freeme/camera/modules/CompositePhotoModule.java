package com.freeme.camera.modules;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.location.Location;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.Surface;

import androidx.annotation.NonNull;

import com.android.camera.CameraActivity;
import com.android.camera.Exif;
import com.android.camera.PhotoModule;
import com.android.camera.PhotoUI;
import com.android.camera.app.AppController;
import com.android.camera.debug.Log;
import com.android.camera.exif.ExifInterface;
import com.android.camera.util.CameraUtil;
import com.android.ex.camera2.portability.CameraAgent;
import com.android.ex.camera2.portability.Size;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public abstract class CompositePhotoModule extends PhotoModule implements ImageReader.OnImageAvailableListener {
    private static final Log.Tag TAG = new Log.Tag("CompositePhoto");

    protected CameraActivity mActivity;
    private FlowableJpegPictureCallback mPictureCallback;

    private Handler mCompositeImageReaderHandler;
    private HandlerThread mCompositeImageReaderHandlerThread;
    private ImageReader mCompositeImageReader;
    private Surface mCompositeSurface;
    private ExifInterface mCurrentExif;
    protected long mStartTime;

    public CompositePhotoModule(AppController app) {
        super(app);
    }

    @Override
    public PhotoUI createUI(CameraActivity activity) {
        mActivity = activity;
        return new CompositePhotoUI(activity, this, activity.getModuleLayoutRoot());
    }

    public boolean isSupportTouchAFAE() {
        return true;
    }

    public boolean isSupportManualMetering() {
        return false;
    }

//    @Override
//    protected void updateParameters3DNR() {
//        if (!CameraUtil.is3DNREnable()) {
//            return;
//        }
//
//        Log.i(TAG, "updateParameters3DNR set3DNREnable : 1");
//        mCameraSettings.set3DNREnable(1);
//    }

    @Override
    protected void updateParametersThumbCallBack() {
        if (CameraUtil.isNormalNeedThumbCallback()){
            Log.i(TAG, "setNeedThumbCallBack true ");
            mCameraSettings.setNeedThumbCallBack(true);
            mCameraSettings.setThumbCallBack(1);
        } else {
            super.updateParametersThumbCallBack();
        }
    }

    @Override
    protected void updateParametersZsl() {
        Log.i(TAG, "Night setZslModeEnable: 0");
        mCameraSettings.setZslModeEnable(0);
    }

    @Override
    public void resume() {
        super.resume();
        mCompositeImageReaderHandlerThread = new HandlerThread("composite image reader");
        mCompositeImageReaderHandlerThread.start();
        mCompositeImageReaderHandler = new Handler(mCompositeImageReaderHandlerThread.getLooper());
    }

    @Override
    public void pause() {
        super.pause();
        releaseCompositeImageReader();
    }

    @Override
    protected void updateParametersPictureSize() {
        super.updateParametersPictureSize();
        updateCompositePictureSize();
    }

    private void updateCompositePictureSize() {
        Size photoSize = mCameraSettings.getCurrentPhotoSize();
        try {
            photoSize = getClosestSize();
            Log.d(TAG, "updateParametersPictureSize: photoSize ["
                    + photoSize.width() + ", " + photoSize.height() + "]");
        } catch (CameraAccessException e) {
            e.printStackTrace();
            Log.d(TAG, "updateCompositePictureSize: getClosestSize failed!");
        }
        if (mCompositeImageReader == null || photoSize.width() != mCompositeImageReader.getWidth()
                || photoSize.height() != mCompositeImageReader.getHeight()) {
            Log.d(TAG, "updateParametersPictureSize: photo size change, now create new reader!");
            releaseCompositeImageReader();
            mCompositeImageReader = ImageReader.newInstance(photoSize.width(), photoSize.height(), ImageFormat.YUV_420_888, 2);
            mCompositeImageReader.setOnImageAvailableListener(this, mCompositeImageReaderHandler);
            mCompositeSurface = mCompositeImageReader.getSurface();
        }
    }

    private Size getClosestSize() throws CameraAccessException {
        CameraManager cameraManager = (CameraManager) mActivity.getSystemService(Context.CAMERA_SERVICE);
        CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(mCameraId + "");
        StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        android.util.Size[] sizes = map.getOutputSizes(ImageFormat.YUV_420_888);
        ArrayList<android.util.Size> suitableSize = new ArrayList<>();
        if (sizes != null) {
            for (android.util.Size s : sizes) {
                if (s.getWidth() % 8 == 0 && s.getHeight() % 8 == 0) {
                    suitableSize.add(new android.util.Size(s.getWidth(), s.getHeight()));
                }
            }
            suitableSize.sort(new CompareSizesByArea());
            android.util.Size s = getClosestCameraPreviewSize(
                    suitableSize.toArray(new android.util.Size[suitableSize.size()]), new android.util.Size(4000, 3000));
            return new Size(s.getWidth(), s.getHeight());
        }
        return null;
    }

    // 找到最接近的大小
    // 这里夜景找最大的,当ret 小的时候，返回prefer,然后看camera hal能返回的大小
    private android.util.Size getClosestCameraPreviewSize(android.util.Size[] supports, android.util.Size prefer) {
        android.util.Size ret = supports[0];
        for (android.util.Size s : supports) {
            if (s.getHeight() >= prefer.getHeight() && s.getWidth() >= prefer.getWidth()) {
                ret = s;
            }
        }
        if (prefer.getWidth() > ret.getWidth() && prefer.getHeight() > ret.getHeight()) {
            return prefer;
        }
        return ret;
    }

    public static class CompareSizesByArea implements Comparator<android.util.Size> {
        @Override
        public int compare(android.util.Size lhs, android.util.Size rhs) {
            return -Long.signum((long) lhs.getWidth() * lhs.getHeight() - (long) rhs.getWidth() * rhs.getHeight());
        }
    }

    @Override
    protected void dosetPreviewDisplayspecial() {
        List<Surface> surfaces = new ArrayList<>();
        surfaces.add(mCompositeSurface);
        mCameraDevice.setRecordSurfaces(surfaces);
        mCameraDevice.setCameraCompositeRequest(getCameraCompositeRequest());
    }

    @Override
    public CameraAgent.CameraPictureCallback getCameraPictureCallback(Location loc) {
        mPictureCallback = new FlowableJpegPictureCallback(loc);
        return (data, camera) -> {
            mStartTime = System.currentTimeMillis();
            mCurrentExif = Exif.getExif(data);
        };
    }

    @Override
    protected void updateParametersComposite() {
        Log.i(TAG, "updateParametersComposite true");
        mCameraSettings.setCompositeEnable(true);
    }

    @Override
    public void onCameraClose() {
        releaseCompositeImageReader();
    }

    public void releaseCompositeImageReader() {
        if (mCompositeImageReader != null) {
            mCompositeImageReader.close();
            mCompositeImageReader = null;
        }
    }

    protected FlowableJpegPictureCallback getJpegPictureCallback() {
        return mPictureCallback;
    }

    protected ExifInterface getCurrentExif() {
        return mCurrentExif;
    }

    protected Surface getCompositeSurface() {
        return mCompositeImageReader.getSurface();
    }

    public abstract @NonNull
    CameraAgent.CameraCompositeRequest getCameraCompositeRequest();
}
