package com.dream.camera.effect;

import android.content.Context;
import android.opengl.GLES20;

public class EffectRenderHelper {

    private Context mContext;

    private OpenGLRender mGLRender;
    private int mSurfaceWidth;
    private int mSurfaceHeight;
    private int mImageWidth;
    private int mImageHeight;

    public EffectRenderHelper(Context context) {
        mContext = context;
        mGLRender = new OpenGLRender(context);
    }

    public void initViewPort(int width, int height) {
        if (width != 0 && height != 0) {
            this.mSurfaceWidth = width;
            this.mSurfaceHeight = height;
            GLES20.glViewport(0, 0, mSurfaceWidth, mSurfaceHeight);
            mGLRender.calculateVertexBuffer(mSurfaceWidth, mSurfaceHeight, mImageWidth, mImageHeight);
            mGLRender.init(mImageWidth, mImageHeight);
        }
    }

    /**
     * 设置SDK的输入尺寸，该尺寸是转至人脸为正后的宽高
     * @param mImageWidth
     * @param mImageHeight
     */
    public void setImageSize(int mImageWidth, int mImageHeight) {
        //this.mImageHeight = mImageHeight;
        //this.mImageWidth = mImageWidth;
        this.mImageHeight = mImageWidth;
        this.mImageWidth = mImageHeight;
    }

    public int processTexture(int textureID) {
        int srcTexture = mGLRender.preProcess(textureID);
        //int dstTexture = mGLRender.getOutputTexture();
        //if (dstTexture == ShaderHelper.NO_TEXTURE) {
        //    return srcTexture;
        //}
        return srcTexture;
    }

    public void drawFrame(int textureId) {
        //GLES20.glViewport(0, 0, mSurfaceWidth, mSurfaceHeight);
        mGLRender.onDrawFrame(textureId);
    }

    public void adjustTextureBuffer(int orientation, boolean flipHorizontal, boolean flipVertical) {
        mGLRender.adjustTextureBuffer(orientation, flipHorizontal, flipVertical);
    }
}
