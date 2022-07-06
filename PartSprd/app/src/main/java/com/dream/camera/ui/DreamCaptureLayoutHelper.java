
package com.dream.camera.ui;

import android.util.DisplayMetrics;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;

import com.android.camera.CameraActivity;
import com.android.camera.CaptureLayoutHelper;
import com.android.camera.TextureViewHelper;
import com.android.camera.debug.Log;
import com.android.camera.util.CameraUtil;

import com.android.camera2.R;
import com.freeme.utils.FreemeUIPositionHelp;

public class DreamCaptureLayoutHelper extends CaptureLayoutHelper {

    private static final Log.Tag TAG = new Log.Tag("CaptureLayoutHelper");

    private CameraActivity mActivity;
    private FrameLayout mBottomFrame;
    private FrameLayout mTopPanelParent;
    private int mTopHeight;
    private int mMarginFinal = -1;
    private int mModeSwitchHeight;
    private int mBottomBarMarginBottom = 0;
    private int mBottomAreaHeight = 0;

    public DreamCaptureLayoutHelper(int bottomBarMinHeight, int bottomBarMaxHeight,
                                    int bottomBarOptimalHeight) {
        super(bottomBarMinHeight, bottomBarMaxHeight, bottomBarOptimalHeight);
    }

    @Override
    protected PositionConfiguration getPositionConfiguration(int width, int height,
                                                             float previewAspectRatio, int rotation) {
        if (true) {
            return getFreemePositionConfiguration(width, height, previewAspectRatio);
        }
        PositionConfiguration config = new PositionConfiguration();
        mBottomFrame = (FrameLayout) mActivity.findViewById(R.id.bottom_frame);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mBottomFrame.getLayoutParams();
        //SPRD: Add landscape for preview.the preview UI appear chaotic when from the filmstrip back with horizontal screen
        boolean landscape = width > height;
        int bottomHeight = 0;
        int slideHeight = 0;
        if (mActivity.getResources() != null) { // Bug 1159299 (REVERSE_INULL)
            mTopHeight = mActivity.getResources().getDimensionPixelSize(R.dimen.top_panel_height);
            bottomHeight = mActivity.getResources().getDimensionPixelSize(R.dimen.bottom_bar_height);
            slideHeight = mActivity.getResources().getDimensionPixelSize(R.dimen.slide_panel_height);
            final DisplayMetrics metrics = new DisplayMetrics();
            mActivity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        }

        FreemeUIPositionHelp.I.create(mActivity, width, height);
        config.mBottomBarRect.set(FreemeUIPositionHelp.I.getBottomBarArea(landscape));

        int navigationBarHeight = 0;
        if (CameraUtil.isNavigationEnable()) {
            navigationBarHeight = CameraUtil.getNormalNavigationBarHeight();
        }
        layoutParams.setMargins(0, 0, 0, 0);
        layoutParams.height = FreemeUIPositionHelp.I.getBottomBarArea(landscape).height() + navigationBarHeight;
        mBottomFrame.setLayoutParams(layoutParams);

        if (previewAspectRatio == TextureViewHelper.MATCH_SCREEN) {
            config.mPreviewRect.set(0, 0, width, height);
        } else {
            if (previewAspectRatio < 1) {
                previewAspectRatio = 1 / previewAspectRatio;
            }
            // Get the bottom bar width and height.
            float barSize;
            int longerEdge = Math.max(width, height);
            int shorterEdge = Math.min(width, height);

            // Check the remaining space if fit short edge.
            float spaceNeededAlongLongerEdge = shorterEdge * previewAspectRatio;
            float remainingSpaceAlongLongerEdge = longerEdge - spaceNeededAlongLongerEdge;

            float previewShorterEdge;
            float previewLongerEdge;

            if (remainingSpaceAlongLongerEdge <= 0) {
                // Preview aspect ratio > screen aspect ratio: fit longer edge.
                previewLongerEdge = longerEdge;
                previewShorterEdge = longerEdge / previewAspectRatio;
                barSize = bottomHeight - slideHeight;
                config.mBottomBarOverlay = true;

                if (landscape) {
                    config.mPreviewRect.set(0, height / 2 - previewShorterEdge / 2, previewLongerEdge,
                            height / 2 + previewShorterEdge / 2);
                } else {
                    config.mPreviewRect.set(width / 2 - previewShorterEdge / 2, 0,
                            width / 2 + previewShorterEdge / 2, previewLongerEdge);
                }
            } else {
                config.mPreviewRect.set(FreemeUIPositionHelp.I.getPreviewArea(previewAspectRatio, landscape));
                config.mBottomBarOverlay = previewAspectRatio > 14f / 9f;
            }
        }

        round(config.mBottomBarRect);
        round(config.mPreviewRect);

        return config;
    }

    protected PositionConfiguration getFreemePositionConfiguration(int width, int height, float previewAspectRatio) {
        PositionConfiguration config = new PositionConfiguration();
        boolean isLandscape = width > height;
        int modeSwitchHeight = 0;
        int mBottomBarMinHeight = 0;
        if (mActivity.getResources() != null) {
            mTopHeight = mActivity.getResources().getDimensionPixelSize(R.dimen.top_panel_height);
            modeSwitchHeight = getModeSwitchHeight();
            mBottomBarMinHeight = mActivity.getResources().getDimensionPixelSize(R.dimen.dream2_bottom_bar_height_min);
        }
        boolean navgEnable = CameraUtil.isNavigationEnable();
        int navgH = navgEnable ? CameraUtil.getNormalNavigationBarHeight() : 0;


        int longerEdge = Math.max(width, height);
        int shorterEdge = Math.min(width, height);

        float spaceNeededAlongLongerEdge = shorterEdge * previewAspectRatio;//936
        float remainingSpaceAlongLongerEdge = longerEdge - spaceNeededAlongLongerEdge;//604
        float remainingSpaceAlongLongerForCommon = longerEdge - shorterEdge * 4f / 3;//604
        float bottomBarHeight = remainingSpaceAlongLongerForCommon - mTopHeight - navgH;

        if (previewAspectRatio != 0 && previewAspectRatio < 1) {
            previewAspectRatio = 1 / previewAspectRatio;
        }
        float previewShorterEdge;
        float previewLongerEdge;
        if (remainingSpaceAlongLongerEdge <= 0) {
            previewLongerEdge = longerEdge;
            previewShorterEdge = longerEdge / previewAspectRatio;
            config.mBottomBarOverlay = true;

            if (bottomBarHeight <= mBottomBarMinHeight) {
                bottomBarHeight += modeSwitchHeight;
            }

            if (isLandscape) {
                config.mPreviewRect.set(0, height / 2f - previewShorterEdge / 2, previewLongerEdge,
                        height / 2f + previewShorterEdge / 2);
                config.mBottomBarRect.set(width - bottomBarHeight - navgH, height / 2f - previewShorterEdge / 2,
                        width, height / 2f + previewShorterEdge / 2);
            } else {
                config.mPreviewRect.set(width / 2f - previewShorterEdge / 2, 0,
                        width / 2f + previewShorterEdge / 2, previewLongerEdge);
                config.mBottomBarRect.set(width / 2f - previewShorterEdge / 2, height - bottomBarHeight - navgH,
                        width / 2f + previewShorterEdge / 2, height- navgH);
            }
        } else {
            previewShorterEdge = shorterEdge;
            previewLongerEdge = shorterEdge * previewAspectRatio;
            config.mBottomBarOverlay = true;
            float longerEdge16_9 = shorterEdge / 9f * 16f;
            float longerEdge4_3 = shorterEdge / 3f * 4f;
            float leftSpace = longerEdge - longerEdge16_9 - navgH;
            float topSpace = 0;
            if (leftSpace >= mTopHeight) {
                leftSpace = leftSpace - mTopHeight;
                mTopHeight += (leftSpace / 2);
                topSpace = mTopHeight;
            }
            Log.d(TAG, "getFreemePositionConfiguration: " +
                    "\nnavgH = " + navgH +
                    "\npreviewLongerEdge = " + previewLongerEdge +
                    "\nlongerEdge = " + longerEdge +
                    "\nshorterEdge = " + shorterEdge +
                    "\nlongerEdge16_9 = " + longerEdge16_9 +
                    "\nlongerEdge4_3 = " + longerEdge4_3 +
                    "\nleftSpace = " + leftSpace +
                    "\nmTopPanelHeight = " + mTopHeight
            );
            float offset = topSpace;
            if (isLandscape) {
                config.mPreviewRect.set(
                        offset,
                        0,
                        previewLongerEdge + offset,
                        previewShorterEdge
                );
                config.mBottomBarRect.set(
                        longerEdge4_3 + offset,
                        0,
                        longerEdge16_9 + offset,
                        previewShorterEdge
                );
            } else {
                config.mPreviewRect.set(
                        0,
                        offset,
                        previewShorterEdge,
                        previewLongerEdge + offset
                );
                config.mBottomBarRect.set(
                        0,
                        longerEdge4_3 + offset,
                        previewShorterEdge,
                        longerEdge16_9 + offset
                );
            }
        }
        round(config.mBottomBarRect);
        round(config.mPreviewRect);
        mBottomBarMarginBottom = Math.round(longerEdge - config.mBottomBarRect.bottom);
        mBottomAreaHeight = Math.round(longerEdge - config.mBottomBarRect.top);
        mActivity.getCameraAppUI().updateBottomFrame(config.mBottomBarRect, mBottomBarMarginBottom);

        SurfaceView coverView = mActivity.getCameraAppUI().getBottomCoverView();
        if (previewAspectRatio <= (4f/3f)){
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) coverView.getLayoutParams();
            params.height = mBottomAreaHeight;
            coverView.setLayoutParams(params);
            coverView.setVisibility(View.VISIBLE);
        }else {
            coverView.setVisibility(View.GONE);
        }
        return config;
    }

    public int getModeSwitchHeight() {
        if (mModeSwitchHeight == 0)
            mModeSwitchHeight = mActivity.getResources().getDimensionPixelSize(R.dimen.dream2_bottom_mode_switch_height);
        return mModeSwitchHeight;
    }

    public int getTopPanelMarginTop() {
        int topPanelIconSize = mActivity.getResources().getDimensionPixelOffset(R.dimen.freeme_top_panel_icon_size);
        float marginTop = (mTopHeight - topPanelIconSize) / 2f;
        return Math.round(marginTop);
    }

    public int getBottomBarMarginBottom() {
        return mBottomBarMarginBottom;
    }
    public int getBottomAreaHeight() {
        return mBottomAreaHeight;
    }

    public void setActivity(CameraActivity activity) {
        mActivity = activity;
    }
}
