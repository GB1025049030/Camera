package com.freeme.camera.common.custom;

import android.app.Application;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.Log;

import com.android.camera2.R;

import java.util.HashMap;
import java.util.Map;

public enum CameraCustomManager {
    I;

    private static final String TAG = "CameraCustomManager";
    private static final Map<String, String> sParamPictureSizeValues = new HashMap<>(22);
    private Resources mResources;
    private String mPackageName;

    public void init(Application application) {
        if (application != null) {
            this.mResources = application.getResources();
            this.mPackageName = application.getPackageName();
            createPictureSizeList();
        }
    }

    //*/ Function
    public boolean isSupportFrontZoomPanel() {
        return isSupport("CAMERA_FRONT_ZOOM_PANEL", false, R.bool.camera_front_zoom_panel);
    }

    public boolean isSupportBackZoomPanel() {
        return isSupport("CAMERA_BACK_ZOOM_PANEL", false, R.bool.camera_back_zoom_panel);
    }

    public boolean isSupportGradienter() {
        return isSupport("CAMERA_GRADIENTER_ENABLE", false, R.bool.camera_gradienter_enable);
    }

    public boolean isSupportTimeWaterMark() {
        return isSupport("CAMERA_TIME_WATER_MARK_ENABLE", false, R.bool.camera_time_water_mark_enable);
    }

    public boolean isSupportLocationWaterMark() {
        return isSupport("CAMERA_Location_WATER_MARK_ENABLE", false, R.bool.camera_location_water_mark_enable);
    }

    public boolean isSupportBrandWaterMark() {
        return isSupport("CAMERA_BRAND_WATER_MARK_ENABLE", false, R.bool.camera_brand_water_mark_enable);
    }

    public boolean isSupportSprdBackBlur() {
        return isSupport("CAMERA_SPRD_BACK_BLUR_ENABLE", false, R.bool.camera_sprd_back_blur_enable);
    }

    public boolean isSupportFrontFocusArea() {
        return isSupport("CAMERA_FRONT_FOCUS_AREA_ENABLE", false, R.bool.camera_front_focus_area_enable);
    }

    public boolean isOnlyDisplayPhotoProportion(){
        return CameraCustomXmlParser.isSupport("CAMERA_DISPLAY_PHOTO_PROPORTION_ONLY", false);
    }

    public boolean isShowRefocusCoveredTips(){
        return CameraCustomXmlParser.isSupport("CAMERA_SHOW_REFOCUS_COVERED_TIPS", true);
    }

    public boolean isCTATestVersion(){
        return CameraCustomXmlParser.isSupport("CAMERA_CTA_TEST_VERSION", false);
    }
    //*/

    //*/ Mode
    public boolean isSupportManualMode() {
        return isSupport("CAMERA_MANUAL_PHOTO_ENABLE", true, R.bool.camera_manual_photo_enable);
    }

    public boolean isSupportQrCodeMode() {
        return isSupport("CAMERA_QRCODE_PHOTO_ENABLE", true, R.bool.camera_qrcode_photo_enable);
    }

    public boolean isSupportContinueMode() {
        return isSupport("CAMERA_CONTINUE_PHOTO_ENABLE", true, R.bool.camera_continue_photo_enable);
    }

    public boolean isSupportFilterMode() {
        return isSupport("CAMERA_FILTER_PHOTO_ENABLE", true, R.bool.camera_filter_photo_enable);
    }

    public boolean isSupportIntervalMode() {
        return isSupport("CAMERA_INTERVAL_PHOTO_ENABLE", true, R.bool.camera_interval_photo_enable);
    }

    public boolean isSupportSlrMode() {
        return isSupport("CAMERA_SLR_PHOTO_ENABLE", false, R.bool.camera_slr_photo_enable);
    }

    public boolean isSupportDepthBlurMode() {
        return isSupport("CAMERA_DEPTH_BLUR_PHOTO_ENABLE", false, R.bool.camera_depth_blur_photo_enable);
    }

    public boolean isSupportIkoMode() {
        return isSupport("CAMERA_IKO_PHOTO_ENABLE", true, R.bool.camera_iko_photo_enable);
    }

    public boolean isSupportEffectVideoMode() {
        return isSupport("CAMERA_DBLEXP_PHOTO_ENABLE", false, R.bool.camera_dblexp_photo_enable);
    }

    public boolean isSupportSlowVideo() {
        return isSupport("CAMERA_SLOW_VIDEO_ENABLE", false, R.bool.camera_slow_video_enable);
    }

    public boolean isSupportHighResolutionMode(){
        return CameraCustomXmlParser.isSupport("CAMERA_HIGH_RESOLUTION_PHOTO_ENABLE", true);
    }

    public boolean isSupportMoreMode(){
        return CameraCustomXmlParser.isSupport("CAMERA_MORE_MODE_ENABLE", true);
    }

    public boolean isSupportPanoramaMode(){
        return CameraCustomXmlParser.isSupport("CAMERA_PANORAMA_PHOTO_ENABLE", true);
    }

    public boolean isSupportMacroPhotoMode(){
        return CameraCustomXmlParser.isSupport("CAMERA_MACRO_PHOTO_ENABLE", true);
    }

    public boolean isSupportFlashOnMacroMode(){
        return CameraCustomXmlParser.isSupport("CAMERA_MACRO_MODE_FLASH_ENABLE", true);
    }

    public boolean isSupportQrCodeModeScanPictureFromOnlyGallery() {
        return CameraCustomXmlParser.isSupport("CAMERA_QRCODE_SCAN_PICTURE_FROM_ONLY_GALLERY", true);
    }

    public String getCustomSlideModeList() {
        return CameraCustomXmlParser.getString("MODE_SLIDE_LIST");
    }
    //*/

    //*/ Default Value
    public String getPictureSizeSelectorDegressiveRatio() {
        return getStringValue("PICTURESIZE_SELECTOR_DEGRESSIVE_RATIO", "",
                R.string.picturesize_selector_degreesive_ratio);
    }

    public String getIkoUseSpecifiedBrowserPkg() {
        return getStringValue("IKO_USE_SPECIFIED_BROWSER_PKG", "com.feimi.browser",
                R.string.camera_iko_use_specified_browser_pkg);
    }

    public String getSkinSmoothDefaultValueBack(String def){
        return getStringValueFromFreemeXml("SKIN_SMOOTH_DEFAULT_VALUE_BACK", def);
    }

    public String getSkinSmoothDefaultValueFront(String def){
        return getStringValueFromFreemeXml("SKIN_SMOOTH_DEFAULT_VALUE_FRONT", def);
    }

    public String getRemoveBlemishDefaultValueBack(String def){
        return getStringValueFromFreemeXml("REMOVE_BLEMISH_DEFAULT_VALUE_BACK", def);
    }

    public String getRemoveBlemishDefaultValueFront(String def){
        return getStringValueFromFreemeXml("REMOVE_BLEMISH_DEFAULT_VALUE_FRONT", def);
    }

    public String getSkinBrightDefaultValueBack(String def){
        return getStringValueFromFreemeXml("SKIN_BRIGHT_DEFAULT_VALUE_BACK", def);
    }

    public String getSkinBrightDefaultValueFront(String def){
        return getStringValueFromFreemeXml("SKIN_BRIGHT_DEFAULT_VALUE_FRONT", def);
    }

    public String getSkinColorDefaultValueBack(String def){
        return getStringValueFromFreemeXml("SKIN_COLOR_DEFAULT_VALUE_BACK", def);
    }

    public String getSkinColorDefaultValueFront(String def){
        return getStringValueFromFreemeXml("SKIN_COLOR_DEFAULT_VALUE_FRONT", def);
    }

    public String getSkinColorDefaultTypeBack(String def){
        return getStringValueFromFreemeXml("SKIN_COLOR_DEFAULT_TYPE_BACK", def);
    }

    public String getSkinColorDefaultTypeFront(String def){
        return getStringValueFromFreemeXml("SKIN_COLOR_DEFAULT_TYPE_FRONT", def);
    }

    public String getEnlargeEyesDefaultValueBack(String def){
        return getStringValueFromFreemeXml("ENLARGE_EYES_DEFAULT_VALUE_BACK", def);
    }

    public String getEnlargeEyesDefaultValueFront(String def){
        return getStringValueFromFreemeXml("ENLARGE_EYES_DEFAULT_VALUE_FRONT", def);
    }

    public String getSlimFaceDefaultValueBack(String def){
        return getStringValueFromFreemeXml("SLIM_FACE_DEFAULT_VALUE_BACK", def);
    }

    public String getSlimFaceDefaultValueFront(String def){
        return getStringValueFromFreemeXml("SLIM_FACE_DEFAULT_VALUE_FRONT", def);
    }

    public String getLipsColorDefaultValueBack(String def){
        return getStringValueFromFreemeXml("LIPS_COLOR_DEFAULT_VALUE_BACK", def);
    }

    public String getLipsColorDefaultValueFront(String def){
        return getStringValueFromFreemeXml("LIPS_COLOR_DEFAULT_VALUE_FRONT", def);
    }

    public String getLipsColorDefaultTypeBack(String def){
        return getStringValueFromFreemeXml("LIPS_COLOR_DEFAULT_TYPE_BACK", def);
    }

    public String getLipsColorDefaultTypeFront(String def){
        return getStringValueFromFreemeXml("LIPS_COLOR_DEFAULT_TYPE_FRONT", def);
    }

    public String getCameraCustomBrightness(){
        return getStringValueFromFreemeXml("CAMERA_CUSTOM_BRIGHTNESS_VALUE", "");
    }

    //*/ Picture Size
    public boolean isSupportPictureSizeCustom() {
        return isSupport("CAMERA_PICTURE_SIZE_CUSTOM_ENABLE", false,
                R.bool.camera_picture_size_custom_enable);
    }

    public String getPictureSize(String sizeKey) {
        String entry = CameraCustomXmlParser.getPictureSize(sizeKey);
        if (TextUtils.isEmpty(entry)) {
            if (sParamPictureSizeValues.containsKey(sizeKey)) {
                entry = sParamPictureSizeValues.get(sizeKey);
            }
        }
        return entry;
    }

    private void createPictureSizeList() {
        if (isSupportPictureSizeCustom()) {
            createPictureSizeList(true, R.array.picture_size_back_entries);
            createPictureSizeList(false, R.array.picture_size_front_entries);
        }
    }

    private void createPictureSizeList(boolean isBackCamera, int resId) {
        TypedArray pictureSizeListRes = mResources.obtainTypedArray(resId);
        getPictureSizeInfo(pictureSizeListRes,
                CameraCustomInterpol.getPictureSize(isBackCamera),
                CameraCustomInterpol.getLength(isBackCamera), isBackCamera);
        pictureSizeListRes.recycle();
    }

    private void getPictureSizeInfo(TypedArray pictureSizeRes, int size, int length, boolean isBackCamera) {
        if (pictureSizeRes == null || size <= 0 || length <= 0) {
            return;
        }

        int itemLength = (length << 1) + 1;
        if (pictureSizeRes.length() % itemLength != 0) {
            Log.d(TAG, "getPictureSizeInfo: Size error! Please check your picture size array!");
            return;
        }

        int count = pictureSizeRes.length() / itemLength;
        for (int i = 0; i < count; i++) {
            int startPos = itemLength * i;
            int pictureSize = pictureSizeRes.getInteger(startPos, -1);
            if (pictureSize != size) {
                break;
            }
            String entry;
            for (int j = 0; j < length << 1; j++) {
                entry = pictureSizeRes.getString(startPos + j + 1);
                if (!TextUtils.isEmpty(entry))
                    sParamPictureSizeValues.put(String.valueOf((isBackCamera ? 0 : 8) | j), entry);
            }
        }
    }
    //*/

    private boolean isSupport(String key, boolean def, int resId) {
        if (mResources.getBoolean(R.bool.use_custom_xml)) {
            return mResources.getBoolean(resId);
        } else {
            return CameraCustomXmlParser.isSupport(key, def);
        }
    }

    private String getStringValue(String key, String def, int resId) {
        String result = CameraCustomXmlParser.getString(key);
        if (TextUtils.isEmpty(result)) {
            result = mResources.getString(resId);
        }
        return result;
    }

    private String getStringValueFromFreemeXml(String key, String def){
        String result = CameraCustomXmlParser.getString(key);
        if (TextUtils.isEmpty(result)){
            return def;
        }else {
            return result;
        }
    }
}
