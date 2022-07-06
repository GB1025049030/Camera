package com.freeme.camera.modules.openglfilter;

import android.content.Context;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;

import com.android.camera.CameraActivity;
import com.android.camera.PhotoController;
import com.android.camera.PhotoUI;
import com.android.camera2.R;
import com.binbin.camera.listener.ICameraRenderLifecycle;
import com.binbin.camera.listener.IRenderManager;
import com.binbin.camera.listener.IRenderManagerCallback;
import com.dream.camera.ui.DreamCaptureLayoutHelper;

public class GLPhotoUI extends PhotoUI implements ICameraRenderLifecycle, IRenderManagerCallback{

    private TextureView mPreviewSurfaceView;

    public GLPhotoUI(CameraActivity activity, PhotoController controller, View parent) {
        super(activity, controller, parent);
        if (mBasicModule.isUseGLFilter()) {
            mActivity.getFilterManager().getBuilder()
                    .setCameraSurfaceParent(mActivity.getCameraAppUI().getGLPreviewRoot())
                    .setOnFpsChangeListener(null)
                    //.setCameraPictureCallback(jpeg -> mBasicModule.onPictureTaken(jpeg))
                    //.setCameraPreviewCallback(bitmap -> ((GLPhotoModule) mBasicModule).freezeScreen(bitmap))
                    .setRenderManagerCallback(this)
                    .setCameraRenderLifecycle(this)
                    .prepared();
            mPreviewSurfaceView = mActivity.getFilterManager().getGLDisplaySurface();
            updateGLPreviewLayoutParams();
        }
    }

    public void updateGLPreviewLayoutParams() {
        DreamCaptureLayoutHelper helper = mActivity.getCameraAppUI().getCaptureLayoutHelper();
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mActivity.getCameraAppUI().getGLPreviewRoot().getLayoutParams();
        params.setMargins(0, (int) helper.getPreviewRect().top, 0, 0);
        mActivity.getCameraAppUI().getGLPreviewRoot().setLayoutParams(params);
    }

    @Override
    protected int getTopPanelConfigID() {
        return R.array.gl_photo_top_panel;
    }

    public TextureView getPreviewSurfaceView() {
        return mPreviewSurfaceView;
    }

    @Override
    public void addCustomFilters(Context context, IRenderManager renderManager) {

    }

    @Override
    public void onSurfaceCreated() {

    }

    @Override
    public void onSurfaceChanged(int i, int i1) {

    }

    @Override
    public void onSurfaceDestroyed() {

    }
}