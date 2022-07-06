package com.android.slrblur;

import android.graphics.Bitmap;

public class SmoothBlurJni {

    static {
        System.loadLibrary("SmoothBlur");
    }

    public static native void smoothRender(Bitmap blurBitmap, Bitmap oriBitmap, BlurInfo info);
}
