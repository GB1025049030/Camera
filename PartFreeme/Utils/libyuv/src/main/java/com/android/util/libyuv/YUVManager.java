package com.android.util.libyuv;

import android.util.Log;

public enum YUVManager {
    I;

    private static final String TAG = "YUVManager";

    public void destroy() {
        YUVUtil.free();
    }

    public void update(final int srcWidth, final int srcHeight, final int dstWidth, final int dstHeight) {
        YUVUtil.update(srcWidth, srcHeight, dstWidth, dstHeight);
    }

    public void rotateNV21(byte[] dst_yuv, byte[] src_yuv, int width, int height, int rotate) {
        doWork(() -> YUVUtil.rotateNV21(dst_yuv, src_yuv, width, height, rotate));
    }

    public void transformNV21ToRGBA(byte[] dst_rgba, byte[] src_yuv, int width, int height, int rotate) {
        doWork(() -> YUVUtil.transformNV21ToRGBA(dst_rgba, src_yuv, width, height, rotate));
    }

    public void transformRGBAToI420(byte[] dst_i420, byte[] src_rgba, int width, int height) {
        doWork(() -> YUVUtil.transformRGBAToI420(dst_i420, src_rgba, width, height));
    }

    public void compressNV21ToRGBA(byte[] dst_rgba, byte[] src_yuv, int width, int height, int dst_width, int dst_height, int rotate) {
        doWork(() -> YUVUtil.compressNV21ToRGBA(dst_rgba, src_yuv, width, height, dst_width, dst_height, rotate));
    }

    public void nv21ConvertToI420(byte[] src_data, int width, int height, byte[] dst_data, int dst_width,
                                  int dst_height, int left, int top) {
        doWork(() -> YUVUtil.copyYUV(src_data, width, height, dst_data, dst_width, dst_height, left, top));
    }

    private void doWork(Consumer consumer) {
        if (!consumer.accept()) {
            Log.d(TAG, "YUV has not init !!!");
        }
    }

    public interface Consumer {
        boolean accept();
    }
}
