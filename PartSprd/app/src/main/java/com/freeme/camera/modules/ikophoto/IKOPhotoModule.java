package com.freeme.camera.modules.ikophoto;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;

import com.android.camera.CameraActivity;
import com.android.camera.MultiToggleImageButton;
import com.android.camera.PhotoModule;
import com.android.camera.PhotoUI;
import com.android.camera.app.AppController;
import com.android.camera.debug.Log;
import com.freeme.camera.common.custom.CameraCustomManager;
import com.android.camera.settings.Keys;
import com.android.camera.util.CameraUtil;
import com.android.camera.util.Size;
import com.android.camera2.R;
import com.android.ex.camera2.portability.CameraAgent;
import com.android.ex.camera2.portability.CameraCapabilities;
import com.android.library.baidu.aip.ui.BaikeInfoActivity;
import com.android.util.libyuv.YUVManager;
import com.dream.camera.ButtonManagerDream;
import com.freeme.camera.common.network.NetworkUtils;
import com.freeme.camera.common.rxjava.DisposableManager;
import com.freeme.camera.privacy.FreemePrivacyManager;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class IKOPhotoModule extends PhotoModule {
    private static final Log.Tag TAG = new Log.Tag("IKOPhotoModule");

    private SensorManagerClient mSensorManagerClient;

    private boolean mAutoFocusMovingState = true;
    private boolean mIsSilent = true;
    private boolean mIsStartingActivity = true;

    private int mPreviewWidth;
    private int mPreviewHeight;
    private int mDstPreviewWidth;
    private int mDstPreviewHeight;
    private byte[] mRGBAArray;
    private byte[] mYUVData;
    private int mDstHeight;

    public IKOPhotoModule(AppController app) {
        super(app);
    }

    @Override
    public PhotoUI createUI(CameraActivity activity) {
        return new IKOPhotoUI(activity, this, activity.getModuleLayoutRoot());
    }

    @Override
    public void resume() {
        super.resume();
        mSensorManagerClient = new SensorManagerClient(mActivity.getApplicationContext());
        mSensorManagerClient.resume(isSilent -> mIsSilent = isSilent);
        mDstHeight = mActivity.getResources().getDimensionPixelSize(R.dimen.iko_original_image_width);

        mPreviewWidth = 0;
        mPreviewHeight = 0;
        mDstPreviewWidth = 0;
        mDstPreviewHeight = 0;

        Disposable timerDisposable = Observable.timer(1500, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> mIsStartingActivity = false);
        DisposableManager.I.add(timerDisposable);
        if (!mActivity.isPrivacyEnable()) {
            mActivity.getCameraAppUI().setShutterButtonEnabled(false);
            mActivity.requestPrivacyEnable(state -> {
                boolean enable = state == FreemePrivacyManager.PRIVACY_RESULT_OK;
                updateIKOTopTips(enable);
                mActivity.getCameraAppUI().setShutterButtonEnabled(enable);
            });
        }
        updateIKOTopTips(mActivity.isPrivacyEnable());
    }

    @Override
    public void pause() {
        super.pause();
        Optional.ofNullable(mSensorManagerClient).ifPresent(SensorManagerClient::pause);
        mActivity.getCameraAppUI().setCenterScanText(mActivity.getResources().getString(R.string.object_tip));
        YUVManager.I.destroy();
        DisposableManager.I.clear();
        mActivity.getCameraAppUI().setShutterButtonEnabled(true);
    }

    @Override
    public void destroy() {
        super.destroy();
        mActivity.getCameraAppUI().setCenterScanText(null);
    }

    @Override
    public void onShutterButtonClick() {
    }

    @Override
    public boolean isisUsePreviewFrame() {
        return true;
    }

    @Override
    public void onPreviewDataUpdate(byte[] data, CameraAgent.CameraProxy camera) {
        if (NetworkUtils.isConnect() && mIsSilent && !mAutoFocusMovingState) {
            if (!mIsStartingActivity) {
                mIsStartingActivity = true;
                mYUVData = new byte[data.length];
                System.arraycopy(data, 0, mYUVData, 0, data.length);
                openInfoActivityWithDelay(mYUVData, ((IKOPhotoUI) mUI).getPreviewWidth(), ((IKOPhotoUI) mUI).getPreviewHeight());
            }
        }
    }

    private void updateIKOTopTips(boolean enablePrivacy) {
        mActivity.getCameraAppUI().setCenterScanText(mActivity.getString(
                enablePrivacy ? R.string.object_tip : R.string.camera_iko_top_privacy_disable));
    }

    private void openInfoActivityWithDelay(byte[] data, int width, int height) {
        Observable<Long> timer = Observable.timer(500, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        Observable<byte[]> compress = Observable
                .just(compressNV21ToJPEG(data, width, height, mDstHeight * width / height, mDstHeight))
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());

        Disposable timerDisposable = timer.subscribe(aLong -> {
            Disposable compressDisposable = compress.subscribe(bytes -> {
                mActivity.startActivity(getTargetIntent(bytes));
                mActivity.overridePendingTransition(R.anim.zoom_in, R.anim.camera_zoom);
            }, throwable -> throwable.printStackTrace());
            DisposableManager.I.add(compressDisposable);
        });
        DisposableManager.I.add(timerDisposable);
    }

    private Intent getTargetIntent(byte[] bytes) {
        Intent intent = new Intent();
        intent.setClass(mActivity, BaikeInfoActivity.class);
        Bundle bundle = new Bundle();
        bundle.putByteArray(BaikeInfoActivity.RGBA_DATA_KEY, bytes);
        bundle.putString(BaikeInfoActivity.SPECIFIED_BROWSER_PKG_KEY,
                CameraCustomManager.I.getIkoUseSpecifiedBrowserPkg());
        intent.putExtras(bundle);
        return intent;
    }

    @Override
    protected void updateParametersPictureSize() {
        if (mCameraDevice == null) return;
        Size optimalSize = IKOCameraUtil.I.getBestPreviewSize(mActivity, mCameraCapabilities, IKOCameraUtil.PREVIEW_16_9_MODE);
        if (optimalSize.width() != 0 && optimalSize.height() != 0) {
            mCameraSettings.setPhotoSize(optimalSize.toPortabilitySize());
            mCameraSettings.setExifThumbnailSize(CameraUtil.getAdaptedThumbnailSize(optimalSize,
                    mAppController.getCameraProvider()).toPortabilitySize());
            mCameraSettings.setPreviewSize(optimalSize.toPortabilitySize());
            mUI.updatePreviewAspectRatio((float) optimalSize.width() / (float) optimalSize.height());
        }
    }

    @Override
    public void onAutoFocusMoving(boolean moving, CameraAgent.CameraProxy camera) {
        mAutoFocusMovingState = moving;
    }

    @Override
    public void onDreamSettingChangeListener(HashMap<String, String> keyList) {
        for (String key : keyList.keySet()) {
            switch (key) {
                case Keys.KEY_DREAM_FLASH_GIF_PHOTO_MODE:
                    updateTorchModeCameraFlash();
                    break;
            }
        }
        super.onDreamSettingChangeListener(keyList);
    }

    @Override
    public void updateBatteryLevel(int level) {
        super.updateBatteryLevel(level);

        if (mDataModuleCurrent == null
                || !mDataModuleCurrent.isEnableSettingConfig(Keys.KEY_DREAM_FLASH_GIF_PHOTO_MODE)) {
            return;
        }
        String BEFORE_LOW_BATTERY = "_before_low_battery";
        int batteryLevel = CameraUtil.getLowBatteryNoFlashLevel();
        String beforeMode = mDataModuleCurrent.getString(Keys.KEY_DREAM_FLASH_GIF_PHOTO_MODE + BEFORE_LOW_BATTERY);
        String currentMode = mDataModuleCurrent.getString(Keys.KEY_DREAM_FLASH_GIF_PHOTO_MODE);
        if (level <= batteryLevel) {
            if (TextUtils.isEmpty(beforeMode)) {
                mDataModuleCurrent.set(Keys.KEY_DREAM_FLASH_GIF_PHOTO_MODE + BEFORE_LOW_BATTERY, currentMode);
            }

            if (!"off".equals(currentMode) && mCameraSettings != null) {
                if (!isPhotoFocusing()) {
                    updateTorchModeCameraFlash();
                }
            }
            mAppController.getButtonManager().disableButton(ButtonManagerDream.BUTTON_GIF_PHOTO_FLASH_DREAM);
            if (isFirstShowToast || isBatteryAgainLowLevel) {
                if (isFirstShowToast == false) {
                    isBatteryAgainLowLevel = false;
                }
                isFirstShowToast = false;
                mActivity.runOnUiThread(() -> CameraUtil.toastHint(mActivity, R.string.battery_level_low));
            }
        } else {
            isBatteryAgainLowLevel = true;
            if (TextUtils.isEmpty(beforeMode)) {
                return;
            }
            mDataModuleCurrent.set(Keys.KEY_DREAM_FLASH_GIF_PHOTO_MODE + BEFORE_LOW_BATTERY, null);
            mAppController.getButtonManager().enableButton(ButtonManagerDream.BUTTON_GIF_PHOTO_FLASH_DREAM);
            boolean hdrState = mDataModuleCurrent.getBoolean(Keys.KEY_CAMERA_HDR);
            if (!hdrState && mCameraSettings != null) {
                updateTorchModeCameraFlash();
            }
        }
        if (mCameraDevice != null) {
            if (!isPhotoFocusing()) {
                mCameraDevice.applySettings(mCameraSettings);
            }
        }
    }

    @Override
    protected void onPreviewStarted() {
        super.onPreviewStarted();
        updateTorchModeCameraFlash();
    }

    @Override
    public boolean onBackPressed() {
        return mIsStartingActivity || super.onBackPressed();
    }

    private byte[] compressNV21ToJPEG(byte[] data, int width, int height, int dst_width, int dst_height) {
        long startTime = System.currentTimeMillis();
        if (mPreviewWidth != width || mPreviewHeight != height
                || mDstPreviewWidth != dst_width || mDstPreviewHeight != dst_height) {
            mPreviewWidth = width;
            mPreviewHeight = height;
            mDstPreviewWidth = dst_width;
            mDstPreviewHeight = dst_height;
            mRGBAArray = new byte[dst_width * dst_height * 4];
            YUVManager.I.update(width, height, dst_width, dst_height);
        }
        YUVManager.I.compressNV21ToRGBA(mRGBAArray, data, width, height, dst_width, dst_height, 90);

        Bitmap bitmap = Bitmap.createBitmap(dst_height, dst_width, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(mRGBAArray));

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, os);
        Log.d(TAG, "compressNV21ToJPEG cost: " + (System.currentTimeMillis() - startTime));
        return os.toByteArray();
    }

    public void updateTorchModeCameraFlash() {
        MultiToggleImageButton flash = ((IKOPhotoUI) mUI).getFlashButton();
        if (flash != null) {
            int state = flash.getState();
            if (state == 0 && !mActivity.getIsButteryLow()) {
                enableTorchMode(true);
            } else {
                enableTorchMode(false);
            }
        }
    }

    private boolean isCameraOpening() {
        return mCameraSettings == null || mCameraCapabilities == null;
    }

    private void enableTorchMode(boolean enable) {
        if (isCameraOpening()) {
            return;
        }
        if (mCameraSettings.getCurrentFlashMode() == null) {
            return;
        }

        CameraCapabilities.Stringifier stringifier = mCameraCapabilities
                .getStringifier();
        CameraCapabilities.FlashMode flashMode;

        if (enable) {
            flashMode = stringifier.flashModeFromString(mDataModuleCurrent
                    .getString(Keys.KEY_DREAM_FLASH_GIF_PHOTO_MODE));
        } else {
            flashMode = CameraCapabilities.FlashMode.OFF;
        }

        if (mCameraCapabilities.supports(flashMode)) {
            mCameraSettings.setFlashMode(flashMode);
        }

        if (mCameraDevice != null && !isPhotoFocusing()) {
            mCameraDevice.applySettings(mCameraSettings);
            mCameraSettings = mCameraDevice.getSettings();
        }
    }

    @Override
    public boolean isSupportGradienter() {
        return false;
    }
}
