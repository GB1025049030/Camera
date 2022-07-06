package com.freeme.camera.modules.slrphoto;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;

import com.android.camera.bitmap.bitmappool.BitmapPoolManager;

import static android.renderscript.Allocation.MipmapControl.MIPMAP_NONE;
import static android.renderscript.Allocation.USAGE_SCRIPT;

public enum BlurUtils {
    I;

    private RenderScript mRenderScript;
    private ScriptIntrinsicBlur mScriptBlur;

    private void create(Context context) {
        if (mRenderScript == null || mScriptBlur == null) {
            mRenderScript = RenderScript.create(context);
            mScriptBlur = ScriptIntrinsicBlur.create(mRenderScript, Element.U8_4(mRenderScript));
        }
    }

    public void destroy() {
        if (mScriptBlur != null) {
            mScriptBlur.destroy();
            mScriptBlur = null;
        }
        if (mRenderScript != null) {
            mRenderScript.destroy();
            mRenderScript = null;
        }
    }

    public Bitmap getBitmap(Context context, Bitmap sentBitmap, int radius) {
        create(context);
        return getBitmap(mRenderScript, mScriptBlur, sentBitmap, radius);
    }

    public Bitmap getBitmap(Context context, Bitmap sentBitmap, int radius, int width) {
        create(context);
        return getBitmap(mRenderScript, mScriptBlur, sentBitmap, radius, width);
    }

    private Bitmap getBitmap(RenderScript renderScript, ScriptIntrinsicBlur scriptBlur,
                             Bitmap sentBitmap, final int radius) {
        if (sentBitmap == null
                || radius < 0 || radius > 25
                || renderScript == null
                || scriptBlur == null) {
            return null;
        }

        final Allocation input = Allocation.createFromBitmap(renderScript, sentBitmap, MIPMAP_NONE, USAGE_SCRIPT);
        scriptBlur.setInput(input);
        final Allocation output = Allocation.createTyped(renderScript, input.getType());
        scriptBlur.forEach(output);
        scriptBlur.setRadius(radius);

        Bitmap bitmap = BitmapPoolManager.I.get(sentBitmap.getWidth(), sentBitmap.getHeight(), sentBitmap.getConfig());
        output.copyTo(bitmap);

        input.destroy();
        output.destroy();

        return bitmap;
    }

    private Bitmap getBitmap(RenderScript renderScript, ScriptIntrinsicBlur scriptBlur,
                             Bitmap sentBitmap, final int radius, final int scaleSize) {
        if (sentBitmap == null
                || radius < 0 || radius > 25
                || renderScript == null
                || scriptBlur == null
                || scaleSize < 0) {
            return null;
        }

        final int pictureWidth = sentBitmap.getWidth();
        final int pictureHeight = sentBitmap.getHeight();
        sentBitmap = Bitmap.createScaledBitmap(
                sentBitmap,
                scaleSize,
                scaleSize * pictureHeight / pictureWidth, // 计算预览界面的高度
                false);
        Bitmap bitmap = getBitmap(renderScript, scriptBlur, sentBitmap, radius);
        bitmap = Bitmap.createScaledBitmap(bitmap, pictureWidth, pictureHeight, false);
        return bitmap;
    }

    public Bitmap small(Bitmap bitmap) {
        Matrix matrix = new Matrix();
        matrix.postScale(0.1f, 0.1f);
        Bitmap resizeBmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(),
                matrix, true);
        return resizeBmp;
    }
}
