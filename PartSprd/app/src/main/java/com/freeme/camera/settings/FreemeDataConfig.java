package com.freeme.camera.settings;

import com.android.camera.settings.SettingsScopeNamespaces;

public interface FreemeDataConfig {
    /**
     * child mode of photo
     */
    public interface PhotoModeType {
        public static final int PHOTO_MODE_BACK_SLR = FreemeSettingsScopeNamespaces.SLR_PHOTO;
        public static final int PHOTO_MODE_BACK_IKO = FreemeSettingsScopeNamespaces.IKO_PHOTO;
        int PHOTO_MODE_BACK_DEPTH_BLUR = FreemeSettingsScopeNamespaces.DEPTH_BLUR_PHOTO;
        int PHOTO_MODE_FRONT_DEPTH_BLUR = FreemeSettingsScopeNamespaces.DEPTH_BLUR_PHOTO;
        int PHOTO_MODE_BACK_EFFECT = FreemeSettingsScopeNamespaces.EFFECT_PHOTO;
        int PHOTO_MODE_FRONT_EFFECT = FreemeSettingsScopeNamespaces.EFFECT_PHOTO;
        int PHOTO_MODE_BACK_NIGHT = FreemeSettingsScopeNamespaces.NIGHT_PHOTO;
        int PHOTO_MODE_FRONT_NIGHT = FreemeSettingsScopeNamespaces.NIGHT_PHOTO;
    }

    /**
     * child mode of video
     */
    public interface VideoModeType {
//        public static final int VIDEO_MODE_BACK_AUTO = SettingsScopeNamespaces.AUTO_VIDEO;
        public static final int VIDEO_MODE_BACK_EFFECT_VIDEO = FreemeSettingsScopeNamespaces.EFFECT_VIDEO;
        public static final int VIDEO_MODE_FRONT_EFFECT_VIDEO = FreemeSettingsScopeNamespaces.EFFECT_VIDEO;
        public static final int VIDEO_MODE_BACK_SLOW_VIDEO = FreemeSettingsScopeNamespaces.SLOW_VIDEO;
        public static final int VIDEO_MODE_FRONT_SLOW_VIDEO= FreemeSettingsScopeNamespaces.SLOW_VIDEO;
    }
}
