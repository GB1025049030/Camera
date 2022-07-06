package com.dream.camera.modules.qr;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.android.util.libyuv.YUVManager;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

public enum  QrDecodeManager {
    I;

    private int mScreenWidth, mScreenHeight;

    private void createSize(Context context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        manager.getDefaultDisplay().getMetrics(displayMetrics);
        mScreenWidth = displayMetrics.widthPixels;
        mScreenHeight = displayMetrics.heightPixels;
    }

    public void decode(IResult listener, byte[] data, int width, int height, Rect rect) {
        if (listener == null) {
            return;
        }

        if (data == null || width <= 0 || height <= 0) {
            listener.onFailed();
            return;
        }

        Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
        Collection<BarcodeFormat> decodeFormats = new ArrayList<>(DecodeFormatManager.getQrCodeFormats());
        hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);
        DecodeCore decodeCore = new DecodeCore(hints);

        Result result = decodeCore.decode(data, width, height, rect, true);
        if (result != null && !TextUtils.isEmpty(result.getText())) {
            listener.onSuccess(result);
        } else {
            listener.onFailed();
        }
    }

    public void decode(Context context, IResult listener, String imagePath) {
        if (mScreenWidth == 0 || mScreenHeight == 0) {
            createSize(context);
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);

        options.inSampleSize = Math.max(options.outWidth / mScreenWidth, options.outHeight / mScreenHeight);
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);
        if (bitmap == null) {
            if (listener != null) listener.onFailed();
            return;
        }

        if (bitmap.getConfig() != Bitmap.Config.ARGB_8888) {
            bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, false);
        }

        byte[] rgbaData = bitmap2Bytes(bitmap);
        if (rgbaData == null) {
            if (listener != null) listener.onFailed();
            return;
        }

        int width = bitmap.getWidth() - (bitmap.getWidth() % 6);
        int height = bitmap.getHeight() - (bitmap.getHeight() % 6);

        final byte[] i420Data = new byte[width * height * 3 / 2];
        YUVManager.I.transformRGBAToI420(i420Data, rgbaData, width, height);
        if (i420Data == null) {
            if (listener != null) listener.onFailed();
            return;
        }

        Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
        Collection<BarcodeFormat> decodeFormats = new ArrayList<>(DecodeFormatManager.getQrCodeFormats());
        hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);
        DecodeCore decodeCore = new DecodeCore(hints);

        Result result = decodeCore.decode(i420Data, width, height, null, false);
        if (result != null && !TextUtils.isEmpty(result.getText())) {
            if (listener != null) listener.onSuccess(result);
        } else {
            if (listener != null) listener.onFailed();
        }
    }

    private byte[] bitmap2Bytes(Bitmap bitmap) {
        int picw = bitmap.getWidth(), pich = bitmap.getHeight();
        int[] pix = new int[picw * pich];
        bitmap.getPixels(pix, 0, picw, 0, 0, picw, pich);

        int tempH = pich - (pich % 6);
        int tempW = picw - (picw % 6);
        byte[] result = new byte[tempW * tempH * 4];

        for (int y = 0; y < tempH; y++) {
            for (int x = 0; x < tempW; x++) {
                int dstIndex = y * tempW + x;
                int srcIndex = y * picw + x;
                result[dstIndex * 4] = (byte) ((pix[srcIndex] >> 16) & 0xff);     //bitwise shifting
                result[dstIndex * 4 + 1] = (byte) ((pix[srcIndex] >> 8) & 0xff);
                result[dstIndex * 4 + 2] = (byte) (pix[srcIndex] & 0xff);
                result[dstIndex * 4 + 3] = (byte) 0xff;
            }
        }
        return result;
    }

    public interface IResult {
        void onSuccess(Result result);
        void onFailed();
    }
}
