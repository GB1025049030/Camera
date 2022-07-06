package com.dream.camera.modules.tdnrphoto;

import com.android.camera.CameraActivity;
import com.android.camera.debug.Log;
import com.android.camera.util.CameraUtil;
import com.android.camera.PhotoUI;
import com.android.camera.app.AppController;
import com.android.camera.PhotoModule;


public class TDNRPhotoModule extends PhotoModule {
    private static final Log.Tag TAG = new Log.Tag("TDNRPhotoModule");

    public TDNRPhotoModule(AppController app) {
        super(app);
    }

    @Override
    public PhotoUI createUI(CameraActivity activity) {
        return new TDNRPhotoUI(activity, this, activity.getModuleLayoutRoot());
    }

    public boolean isSupportTouchAFAE() {
        return true;
    }

    public boolean isSupportManualMetering() {
        return false;
    }

    @Override
    protected void updateParameters3DNR() {
        if (!CameraUtil.is3DNREnable()) {
            return;
        }

        Log.i(TAG, "updateParameters3DNR set3DNREnable : 1");
        mCameraSettings.set3DNREnable(1);
    }

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
        Log.i(TAG, "TDNR setZslModeEnable: 0");
        mCameraSettings.setZslModeEnable(0);
    }

    public int getModuleType() {
        return TDNR_PHOTO_MODULE;
    }
}

