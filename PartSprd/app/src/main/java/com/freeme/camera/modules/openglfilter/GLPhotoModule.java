package com.freeme.camera.modules.openglfilter;

import android.graphics.Bitmap;
import android.view.ViewStub;

import com.android.camera.CameraActivity;
import com.android.camera.PhotoModule;
import com.android.camera.PhotoUI;
import com.android.camera.app.AppController;
import com.android.camera.util.CameraUtil;
import com.android.camera2.R;
import com.android.ex.camera2.portability.CameraAgent;

public class GLPhotoModule extends PhotoModule {

    private static final String TAG = "GLPhotoModule";

    public GLPhotoModule(AppController app) {
        super(app);
    }

    @Override
    public PhotoUI createUI(CameraActivity activity) {
        ViewStub viewStubAdjustPanel = activity.findViewById(R.id.layout_ae_lock_panel_id);
        if (viewStubAdjustPanel != null) {
            viewStubAdjustPanel.inflate();
        }
        return new GLPhotoUI(activity, this, activity.getModuleLayoutRoot());
    }

    @Override
    public int getModuleType() {
        return FreemeModule.GL_FILTER_MODULE;
    }

    @Override
    protected void switchCamera() {
        freezeScreen(CameraUtil.isFreezeBlurEnable(), CameraUtil.isSwitchAnimationEnable());
        mHandler.post(() -> {
            if (!mPaused) {
                mActivity.switchFrontAndBackMode();
            }
        });
    }

    @Override
    public void freezeScreen(boolean needBlur, boolean needSwitch) {
        mActivity.runOnUiThread(() -> {
            Bitmap freezeBitmap = ((GLPhotoUI) mUI).getPreviewSurfaceView().getBitmap();
            if (needBlur || needSwitch) {
                freezeBitmap = CameraUtil.blurBitmap(CameraUtil.computeScale(freezeBitmap, 0.2f), mActivity);
            }

            if (needSwitch) {
                mAppController.getCameraAppUI().startSwitchAnimation(freezeBitmap);
            }
            mAppController.freezeScreenUntilPreviewReady(freezeBitmap);
        });
    }

    @Override
    protected void doStartPreview(CameraAgent.CameraStartPreviewCallback startPreviewCallback, CameraAgent.CameraProxy cameraDevice) {
        super.doStartPreview(startPreviewCallback, cameraDevice);
        com.android.ex.camera2.portability.Size size = mCameraSettings.getCurrentPreviewSize();
        doStartPreview(mDisplayOrientation, isCameraFrontFacing(), size.height(), size.width());
    }

    public void doStartPreview(int orientation, boolean isFacingFront, int width, int height) {
        mActivity.getFilterManager().getBuilder().startWithPreviewSize(width, height);
    }

    @Override
    public void stopPreview() {
        super.stopPreview();
        mActivity.getFilterManager().release();
    }

    @Override
    public void pause() {
        super.pause();
        mActivity.getFilterManager().releaseAll();
    }

    @Override
    public boolean isUseSurfaceView() {
        return false;
    }
}