package com.freeme.camera.modules;

import android.graphics.Bitmap;
import android.location.Location;

import androidx.annotation.NonNull;

import com.android.camera.Exif;
import com.android.camera.exif.ExifInterface;

import java.io.ByteArrayOutputStream;

public class FlowablePhotoBean {
    @NonNull private final byte[] mJpegData;
    @NonNull private final Location mLocation;
    @NonNull private final ExifInterface mExif;
    private final boolean mInBurstMode;
    private final boolean mShouldResizeTo16x9;

    private FlowablePhotoBean(@NonNull byte[] jpeg, @NonNull ExifInterface exif,
                             @NonNull Location location, boolean inBurstMode, boolean shouldResizeTo16x9) {
        this.mJpegData = jpeg;
        this.mExif = exif;
        this.mLocation = location;
        this.mInBurstMode = inBurstMode;
        this.mShouldResizeTo16x9 = shouldResizeTo16x9;
    }

    public byte[] getJpegData() {
        return mJpegData;
    }

    public Location getLocation() {
        return mLocation;
    }

    public ExifInterface getExif() {
        return mExif;
    }

    public boolean inBurstMode() {
        return mInBurstMode;
    }

    public boolean shouldResizeTo16x9() {
        return mShouldResizeTo16x9;
    }

    public static class Build {
        private byte[] mJpegData;
        private Location mLocation;
        private ExifInterface mExif;
        private boolean mInBurstMode;
        private boolean mShouldResizeTo16x9;

        public Build setJpegData(@NonNull byte[] data) {
            this.mJpegData = data;
            return this;
        }

        public Build setBitmap(@NonNull Bitmap bitmap) {
            this.mJpegData = conversion(bitmap);
            return this;
        }

        public Build setLocation(@NonNull Location location) {
            this.mLocation = location;
            return this;
        }

        public Build setExif(@NonNull ExifInterface exif) {
            this.mExif = exif;
            return this;
        }

        public Build setBurstModeState(boolean state) {
            this.mInBurstMode = state;
            return this;
        }

        public Build setResizeState(boolean state) {
            this.mShouldResizeTo16x9 = state;
            return this;
        }

        private byte[] conversion(Bitmap bitmap) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            bitmap.recycle();
            return os.toByteArray();
        }

        public FlowablePhotoBean build() {
            if (mJpegData == null) {
                throw new NullPointerException("mJpegData is NULL");
            }
            return new FlowablePhotoBean(mJpegData, mExif, mLocation, mInBurstMode, mShouldResizeTo16x9);
        }
    }
}
