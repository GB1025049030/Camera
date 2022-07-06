
package com.dream.camera.modules.autovideo;

import com.android.camera.app.AppController;
import com.android.camera.CameraActivity;
import com.android.camera.VideoUI;
import com.android.camera.util.CameraUtil;
import com.android.camera.util.GservicesHelper;

import com.android.camera.VideoModule;

public class AutoVideoModule extends VideoModule {

    public AutoVideoModule(AppController app) {
        super(app);
    }

    @Override
    public VideoUI createUI(CameraActivity activity) {
        return new AutoVideoUI(activity, this, activity.getModuleLayoutRoot());
    }

    @Override
    public boolean useNewApi() {
        return GservicesHelper.useCamera2ApiThroughPortabilityLayer(mActivity.getContentResolver());
    }

    @Override
    public boolean isUseSurfaceView() {
        return CameraUtil.isSurfaceViewAlternativeEnabled();
    }
}
