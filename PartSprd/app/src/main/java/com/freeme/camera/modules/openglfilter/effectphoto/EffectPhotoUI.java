package com.freeme.camera.modules.openglfilter.effectphoto;

import android.content.Context;
import android.view.View;
import com.android.camera.CameraActivity;
import com.android.camera.PhotoController;
import com.binbin.camera.listener.IRenderManager;
import com.binbin.camera.render.RenderIndex;
import com.binbin.filter.glfilter.base.FilterController;
import com.bytedance.labcv.core.effect.EffectManager;
import com.bytedance.labcv.core.util.LogUtils;
import com.bytedance.labcv.effectsdk.BytedEffectConstants;
import com.bytedance.labcv.effectwork.effect.fragment.EffectFragment;
import com.bytedance.labcv.effectwork.effect.model.ComposerNode;
import com.freeme.camera.modules.openglfilter.GLPhotoUI;
import java.util.Arrays;
public class EffectPhotoUI extends GLPhotoUI {
    private GLImageEffectFilter mGLImageEffectFilter;
    private GLImageEffectFilterController mFilterController;
    private EffectManager mEffectManager;
    private EffectAdjustManager mEffectAdjustManager;
    private int mCurrentItem = -2;
    public EffectPhotoUI(CameraActivity activity, PhotoController controller, View parent) {
        super(activity, controller, parent);
    }
    @Override
    public void addCustomFilters(Context context, IRenderManager renderManager) {
        super.addCustomFilters(context, renderManager);
        mGLImageEffectFilter = new GLImageEffectFilter(context, new FilterController().setEnable(true));
        renderManager.addCustomFilter(RenderIndex.BeautyIndex, mGLImageEffectFilter);
        initByteDanceController();
    }
    private void initByteDanceController() {
        mEffectManager = mGLImageEffectFilter.getEffectManager();
        mActivity.runOnUiThread(() -> {
            if (mEffectAdjustManager == null) {
                mEffectAdjustManager = new EffectAdjustManager(mActivity.getCameraAppUI().getExtendPanelParent(), mActivity);
//                mEffectAdjustManager.setCallback(new EffectFragment.IEffectCallback() {
//                    @Override
//                    public void updateComposeNodes(String[] nodes) {
//                        LogUtils.e("update composer nodes: " + Arrays.toString(nodes));
//                        mEffectRenderHelper.setComposeNodes(nodes);
//                    }
//                    @Override
//                    public void updateComposeNodeIntensity(ComposerNode node) {
//                        LogUtils.e("update composer node intensity: node: "
//                                + node.getNode() + ", key: "
//                                + node.getKey() + ", value: " + node.getValue());
//                        mEffectRenderHelper.updateComposeNode(node);
//                    }
//                    @Override
//                    public void setEffectOn(final boolean isOn) {
//                        mEffectRenderHelper.setEffectOn(isOn);
//                    }
//                    @Override
//                    public void updateFilterIntensity(BytedEffectConstants.IntensityType intensitytype, float intensity) {
//                        mEffectRenderHelper.updateIntensity(intensitytype, intensity);
//                    }
//                });
            }
            if (mCurrentItem != -2) {
                mEffectAdjustManager.switcher(mCurrentItem, false);
            }
            if (mEffectAdjustManager.getCollpased()) {
                mEffectAdjustManager.collpase();
            }
        });
    }
    @Override
    public void onPause() {
        super.onPause();
        if (mEffectAdjustManager != null) {
            mCurrentItem = mEffectAdjustManager.getCurrentItem();
            mEffectAdjustManager.removeView(mActivity.getCameraAppUI().getExtendPanelParent());
            mEffectAdjustManager = null;
        }
    }
    @Override
    public void onSingleTapUp() {
        super.onSingleTapUp();
        if (null != mEffectAdjustManager && !mEffectAdjustManager.getCollpased()) {
            mEffectAdjustManager.collpase();
        }
    }

    @Override
    public void onSurfaceCreated() {
        super.onSurfaceCreated();
        int ret =  mEffectManager.init();
        if (ret != BytedEffectConstants.BytedResultCode.BEF_RESULT_SUC){
            LogUtils.e("mEffectManager.init() fail!! error code ="+ret);
        }
    }
}
