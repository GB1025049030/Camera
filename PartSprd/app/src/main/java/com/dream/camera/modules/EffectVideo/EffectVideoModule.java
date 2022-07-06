
package com.dream.camera.modules.EffectVideo;

import com.android.camera.CameraActivity;
import com.android.camera.EffectVideoModuleBase;
import com.android.camera.EffectVideoUIBase;
import com.android.camera.VideoModule;
import com.android.camera.VideoUI;
import com.android.camera.app.AppController;
import com.android.camera.util.CameraUtil;
import com.android.camera.util.GservicesHelper;

public class EffectVideoModule extends EffectVideoModuleBase {

    public EffectVideoModule(AppController app) {
        super(app);
    }

    @Override
    public EffectVideoUIBase createUI(CameraActivity activity) {
        return new EffectVideoUI(activity, this, activity.getModuleLayoutRoot());
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
