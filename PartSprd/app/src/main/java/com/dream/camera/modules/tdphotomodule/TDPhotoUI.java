
package com.dream.camera.modules.tdphotomodule;

import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.ImageButton;
import com.android.camera.debug.Log;
import com.android.camera2.R;
import com.android.camera.CameraActivity;
import com.android.camera.PhotoController;
import com.android.camera.util.CameraUtil;
import com.android.camera.PhotoUI;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class TDPhotoUI extends PhotoUI {
    private static final Log.Tag TAG = new Log.Tag("TDPhotoUI");
    private ImageButton mSettingsButton;
    private ImageButton mSwitchPreviewButton;
    private View topPanel;
    public TDPhotoUI(CameraActivity activity, PhotoController controller, View parent//,
                     /*MakeupController.MakeupInterface makeupInterface*/) {
        super(activity, controller, parent/*, makeupInterface*/);
    }

    @Override
    protected int getTopPanelConfigID() {
        return R.array.tdnr_photo_top_panel;
    }

    @Override
    public void updateBottomPanel() {
        // TODO Auto-generated method stub
        super.updateBottomPanel();
    }

    @Override
    public void fitExtendPanel(ViewGroup extendPanelParent) {
        // TODO Auto-generated method stub
        if (CameraUtil.isMakeup3DEnable()) {
            LayoutInflater lf = LayoutInflater.from(mActivity);
            // mFreezeFrame = extendPanelParent;
            View extendPanel = lf.inflate(
                    R.layout.tdphoto_front_extend_panel, extendPanelParent);
            //initMakeupControl(extendPanelParent);
        }
        super.fitExtendPanel(extendPanelParent);
    }
}
