
package com.dream.camera.modules.portraitphoto;

import com.dream.camera.modules.blurrefocus.BlurRefocusModule;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewStub;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.net.Uri;

import com.android.camera.CameraActivity;
import com.android.camera.PhotoUI;
import com.android.camera.app.AppController;
import com.android.camera.app.CameraAppUI;
import com.android.camera.app.MediaSaver;
import com.android.camera.data.FilmstripItemData;
import com.android.camera.debug.Log;
import com.android.camera.exif.ExifInterface;
import com.android.camera.settings.Keys;
import com.android.camera.util.CameraUtil;
import com.android.camera.util.Size;
import com.android.camera.util.ToastUtil;
import com.android.camera.widget.ModeOptions;
import com.android.camera.widget.ModeOptionsOverlay;
import com.android.camera2.R;
import com.android.ex.camera2.portability.CameraAgent;
import com.android.ex.camera2.portability.CameraCapabilities;
import com.android.ex.camera2.portability.CameraSettings;
import com.android.camera.PhotoModule;
import com.dream.camera.modules.blurrefocus.BlurRefocusUI;
import com.dream.camera.util.DreamUtil;
import com.sprd.camera.voice.PhotoVoiceMessage;
import com.dream.camera.DreamModule;
import com.dream.camera.BlurRefocusController;
import com.dream.camera.BlurRefocusController.BlurRefocusFNumberListener;
import com.dream.camera.ui.BlurPanel;
import com.dream.camera.ui.VerticalSeekBar;
import com.dream.camera.ui.AdjustPanel;

import com.android.camera.app.OrientationManager;
import com.android.ex.camera2.portability.CameraDeviceInfo.Characteristics;

import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.location.Location;

import java.util.List;
import android.net.Uri;


public class PortraitPhotoModule extends PhotoModule {
    private static final Log.Tag TAG = new Log.Tag("PortraitPhotoModule");

    public PortraitPhotoModule(AppController app) {
        super(app);
    }

    @Override
    public PhotoUI createUI(CameraActivity activity) {
        if (activity == null) {
            return null;
        }
        showBlurRefocusHind();
        return new PortraitPhotoUI(activity, this, activity.getModuleLayoutRoot());
    }

    @Override
    public boolean isSupportTouchAFAE() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isSupportManualMetering() {
        // TODO Auto-generated method stub
        return false;
    }



    public void showBlurRefocusHind() {
        boolean shouldShowBlurRefocusHind = mDataModule.getBoolean(Keys.KEY_CAMERA_BLUR_REFOCUS_HINT);
        if (shouldShowBlurRefocusHind == true) {
            mDataModule.set(Keys.KEY_CAMERA_BLUR_REFOCUS_HINT, false);
        }
    }

    @Override
    public void onLongPress(MotionEvent var1){}

    @Override
    public void onSingleTapUp(View view, int x, int y) { }

    @Override
    protected void requestCameraOpen() {

        mCameraId = DreamUtil.BACK_CAMERA != DreamUtil.getRightCamera(mCameraId) ? CameraUtil.getFrontBlurCameraId()
                :CameraUtil.BACK_PORTRAIT_ID;
        Log.i(TAG, "requestCameraOpen mCameraId:" + mCameraId);
        mActivity.getCameraProvider().requestCamera(mCameraId, useNewApi());

    }

    @Override
    protected void startPreview(boolean optimize) {
        //SPRD:fix Bug746853
        if (mPaused || mCameraDevice == null) {
            Log.i(TAG, "attempted to start preview before camera device");
            // do nothing
            return;
        }
        int deviceOrientation = mAppController.getOrientationManager()
                .getDeviceOrientation().getDegrees();
        if(mCameraDevice != null && mCameraSettings != null) {
            mCameraSettings.setDeviceOrientation(deviceOrientation);
            mCameraDevice.applySettings(mCameraSettings);
        }
        Log.i(TAG, "startPreview deviceOrientation:" + deviceOrientation);

        if(mCameraSettings != null && mCameraDevice != null) {
            mCameraSettings.setFNumberValue(CameraUtil.getPortraitDefaultFNumber());
            Log.i(TAG, "startPreview F_Number:" + CameraUtil.getPortraitDefaultFNumber());
        }

        super.startPreview(optimize);
    }

    @Override
    public void onOrientationChanged(OrientationManager orientationManager, OrientationManager.DeviceOrientation deviceOrientation) {
        int deviceRotation = deviceOrientation.getDegrees();
        Log.i(TAG, "back blur onOrientationChanged  deviceOrientation = " + deviceRotation);
        if(mCameraDevice != null && mCameraSettings != null) {
            mCameraSettings.setDeviceOrientation(deviceRotation);
            mCameraDevice.applySettings(mCameraSettings);
        }
        super.onOrientationChanged(orientationManager, deviceOrientation);
    }

    /*@Override*/
    public void addImage(byte[] data, String title, long date, Location loc, int width, int height,
                         int orientation, ExifInterface exif, MediaSaver.OnMediaSavedListener l) {
        Integer val = exif.getTagIntValue(ExifInterface.TAG_CAMERATYPE_IFD);
        Log.i(TAG, "addImage TAG_CAMERATYPE_IFD val="+val);
        if (val == null || val == 0) {
            mIsBlurRefocusPhoto = true;
        }
        getServices().getMediaSaver().addImage(
                data, title, date, loc, width, height,
                orientation, exif, l,
                FilmstripItemData.MIME_TYPE_JPEG, null);
    }
    /* @} */

    public boolean isUseSurfaceView() {
        return CameraUtil.isSurfaceViewAlternativeEnabled();
    }

    public boolean useNewApi() {
//        if (isUseSurfaceView()) {
//            return true;
//        }
        return true;
    }

    @Override
    public void pause() {
        super.pause();
        mAppController.getCameraAppUI().setRefocusModuleTipVisibility(View.GONE);
        mAppController.getCameraAppUI().setBlurEffectTipVisibity(View.GONE);
    }


    @Override
    protected void updateParametersThumbCallBack() {
        if (CameraUtil.isBlurNeedThumbCallback() &&
                DreamUtil.BACK_CAMERA == DreamUtil.getRightCamera(mCameraId)){
            Log.i(TAG, "setNeedThumbCallBack true ");
            mCameraSettings.setNeedThumbCallBack(true);
        } else {
            Log.i(TAG, "setNeedThumbCallBack false");
            mCameraSettings.setNeedThumbCallBack(false);
        }
    }

    @Override
    public int getModuleType() {
        int s = DreamUtil.BACK_CAMERA == DreamUtil.getRightCamera(mCameraId)
                ? DreamModule.REFOCUS_MODULE:DreamModule.FRONT_REFOCUS_MODULE;

        Log.i(TAG, "getModuleTpye = " + s);

        return s;
    }

}
