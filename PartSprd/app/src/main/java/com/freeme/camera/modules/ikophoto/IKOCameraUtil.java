package com.freeme.camera.modules.ikophoto;

import android.content.Context;
import android.view.Display;
import android.view.WindowManager;

import com.android.camera.util.Size;
import com.android.ex.camera2.portability.CameraCapabilities;

import java.util.List;

public enum  IKOCameraUtil {
    I;

    public static final int PREVIEW_16_9_MODE = 1;
    public static final int PREVIEW_4_3_MODE = (1 << 1);

    private Size getScreenSize(Context context) {
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        return new Size(display.getHeight(), display.getWidth());
    }

    public Size getBestPreviewSize(Context context, CameraCapabilities mCameraCapabilities, int mode) {
        List<Size> previewSizes = Size.convert(mCameraCapabilities.getSupportedPreviewSizes());
        Size screenSize = getScreenSize(context);

        int requestHeight = screenSize.getHeight();
        int requestWidth = (mode & PREVIEW_16_9_MODE) != 0 ? (requestHeight << 4) / 9 : (requestHeight << 2) / 3;

        int currentWidth;
        int currentHeight;
        int bestPreviewWidth = 0;
        int bestPreviewHeight = 0;
        int diffs = Integer.MAX_VALUE;

        for (int i = 0; i < previewSizes.size(); i++) {
            Size size = previewSizes.get(i);
            currentWidth = size.getWidth();
            currentHeight = size.getHeight();

            int newDiffs = Math.abs(currentWidth - requestWidth) + Math.abs(currentHeight - requestHeight);
            if(newDiffs == 0){
                bestPreviewWidth = currentWidth;
                bestPreviewHeight = currentHeight;
                break;
            }
            if(diffs > newDiffs){
                bestPreviewWidth = currentWidth;
                bestPreviewHeight = currentHeight;
                diffs = newDiffs;
            }
        }
        return new Size(bestPreviewWidth, bestPreviewHeight);
    }
}
