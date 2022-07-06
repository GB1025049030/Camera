package com.android.util.libyuv;

class YUVUtil {
    static {
        System.loadLibrary("yuvutil");
    }

    protected static native void update(int width, int height, int dst_width, int dst_height);

    protected static native void free();

    protected static native void compressYUV(byte[] src_, int width, int height,
                                             byte[] dst_, int dst_width, int dst_height,
                                             int mode, int degree, boolean isMirror);

    protected static native boolean copyYUV(byte[] src_data, int width, int height,
                                         byte[] dst_data, int dst_width, int dst_height,
                                         int left, int top);

    protected static native boolean rotateNV21(byte[] dst_yuv, byte[] src_yuv, int width, int height, int rotate);

    protected static native void transformNV21ToI420(byte[] dst_i420, byte[] src_yuv, int width, int height, int rotate);

    protected static native boolean transformNV21ToRGBA(byte[] dst_rgba, byte[] src_yuv, int width, int height, int rotate);

    protected static native boolean transformRGBAToI420(byte[] dst_i420, byte[] src_rgba, int width, int height);

    protected static native boolean compressNV21ToRGBA(byte[] dst_rgba, byte[] src_yuv, int width, int height, int dst_width, int dst_height, int rotate);
}
