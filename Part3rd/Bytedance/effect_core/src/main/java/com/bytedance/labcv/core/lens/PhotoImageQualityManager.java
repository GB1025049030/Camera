package com.bytedance.labcv.core.lens;

import android.content.Context;
import android.content.Intent;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bytedance.labcv.core.Config;
import com.bytedance.labcv.core.license.EffectLicenseHelper;
import com.bytedance.labcv.core.license.EffectLicenseProvider;
import com.bytedance.labcv.core.util.LogUtils;
import com.bytedance.labcv.effectsdk.BytedEffectConstants;
import com.bytedance.labcv.effectsdk.PhotoNightScene;
import com.bytedance.labcv.effectsdk.RenderManager;

import java.nio.ByteBuffer;

public class PhotoImageQualityManager {
    protected Context mContext;
    protected ImageQualityResourceProvider mResourceProvider;
    protected EffectLicenseProvider mLicenseProvider;
    public boolean mEnablePhotoNightScene = false;
    private boolean mCloseWhenProcessing = false;
    private boolean mProcessing = false;

    // 拍照的夜景增强
    private PhotoNightScene mPhotoNightScene = null;
    public int mPhotoNightSceneWidth;
    public int mPhotoNightSceneHeight;
    public int mPhotoNightSceneImageNumber = 6;
    public BytedEffectConstants.YUV420Type mPhotoNightSceneType  = BytedEffectConstants.YUV420Type.YUV_420_TYPE_NV21;

    public PhotoImageQualityManager(Context context, ImageQualityResourceProvider provider) {
        mContext = context;
        mResourceProvider = provider;

        mLicenseProvider = EffectLicenseHelper.getInstance(mContext);
    }

    public void destroy() {
        if (mPhotoNightScene != null) {
            if (!mProcessing) {
                mPhotoNightScene.destroy();
                mPhotoNightScene = null;
                mEnablePhotoNightScene = false;
            } else {
                mCloseWhenProcessing = true;
            }
        }
    }

    public void setImageQuality(BytedEffectConstants.PhotoQualityType type, boolean on) {
        if (type == BytedEffectConstants.PhotoQualityType.PHOTO_QUALITY_TYPE_NIGNT_SCENE) {
           mEnablePhotoNightScene = on;
           if (on) {
               if (mPhotoNightScene == null) {
                   mPhotoNightScene = new PhotoNightScene();
                   int ret = mPhotoNightScene.init(mContext, mLicenseProvider.getLicensePath(), mResourceProvider.getSkinSegPath(), mPhotoNightSceneWidth, mPhotoNightSceneHeight, mPhotoNightSceneImageNumber,  mPhotoNightSceneType.getValue(), mLicenseProvider.getLicenseMode() == EffectLicenseProvider.LICENSE_MODE_ENUM.ONLINE_LICENSE);
                   if (!checkResult("PhotoNightScene init", ret)) return ;
               }
           } else {
               if (mPhotoNightScene!=null) {
                   mPhotoNightScene.destroy();
               }
           }
        }
    }

    public ByteBuffer processBuffer(ByteBuffer[] inputs) {
        if (mEnablePhotoNightScene) {
            mProcessing = true;
            mPhotoNightScene.process(inputs);
            mProcessing = false;

            if (!mCloseWhenProcessing) {
                return mPhotoNightScene.getResultBuffer();
            } else {
                mPhotoNightScene.destroy();
                mPhotoNightScene = null;
            }
        }
        return  null;
    }

    protected boolean checkResult(String msg, int ret) {
        if (ret != 0 && ret != -11 && ret != 1) {
            String log = msg + " error: " + ret;
            LogUtils.e(log);
            String toast = RenderManager.formatErrorCode(ret);
            if (toast == null) {
                toast = log;
            }
            Intent intent = new Intent(Config.CHECK_RESULT_BROADCAST_ACTION);
            intent.putExtra("msg", toast);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
            return false;
        }
        return true;
    }

}
