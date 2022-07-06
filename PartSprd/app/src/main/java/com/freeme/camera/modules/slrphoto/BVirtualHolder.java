package com.freeme.camera.modules.slrphoto;

import com.android.camera.debug.Log;
import com.android.slrblur.BlurInfo;

public class BVirtualHolder {
    private static final Log.Tag TAG = new Log.Tag("BVirtualHolder");

    private byte[] mData;
    private int mDataWidth;
    private int mDataHeight;
    private int mTargetWidth;
    private int mTargetHeight;
    private int mBlurDegree;
    private final BlurInfo mBlurInfo;

    public BVirtualHolder() {
        this(null, 0, 0, 0, new BlurInfo());
    }

    public BVirtualHolder(BVirtualHolder holder) {
        this();
        if (holder != null) {
            update(holder.mData, holder.mDataWidth, holder.mDataHeight, holder.mTargetWidth,
                    holder.mTargetHeight, holder.mBlurDegree, holder.mBlurInfo);
        }
    }

    public BVirtualHolder(byte[] data, int dataWidth, int dataHeight, int blurDegree, BlurInfo blurInfo) {
        this(data, dataWidth, dataHeight, -1, -1, blurDegree, blurInfo);
    }

    public BVirtualHolder(byte[] data, int dataWidth, int dataHeight, int targetWidth,
                          int targetHeight, int blurDegree, BlurInfo blurInfo) {
        if (data != null) {
            this.mData = new byte[data.length];
            System.arraycopy(data, 0, this.mData, 0, data.length);
        }
        this.mDataWidth = dataWidth;
        this.mDataHeight = dataHeight;
        this.mTargetWidth = targetWidth;
        this.mTargetHeight = targetHeight;
        this.mBlurDegree = blurDegree;
        this.mBlurInfo = new BlurInfo(blurInfo);
    }

    public byte[] getData() {
        return mData;
    }

    public int getDataWidth() {
        return mDataWidth;
    }

    public int getDataHeight() {
        return mDataHeight;
    }

    public int getTargetWidth() {
        return mTargetWidth;
    }

    public int getTargetHeight() {
        return mTargetHeight;
    }

    public int getBlurDegree() {
        return mBlurDegree;
    }

    public BlurInfo getBlurInfo() {
        return mBlurInfo;
    }

    public void update(byte[] data, int dataWidth, int dataHeight, int blurDegree, BlurInfo blurInfo) {
        update(data, dataWidth, dataHeight, -1, -1, blurDegree, blurInfo);
    }

    public void update(byte[] data, int dataWidth, int dataHeight, int targetWidth,
                       int targetHeight, int blurDegree, BlurInfo blurInfo) {
        if (data != null) {
            if (this.mData == null || this.mData.length != data.length) {
                this.mData = new byte[data.length];
            }
            System.arraycopy(data, 0, this.mData, 0, data.length);
        }
        this.mDataWidth = dataWidth;
        this.mDataHeight = dataHeight;
        this.mTargetWidth = targetWidth;
        this.mTargetHeight = targetHeight;
        this.mBlurDegree = blurDegree;
        if (blurInfo != null) {
            blurInfo.copyTo(this.mBlurInfo);
        }
    }

    public void copyTo(BVirtualHolder bVirtualHolder) {
        if (bVirtualHolder != null) {
            if (this.mData != null) {
                if (bVirtualHolder.mData == null || bVirtualHolder.mData.length != this.mData.length) {
                    bVirtualHolder.mData = new byte[this.mData.length];
                }
                System.arraycopy(this.mData, 0, bVirtualHolder.mData, 0, this.mData.length);
            }
            bVirtualHolder.mDataWidth = this.mDataWidth;
            bVirtualHolder.mDataHeight = this.mDataHeight;
            bVirtualHolder.mTargetWidth = this.mTargetWidth;
            bVirtualHolder.mTargetHeight = this.mTargetHeight;
            bVirtualHolder.mBlurDegree = this.mBlurDegree;
            if (this.mBlurInfo != null) {
                this.mBlurInfo.copyTo(bVirtualHolder.mBlurInfo);
            }
        } else {
            Log.d(TAG, "copyTo: bVirtualHolder is NULL !!!");
        }
    }
}
