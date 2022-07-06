package com.freeme.utils;

import android.graphics.Rect;
import android.util.Log;
import android.widget.FrameLayout;

import com.android.camera.CameraActivity;
import com.android.camera.util.CameraUtil;
import com.android.camera2.R;

public enum FreemeUIPositionHelp {
    I;

    private int mWidth;
    private int mHeight;
    private int mNavigationBarHeight;
    private int mBottomBarBottomMargin;
    private int mBottomBarHeight;

    private Rect mBottomBarArea;
    private Rect mLandBottomBarArea;
    private Rect mPreviewAreaNS;
    private Rect mLandPreviewAreaNS;
    private Rect mPreviewAreaBS;
    private Rect mLandPreviewAreaBS;
    private int mBottomBarTopPosition;

    public void create(CameraActivity activity, int width, int height) {
        mWidth = Math.min(width, height);
        mHeight = Math.max(width, height);
        if (CameraUtil.isNavigationEnable()) {
            mNavigationBarHeight = CameraUtil.getNormalNavigationBarHeight();
        }
        int topSpaceHeight = getTopSpaceHeight(activity);
        int previewHeight_16_9 = mWidth / 9 * 16;
        mBottomBarHeight = previewHeight_16_9 - mWidth / 3 * 4;
        mBottomBarBottomMargin = getBottomBarMarginBottom(mHeight, topSpaceHeight,
                mNavigationBarHeight, previewHeight_16_9);

        mBottomBarTopPosition = mHeight - mNavigationBarHeight - mBottomBarBottomMargin - mBottomBarHeight;
        mBottomBarArea = new Rect(
                0,
                mBottomBarTopPosition,
                mWidth,
                mHeight - mNavigationBarHeight);

        mLandBottomBarArea = new Rect(
                mBottomBarTopPosition,
                0,
                mHeight - mNavigationBarHeight,
                mWidth);

        mPreviewAreaNS = new Rect(
                0,
                mHeight - mNavigationBarHeight - mBottomBarBottomMargin,
                mWidth,
                mHeight - mNavigationBarHeight - mBottomBarBottomMargin);

        mLandPreviewAreaNS = new Rect(
                mHeight - mNavigationBarHeight - mBottomBarBottomMargin,
                0,
                mHeight - mNavigationBarHeight - mBottomBarBottomMargin,
                mWidth);

        mPreviewAreaBS = new Rect(
                0,
                mHeight - mNavigationBarHeight - mBottomBarBottomMargin - mBottomBarHeight,
                mWidth,
                mHeight - mNavigationBarHeight - mBottomBarBottomMargin - mBottomBarHeight);

        mLandPreviewAreaBS = new Rect(
                mHeight - mNavigationBarHeight - mBottomBarBottomMargin - mBottomBarHeight,
                0,
                mHeight - mNavigationBarHeight - mBottomBarBottomMargin - mBottomBarHeight,
                mWidth);
    }

    public Rect getBottomBarArea(boolean isLand) {
        return isLand ? mLandBottomBarArea : mBottomBarArea;
    }

    public Rect getPreviewArea(float previewAspectRatio, boolean isLand) {
        Rect area;
        if (previewAspectRatio > 14f / 9f) {
            if (isLand) {
                mLandPreviewAreaNS.left = mHeight - mNavigationBarHeight - mBottomBarBottomMargin
                        - (int) (mWidth * previewAspectRatio);
                area = mLandPreviewAreaNS;
            } else {
                mPreviewAreaNS.top = mHeight - mNavigationBarHeight - mBottomBarBottomMargin
                        - (int) (mWidth * previewAspectRatio);
                area = mPreviewAreaNS;
            }
        } else {
            if (isLand) {
                mLandPreviewAreaBS.left = mHeight - mNavigationBarHeight - mBottomBarBottomMargin - mBottomBarHeight
                        - (int) (mWidth * previewAspectRatio);
                area = mLandPreviewAreaBS;
            } else {
                mPreviewAreaBS.top = mHeight - mNavigationBarHeight - mBottomBarBottomMargin - mBottomBarHeight
                        - (int) (mWidth * previewAspectRatio);
                area = mPreviewAreaBS;
            }
        }
        return area;
    }

    public int getBottomBarTopPosition() {
        return mBottomBarTopPosition;
    }

    public int getBottomFrameHeight() {
        return mBottomBarHeight + mBottomBarBottomMargin;
    }

    public int getBottomBarHeight() {
        return mBottomBarHeight;
    }

    public int getBottomBarBottomMargin() {
        return mBottomBarBottomMargin;
    }

    private int getTopSpaceHeight(CameraActivity activity) {
        int topPanelHeight = activity.getResources().getDimensionPixelSize(R.dimen.top_panel_height);
        FrameLayout topPanelParent = activity.findViewById(R.id.top_panel_parent);
        FrameLayout.LayoutParams topParams = (FrameLayout.LayoutParams) topPanelParent.getLayoutParams();
        return topPanelHeight + topParams.topMargin + topParams.bottomMargin;
    }

    private int getBottomBarMarginBottom(int height, int topSpaceHeight, int navigationBarHeight, int maxPreviewHeight) {
        return (height - topSpaceHeight - navigationBarHeight - maxPreviewHeight) / 2;
    }
}
