package com.freeme.camera.modules.openglfilter.depthblurphoto;

import android.util.Log;
import android.view.View;

import com.android.camera.CameraActivity;
import com.android.camera.PhotoUI;
import com.android.camera.app.AppController;
import com.freeme.camera.modules.openglfilter.GLPhotoModule;

public class DepthBlurPhotoModule extends GLPhotoModule {
    public DepthBlurPhotoModule(AppController app) {
        super(app);
    }

    @Override
    public PhotoUI createUI(CameraActivity activity) {
        return new DepthBlurPhotoUI(activity, this, activity.getModuleLayoutRoot());
    }

    @Override
    public void onSingleTapUp(View view, int x, int y) {
        super.onSingleTapUp(view, x, y);
        y = Math.round(y - mActivity.getCameraAppUI().getPreviewArea().top);
        if (mUI instanceof DepthBlurPhotoUI) {
            DepthBlurPhotoUI depthBlurPhotoUI = (DepthBlurPhotoUI) mUI;
            depthBlurPhotoUI.getFilterController().setBlurCenterPoint(
                    x * 1f / depthBlurPhotoUI.getPreviewSurfaceView().getWidth(),
                    1f - y * 1f / depthBlurPhotoUI.getPreviewSurfaceView().getHeight()
            );
        }
    }
}
