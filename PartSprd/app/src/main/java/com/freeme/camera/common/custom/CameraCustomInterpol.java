package com.freeme.camera.common.custom;

import android.os.SystemProperties;
import android.text.TextUtils;

import com.android.camera.util.Size;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class CameraCustomInterpol {
    private static final String[] mBackValue;
    private static final String[] mFrontValue;
    private static final DecimalFormat sMegaPixelFormat;

    static {
        mBackValue = getInterpolProp(
                "persist.vendor.external.camera0.interpol",
                "persist.vendor.freeme.camera0.interpol",
                "ro.vendor.external.default.camera0",
                "ro.vendor.freeme.default.camera0"
        );
        mFrontValue = getInterpolProp(
                "persist.vendor.external.camera1.interpol",
                "persist.vendor.freeme.camera1.interpol",
                "ro.vendor.external.default.camera1",
                "ro.vendor.freeme.default.camera1");
        sMegaPixelFormat = new DecimalFormat("##0");
    }

    private static String[] getInterpolProp(final String prop, String... props) {
        String values = SystemProperties.get(prop);
        if (TextUtils.isEmpty(values)) {
            for (String propName : props) {
                values = SystemProperties.get(propName);
                if (!TextUtils.isEmpty(values)) {
                    break;
                }
            }
        }
        if (!TextUtils.isEmpty(values)) {
            return values.split("_");
        }
        return null;
    }

    private static int getPictureSize(String[] values) {
        if (values != null) {
            return Integer.parseInt(values[0].replaceAll("M", ""));
        }
        return -1;
    }

    private static int getLength(String[] values) {
        if (values != null && values.length > 1) {
            return Integer.parseInt(values[1]);
        }
        return -1;
    }

    public static int getPictureSize(boolean isBackCamera) {
        return getPictureSize(isBackCamera ? mBackValue : mFrontValue);
    }

    public static int getLength(boolean isBackCamera) {
        return Math.min(getLength(isBackCamera ? mBackValue : mFrontValue), 4);
    }

    public static List<Size> filterSize(List<Size> sizes, boolean isBackCamera) {
        if (sizes == null || sizes.size() == 0) return sizes;
        int maxSize = getPictureSize(isBackCamera);
        if (maxSize == -1) return sizes;
        List<Size> removes = new ArrayList<>(sizes.size());
        for (Size size : sizes) {
            String megaPixels = sMegaPixelFormat.format((size.width() * size
                    .height()) / 1e6);
            int currentSize = Integer.parseInt(megaPixels);
            if (currentSize > maxSize) {
                removes.add(size);
            }
        }
        if (removes.size() > 0) sizes.removeAll(removes);
        return sizes;
    }
}
