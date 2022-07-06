
package com.dream.camera.modules.slowvideo;

import com.android.camera.CameraActivity;
import com.android.camera.EffectVideoModuleBase;
import com.android.camera.EffectVideoUIBase;
import com.android.camera.app.AppController;
import com.android.camera.util.CameraUtil;
import com.android.camera.util.GservicesHelper;

public class SlowVideoModule
        extends EffectVideoModuleBase {

    private static final String TAG = "SlowVideoModule";
    private SlowVideoUI mSlowVideoUI;

    public SlowVideoModule(AppController app) {
        super(app);
        setSpeed(CameraUtil.SLOW_VIDEO_RECORD_SPEED_DEFAULT);
    }

    @Override
    public EffectVideoUIBase createUI(CameraActivity activity) {
        mSlowVideoUI = new SlowVideoUI(activity, this, activity.getModuleLayoutRoot());
        return mSlowVideoUI;
    }

    @Override
    public boolean useNewApi() {
        return GservicesHelper.useCamera2ApiThroughPortabilityLayer(mActivity.getContentResolver());
    }

    @Override
    public boolean isUseSurfaceView() {
        return CameraUtil.isSurfaceViewAlternativeEnabled();
    }

    @Override
    public boolean isMakeUpEnable() {
        return false;
    }
}
