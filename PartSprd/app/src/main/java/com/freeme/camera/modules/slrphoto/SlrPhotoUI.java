package com.freeme.camera.modules.slrphoto;

import android.graphics.Color;
import android.graphics.RectF;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.android.camera.CameraActivity;
import com.android.camera.PhotoController;
import com.android.camera.PhotoUI;
import com.android.camera.debug.Log;
import com.android.camera.settings.Keys;
import com.android.camera.util.CameraUtil;
import com.android.camera2.R;
import com.dream.camera.SlidePanelManager;
import com.dream.camera.settings.DataModuleManager;
import com.dream.camera.settings.DataModuleManager.ResetListener;
import com.dream.camera.util.DreamUtil;

public class SlrPhotoUI extends PhotoUI implements ResetListener {
    private static final Log.Tag TAG = new Log.Tag("SlrPhotoUI");

    private BVirtualView mBVirtualView;

    private View mTopPanel;
    private DreamUtil mDreamUtil;
    private TextView mTipTextView;

    public SlrPhotoUI(CameraActivity activity, PhotoController controller,
                      View parent) {
        super(activity, controller, parent);
        mDreamUtil = new DreamUtil();
        mActivity.getCameraAppUI().initAiSceneView();

        // Bug 1024253 - NEW FEATURE: Ultra Wide Angle
        if(CameraUtil.isUltraWideAngleEnabled() && DreamUtil.BACK_CAMERA == mDreamUtil.getRightCamera(DataModuleManager
                .getInstance(mActivity).getDataModuleCamera()
                .getInt(Keys.KEY_CAMERA_ID)))
            mActivity.getCameraAppUI().initUltraWideAngleSwitchView(false);
    }

    @Override
    protected int getTopPanelConfigID() {
        return R.array.slr_photo_top_panel;
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
        DreamUtil dreamUtil = new DreamUtil();
        int cameraid = dreamUtil.getRightCamera(DataModuleManager
                .getInstance(mActivity).getDataModuleCamera()
                .getInt(Keys.KEY_CAMERA_ID));
        if (DreamUtil.BACK_CAMERA != cameraid) {
            mBasicModule.updateMakeLevel();
        }
    }

    @Override
    public void onResume(){
        DataModuleManager.getInstance(mActivity).addListener(this);
        createBVirtualView();
    }

    @Override
    public void onPause() {
        super.onPause();
        DataModuleManager.getInstance(mActivity).removeListener(this);

        if(mTipTextView != null){
            mTipTextView.setVisibility(View.GONE);
        }

        if(mActivity != null && mActivity.getModuleLayoutRoot() != null
                && mShowMontionPhotoTipRunnable != null){
            mActivity.getModuleLayoutRoot().removeCallbacks(mShowMontionPhotoTipRunnable);
        }

        destroyBVirtualView();
    }

    @Override
    public void showMotionPhotoTipText(boolean visible){
        if(mActivity == null) return;
        if(mTipTextView == null){
            mTipTextView = (TextView)mActivity.findViewById(R.id.motion_photo_tip);
        }

        Log.d(TAG,"show motionPhotoTip :" + visible);
        if(visible){
            setMotionPhotoTipStyle(mTipTextView, R.string.motion_photo_on,
                    mActivity.getColor(R.color.blur_effect_highlight) , R.drawable.blur_effect_highlight, View.VISIBLE);
        } else {
            setMotionPhotoTipStyle(mTipTextView, R.string.motion_photo_off,
                    Color.WHITE,R.drawable.blur_effect_disable, View.VISIBLE);
        }
    }

    public void setMotionPhotoTipStyle(TextView tipTextView, int message, int mColor, int backgroundResource, int visible){
        if(tipTextView != null){
            tipTextView.setText(message);
            tipTextView.setTextColor(mColor);
            int _pL = tipTextView.getPaddingLeft();
            int _pR = tipTextView.getPaddingLeft();
            int _pT = tipTextView.getPaddingTop();
            int _pB = tipTextView.getPaddingBottom();
            tipTextView.setBackgroundResource(backgroundResource);
            tipTextView.setPadding(_pL, _pT, _pR, _pB);
            tipTextView.setVisibility(visible);
        }

        if(mShowMontionPhotoTipRunnable != null && mActivity != null && mActivity.getModuleLayoutRoot() != null) {
            mActivity.getModuleLayoutRoot().removeCallbacks(mShowMontionPhotoTipRunnable);
            mActivity.getModuleLayoutRoot().postDelayed(mShowMontionPhotoTipRunnable, 3000);
        }
    }

    protected final Runnable mShowMontionPhotoTipRunnable = new Runnable() {
        @Override
        public void run() {
            if (mTipTextView != null) {
                mTipTextView.setVisibility(View.GONE);
            }
        }
    };

    private void createBVirtualView() {
        mBVirtualView = mActivity.findViewById(R.id.bv_view);
        if (mBVirtualView == null) {
            FrameLayout group = mActivity.findViewById(R.id.preview_content_frame);
            if (group != null) {
                mBVirtualView = (BVirtualView) mActivity.getLayoutInflater().inflate(R.layout.freeme_bv_view, group, false);
                group.addView(mBVirtualView);
            }
        } else {
            mBVirtualView.setVisibility(View.VISIBLE);
        }
        mBVirtualView.start();
        mActivity.findViewById(R.id.focus_ring).setVisibility(View.INVISIBLE);
    }

    private void destroyBVirtualView() {
        if (mBVirtualView != null) {
            mBVirtualView.setVisibility(View.GONE);
            mBVirtualView.stop();
        }
        mActivity.findViewById(R.id.focus_ring).setVisibility(View.VISIBLE);
    }

    public BVirtualView getBVirtualView() {
        return mBVirtualView;
    }

    @Override
    public void onPreviewAreaChanged(RectF previewArea) {
        super.onPreviewAreaChanged(previewArea);
        if (mBVirtualView != null) {
            mBVirtualView.onPreviewAreaChanged(previewArea);
        }
    }
}
