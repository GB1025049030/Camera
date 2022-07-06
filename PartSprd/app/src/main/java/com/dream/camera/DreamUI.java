
package com.dream.camera;

import android.graphics.Bitmap;

import com.android.camera.CameraActivity;
import com.android.camera.settings.Keys;
import com.android.camera.ui.RotateImageView;
import com.android.camera.util.CameraUtil;
import com.android.camera2.R;
import com.dream.camera.dreambasemodules.DreamInterface;
import com.dream.camera.settings.DataModuleManager;
import com.dream.camera.ui.AdjustFlashPanel.AdjustFlashPanelInterface;
import com.dream.camera.ui.DreamCaptureLayoutHelper;
import com.freeme.camera.widget.PanelManager;
import com.dream.camera.util.DreamUtil;
import com.freeme.camera.ui.PanelLayout;
import com.freeme.camera.util.PanelUtil;

import android.graphics.RectF;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.ArrayList;

public abstract class DreamUI implements AdjustFlashPanelInterface, DreamInterface {

    public final static int UNDEFINED = -1;
    public final static int DREAM_WIDEANGLEPANORAMA_UI = 1;
    public final static int DREAM_FILTER_UI = 2;
    public final static int DREAM_REFOCUS_UI = 3;

    private int mTopPanelValue = -1;

    protected CameraActivity mActivity;

    protected PanelManager mPanelManager;
    protected PanelLayout mTopPanel;
    protected PanelLayout mSidePanel;
    protected DreamCaptureLayoutHelper mCaptureLayoutHelper;

    public DreamUI(CameraActivity activity){
        mActivity = activity;
    }

    public int getUITpye() {
        return UNDEFINED;
    }

    public void showPanels() {
    }

    public void hidePanels() {
    }

    public void onThumbnail(final Bitmap thumbmail) {
    }

    public void adjustUI(int orientation) {
    }

    public void onCloseModeListOrSettingLayout() {
    }

    public boolean isFreezeFrameShow() {
        return false;
    }

    public boolean isReviewImageShow() {
        return false;
    }

    public void updateTopPanelValue(CameraActivity mActivity) {
        DreamUtil dreamUtil = new DreamUtil();
        if (DreamUtil.BACK_CAMERA == dreamUtil.getRightCamera(DataModuleManager
                .getInstance(mActivity).getDataModuleCamera()
                .getInt(Keys.KEY_CAMERA_ID))) {
            mTopPanelValue = DreamUtil.BACK_CAMERA;
        } else {
            mTopPanelValue = DreamUtil.FRONT_CAMERA;
        }
    }

    public int getTopPanelValue() {
        return mTopPanelValue;
    }

    public boolean isSupportSettings() {
        return true;
    }

    public void onDestroy() {
    }

    public void updateAiSceneView(RotateImageView view , int visible , int type) {
    }

    public int getAiSceneTip() {
        return -1;
    }

    public boolean isCountingDown() {
        return false;
    }

    public void onSingleTapUp() {}

    @Override
    public void initializeadjustFlashPanel(int maxValue, int minvalue, int currentValue) {
    }

    @Override
    public void showAdjustFlashPanel() {
    }

    @Override
    public void hideAdjustFlashPanel() {
    }

    @Override
    public void fitTopPanel(ViewGroup topPanelParent) {
        updateTopPanelParentLP(topPanelParent);
        LayoutInflater lf = LayoutInflater.from(topPanelParent.getContext());
        mTopPanel = (PanelLayout) (lf.inflate(R.layout.top_panel_layout, null));
        if (mPanelManager == null) {
            mPanelManager = new PanelManager();
        }
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
        updateTopPanelButtonStatus();
    }

    @Override
    public void fitSidePanel(ViewGroup sidePanelParent) {
        updateSideBottomPanelParentLP(sidePanelParent);
        LayoutInflater lf = LayoutInflater.from(sidePanelParent.getContext());
        mSidePanel = (PanelLayout) (lf.inflate(R.layout.side_panel_layout, null));
        if (mPanelManager == null) {
            mPanelManager = new PanelManager();
        }
        mPanelManager.addPanel(mSidePanel);
        int configID = getSidePanelConfigID();
        if (configID <= 0) {
            return;
        }
        ArrayList<PanelUtil.IconAndDes> list = PanelUtil.generatePanelList(sidePanelParent.getContext(), configID);
        filterSidePanelConfig(list);
        mSidePanel.addButtons(list);
        ((CameraActivity) sidePanelParent.getContext()).getButtonManager().load(mPanelManager.getPanels());
        bindSidePanelButton();
        FrameLayout.LayoutParams lay = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lay.setMargins(0,0,0, mActivity.getCameraAppUI().getCaptureLayoutHelper().getBottomBarMarginBottom());
        sidePanelParent.addView(mSidePanel, lay);
        updateSidePanelStatus();
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

    protected void updateSideBottomPanelParentLP(ViewGroup sideBottomPanelParent) {
        RectF bottomBarRect = mActivity.getCameraAppUI().getCaptureLayoutHelper().getBottomBarRect();
        int marginBottom = sideBottomPanelParent.getContext().getResources().getDimensionPixelOffset(R.dimen.freeme_side_button_margin_bottom);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) sideBottomPanelParent.getLayoutParams();
        params.setMargins(
                params.leftMargin,
                0,
                params.rightMargin,
                CameraUtil.getNormalNavigationBarHeight() + (int)bottomBarRect.height() + marginBottom
        );
        sideBottomPanelParent.setLayoutParams(params);
    }



    public boolean isSidePanelShow() {
        return mPanelManager.isSidePanelShow();
    }

    public void updateSidePanelStatus(){
        mPanelManager.updateSidePanelVisibility();
    }
    public void setFirstSideButtonState(int state) {
        mPanelManager.setFirstSideButtonState(state);
    }

    protected  abstract int getTopPanelConfigID();
    protected  abstract void filterTopPanelConfig(ArrayList<PanelUtil.IconAndDes> list);
    protected  abstract void bindTopPanelButton();
    protected  void updateTopPanelButtonStatus(){};

    protected int getSidePanelConfigID() {return -1; }
    protected void filterSidePanelConfig(ArrayList<PanelUtil.IconAndDes> list) {}
    protected void bindSidePanelButton() {}
    protected  void updateSidePanelButtonStatus(){};
}
