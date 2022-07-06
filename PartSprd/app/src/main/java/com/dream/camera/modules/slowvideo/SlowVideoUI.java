
package com.dream.camera.modules.slowvideo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.camera.CameraActivity;
import com.android.camera.EffectVideoUIBase;
import com.android.camera.VideoController;
import com.android.camera2.R;
import com.dream.camera.MakeupController;
import com.dream.camera.SlidePanelManager;

public class SlowVideoUI extends EffectVideoUIBase {

    private static final String TAG = "SlowVideoUI";
    private View topPanel;
    private SlowVideoModule mSlowVieoModule;

    public SlowVideoUI(CameraActivity activity, VideoController controller, View parent) {
        super(activity, controller, parent);
        mSlowVieoModule = (SlowVideoModule) controller;
    }

    @Override
    protected int getTopPanelConfigID() {
        return R.array.effect_video_top_panel;
    }

    @Override
    protected int getSidePanelConfigID() {
        return R.array.effect_video_side_panel;
    }

    @Override
    public void fitExtendPanel(ViewGroup extendPanelParent) {
        if (mController.isMakeUpEnable()) {
                LayoutInflater lf = LayoutInflater.from(mActivity);
                // mFreezeFrame = extendPanelParent;
                View extendPanel = lf.inflate(R.layout.video_extend_panel,
                        extendPanelParent);
                new MakeupController(extendPanel, mController,mActivity);
        }
        super.fitExtendPanel(extendPanelParent);
    }

    @Override
    public void updateBottomPanel() {
        super.updateBottomPanel();
    }

    @Override
    public void updateSlidePanel() {
        if (!mActivity.isSecureCamera()) {
            SlidePanelManager.getInstance(mActivity).udpateSlidePanelShow(
                    SlidePanelManager.SETTINGS,View.VISIBLE);
            SlidePanelManager.getInstance(mActivity).focusItem(
                    SlidePanelManager.CAPTURE, false);
        } else {
            SlidePanelManager.getInstance(mActivity).udpateSlidePanelShow(
                    SlidePanelManager.MODE,View.INVISIBLE);
            SlidePanelManager.getInstance(mActivity).udpateSlidePanelShow(
                    SlidePanelManager.SETTINGS,View.VISIBLE);
            SlidePanelManager.getInstance(mActivity).focusItem(
                    SlidePanelManager.CAPTURE, false);
        }
    }

    @Override
    protected void bindSidePanelButton() { }
}
