package com.dream.camera.modules.qr;

import android.graphics.Rect;
import android.util.Log;

import com.android.camera.util.CameraUtil;
import com.android.util.libyuv.YUVManager;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.util.Map;

public class DecodeCore {
    private static final String TAG = DecodeCore.class.getSimpleName();
    private final MultiFormatReader multiFormatReader;

    public DecodeCore(Map<DecodeHintType, Object> hints) {
        multiFormatReader = new MultiFormatReader();
        multiFormatReader.setHints(hints);
    }

    private byte[] getMatrix(byte[] src, int oldWidth, int oldHeight, Rect rect) {
        byte[] matrix = new byte[rect.width() * rect.height() * 3 / 2];
        YUVManager.I.nv21ConvertToI420(src, oldWidth, oldHeight, matrix, rect.width(), rect.height(), rect.left, rect.top);
        return matrix;
    }

    public Result decode(byte[] data, int width, int height, Rect cropRect, boolean needRotate90) {
        byte[] rotatedData = data;

        if (needRotate90) {
            rotatedData = new byte[data.length];
            YUVManager.I.update(width, height, width, height);
            YUVManager.I.rotateNV21(rotatedData, data, width, height, 90);
            int tmp = width;
            width = height;
            height = tmp;
        }

        byte[] processSrc = rotatedData;
        int processWidth = width;
        int processHeight = height;
        if (cropRect != null) {
            processWidth = cropRect.width();
            processHeight = cropRect.height();

            if (processWidth % 6 != 0) {
                processWidth -= processWidth % 6;

                cropRect.right = cropRect.left + processWidth;
            }
            if (processHeight % 6 != 0) {
                processHeight -= processHeight % 6;
                cropRect.bottom = cropRect.top + processHeight;
            }

            processSrc = getMatrix(rotatedData, width, height, cropRect);
            CameraUtil.saveYUVToFile(processSrc, processWidth, processHeight);
        }

        Result rawResult = null;

        //zxing
        PlanarYUVLuminanceSource source = buildLuminanceSource(processSrc, processWidth, processHeight);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        try {
            rawResult = multiFormatReader.decodeWithState(bitmap);
            Log.v(TAG, "zxing success");
        } catch (NotFoundException e) {
            e.printStackTrace();
        } finally {
            multiFormatReader.reset();
        }

        //zbar
        if (rawResult == null) {
            String zbarResult = ZBarController.I.scan(processSrc, processWidth, processHeight);
            if (zbarResult != null) {
                rawResult = new Result(zbarResult, null, null, null);
                Log.v(TAG, "zbar success");
            }
        }
        return rawResult;
    }

    public PlanarYUVLuminanceSource buildLuminanceSource(byte[] data, int width, int height) {
        Rect rect = new Rect(0, 0, width, height);
        return new PlanarYUVLuminanceSource(data, width, height, rect.left, rect.top, rect.width(), rect
            .height(), false);
    }
}
