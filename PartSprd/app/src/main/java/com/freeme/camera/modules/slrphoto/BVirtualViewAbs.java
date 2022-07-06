package com.freeme.camera.modules.slrphoto;

import android.annotation.Nullable;
import android.content.Context;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.android.camera.ui.PreviewStatusListener;
import com.android.camera.ui.Rotatable;


public class BVirtualViewAbs extends View implements Rotatable, GestureDetector.OnGestureListener,
        PreviewStatusListener.PreviewAreaChangedListener {

    public BVirtualViewAbs(Context context) {
        this(context, null);
    }

    public BVirtualViewAbs(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BVirtualViewAbs(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setOrientation(int orientation, boolean animation) {

    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    public void onPreviewAreaChanged(RectF previewArea) {

    }
}
