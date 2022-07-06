
package com.dream.camera.modules.intentvideo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.camera.CameraActivity;
import com.android.camera.VideoController;

import com.android.camera.settings.Keys;
import com.android.camera2.R;

import com.android.camera.VideoUI;
import com.dream.camera.settings.DataModuleManager;
import com.dream.camera.util.DreamUtil;

public class DreamIntentVideoUI extends VideoUI {

    private View topPanel;

    public DreamIntentVideoUI(CameraActivity activity, VideoController controller,
            View parent) {
        super(activity, controller, parent);
    }

    @Override
    protected int getTopPanelConfigID() {
        return R.array.intent_video_top_panel;
    }

    @Override
    public void fitExtendPanel(ViewGroup extendPanelParent) {
        super.fitExtendPanel(extendPanelParent);
    }

    @Override
    public void updateSlidePanel() {
        mActivity.getCameraAppUI().hideSlide();
    }

    @Override
    public void updateBottomPanel() {
        mActivity.getCameraAppUI().hideBottomPanelLeft();
    }

    @Override
    public boolean isSupportSettings() {
        return false;
    }

}
