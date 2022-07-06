package com.dream.camera.effect;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import com.android.ex.camera2.portability.debug.Log;

public class OpenGLRender {

    private static final Log.Tag TAG = new Log.Tag("OpenGLRender");

    private static final String CAMERA_INPUT_VERTEX_SHADER = "" +
            "attribute vec4 position;\n" +
            "attribute vec4 inputTextureCoordinate;\n" +
            "\n" +
            "varying vec2 textureCoordinate;\n" +
            "\n" +
            "void main()\n" +
            "{\n" +
            "	textureCoordinate = inputTextureCoordinate.xy;\n" +
            "	gl_Position = position;\n" +
            "}";

    private static final String CAMERA_INPUT_FRAGMENT_SHADER_OES = "" +
            "#extension GL_OES_EGL_image_external : require\n" +
            "\n" +
            "precision mediump float;\n" +
            "varying vec2 textureCoordinate;\n" +
            "uniform samplerExternalOES inputImageTexture;\n" +
            "\n" +
            "void main()\n" +
            "{\n" +
            "	gl_FragColor = texture2D(inputImageTexture, textureCoordinate);\n" +
            "}";

    public static final String CAMERA_INPUT_FRAGMENT_SHADER = "" +
            "precision mediump float;\n" +
            "varying highp vec2 textureCoordinate;\n" +
            " \n" +
            "uniform sampler2D inputImageTexture;\n" +
            " \n" +
            "void main()\n" +
            "{\n" +
            "     gl_FragColor = texture2D(inputImageTexture, textureCoordinate);\n" +
            "}";

    private final static String PROGRAM_ID = "program";
    private final static String POSITION_COORDINATE = "position";
    private final static String TEXTURE_UNIFORM = "inputImageTexture";
    private final static String TEXTURE_COORDINATE = "inputTextureCoordinate";

    private Context mContext;
    private final FloatBuffer mGLCubeBuffer;
    private final FloatBuffer mGLTextureBuffer;
    private boolean mIsInitialized;
    private ArrayList<HashMap<String, Integer>> mArrayPrograms = new ArrayList<HashMap<String, Integer>>(2) {
        {
            for (int i = 0; i < 2; ++i) {
                HashMap<String, Integer> hashMap = new HashMap<>();
                hashMap.put(PROGRAM_ID, 0);
                hashMap.put(POSITION_COORDINATE, -1);
                hashMap.put(TEXTURE_UNIFORM, -1);
                hashMap.put(TEXTURE_COORDINATE, -1);
                add(hashMap);
            }
        }
    };

    private int[] mFrameBuffers;
    private int[] mFrameBufferTextures;
    private final static int FRAME_BUFFER_NUM = 3;

    private FloatBuffer mTextureBuffer;
    private FloatBuffer mVertexBuffer;

    // 纹理宽度
    private int mViewPortWidth;
    // 纹理高度
    private int mViewPortHeight;

    public OpenGLRender(Context context) {
        mContext = context;
        mGLCubeBuffer = ByteBuffer.allocateDirect(TextureRotationUtil.CUBE.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mGLCubeBuffer.put(TextureRotationUtil.CUBE).position(0);
        mGLTextureBuffer = ByteBuffer.allocateDirect(TextureRotationUtil.TEXTURE_FLIPPED.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        /* */
        /**
         * 用来绘制到屏幕的纹理坐标 注意需要y轴翻转
         *          1  2
         *          3  4
         */
        mGLTextureBuffer.put(TextureRotationUtil.TEXTURE_FLIPPED).position(0);
    }

    public void init(int width, int height) {
        initProgram(CAMERA_INPUT_FRAGMENT_SHADER_OES, mArrayPrograms.get(0));
        initProgram(CAMERA_INPUT_FRAGMENT_SHADER, mArrayPrograms.get(1));
        mViewPortWidth = width;
        mViewPortHeight = height;
        initFrameBuffers(width, height);
        mIsInitialized = true;
    }

    /**
     * frameBuffer和texture的初始化
     */
    private void initFrameBuffers(int width, int height) {
        destroyFrameBuffers();
        if (mFrameBuffers == null) {
            mFrameBuffers = new int[FRAME_BUFFER_NUM];
            mFrameBufferTextures = new int[FRAME_BUFFER_NUM];

            GLES20.glGenFramebuffers(FRAME_BUFFER_NUM, mFrameBuffers, 0);
            GLES20.glGenTextures(FRAME_BUFFER_NUM, mFrameBufferTextures, 0);

            bindFrameBuffer(mFrameBufferTextures[0], mFrameBuffers[0], width, height);
            bindFrameBuffer(mFrameBufferTextures[1], mFrameBuffers[1], width, height);
            // 加速glReadPixel的读取速度
            bindFrameBuffer(mFrameBufferTextures[2], mFrameBuffers[2], width, height);
        }
    }

    /**
     * 纹理参数设置+buffer绑定
     */
    private void bindFrameBuffer(int textureId, int frameBuffer, int width, int height) {
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0,
                GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D, textureId, 0);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

    private void destroyFrameBuffers() {
        if (mFrameBufferTextures != null) {
            GLES20.glDeleteTextures(FRAME_BUFFER_NUM, mFrameBufferTextures, 0);
            mFrameBufferTextures = null;
        }
        if (mFrameBuffers != null) {
            GLES20.glDeleteFramebuffers(FRAME_BUFFER_NUM, mFrameBuffers, 0);
            mFrameBuffers = null;
        }
    }

    private void initProgram(String fragment, HashMap<String, Integer> programInfo) {
        int proID = programInfo.get(PROGRAM_ID);
        if (proID == 0) {
            proID = ShaderHelper.buildProgram(CAMERA_INPUT_VERTEX_SHADER, fragment);
            programInfo.put(PROGRAM_ID, proID);
            programInfo.put(POSITION_COORDINATE, GLES20.glGetAttribLocation(proID, POSITION_COORDINATE));
            programInfo.put(TEXTURE_UNIFORM, GLES20.glGetUniformLocation(proID, TEXTURE_UNIFORM));
            programInfo.put(TEXTURE_COORDINATE, GLES20.glGetAttribLocation(proID, TEXTURE_COORDINATE));
        }
    }

    /**
     * 此函数有三个功能
     * 1. 将OES的纹理转换为标准的GL_TEXTURE_2D格式
     * 2. 根据相机预览帧的旋转角度将纹理转正（即人脸为正），如果是前置摄像头，还会进行水平翻转
     * 3. 读取上面两个步骤后纹理的内容到cpu内存，存储为RGBA格式的buffer
     *
     * @param textureId 输入的OES的纹理id
     * @return 转换后的GL_TEXTURE_2D的纹理id
     */
    public int preProcess(int textureId) {
        if (mFrameBuffers == null
                || !mIsInitialized)
            return -2;
        GLES20.glUseProgram(mArrayPrograms.get(0).get(PROGRAM_ID));
        ShaderHelper.checkGlError("glUseProgram " + mArrayPrograms.get(0).get(PROGRAM_ID));
        //设置顶点的属性
        mGLCubeBuffer.position(0);
        int glAttribPosition = mArrayPrograms.get(0).get(POSITION_COORDINATE);
        //设置顶点buffer的类型和访问的数据偏移量
        GLES20.glVertexAttribPointer(glAttribPosition, 2, GLES20.GL_FLOAT, false, 0, mGLCubeBuffer);
        //接下来渲染使用缓存中的数据
        GLES20.glEnableVertexAttribArray(glAttribPosition);

        mTextureBuffer.position(0);
        int glAttribTextureCoordinate = mArrayPrograms.get(0).get(TEXTURE_COORDINATE);
        GLES20.glVertexAttribPointer(glAttribTextureCoordinate, 2, GLES20.GL_FLOAT, false, 0, mTextureBuffer);
        GLES20.glEnableVertexAttribArray(glAttribTextureCoordinate);

        if (textureId != -1) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
            GLES20.glUniform1i(mArrayPrograms.get(0).get(TEXTURE_UNIFORM), 0);
        }

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffers[0]);
        ShaderHelper.checkGlError("glBindFramebuffer");

        GLES20.glViewport(0, 0, mViewPortWidth, mViewPortHeight);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glDisableVertexAttribArray(glAttribPosition);
        GLES20.glDisableVertexAttribArray(glAttribTextureCoordinate);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glUseProgram(0);

        return mFrameBufferTextures[0];
    }

    public int getOutputTexture() {
        if (mFrameBufferTextures != null) {
            return mFrameBufferTextures[1];
        }
        return ShaderHelper.NO_TEXTURE;
    }

    /**
     * 绘制图像
     */
    public int onDrawFrame(final int textureId) {
        if (!mIsInitialized) {
            return ShaderHelper.NOT_INIT;
        }

        GLES20.glUseProgram(mArrayPrograms.get(1).get(PROGRAM_ID));
        mVertexBuffer.position(0);
        int glAttribPosition = mArrayPrograms.get(1).get(POSITION_COORDINATE);
        GLES20.glVertexAttribPointer(glAttribPosition, 2, GLES20.GL_FLOAT, false, 0, mVertexBuffer);
        GLES20.glEnableVertexAttribArray(glAttribPosition);

        mGLTextureBuffer.position(0);
        int glAttribTextureCoordinate = mArrayPrograms.get(1).get(TEXTURE_COORDINATE);
        GLES20.glVertexAttribPointer(glAttribTextureCoordinate, 2, GLES20.GL_FLOAT, false, 0,
                mGLTextureBuffer);
        GLES20.glEnableVertexAttribArray(glAttribTextureCoordinate);

        //将纹理绑定
        if (textureId != ShaderHelper.NO_TEXTURE) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
            GLES20.glUniform1i(mArrayPrograms.get(1).get(TEXTURE_UNIFORM), 0);
        }

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glDisableVertexAttribArray(glAttribPosition);
        GLES20.glDisableVertexAttribArray(glAttribTextureCoordinate);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        return ShaderHelper.ON_DRAWN;
    }

    public void calculateVertexBuffer(int displayW, int displayH, int imageW, int imageH) {
        float[] cube = calcVertex(displayW, displayH, imageW, imageH);
        if (mVertexBuffer == null) {
            mVertexBuffer = ByteBuffer.allocateDirect(cube.length * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer();
        }
        mVertexBuffer.clear();
        mVertexBuffer.put(cube).position(0);
    }

    public void adjustTextureBuffer(int orientation, boolean flipHorizontal, boolean flipVertical) {
        float[] textureCords = TextureRotationUtil.getRotation(orientation, flipHorizontal, flipVertical);
        if (mTextureBuffer == null) {
            mTextureBuffer = ByteBuffer.allocateDirect(textureCords.length * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer();
        }
        mTextureBuffer.clear();
        mTextureBuffer.put(textureCords).position(0);
    }

    private float[] calcVertex(int displayW, int displayH, int imageW, int imageH) {
        int outputHeight = displayH;
        int outputWidth = displayW;

        float ratio1 = (float) outputWidth / imageW;
        float ratio2 = (float) outputHeight / imageH;

        float ratio = Math.min(ratio1, ratio2);

        int imageWidthNew = Math.round(imageW * ratio);
        int imageHeightNew = Math.round(imageH * ratio);


        float ratioWidth = imageWidthNew / (float) outputWidth;
        float ratioHeight = imageHeightNew / (float) outputHeight;


        float[] cube = new float[]{
                TextureRotationUtil.CUBE[0] / ratioHeight, TextureRotationUtil.CUBE[1] / ratioWidth,
                TextureRotationUtil.CUBE[2] / ratioHeight, TextureRotationUtil.CUBE[3] / ratioWidth,
                TextureRotationUtil.CUBE[4] / ratioHeight, TextureRotationUtil.CUBE[5] / ratioWidth,
                TextureRotationUtil.CUBE[6] / ratioHeight, TextureRotationUtil.CUBE[7] / ratioWidth,
        };


        return cube;
    }
}
