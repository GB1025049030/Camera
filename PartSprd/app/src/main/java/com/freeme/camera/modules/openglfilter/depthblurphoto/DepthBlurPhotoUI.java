package com.freeme.camera.modules.openglfilter.depthblurphoto;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.android.camera.CameraActivity;
import com.android.camera.PhotoController;
import com.android.camera.PhotoUI;
import com.android.camera.app.AppController;
import com.android.camera2.R;
import com.binbin.camera.listener.IRenderManager;
import com.binbin.camera.render.RenderIndex;
import com.binbin.filter.glfilter.base.GLImageDepthBlurFilter;
import com.binbin.filter.glfilter.base.GLImageDepthBlurFilterController;
import com.dream.camera.ui.VerticalSeekBar;
import com.freeme.camera.modules.openglfilter.GLPhotoUI;

public class DepthBlurPhotoUI extends GLPhotoUI {
    private VerticalSeekBar mBlurShiftSizeSeekBar;
    private GLImageDepthBlurFilterController mFilterController;

    public DepthBlurPhotoUI(CameraActivity activity, PhotoController controller, View parent) {
        super(activity, controller, parent);
    }

    @Override
    public void addCustomFilters(Context context, IRenderManager renderManager) {
        super.addCustomFilters(context, renderManager);
        mFilterController = new GLImageDepthBlurFilterController();
        renderManager.addCustomFilter(RenderIndex.DepthBlurIndex, new GLImageDepthBlurFilter(context, mFilterController.setEnable(true)));
        if (mBlurShiftSizeSeekBar != null) {
            mFilterController.setBlurShiftSize(getCurrent(mBlurShiftSizeSeekBar, mBlurShiftSizeSeekBar.getProgress()));
        }
    }

    @Override
    public void fitExtendPanel(ViewGroup extendPanelParent) {
        LayoutInflater lf = LayoutInflater.from(mActivity);
        ViewGroup extendPanel = (ViewGroup) lf.inflate(R.layout.depth_blur_photo_extend_panel, extendPanelParent);
        updateDepthBlurParentParams(extendPanel);
        mBlurShiftSizeSeekBar = extendPanel.findViewById(R.id.depth_seek_bar);
        mBlurShiftSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mFilterController.setBlurShiftSize(getCurrent(seekBar, progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void updateDepthBlurParentParams(ViewGroup extendPanel) {
        RelativeLayout parent = extendPanel.findViewById(R.id.depth_blur_parent);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) parent.getLayoutParams();
        params.height = Math.round(mActivity.getCameraAppUI().getCaptureLayoutHelper().getBottomBarRect().top
                - mActivity.getCameraAppUI().getCaptureLayoutHelper().getPreviewRect().top);
        parent.setLayoutParams(params);
    }

    private float getCurrent(SeekBar seekBar, int progress) {
        return (progress - seekBar.getMin()) * 1f / (seekBar.getMax() - seekBar.getMin());
    }

    public GLImageDepthBlurFilterController getFilterController() {
        return mFilterController;
    }
}
