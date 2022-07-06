package com.dream.camera.modules.portraitphoto;

import com.dream.camera.modules.blurrefocus.BlurRefocusUI;
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
import com.dream.camera.settings.DataModuleManager;



public class PortraitPhotoUI extends PhotoUI  {
    private static final Log.Tag TAG = new Log.Tag("PortraitPhotoUI");
    private View topPanel;
    public PortraitPhotoUI(CameraActivity activity, PhotoController controller, View parent//,
                     /*MakeupController.MakeupInterface makeupInterface*/) {
        super(activity, controller, parent/*, makeupInterface*/);
    }

    @Override
    protected int getTopPanelConfigID() {
        return R.array.portrait_photo_top_panel;
    }

    @Override
    public void onPreviewStarted() {
        super.onPreviewStarted();
//        showFaceDetectedTips(true);
    }
    @Override
    protected void showFaceDetectedTips(boolean show){
        Log.e(TAG, " faces show  = " + show, new Throwable());
        if(show){
            mHdrTips.setText(R.string.portrait_detect_tips);
            mHdrTips.setVisibility(View.VISIBLE);
        } else {
            mHdrTips.setVisibility(View.GONE);
        }
    }

    @Override
    public void fitExtendPanel(ViewGroup extendPanelParent) {
        if(mBasicModule.isBeautyCanBeUsed() &&
                DataModuleManager.getInstance(mActivity).getCurrentDataModule().
                        isEnableSettingConfig(Keys.KEY_MAKE_UP_DISPLAY) ) {
            initMakeupControl(extendPanelParent);
        }
        super.fitExtendPanel(extendPanelParent);
    }
}
