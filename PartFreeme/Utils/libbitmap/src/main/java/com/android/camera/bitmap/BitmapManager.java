package com.android.camera.bitmap;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;

public class BitmapManager {
    static {
        System.loadLibrary("bitmaputils");
    }

    /**
     * 获取透明Bitmap
     * @param baseBitmap
     * @param alpha alpha 255 > alpha > 0
     */
    public static native boolean getAlphaBitmap(Bitmap baseBitmap, float alpha);

    public static native boolean getMixAlphaBitmap(Bitmap targetBitmap, Bitmap baseBitmap, float alpha);

    public static Bitmap doWaterMark(Bitmap src, String watermark) {
        if (TextUtils.isEmpty(watermark)) return src;
        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();
        Bitmap newBitmap = Bitmap.createBitmap(srcWidth, srcHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
        canvas.drawBitmap(src, 0, 0, null);
        float radio = Math.min(srcWidth, srcHeight) / 720f;
        float textSize = 25f * radio;
        TextPaint textPaint = new TextPaint();
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(textSize);
        textPaint.setColor(0xFFf5f5f5);
        int lineNum = watermark.split("\n").length;
        final float spacingAdd = 40f;
        StaticLayout.Builder builder =
                StaticLayout.Builder.obtain(watermark, 0, watermark.length(), textPaint, canvas.getWidth())
                        .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                        .setLineSpacing(spacingAdd, 1)
                        .setIncludePad(true);
        StaticLayout myStaticLayout = builder.build();
        float dx = textSize;
        float dy = textSize * (lineNum + 2);
        if (lineNum > 1) {
            dy = textSize * (lineNum + 2) + spacingAdd * (lineNum - 1);
        }
        canvas.translate(dx, srcHeight - dy);
        myStaticLayout.draw(canvas);
        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();
        return newBitmap;
    }
}
