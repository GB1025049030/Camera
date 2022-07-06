package com.freeme.camera.modules.openglfilter.effectphoto;

import android.content.Context;
import android.opengl.GLES20;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;
import com.binbin.filter.glfilter.base.GLImageFilter;
import com.binbin.filter.glfilter.impl.IFilterController;
import com.binbin.filter.glfilter.utils.OpenGLUtils;
import com.bytedance.labcv.core.effect.EffectManager;
import com.bytedance.labcv.core.effect.EffectResourceHelper;
import com.bytedance.labcv.core.license.EffectLicenseHelper;
import com.bytedance.labcv.core.util.LogUtils;
import com.bytedance.labcv.core.util.OrientationSensor;
import com.bytedance.labcv.effectsdk.BytedEffectConstants;
import java.io.File;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
public class GLImageEffectFilter extends GLImageFilter implements EffectManager.OnEffectListener {
    private static final String TAG = GLImageEffectFilter.class.getSimpleName();
    private static final int FRAME_BUFFER_NUM = 2;
    private EffectManager mEffectManager;

    protected boolean mFirstEnter = true;

    public GLImageEffectFilter(Context context, IFilterController controller) {
        super(context, controller);
        init(context);
    }
    public GLImageEffectFilter(Context context, IFilterController controller, String vertexShader, String fragmentShader) {
        super(context, controller, vertexShader, fragmentShader);
        init(context);
    }
    private void init(Context context) {
        Log.d(TAG, "init: ");
        mEffectManager = new EffectManager(context, new EffectResourceHelper(context), EffectLicenseHelper.getInstance(context));
        mEffectManager.setOnEffectListener(this);
        //mEffectRenderHelper.resetComposeNodes();
        //List<File> mFileList = new ArrayList<>(Arrays.asList(ResourceHelper.getFilterResources(context)));
        //mEffectRenderHelper.setFilter(mFileList.get(0).getAbsolutePath());
        //mEffectRenderHelper.setEffectOn(true);
    }
    @Override
    public void initFrameBuffer(int width, int height) {
        Log.d(TAG, "initFrameBuffer: [" + width + ", " + height + "]");
        if (!isInitialized()) {
            return;
        }
        if (mFrameBuffers != null && (mFrameWidth != width || mFrameHeight != height)) {
            destroyFrameBuffer();
        }
        if (mFrameBuffers == null) {
            mFrameWidth = width;
            mFrameHeight = height;
            mFrameBuffers = new int[FRAME_BUFFER_NUM];
            mFrameBufferTextures = new int[FRAME_BUFFER_NUM];
            OpenGLUtils.createFrameBuffer(mFrameBuffers, mFrameBufferTextures, width, height);
        }
        //mEffectRenderHelper.setImageSize(width, height);
    }
    @Override
    public void onDisplaySizeChanged(int width, int height) {
        super.onDisplaySizeChanged(width, height);
        Log.d(TAG, "onDisplaySizeChanged: [" + width + ", " + height + "]");
        //mEffectRenderHelper.initViewPort(width, height);
        //mEffectRenderHelper.initSDKModules();
        //mEffectRenderHelper.initComposeNodes();
        //mEffectRenderHelper.resetComposeNodes();
        //mEffectRenderHelper.recoverStatus(mContext);
    }
    @Override
    public int drawFrameBuffer(int textureId, FloatBuffer vertexBuffer, FloatBuffer textureBuffer) {
        BytedEffectConstants.Rotation rotation = OrientationSensor.getOrientation();
        if (mEffectManager.process(textureId, mFrameBufferTextures[1], mFrameWidth, mFrameHeight, rotation, System.currentTimeMillis())) {
            return super.drawFrameBuffer(mFrameBufferTextures[1], vertexBuffer, textureBuffer);
        }
        return super.drawFrameBuffer(textureId, vertexBuffer, textureBuffer);
    }
    @Override
    public int unBindFrameBuffer(int textureId) {
        return super.unBindFrameBuffer(textureId);
    }
    @Override
    public void release() {
        super.release();
        //mEffectRenderHelper.destroySDKModules();
    }
    @Override
    public void destroyFrameBuffer() {
        Log.d(TAG, "destroyFrameBuffer: ");
        if (!mIsInitialized) {
            return;
        }
        if (mFrameBufferTextures != null) {
            GLES20.glDeleteTextures(FRAME_BUFFER_NUM, mFrameBufferTextures, 0);
            mFrameBufferTextures = null;
        }
        if (mFrameBuffers != null) {
            GLES20.glDeleteFramebuffers(FRAME_BUFFER_NUM, mFrameBuffers, 0);
            mFrameBuffers = null;
        }
        mFrameWidth = -1;
        mFrameHeight = -1;
    }
    public EffectManager getEffectManager() {
        return mEffectManager;
    }
    public double getSurfaceTimeStamp() {
        if (mTimeStamp == -1) {
            return -1;
        }
        long cur_time_nano = System.nanoTime();
        long delta_nano_time = Math.abs(cur_time_nano - mTimeStamp);
        long delta_elapsed_nano_time = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 ?
                Math.abs(SystemClock.elapsedRealtimeNanos() - mTimeStamp) : Long.MAX_VALUE;
        long delta_uptime_nano = Math.abs(SystemClock.uptimeMillis() * 1000000 - mTimeStamp);
        double lastTimeStamp = cur_time_nano - Math.min(Math.min(delta_nano_time, delta_elapsed_nano_time), delta_uptime_nano);
        return lastTimeStamp / 1e9;
    }

    @Override
    public void onEffectInitialized() {
        if (!mFirstEnter) {
            return;
        }
        mFirstEnter = false;
        resetDefault();
    }

    protected void resetDefault( ){
        LogUtils.d("resetDefault");
        mEffectManager.setFilter("");
        mEffectManager.setSticker("");
    }
}
