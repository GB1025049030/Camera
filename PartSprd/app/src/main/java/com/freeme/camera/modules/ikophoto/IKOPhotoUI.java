package com.freeme.camera.modules.ikophoto;

import android.graphics.RectF;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.camera.CameraActivity;
import com.android.camera.MultiToggleImageButton;
import com.android.camera.PhotoController;
import com.android.camera.PhotoUI;
import com.android.camera.debug.Log;
import com.android.camera.settings.Keys;
import com.android.camera.util.CameraUtil;
import com.android.camera2.R;
import com.dream.camera.ButtonManagerDream;
import com.dream.camera.settings.DataModuleManager;
import com.dream.camera.util.DreamUtil;
import com.freeme.camera.common.network.NetworkConnectStateManager;
import com.freeme.camera.common.network.NetworkUtils;

public class IKOPhotoUI extends PhotoUI {

    private static final Log.Tag TAG = new Log.Tag("IKOPhotoUI");
    private View topPanel;
    private RelativeLayout mParent;
    private TextView mTipsView;
    private TextView mIdentifier;
    private MultiToggleImageButton mFlashButton;

    private int mPreviewWidth;
    private int mPreviewHeight;

    private NetworkConnectStateManager.NetworkConnectStateChangeListener mNCSListener;

    public IKOPhotoUI(CameraActivity activity, PhotoController controller, View parent) {
        super(activity, controller, parent);
        mNCSListener = () -> updateViewStatus();
    }

    @Override
    protected int getTopPanelConfigID() {
        return R.array.iko_photo_top_panel;
    }

    @Override
    public void fitExtendPanel(ViewGroup extendPanelParent) {
        super.fitExtendPanel(extendPanelParent);
    }

    @Override
    public void onPreviewAreaChanged(RectF previewArea) {
        super.onPreviewAreaChanged(previewArea);
        mPreviewHeight = (int) (previewArea.right - previewArea.left);
        mPreviewWidth = (int) (previewArea.bottom - previewArea.top);
        if (mParent != null) {
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mParent.getLayoutParams();
            params.setMargins((int) previewArea.left, (int) previewArea.top + CameraUtil.getCutoutHeight(), 0, 0);
            mParent.setLayoutParams(params);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateViewStatus();
        NetworkConnectStateManager.I.addListener(mActivity.getApplication(), mNCSListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        NetworkConnectStateManager.I.removeListener(mNCSListener);
        getModuleRoot().removeView(mParent);
        mParent = null;
    }

    public MultiToggleImageButton getFlashButton() {
        return mFlashButton;
    }

    public int getPreviewWidth() {
        return mPreviewWidth;
    }

    public int getPreviewHeight() {
        return mPreviewHeight;
    }

    private void updateViewStatus() {
        if (mParent == null || mTipsView == null || mIdentifier == null) {
            mActivity.getLayoutInflater().inflate(
                    R.layout.freeme_iko_preview_after_layout, getModuleRoot(), true);
            mParent = getModuleRoot().findViewById(R.id.freeme_iko_layout);
            mTipsView = getModuleRoot().findViewById(R.id.no_network_tips);
            mIdentifier = getModuleRoot().findViewById(R.id.is_identifier);
        }
        boolean isConnect = NetworkUtils.isConnect();
        mTipsView.setVisibility(isConnect ? View.GONE : View.VISIBLE);
        mIdentifier.setVisibility(isConnect ? View.VISIBLE : View.GONE);
    }
}
