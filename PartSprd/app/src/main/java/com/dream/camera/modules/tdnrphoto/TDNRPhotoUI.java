package com.dream.camera.modules.tdnrphoto;

import java.util.HashMap;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.android.camera.CameraActivity;
import com.android.camera.PhotoController;
import com.android.camera.PhotoModule;

import com.android.camera.debug.Log;
import com.android.camera.settings.Keys;
import com.android.camera2.R;

import android.widget.FrameLayout;
import com.dream.camera.SlidePanelManager;
import com.android.camera.PhotoUI;
import com.dream.camera.settings.DataModuleBasic.DreamSettingChangeListener;
import com.dream.camera.settings.DataModuleManager;
import com.dream.camera.settings.DataModuleManager.ResetListener;
import com.dream.camera.util.DreamUtil;
import android.hardware.Camera.Face;
import com.android.camera.ui.FaceView;
import android.graphics.RectF;
import com.dream.camera.MakeupController;
import com.dream.camera.MakeupController.*;

public class TDNRPhotoUI extends PhotoUI implements ResetListener {
    private static final Log.Tag TAG = new Log.Tag("TDNRPhotoUI");

    //private ImageButton mSettingsButton;
    private View topPanel;

    public TDNRPhotoUI(CameraActivity activity, PhotoController controller,
            View parent) {
        super(activity, controller, parent);
    }

    @Override
    protected int getTopPanelConfigID() {
        return R.array.tdnr_photo_top_panel;
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

    @Override
    public void onSettingReset() {
    }

    @Override
    public void onResume(){
        DataModuleManager.getInstance(mActivity).addListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        DataModuleManager.getInstance(mActivity).removeListener(this);
    }

    public boolean isInFrontCamera() {
        DreamUtil dreamUtil = new DreamUtil();
        return DreamUtil.FRONT_CAMERA == dreamUtil.getRightCamera(DataModuleManager
                .getInstance(mActivity).getDataModuleCamera()
                .getInt(Keys.KEY_CAMERA_ID));
    }
}
