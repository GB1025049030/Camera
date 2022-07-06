package com.freeme.camera.privacy;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.freeme.app.FreemePrivacyActivity;

public class FreemeCameraPrivacyActivity extends FreemePrivacyActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window win = getWindow();
        WindowManager.LayoutParams params = win.getAttributes();
        params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS;
        params.flags |= WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
        win.setAttributes(params);
    }
}