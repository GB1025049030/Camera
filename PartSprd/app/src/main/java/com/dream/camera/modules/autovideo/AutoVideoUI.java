
package com.dream.camera.modules.autovideo;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.android.camera.settings.Keys;
import com.android.camera.util.CameraUtil;
import com.android.camera2.R;
import com.android.camera.CameraActivity;
import com.android.camera.VideoController;
import com.dream.camera.MakeupController;
import com.dream.camera.SlidePanelManager;
import com.dream.camera.MakeupController.MakeupListener;
import com.android.camera.VideoUI;
import com.dream.camera.settings.DataModuleManager;
import com.dream.camera.util.DreamUtil;

public class AutoVideoUI extends VideoUI {


    //private ImageButton mSettingsButton;
    private View topPanel;

    public AutoVideoUI(CameraActivity activity, VideoController controller, View parent) {
        super(activity, controller, parent);
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
    protected int getTopPanelConfigID() {
        return R.array.auto_video_top_panel;
    }

    @Override
    protected int getSidePanelConfigID() {
        return R.array.auto_video_side_panel;
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
}
