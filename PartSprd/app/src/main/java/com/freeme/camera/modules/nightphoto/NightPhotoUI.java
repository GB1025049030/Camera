package com.freeme.camera.modules.nightphoto;

import android.view.View;

import com.android.camera.CameraActivity;
import com.android.camera.PhotoController;
import com.android.camera.debug.Log;
import com.android.camera2.R;
import com.freeme.camera.modules.CompositePhotoUI;

public class NightPhotoUI extends CompositePhotoUI {
    private static final Log.Tag TAG = new Log.Tag("NightPhotoUI");

    public NightPhotoUI(CameraActivity activity, PhotoController controller, View parent) {
        super(activity, controller, parent);
    }

    @Override
    protected int getTopPanelConfigID() {
        return R.array.night_photo_top_panel;
    }
}
