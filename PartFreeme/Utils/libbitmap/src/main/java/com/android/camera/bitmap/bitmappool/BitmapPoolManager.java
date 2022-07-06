package com.android.camera.bitmap.bitmappool;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;

import com.bumptech.glide.MemoryCategory;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPoolAdapter;
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool;
import com.bumptech.glide.util.Util;

public enum BitmapPoolManager {
    I;

    private BitmapPool mBitmapPool;

    /**
     * init in Application
     * @param context
     */
    public void initialization(Context context) {
        MemorySizeCalculator calculator = new MemorySizeCalculator(context.getApplicationContext());
        if (mBitmapPool == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                int size = calculator.getBitmapPoolSize();
                mBitmapPool = new LruBitmapPool(size);
            } else {
                mBitmapPool = new BitmapPoolAdapter();
            }
        }
    }

    public Bitmap get(int width, int height, Bitmap.Config config) {
        Bitmap bitmap = mBitmapPool.get(width, height, config);
        if (bitmap == null) {
            bitmap = Bitmap.createBitmap(width, height, config);
        }
        return bitmap;
    }

    public void put(Bitmap bitmap) {
        mBitmapPool.put(bitmap);
    }

    /**
     * clear when onLowMemory in Application
     */
    public void clearMemory() {
        // Engine asserts this anyway when removing resources, fail faster and consistently
        Util.assertMainThread();
        mBitmapPool.clearMemory();
    }

    public void trimMemory(int level) {
        // Engine asserts this anyway when removing resources, fail faster and consistently
        Util.assertMainThread();
        mBitmapPool.trimMemory(level);
    }

    public void setMemoryCategory(MemoryCategory memoryCategory) {
        // Engine asserts this anyway when removing resources, fail faster and consistently
        Util.assertMainThread();
        mBitmapPool.setSizeMultiplier(memoryCategory.getMultiplier());
    }
}
