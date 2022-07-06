
package com.dream.camera.modules.intentcapture;

import java.util.HashMap;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.FrameLayout;

import com.android.camera.CameraActivity;
import com.android.camera.PhotoController;
import com.dream.camera.SlidePanelManager;
import com.dream.camera.util.DreamUtil;
import com.android.camera2.R;
import android.graphics.RectF;
import android.hardware.Camera.Face;
import com.android.camera.debug.Log;
import com.android.camera.settings.Keys;
import com.android.camera.ui.FaceView;
import com.android.camera.PhotoUI;
import com.dream.camera.settings.DataModuleManager;

public class DreamIntentCaptureUI extends PhotoUI {
    private static final Log.Tag TAG = new Log.Tag("DreamIntentCaptureUI");
    private View topPanel;

    public DreamIntentCaptureUI(CameraActivity activity, PhotoController controller,
            View parent) {
        super(activity, controller, parent);
    }

    @Override
    protected int getTopPanelConfigID() {
        return R.array.intent_capture_photo_top_panel;
    }

    @Override
    protected int getSidePanelConfigID() {
        return R.array.intent_photo_side_panel;
    }

    @Override
    public void fitExtendPanel(ViewGroup extendPanelParent) {
        if(mBasicModule.isBeautyCanBeUsed()) {
            initMakeupControl(extendPanelParent);
        }
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


    public boolean isInFrontCamera() {
        DreamUtil dreamUtil = new DreamUtil();
        return DreamUtil.FRONT_CAMERA == dreamUtil.getRightCamera(DataModuleManager
                .getInstance(mActivity).getDataModuleCamera()
                .getInt(Keys.KEY_CAMERA_ID));
    }

    @Override
    public boolean isSupportSettings() {
        return false;
    }

}
