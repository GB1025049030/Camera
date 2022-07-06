package com.dream.camera.modules.ultrawideangle;

import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.ImageButton;
import com.android.camera.debug.Log;
import com.android.camera2.R;
import com.android.camera.CameraActivity;
import com.android.camera.MultiToggleImageButton;
import com.android.camera.PhotoController;
import com.android.camera.settings.Keys;
import com.android.camera.util.CameraUtil;
import com.dream.camera.BlurRefocusController;
import com.android.camera.PhotoUI;
import com.dream.camera.settings.DataModuleManager;
import com.dream.camera.util.DreamUtil;
import com.dream.camera.SlidePanelManager;

import android.hardware.Camera.Face;
import com.android.ex.camera2.portability.CameraAgent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class UltraWideAngleUI extends PhotoUI {

    private static final Log.Tag TAG = new Log.Tag("UltraWideAngleUI");
    private View topPanel;
    private DreamUtil dreamUtil;

    public UltraWideAngleUI(CameraActivity activity, PhotoController controller, View parent) {
        super(activity, controller, parent);

        // Bug 1024253 - NEW FEATURE: Ultra Wide Angle
        dreamUtil = new DreamUtil();

        if(DreamUtil.BACK_CAMERA == dreamUtil.getRightCamera(DataModuleManager
                .getInstance(mActivity).getDataModuleCamera()
                .getInt(Keys.KEY_CAMERA_ID)))
            mActivity.getCameraAppUI().initUltraWideAngleSwitchView(true);
    }

    @Override
    public void fitExtendPanel(ViewGroup extendPanelParent) {
        if(mBasicModule.isBeautyCanBeUsed()) {
            initMakeupControl(extendPanelParent);
        }
        super.fitExtendPanel(extendPanelParent);
    }

    @Override
    protected int getTopPanelConfigID() {
        return R.array.ultra_wide_angle_photo_top_panel;
    }

    //    @Override
//    public void updateSlidePanel() {
//        if (!mActivity.isSecureCamera()) {
//            SlidePanelManager.getInstance(mActivity).udpateSlidePanelShow(
//                    SlidePanelManager.SETTINGS,View.VISIBLE);
//            SlidePanelManager.getInstance(mActivity).focusItem(
//                    SlidePanelManager.CAPTURE, false);
//        } else {
//            SlidePanelManager.getInstance(mActivity).udpateSlidePanelShow(
//                    SlidePanelManager.MODE,View.INVISIBLE);
//            SlidePanelManager.getInstance(mActivity).udpateSlidePanelShow(
//                    SlidePanelManager.SETTINGS,View.VISIBLE);
//            SlidePanelManager.getInstance(mActivity).focusItem(
//                    SlidePanelManager.CAPTURE, false);
//        }
//    }

    @Override
    public void updateBottomPanel() {
        // TODO Auto-generated method stub
        super.updateBottomPanel();
    }

//    @Override
//    public void fitExtendPanel(ViewGroup extendPanelParent) {
//        // TODO Auto-generated method stub
//        if (CameraUtil.getCurrentBackBlurRefocusVersion() == CameraUtil.BLUR_REFOCUS_VERSION_8) {
//            return;
//        } else {
//            LayoutInflater lf = LayoutInflater.from(mActivity);
//            View extendPanel = lf.inflate(
//                    R.layout.blur_refocus_photo_extend_panel, extendPanelParent);
//            initBlurRefocusControl(extendPanelParent);
//        }
//    }

    public void onPause() {
        super.onPause();
    }

    @Override
    public void onPreviewStarted() {
        super.onPreviewStarted();
    }
}
