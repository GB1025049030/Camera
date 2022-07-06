
package com.dream.camera.modules.slowmotionvideo;

import com.android.camera.CameraActivity;
import com.android.camera.VideoController;
import com.android.camera.VideoUI;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.android.camera2.R;

public class SlowmotionVideoUI extends VideoUI {

    //private ImageButton mSettingsButton;
    private View topPanel;


    public SlowmotionVideoUI(CameraActivity activity, VideoController controller, View parent) {
        super(activity, controller, parent);
    }

    @Override
    protected int getTopPanelConfigID() {
        return R.array.slow_motion_video_top_panel;
    }

    @Override
    public void fitExtendPanel(ViewGroup extendPanelParent) {
        super.fitExtendPanel(extendPanelParent);
    }

    @Override
    public void updateBottomPanel() {
        super.updateBottomPanel();
    }

    @Override
    public void updateSlidePanel() {
        super.updateSlidePanel();
    }

}
