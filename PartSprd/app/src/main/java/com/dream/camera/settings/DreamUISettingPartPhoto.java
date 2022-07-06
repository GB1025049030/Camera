
package com.dream.camera.settings;

import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;

import com.android.camera.settings.Keys;
import com.android.camera.settings.PictureSizeLoader.PictureSizes;
import com.android.camera.settings.SettingsScopeNamespaces;
import com.android.camera.util.CameraUtil;
import com.dream.camera.settings.DataModuleBasic.DataStorageStruct;
import android.preference.Preference;
import com.android.camera2.R;
import com.android.camera.debug.Log;
import com.freeme.camera.common.custom.CameraCustomManager;
import com.freeme.camera.settings.FreemeKeys;
import com.freeme.utils.FreemeCameraUtil;

public class DreamUISettingPartPhoto extends DreamUISettingPartBasic {

    public DreamUISettingPartPhoto(Context context) {
        super(context, null);
    }

    public DreamUISettingPartPhoto(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public DreamUISettingPartPhoto(Context context, AttributeSet attrs,
            int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public DreamUISettingPartPhoto(Context context, AttributeSet attrs,
            int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private static final String TAG = "DreamUISettingPartPhoto";

    @Override
    public void changContent() {
        mDataModule = DataModuleManager.getInstance(getContext())
                .getCurrentDataModule();
        super.changContent();
    }

    /*SPRD: fix bug 606536 not add ui change listener when back from secure camera @*/
    @Override
    public void addListener() {
        super.addListener();
    }
    /* @ */

    @Override
    protected void updatePreItemsAccordingProperties() {

        // update visibility of picturesize
        updateVisibilityPictureSizes();

        // update visibility of EIOS
        updateVisibilityEOIS();

       // update visibility of HighISO
        updateVisibilityHighISO();

        // update visibility of mirror
        updateVisibilityMirror();

        updateVisibilityTouchPhotograph();

        updateVisibility3DQuality();

        updateVisibilityAIDetect();
        updateVisibilitySensorSelfShot();
        updateVisibilityNormalHdr();
        updateVisibilityAntiFlicker();
        updateVisibilityAiSceneDetect();
	    updateVisibilityHDR();
        updateVisibilityAutoTracking();
        updateVisibilityFaceAttributeDetect();
        updateVisibilitySmile();

        updateVisibilityTimeWaterMark();
        updateVisibilityLocationWaterMark();
        updateVisibilityBrandWaterMark();
    }

    private void updateVisibilityAutoTracking() {
        if (!CameraUtil.isAutoChasingSupport()) {
            recursiveDelete(this, findPreference(Keys.KEY_AUTO_TRACKING));
        }
    }

    private void updateVisibilityHDR() {
        if(!CameraUtil.isIsMotionPhotoEnabled() ){
            recursiveDelete(this, findPreference(Keys.KEY_CAMERA_HDR));
            return;
        }

        if((mDataModule.getDataSetting().mMode == SettingsScopeNamespaces.REFOCUS
                || mDataModule.getDataSetting().mMode == SettingsScopeNamespaces.FRONT_BLUR)
                &&!CameraUtil.isHdrBlurSupported()){
            recursiveDelete(this, findPreference(Keys.KEY_CAMERA_HDR));
        }
    }

    //Sprd:fix bug922759 @{
    private void updateVisibilityAntiFlicker() {
        if ((isFrontCamera() && CameraUtil.isFrontYUVSensor())
                || (!isFrontCamera() && CameraUtil.isBackYUVSensor())) {
            recursiveDelete(this, findPreference(Keys.KEY_CAMER_ANTIBANDING));
        }
    }
    // @}

    private void updateVisibility3DQuality() {
        if (!CameraUtil.isTDPhotoEnable()) {
            recursiveDelete(this, findPreference(Keys.KEY_PICTURE_SIZE_FRONT_3D));
        }
    }

    private void updateVisibilityTouchPhotograph() {
        if(!CameraUtil.isTouchPhotoEnable()){
            recursiveDelete(this, findPreference(Keys.KEY_CAMERA_TOUCHING_PHOTOGRAPH));
        }
    }

    private void updateVisibilityHighISO() {
        if(!CameraUtil.isHighISOEnable()){
            recursiveDelete(this, findPreference(Keys.KEY_HIGH_ISO));
        }
    }

    private void updateVisibilitySensorSelfShot() {
        if(!CameraUtil.isSensorSelfShotEnable()){
            recursiveDelete(this, findPreference(Keys.KEY_CAMERA_SENSOR_SELF_SHOT));
        }
    }

    private void updateVisibilityAIDetect() {
        if (!CameraUtil.isAIDetectEnabled()) {
            recursiveDelete(this, findPreference(Keys.KEY_CAMERA_FACE_DATECT));
        }
    }

    private void updateVisibilityPictureSizes() {
        PictureSizes mPictureSizes = ((DataModuleInterfacePV) mDataModule)
                .getPictureSizes();
        ListPreference backPreference = (ListPreference) findPreference(Keys.KEY_PICTURE_SIZE_BACK);
        ListPreference frontPreference = (ListPreference) findPreference(Keys.KEY_PICTURE_SIZE_FRONT);
        if(mPictureSizes != null){
            if (mPictureSizes.backCameraSizes != null && mPictureSizes.backCameraSizes.isEmpty()) {
                recursiveDelete(this, backPreference);
            }
            if (mPictureSizes.frontCameraSizes != null && mPictureSizes.frontCameraSizes.isEmpty()) {
                recursiveDelete(this, frontPreference);
            }
        }
        if (CameraCustomManager.I.isOnlyDisplayPhotoProportion()){
            if (backPreference != null){
                backPreference.setTitle(R.string.dream_setting_back_proportion_photo);
                backPreference.setDialogTitle(R.string.dream_setting_back_proportion_photo);
            }
            if (frontPreference != null){
                frontPreference.setTitle(R.string.dream_setting_front_proportion_photo);
                frontPreference.setDialogTitle(R.string.dream_setting_front_proportion_photo);
            }
        }
    }

    private void updateVisibilityEOIS() {
        if (!CameraUtil.isEOISDcBackEnabled()) {
            recursiveDelete(this, findPreference(Keys.KEY_EOIS_DC_BACK));
        }
        if (!CameraUtil.isEOISDcFrontEnabled()) {
            recursiveDelete(this, findPreference(Keys.KEY_EOIS_DC_FRONT));
        }
    }

    /* SPRD: Fix bug 615081 that update mirror visibility according to property setting @{ */
    private void updateVisibilityMirror() {
        if (!CameraUtil.isFrontCameraMirrorEnable()) {
            recursiveDelete(this, findPreference(Keys.KEY_FRONT_CAMERA_MIRROR));
        }
    }
    /* @} */

    //SPRD : Add for bug 657472 Save normal hdr picture
    private void updateVisibilityNormalHdr() {
       if (!CameraUtil.isNormalHdrEnabled()) {
           recursiveDelete(this, findPreference(Keys.KEY_CAMERA_HDR_NORMAL_PIC));
       }
    }

    //SPRD: Add for bug 948896 Add ai scene detect switcher
    private void updateVisibilityAiSceneDetect() {
        if (!CameraUtil.isAiSceneDetectSupportable()) {
            recursiveDelete(this, findPreference(Keys.KEY_CAMERA_AI_SCENE_DATECT));
        } else {
            if(mDataModule.getDataSetting().mIsFront){
                Preference pref = findPreference(Keys.KEY_CAMERA_AI_SCENE_DATECT);
                if(pref != null){
                    pref.setTitle(mDataModule.getContext().getResources().getString(R.string.pref_camera_ai_scene_title_for_frontcamera));
                    pref.setSummary(mDataModule.getContext().getString(R.string.pref_camera_ai_scene_summy_for_frontcamera));
                }
            }
        }
     }
    private void updateVisibilityFaceAttributeDetect() {
        if (!DataModuleManager.getInstance(getContext()).getDataModulePhoto().getSupportedAttributesEnable()) {
            recursiveDelete(this, findPreference(Keys.KEY_AI_DETECT_FACE_ATTRIBUTES));
        }
    }
    private void updateVisibilitySmile() {
        if (!DataModuleManager.getInstance(getContext()).getDataModulePhoto().getSmileEnable()) {
            recursiveDelete(this, findPreference(Keys.KEY_AI_DETECT_SMILE));
        }
    }

    private void updateVisibilityTimeWaterMark() {
        if (!FreemeCameraUtil.isTimeWaterMarkEnable()) {
            recursiveDelete(this, findPreference(FreemeKeys.KEY_TIME_WATER_MARK));
        }
    }

    private void updateVisibilityLocationWaterMark() {
        if (!FreemeCameraUtil.isLocationWaterMarkEnable()) {
            recursiveDelete(this, findPreference(FreemeKeys.KEY_LOCATION_WATER_MARK));
        }
    }

    private void updateVisibilityBrandWaterMark() {
        if (!FreemeCameraUtil.isBrandWaterMarkEnable()) {
            recursiveDelete(this, findPreference(FreemeKeys.KEY_BRAND_WATER_MARK));
        }
    }
}
