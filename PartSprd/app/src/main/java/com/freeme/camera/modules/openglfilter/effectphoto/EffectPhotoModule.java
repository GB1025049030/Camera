package com.freeme.camera.modules.openglfilter.effectphoto;

import com.android.camera.CameraActivity;
import com.android.camera.PhotoUI;
import com.android.camera.app.AppController;
import com.android.ex.camera2.portability.CameraAgent;
import com.freeme.camera.modules.openglfilter.GLPhotoModule;
public class EffectPhotoModule extends GLPhotoModule {
    private EffectPhotoUI mEffectPhotoUI;
    public EffectPhotoModule(AppController app) {
        super(app);
    }
    @Override
    public PhotoUI createUI(CameraActivity activity) {
        return new EffectPhotoUI(activity, this, activity.getModuleLayoutRoot());
    }
    @Override
    public void init(CameraActivity activity, boolean isSecureCamera, boolean isCaptureIntent) {
        super.init(activity, isSecureCamera, isCaptureIntent);
        mEffectPhotoUI = (EffectPhotoUI) mUI;
    }
    @Override
    public void doStartPreview(int orientation, boolean isFacingFront, int width, int height) {
        super.doStartPreview(orientation, isFacingFront, width, height);
        //mEffectPhotoUI.getEffectRenderHelper().setImageSize(width, height);
        //mEffectPhotoUI.getEffectRenderHelper().adjustTextureBuffer(orientation, isFacingFront, false);
    }
}
