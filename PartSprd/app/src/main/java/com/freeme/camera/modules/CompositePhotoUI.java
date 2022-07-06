package com.freeme.camera.modules;

import android.view.View;
import android.view.ViewGroup;

import com.android.camera.CameraActivity;
import com.android.camera.PhotoController;
import com.android.camera.PhotoUI;
import com.android.camera.debug.Log;
import com.android.camera.settings.Keys;
import com.dream.camera.settings.DataModuleManager;
import com.dream.camera.util.DreamUtil;

public class CompositePhotoUI extends PhotoUI implements DataModuleManager.ResetListener {
    private static final Log.Tag TAG = new Log.Tag("CompositePhotoUI");

    public CompositePhotoUI(CameraActivity activity, PhotoController controller,
                            View parent) {
        super(activity, controller, parent);
    }

    @Override
    public void fitExtendPanel(ViewGroup extendPanelParent) {
    }

    @Override
    public void updateBottomPanel() {
        super.updateBottomPanel();
    }

    @Override
    public void onSettingReset() {
    }

    @Override
    public void onResume() {
        DataModuleManager.getInstance(mActivity).addListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        DataModuleManager.getInstance(mActivity).removeListener(this);
    }

    public boolean isInFrontCamera() {
        return DreamUtil.FRONT_CAMERA == DreamUtil.getRightCamera(DataModuleManager
                .getInstance(mActivity).getDataModuleCamera()
                .getInt(Keys.KEY_CAMERA_ID));
    }
}
