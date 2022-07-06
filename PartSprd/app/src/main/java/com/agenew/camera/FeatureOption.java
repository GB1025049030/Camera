package com.agenew.camera;

import com.android.camera.settings.Keys;

import android.os.SystemProperties;

public class FeatureOption {
    public static final Boolean AGENEW_CAMERA_BV = SystemProperties.getBoolean("ro.agenew.camera_bv", false);
    public static final Boolean AGENEW_DEFAULT_OPEN_FRONT_FLASH = SystemProperties.getBoolean("ro.agenew.default_open_front_flash", false);//add by hanqianqian for AGENEW_DEFAULT_OPEN_FRONT_FLASH
}