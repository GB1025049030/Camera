package com.dream.camera.modules.highresolutionphoto;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.camera.CameraActivity;
import com.android.camera.PhotoController;
import com.android.camera.PhotoUI;
import com.android.camera.debug.Log;
import com.android.camera.settings.Keys;
import com.android.camera.ui.RotateImageView;
import com.android.camera2.R;
import com.dream.camera.ButtonManagerDream;
import com.dream.camera.SlidePanelManager;
import com.dream.camera.settings.DataModuleManager;
import com.dream.camera.settings.DataModuleManager.ResetListener;
import com.dream.camera.util.DreamUtil;

import static com.android.camera.settings.Keys.KEY_CAMERA_BEAUTY_ENTERED;

public class HighResolutionPhotoUI extends PhotoUI implements ResetListener {
    private static final Log.Tag TAG = new Log.Tag("HighResolutionPhotoUI");

    private View topPanel;

    public HighResolutionPhotoUI(CameraActivity activity, PhotoController controller,
                            View parent) {
        super(activity, controller, parent);
        mActivity.getCameraAppUI().initAiSceneView();
    }

    @Override
    public void updateAiSceneView(RotateImageView view , int visible , int index) {
        if (view != null)
            view.setVisibility(View.GONE);
    }

    @Override
    protected int getTopPanelConfigID() {
        return R.array.high_resolution_photo_top_panel;
    }

    @Override
    public void fitExtendPanel(ViewGroup extendPanelParent) {
        if(mBasicModule.isBeautyCanBeUsed()) {
            initMakeupControl(extendPanelParent);
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
}
