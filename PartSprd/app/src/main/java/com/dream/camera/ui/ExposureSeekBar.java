package com.dream.camera.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SeekBar;

import com.android.camera.CameraActivity;
import com.android.camera.CameraModule;
import com.android.camera.PhotoModule;
import com.android.camera.debug.Log;

/**
 * Created by SPREADTRUM\matchbox.chang on 19-3-8.
 * We will change SeekBar performance base on camera state ,so define this class extends SeekBar;
 */

public class ExposureSeekBar extends SeekBar {
    private static final Log.Tag TAG = new Log.Tag("ExposureSeekBar");
    private Context mContext;

    public ExposureSeekBar(Context context) {
        super(context);
        mContext = context;
    }

    public ExposureSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public ExposureSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(isCameraIdle()) {
            return super.onTouchEvent(event);
        } else {
            return true;
        }
    }

    public boolean isCameraIdle(){
        CameraModule mCurrentModule = ((CameraActivity) mContext).getCurrentModule();
        if(mCurrentModule instanceof PhotoModule){
            return ((PhotoModule) mCurrentModule).getCameraState() == PhotoModule.IDLE;
        }
        return false;
    }
}
