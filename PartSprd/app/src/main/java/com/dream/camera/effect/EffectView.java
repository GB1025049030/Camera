package com.dream.camera.effect;

import android.content.Context;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.widget.FrameLayout;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.android.camera.app.CameraAppUI;
import com.android.camera.util.CameraUtil;
import com.dream.camera.effect.encoder.MediaAudioEncoder;
import com.dream.camera.effect.encoder.MediaEncoder;
import com.dream.camera.effect.encoder.MediaMuxerWrapper;
import com.dream.camera.effect.encoder.MediaVideoEncoder;

import com.android.camera.ui.PreviewStatusListener;
import com.android.ex.camera2.portability.debug.Log;

import java.io.FileDescriptor;
import java.io.IOException;

public class EffectView extends GLSurfaceView implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {

    private static final Log.Tag TAG = new Log.Tag("EffectView");
    private Context mContext;
    private int mSurfaceTextureID = ShaderHelper.NO_TEXTURE;
    private SurfaceTexture mSurfaceTexture;
    private volatile boolean mIsPaused = false;
    private EffectRenderHelper mEffectRenderHelper;
    private int dstTexture = ShaderHelper.NO_TEXTURE;
    //相机获取的图片尺寸
    private int mImageWidth;
    private int mImageHeight;

    private SurfaceHolder.Callback mSurfaceHolderListener;
    private SurfaceHolder mSurfaceHolder;

    private MediaMuxerWrapper mMuxer;
    private MediaVideoEncoder mVideoEncoder;
    private MediaAudioEncoder mAudioEncoder;

    private float mAspectRatio = 0;
    private CameraAppUI mCameraAppUI = null;

    private int mSurfaceWidth;
    private int mSurfaceHeight;
    private boolean mIsPreviewing = false;
    private int mSpeed = CameraUtil.SLOW_VIDEO_RECORD_SPEED_STANDARD;

    public static interface MediaMuxerListener {
        public void onMediaRecoderCompleted(boolean isNeedSaveVideo);
    }

    public EffectView(Context context) {
        super(context);
        mContext = context;
        init(context);
        Log.v(TAG, "new EffectView");
    }

    public EffectView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init(context);
    }

    private void init(Context context) {
        setEGLContextClientVersion(2);
        setRenderer(this);
        setRenderMode(RENDERMODE_WHEN_DIRTY);
        mEffectRenderHelper = new EffectRenderHelper(context);
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        requestRender();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.i(TAG, "EffectView onSurfaceCreated");
        GLES20.glEnable(GL10.GL_DITHER);
        GLES20.glClearColor(0, 0, 0, 0);
        prepareSurfaceTexture(this);
        if (mSurfaceHolderListener != null) {
            mSurfaceHolderListener.surfaceCreated(null);
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.i(TAG, "previewsize onSurfaceChanged width=" + width + ",height=" + height);
        if(mIsPaused) {
            return;
        }
        mSurfaceWidth = width;
        mSurfaceHeight = height;
        mEffectRenderHelper.initViewPort(width, height);

        if (mSurfaceHolderListener != null) {
            mSurfaceHolderListener.surfaceChanged(null, 0, width, height);
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (mIsPaused) {
            return;
        }
        mSurfaceTexture.updateTexImage();
        if(!mIsPreviewing) return;
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        dstTexture = mEffectRenderHelper.processTexture(mSurfaceTextureID);

        synchronized (this) {
            if (mVideoEncoder != null) {
                if(mSpeed > 1) {
                    mVideoEncoder.frameAvailableSoon();
                    mVideoEncoder.frameAvailableSoon();
                } else {
                    mVideoEncoder.frameAvailableSoon();
                }
            }
        }

        if (dstTexture != ShaderHelper.NO_TEXTURE) {
            GLES20.glViewport(0, 0, mSurfaceWidth, mSurfaceHeight);
            mEffectRenderHelper.drawFrame(dstTexture);
        }
    }

    /**prepareSurfaceTexture
     * 初始化SurfaceTexture
     *
     * @param listener
     */
    public void prepareSurfaceTexture(final SurfaceTexture.OnFrameAvailableListener listener) {
        if (mSurfaceTextureID == ShaderHelper.NO_TEXTURE) {
            mSurfaceTextureID = ShaderHelper.getExternalOESTextureID();
            mSurfaceTexture = new SurfaceTexture(mSurfaceTextureID);
            mSurfaceTexture.setOnFrameAvailableListener(listener);
        }
    }

    public void setPreviewSize(int width, int height){
        Log.i(TAG, "previewsize setPreviewSize width=" + width + ", height=" + height);
        mImageWidth = width;
        mImageHeight = height;
        mEffectRenderHelper.setImageSize(mImageWidth, mImageHeight);//设置预览尺寸
    }

    public void setCameraId(int cameraId, int orientation) {
        Log.i(TAG,"setCameraId = " + cameraId);
        boolean flipHoriontal = cameraId == 1 ? true : false;
        mEffectRenderHelper.adjustTextureBuffer(/*CameraDevice.get().getOrientation()*/flipHoriontal ? 270 : 90,flipHoriontal, false);
    }

    public void setSurfaceHolderListener(SurfaceHolder.Callback surfaceHolderListener) {
        mSurfaceHolderListener = surfaceHolderListener;
        if (surfaceHolderListener == null) {
            mSurfaceHolder = null;
        }
    }

    public SurfaceTexture getSurfaceTexture() {
        return mSurfaceTexture;
    }

    public void setPreviewing(boolean isPreviewing) {
        mIsPreviewing = isPreviewing;
    }

    public void startRecording(String currentDescriptorName, MediaMuxerListener mediaMuxerListener, boolean isRecordAudio, int orientation, int speed) {
        Log.v(TAG, "startRecording:");
        try {
            mSpeed = speed;
            mMuxer = new MediaMuxerWrapper(currentDescriptorName, mediaMuxerListener);
            if (true) {
                // for video capturing
                MediaEncoder encoder = new MediaVideoEncoder(mMuxer, mMediaEncoderListener, mImageHeight, mImageWidth);
                encoder.setSpeed(speed);
            }
            if (isRecordAudio && speed == 1) {
                // for audio capturing
                new MediaAudioEncoder(mMuxer, mMediaEncoderListener);
            }
            mMuxer.prepare();
            mMuxer.setOrientationHint(orientation);
            mMuxer.startRecording();
        } catch (final IOException e) {
            Log.e(TAG, "startCapture:", e);
        }
    }

    public void startRecording(FileDescriptor fileDescriptor, MediaMuxerListener mediaMuxerListener, boolean isRecordAudio, int orientation, int speed) {
        Log.v(TAG, "startRecording:");
        try {
            mSpeed = speed;
            mMuxer = new MediaMuxerWrapper(fileDescriptor, mediaMuxerListener);
            if (true) {
                // for video capturing
                MediaEncoder encoder = new MediaVideoEncoder(mMuxer, mMediaEncoderListener, mImageHeight, mImageWidth);
                encoder.setSpeed(speed);
            }
            if (isRecordAudio && speed == 1) {
                // for audio capturing
                new MediaAudioEncoder(mMuxer, mMediaEncoderListener);
            }
            mMuxer.prepare();
            mMuxer.setOrientationHint(orientation);
            mMuxer.startRecording();
        } catch (final IOException e) {
            Log.e(TAG, "startCapture:", e);
        }
    }

    /**
     * callback methods from encoder
     */
    private final MediaEncoder.MediaEncoderListener mMediaEncoderListener = new MediaEncoder.MediaEncoderListener() {
        @Override
        public void onPrepared(final MediaEncoder encoder) {
            Log.v(TAG, "onPrepared:encoder=" + encoder);
            if (encoder instanceof MediaVideoEncoder) {
                setVideoEncoder((MediaVideoEncoder) encoder);
            } else if (encoder instanceof MediaAudioEncoder) {
                mAudioEncoder = (MediaAudioEncoder) encoder;
            }
        }

        @Override
        public void onStopped(final MediaEncoder encoder) {
            Log.v(TAG, "onStopped:encoder=" + encoder);
            if (encoder instanceof MediaVideoEncoder) {
                setVideoEncoder(null);
            } else if (encoder instanceof MediaAudioEncoder) {
                mAudioEncoder = null;
            }
        }
    };

    public void setVideoEncoder(final MediaVideoEncoder encoder) {
        Log.v(TAG, "setVideoEncoder:tex_id=" + mSurfaceTextureID + ",encoder=" + encoder);
        queueEvent(new Runnable() {
            @Override
            public void run() {
                synchronized (this) {
                    if (encoder != null) {
                        encoder.setEglContext(EGL14.eglGetCurrentContext(), dstTexture, mEffectRenderHelper);
                    }
                    mVideoEncoder = encoder;
                }
            }
        });
    }

    public void pauseRecording() {
        if(null != mVideoEncoder && !mVideoEncoder.getState()) {
            mVideoEncoder.pause();
        }
        if (null != mAudioEncoder) {
            mAudioEncoder.pause();
        }
    }

    public void resumeRecording() {
        if(null != mVideoEncoder && mVideoEncoder.getState()) {
            mVideoEncoder.resume();
        }
        if (null != mAudioEncoder) {
            mAudioEncoder.resume();
        }
    }

    public void stopRecording() {
        Log.v(TAG, "stopRecording:mMuxer=" + mMuxer);

        if (mMuxer != null) {
            mMuxer.stopRecording();
            mMuxer = null;
        }
    }

    public void setCameraAppUI(CameraAppUI cameraAppUI) {
        mCameraAppUI = cameraAppUI;
    }

    public PreviewStatusListener.PreviewAreaChangedListener getPreviewAreaChangedListener() {
        return mPreviewAreaChangedListener;
    }

    public void setAspectRatio(float aspectRatio) {
        mAspectRatio = aspectRatio;
    }

    private PreviewStatusListener.PreviewAreaChangedListener mPreviewAreaChangedListener =
            new PreviewStatusListener.PreviewAreaChangedListener() {

                @Override
                public void onPreviewAreaChanged(RectF previewArea) {
                    int width = (int) previewArea.width();
                    int height = (int) previewArea.height();
                    setTransformMatrix(Math.round(width),
                            Math.round(height));
                }

                private void setTransformMatrix(int previewWidth, int previewHeight) {
                    float scaledTextureWidth, scaledTextureHeight;
                    if (mAspectRatio == 0 || mCameraAppUI == null) {
                        return;
                    }
                    if (previewWidth > previewHeight) {
                        scaledTextureWidth = Math.min(previewWidth,
                                (int) (previewHeight * mAspectRatio));
                        scaledTextureHeight = Math.min(previewHeight,
                                (int) (previewWidth / mAspectRatio));
                    } else {
                        scaledTextureWidth = Math.min(previewWidth,
                                (int) (previewHeight / mAspectRatio));
                        scaledTextureHeight = Math.min(previewHeight,
                                (int) (previewWidth * mAspectRatio));
                    }
                    FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) getLayoutParams();
                    params.width = (int) scaledTextureWidth;
                    params.height = (int) scaledTextureHeight;
                    RectF rect = mCameraAppUI.getPreviewArea();
                    // horizontal direction
                    params.setMargins((int) rect.left, (int) rect.top, 0, 0);

                    setLayoutParams(params);

                    Log.i(TAG, "setTransformMatrix(): width = " + previewWidth
                            + " height = " + previewHeight
                            + " scaledTextureWidth = " + scaledTextureWidth
                            + " scaledTextureHeight = " + scaledTextureHeight
                            + " mAspectRatio = " + mAspectRatio);
                }
            };

}
