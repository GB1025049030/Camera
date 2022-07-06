package com.bytedance.labcv.core.lens;

import android.content.Context;

import com.bytedance.labcv.core.Config;
import com.bytedance.labcv.core.lens.ImageQualityResourceProvider;

import java.io.File;

/**
 * Created on 2021/5/19 10:21
 */
public class ImageQualityResourceHelper implements ImageQualityResourceProvider {
    public static final String RESOURCE = "resource";
    public static final String SKIN_SEGMENTATION = "skin_segmentation/tt_skin_seg_v4.0.model";

    private Context mContext;

    public ImageQualityResourceHelper(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public String getLicensePath() {
        return new File(new File(getResourcePath(), "LicenseBag.bundle"), Config.LICENSE_NAME).getAbsolutePath();
    }

    private String getModelPath(String modelName) {
        return new File(new File(getResourcePath(), "ModelResource.bundle"), modelName).getAbsolutePath();
    }

    @Override
    public String getSkinSegPath() {
        return getModelPath(SKIN_SEGMENTATION);
    }

    private String getResourcePath() {
        return mContext.getExternalFilesDir("assets").getAbsolutePath() + File.separator + RESOURCE;
    }
}
