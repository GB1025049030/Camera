
package com.dream.camera.modules.manualphoto;

import android.view.MotionEvent;

import com.android.camera.app.AppController;
import com.android.camera.CameraActivity;
import com.android.camera.PhotoUI;

import com.android.camera.PhotoModule;
import com.dream.camera.ButtonManagerDream;

public class ManualPhotoModule extends PhotoModule {
    public ManualPhotoModule(AppController app) {
        super(app);
    }

    @Override
    public PhotoUI createUI(CameraActivity activity) {
        return new ManualPhotoUI(activity, this, activity.getModuleLayoutRoot());
    }

    public boolean isSupportTouchAFAE() {
        return true;
    }

    public boolean isSupportManualMetering() {
        return true;
    }
    @Override
    public void onLongPress(MotionEvent var1){}
}
