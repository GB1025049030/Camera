package com.freeme.utils;

import android.content.Context;
import android.provider.Settings;

import com.android.camera2.BuildConfig;
import com.freeme.camera.common.custom.CameraCustomManager;

public class FreemeCameraUtil {
    public static boolean isGradienterEnable() {
        return CameraCustomManager.I.isSupportGradienter();
    }

    public static boolean isSlrPhotoEnabled() {
        return CameraCustomManager.I.isSupportSlrMode();
    }

    public static boolean isDepthBlurPhotoEnabled() {
        return CameraCustomManager.I.isSupportDepthBlurMode();
    }

    public static boolean isByteDanceEnabled(){
        return true;
    }

    public static boolean isIKOPhotoEnabled() {
        return CameraCustomManager.I.isSupportIkoMode();
    }

    public static boolean isEffectVideoEnabled() {
        return CameraCustomManager.I.isSupportEffectVideoMode();
    }


    public static boolean isPictureSizeCustomEnable() {
        return CameraCustomManager.I.isSupportPictureSizeCustom();
    }

    public static boolean isTimeWaterMarkEnable() {
        return CameraCustomManager.I.isSupportTimeWaterMark();
    }

    public static boolean isLocationWaterMarkEnable() {
        return CameraCustomManager.I.isSupportLocationWaterMark();
    }

    public static boolean isBrandWaterMarkEnable() {
        return CameraCustomManager.I.isSupportBrandWaterMark();
    }

    public static boolean isFrontFocusAreaEnable() {
        return CameraCustomManager.I.isSupportFrontFocusArea();
    }

    public static boolean isSprdBackBlurEnable() {
        return CameraCustomManager.I.isSupportSprdBackBlur();
    }

    public static boolean isSlowVideoEnable() {
        return CameraCustomManager.I.isSupportSlowVideo();
    }

    public static boolean isSupportHighResolutionMode(){
        return CameraCustomManager.I.isSupportHighResolutionMode();
    }

    public static boolean isQrCodeModeScanPictureFromOnlyGalleryEnable() {
        return CameraCustomManager.I.isSupportQrCodeModeScanPictureFromOnlyGallery();
    }

    public static boolean isMacroPhotoEnable() {
        return CameraCustomManager.I.isSupportMacroPhotoMode();
    }

    public static int getBrightnessMode(Context context){
        int brightnessMode = -1;
        try {
            brightnessMode = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return brightnessMode;
    }

    public static int getBrightnessValue(Context context){
        int brightnessValue = -1;
        try {
            brightnessValue = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return brightnessValue;
    }

    public static void setBrightnessMode(Context context, int mode){
        Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, mode);
    }

    public static void setBrightnessValue(Context context, int value) {
        Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, value);
    }
}
