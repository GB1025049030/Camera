package com.android.camera.bitmap.bitmappool;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.util.Log;

class MemorySizeCalculator {
    private static final String TAG = "MemorySizeCalculator";

    // Visible for testing.
    static final int BYTES_PER_ARGB_8888_PIXEL = 4;
    static final int BITMAP_POOL_TARGET_SCREENS = 4;
    static final float MAX_SIZE_MULTIPLIER = 0.4f;
    static final float LOW_MEMORY_MAX_SIZE_MULTIPLIER = 0.33f;

    private final int bitmapPoolSize;
    private final Context context;

    interface ScreenDimensions {
        int getWidthPixels();
        int getHeightPixels();
    }

    public MemorySizeCalculator(Context context) {
        this(context,
                (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE),
                new DisplayMetricsScreenDimensions(context.getResources().getDisplayMetrics()));
    }

    // Visible for testing.
    MemorySizeCalculator(Context context, ActivityManager activityManager, ScreenDimensions screenDimensions) {
        this.context = context;
        final int maxSize = getMaxSize(activityManager);

        final int screenSize = screenDimensions.getWidthPixels() * screenDimensions.getHeightPixels()
                * BYTES_PER_ARGB_8888_PIXEL;

        int targetPoolSize = screenSize * BITMAP_POOL_TARGET_SCREENS;

        if (targetPoolSize <= maxSize) {
            bitmapPoolSize = targetPoolSize;
        } else {
            int part = Math.round((float) maxSize / (BITMAP_POOL_TARGET_SCREENS));
            bitmapPoolSize = part * BITMAP_POOL_TARGET_SCREENS;
        }
    }

    /**
     * Returns the recommended bitmap pool size for the device it is run on in bytes.
     */
    public int getBitmapPoolSize() {
        return bitmapPoolSize;
    }

    private static int getMaxSize(ActivityManager activityManager) {
        final int memoryClassBytes = activityManager.getMemoryClass() * 1024 * 1024;
        final boolean isLowMemoryDevice = isLowMemoryDevice(activityManager);
        return Math.round(memoryClassBytes
                * (isLowMemoryDevice ? LOW_MEMORY_MAX_SIZE_MULTIPLIER : MAX_SIZE_MULTIPLIER));
    }

    private String toMb(int bytes) {
        return Formatter.formatFileSize(context, bytes);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static boolean isLowMemoryDevice(ActivityManager activityManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return activityManager.isLowRamDevice();
        } else {
            return Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB;
        }
    }

    private static class DisplayMetricsScreenDimensions implements ScreenDimensions {
        private final DisplayMetrics displayMetrics;

        public DisplayMetricsScreenDimensions(DisplayMetrics displayMetrics) {
            this.displayMetrics = displayMetrics;
        }

        @Override
        public int getWidthPixels() {
            return displayMetrics.widthPixels;
        }

        @Override
        public int getHeightPixels() {
            return displayMetrics.heightPixels;
        }
    }
}

