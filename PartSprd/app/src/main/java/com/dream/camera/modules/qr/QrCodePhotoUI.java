package com.dream.camera.modules.qr;

import com.android.camera.CameraActivity;
import com.android.camera.debug.Log;
import com.android.camera.settings.Keys;
import com.dream.camera.settings.DataModuleManager;
import com.dream.camera.util.DreamUtil;
import com.android.camera2.R;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import com.dream.camera.ButtonManagerDream;
import com.freeme.camera.widget.PanelManager;
import com.freeme.camera.ui.PanelLayout;
import com.freeme.camera.util.PanelUtil;

import java.util.ArrayList;

public class QrCodePhotoUI extends ReusePhotoUI{
    private static final Log.Tag TAG = new Log.Tag("QrCodePhotoUI");
    private View topPanel;
    private ImageButton mQrCodeGallery;
    private PanelLayout mTopPanel;
    private PanelManager mPanelManager;

    public QrCodePhotoUI(CameraActivity activity, ReuseController controller,
            View parent) {
        super(activity, controller, parent);
        ViewGroup moduleRoot = (ViewGroup) mRootView.findViewById(R.id.module_layout);
        mActivity.getLayoutInflater().inflate(R.layout.qrcode_capture,
                 moduleRoot, true);
    }

    @Override
    public void fitTopPanel(ViewGroup topPanelParent) {
        updateTopPanelParentLP(topPanelParent);
        LayoutInflater lf = LayoutInflater.from(topPanelParent.getContext());
        mTopPanel = (PanelLayout) (lf.inflate(R.layout.top_panel_layout, null));
        mPanelManager = new PanelManager();
        mPanelManager.addPanel(mTopPanel);
        int configID = getTopPanelConfigID();
        ArrayList<PanelUtil.IconAndDes> list = PanelUtil.generatePanelList(topPanelParent.getContext(), configID);
        filterTopPanelConfig(list);
        mTopPanel.addButtons(list);
        ((CameraActivity) topPanelParent.getContext()).getButtonManager().load(mPanelManager.getPanels());
        bindTopPanelButton();
        FrameLayout.LayoutParams lay = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lay.gravity = Gravity.CENTER_VERTICAL;
        topPanelParent.addView(mTopPanel, lay);
    }

    protected int getTopPanelConfigID() {
        return R.array.qr_code_photo_top_panel;
    }

    protected void filterTopPanelConfig(ArrayList<PanelUtil.IconAndDes> list) {
        // camera
        updateTopPanelCamera(list);
    }

    private void updateTopPanelCamera(ArrayList<PanelUtil.IconAndDes> list) {
        boolean isCameraSupported = ((ButtonManagerDream)mActivity.getButtonManager()).mIsTogglable;
        int index = getIdIndex(R.integer.camera_toggle_button_dream,list);
        if(index != -1){
            if(!isCameraSupported){
                list.remove(index);
            }
        }
    }

    protected int getIdIndex(int id, ArrayList<PanelUtil.IconAndDes> list){
        int index = -1;
        for (int i = 0; i < list.size(); i++){
            PanelUtil.IconAndDes temp = list.get(i);
            if(temp.id == id ){
                index = i;
            }
        }
        return index;
    }

    protected void bindTopPanelButton() {
        bindFlashButton();
        bindCameraButton();
        mQrCodeGallery = (ImageButton)mTopPanel.getPanelButton(R.integer.qrcode_gallery_toggle_button);
        bindQrCodeButton(mQrCodeGallery);
        mActivity.getCurrentModule().updateBatteryLevel(mActivity.getCurrentBattery());
    }

    public void bindFlashButton() {
        if(mTopPanel.isContain(R.integer.gif_photo_flash_toggle_button_dream)){
            ((ButtonManagerDream) mActivity.getButtonManager()).initializeButton(
                    ButtonManagerDream.BUTTON_GIF_PHOTO_FLASH_DREAM, null);
        }
    }

    public void bindCameraButton() {
        if(mTopPanel.isContain(R.integer.camera_toggle_button_dream)){
            ((ButtonManagerDream) mActivity.getButtonManager()).initializeButton(
                    ButtonManagerDream.BUTTON_CAMERA_DREAM, mBasicModule.mCameraCallback);

        }
    }

    @Override
    public void fitExtendPanel(ViewGroup extendPanelParent) {
        if (extendPanelParent != null) {
            mActivity.getCameraAppUI().updateExtendLayoutParams(extendPanelParent, 1000);
        }
    }

    @Override
    public void fitSidePanel(ViewGroup sidePanelParent) {

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
    public void setDisplayOrientation(int orientation) {
        super.setDisplayOrientation(orientation);
    }

    @Override
    public void setButtonVisibility(int buttonId, int visibility) {
        ((ButtonManagerDream)mActivity.getButtonManager()).setButtonVisibility(buttonId,visibility);
    }

    @Override
    public void onResume(){}

    @Override
    public void onPause() {
        super.onPause();
    }

    protected void updateTopPanelParentLP(ViewGroup topPanelParent) {
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) topPanelParent.getLayoutParams();
        params.setMargins(
                params.leftMargin,
                mActivity.getCameraAppUI().getCaptureLayoutHelper().getTopPanelMarginTop(),
                params.rightMargin,
                params.bottomMargin
        );
        topPanelParent.setLayoutParams(params);
    }

    public boolean isInFrontCamera() {
        DreamUtil dreamUtil = new DreamUtil();
        return DreamUtil.FRONT_CAMERA == dreamUtil.getRightCamera(DataModuleManager
                .getInstance(mActivity).getDataModuleCamera()
                .getInt(Keys.KEY_CAMERA_ID));
    }

}
