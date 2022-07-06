package com.dream.camera.modules.qr;

import android.Manifest;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.Context;
import android.graphics.*;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.app.Service;

import com.android.camera.app.OrientationManager;
import com.android.util.libyuv.YUVManager;
import com.freeme.utils.FreemeCameraUtil;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.android.camera.app.AppController;
import com.android.camera.settings.Keys;
import com.android.camera.ui.RotateImageButton;
import com.android.camera2.R;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.common.HybridBinarizer;

import android.text.TextUtils;
import android.provider.MediaStore;
import android.database.Cursor;

import com.android.camera.util.ApiHelper;
import com.android.camera.util.CameraUtil;
import android.content.pm.PackageManager;
import com.android.camera.PermissionsActivity;
import java.nio.charset.Charset;
import com.android.camera.MultiToggleImageButton;
import com.android.camera.CameraActivity;
import android.os.Build;
import android.os.Looper;
import android.app.Activity;

import com.dream.camera.settings.DataModuleBasic.DreamSettingChangeListener;
import com.dream.camera.ucam.utils.BitmapUtils;
import com.dream.camera.util.DreamUtil;
import com.dream.camera.ButtonManagerDream;
import com.android.camera.util.GservicesHelper;
import com.android.camera.app.CameraAppUI;
import com.android.camera.util.Size;

import com.android.ex.camera2.portability.CameraAgent;
import com.android.ex.camera2.portability.CameraAgent.CameraProxy;
import com.android.ex.camera2.portability.CameraSettings;
import com.android.ex.camera2.portability.CameraCapabilities;

import java.lang.ref.WeakReference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.HashMap;
import java.util.Vector;
import com.android.camera.debug.Log;
import android.provider.DocumentsContract;
import android.net.Uri;
import android.view.WindowManager;
import android.widget.RelativeLayout;


public class QrCodePhotoModule extends ReuseModule implements Callback,
        DreamSettingChangeListener {

    private static final Log.Tag TAG = new Log.Tag("QrCodePhotoModule");
    private QrCaptureActivityHandler handler;
    private ViewfinderView viewfinderView;
    private boolean hasSurface;
    private Vector<BarcodeFormat> decodeFormats;
    private String characterSet;
    private static final float BEEP_VOLUME = 0.10f;
    private boolean vibrate;
    private boolean isUpdateFlash = false;
    private AppController mAppController;
    private boolean mPaused;
    private static final int REQUEST_CODE = 234;
    private String photo_path;
    private MultiToggleImageButton mFlashButton;
    private RotateImageButton mGalleryButton;
    private SurfaceView mSurfaceView;
    protected ReusePhotoUI mUI;
    private Boolean mHasCriticalPermissions;
    private Bitmap mFreezeBitmap = null;
    protected CameraSettings mCameraSettings;
    protected final Handler mHandler = new MainHandler(this);
    private int mDisplayOrientation;
    private boolean initialized;
    protected CameraCapabilities mCameraCapabilities;
    private static final Pattern COMMA_PATTERN = Pattern.compile(",");
    protected int mCameraPreviewWidth;
    protected int mCameraPreviewHeight;
    private SurfaceHolder mSurfaceHolder;
    private boolean isNotStartPrview = false;

    public QrCodePhotoModule(AppController app) {
        super(app);
    }

    @SuppressLint("ResourceType")
    @Override
    public void init(CameraActivity activity, boolean isSecureCamera,
            boolean isCaptureIntent) {
        super.init(activity, isSecureCamera, isCaptureIntent);
        CameraManager.init(mActivity.getApplication());
        mAppController = activity;
        viewfinderView = (ViewfinderView) mActivity
                .findViewById(R.id.viewfinder_view);

        mFlashButton = (MultiToggleImageButton) mActivity
                .findViewById(R.integer.gif_photo_flash_toggle_button_dream);
        mGalleryButton = (RotateImageButton) mActivity
                .findViewById(R.integer.qrcode_gallery_toggle_button);

        hasSurface = false;
    }

    @Override
    public int getMode() {
        return DreamUtil.PHOTO_MODE;
    }

    @Override
    public void makeModuleUI(ReuseController controller, View parent) {
        // TODO Waiting for merge of WideAngle feature, noted by spread
        mUI = createUI(controller, parent);
    }

    public QrCodePhotoUI createUI(ReuseController controller, View parent) {
        return new QrCodePhotoUI(mActivity, controller, parent);
    }

    public void scanPicture() {
        if (FreemeCameraUtil.isQrCodeModeScanPictureFromOnlyGalleryEnable()) {
            Intent innerIntent = new Intent(Intent.ACTION_PICK, null);
            innerIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
            startActivityForResult(innerIntent, REQUEST_CODE);
        } else {
            Intent innerIntent = new Intent();
            if (Build.VERSION.SDK_INT < 19) {
                innerIntent.setAction(Intent.ACTION_GET_CONTENT);
            } else {
                innerIntent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                if (CameraUtil.isLowRam()) {
                /*SPRD Bug 1166127 insert imageUri ("content://com.android.providers.media.documents/root/images_root")
                to start image tab of DocUI if product is low ram*/
                    Uri imageUri = DocumentsContract.buildRootUri("com.android.providers.media.documents","images_root");
                    innerIntent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, imageUri);
                }
            }
            innerIntent.setType("image/*");
            Intent wrapperIntent = Intent.createChooser(innerIntent,
                    "select qrcode picture");
            startActivityForResult(wrapperIntent, REQUEST_CODE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            isNotStartPrview = true;
            switch (requestCode) {
            case REQUEST_CODE:
                /*
                 * SPRD: Fix bug 589142, qrcode to select the
                 * image interface, the camera has been stopped running @{
                 */
                checkPermissions();
                if (!mHasCriticalPermissions) {
                    return;
                }
                if(data == null || data.getData() == null){
                    return;
                }
                /* @} */
                String[] proj = { MediaStore.Images.Media.DATA };

                // Gets the path to the selected image
                Cursor cursor = mActivity.getContentResolver().query(
                        data.getData(), proj, null, null, null);
                if(cursor == null){
                    return;
                }
                if (cursor.moveToFirst()) {
                    int column_index = cursor
                            .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    photo_path = cursor.getString(column_index);
                    if (photo_path == null) {
                        photo_path = Utils.getPath(
                                mActivity.getApplicationContext(),
                                data.getData());
                    }
                }
                cursor.close();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        isUpdateFlash = true;
                        //scanningImage(photo_path);
                        QrDecodeManager.I.decode(mActivity, new QrDecodeManager.IResult() {
                            @Override
                            public void onSuccess(Result result) {
                                handleDecode(result, null);
                            }

                            @Override
                            public void onFailed() {
                                notFoundQrException();
                            }
                        }, photo_path);
                    }
                }).start();
                break;
            }
        }
    }

    // Annlysis part of pictures
    protected Result scanningImage(String path) {
        Bitmap scanBitmap = null;
        MultiFormatReader multiFormatReader = new MultiFormatReader();
        Result result;
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        // DecodeHintType and EncodeHintType
        Hashtable<DecodeHintType, String> hints = new Hashtable<DecodeHintType, String>();
        BitmapFactory.Options options = new BitmapFactory.Options();
        // Get the original size
        options.inJustDecodeBounds = true;
        scanBitmap = BitmapFactory.decodeFile(path, options);
        // Get new size
        options.inJustDecodeBounds = false;

        int sampleSize = (int) (options.outHeight / (float) CACHE_BITMAP_WH);
        if (sampleSize <= 0)
            sampleSize = 1;
        options.inSampleSize = sampleSize;
        scanBitmap = BitmapFactory.decodeFile(path, options);
        if (options.outHeight < CACHE_BITMAP_WH)
            scanBitmap = scaleBitmap(scanBitmap,CACHE_BITMAP_WH,CACHE_BITMAP_WH);
        if (scanBitmap == null) {
            return null;
        }
        LuminanceSource source = new PlanarYUVLuminanceSource(
                rgb2YUV(scanBitmap), scanBitmap.getWidth(),
                scanBitmap.getHeight(), 0, 0, scanBitmap.getWidth(),
                scanBitmap.getHeight());
        BinaryBitmap binaryBitmap = new BinaryBitmap(
                new HybridBinarizer(source));

        try {
            result = multiFormatReader.decodeWithState(binaryBitmap);
            String content = result.getText();
            handleDecode(result, scanBitmap);
        } catch (NotFoundException e1) {
            // TODO Auto-generated catch block
            notFoundQrException(e1);
        } catch (ArrayIndexOutOfBoundsException e1) {
            notFoundQrException(e1);
        }
        return null;
    }

    private final static int CACHE_BITMAP_WH = 600;

    private Bitmap scaleBitmap(Bitmap origin, int newWidth, int newHeight) {
        if (origin == null) {
            return null;
        }
        int height = origin.getHeight();
        int width = origin.getWidth();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        float scale = Math.min(scaleHeight,scaleWidth);
        matrix.postScale(scale, scale);
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        if (!origin.isRecycled()) {
            origin.recycle();
        }
        return newBM;
    }

    private void notFoundQrException(Exception e1) {
        e1.printStackTrace();
        notFoundQrException();
    }

    private void notFoundQrException() {
        mActivity.getMainHandler().post(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putString("result", "defaulturi");
                intent.putExtras(bundle);
                intent.setClass(mActivity, QrScanResultActivity.class);
                startActivity(intent);
            }
        });
    }

    private String recode(String str) {
        String formart = "";
        try {
            boolean ISO = Charset.forName("ISO-8859-1").newEncoder()
                    .canEncode(str);
            if (ISO) {
                formart = new String(str.getBytes("ISO-8859-1"), "GB2312");
            } else {
                formart = str;
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return formart;
    }

    public byte[] rgb2YUV(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        int len = width * height;
        byte[] yuv = new byte[len * 3 / 2];
        int y, u, v;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int rgb = pixels[i * width + j] & 0x00FFFFFF;

                int r = rgb & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = (rgb >> 16) & 0xFF;

                y = ((66 * r + 129 * g + 25 * b + 128) >> 8) + 16;
                u = ((-38 * r - 74 * g + 112 * b + 128) >> 8) + 128;
                v = ((112 * r - 94 * g - 18 * b + 128) >> 8) + 128;

                y = y < 16 ? 16 : (y > 255 ? 255 : y);
                u = u < 0 ? 0 : (u > 255 ? 255 : u);
                v = v < 0 ? 0 : (v > 255 ? 255 : v);
                yuv[i * width + j] = (byte) y;
            }
        }
        return yuv;
    }

    @Override
    public void resume() {
        Log.i(TAG, "resume start!");
        mPaused = false;
        checkPermissions();
        if (!mHasCriticalPermissions) {
            return;
        }
        mUI.updateGalleryButton();
        //SPRD:fix bug 941057/958939
        mSurfaceView = (SurfaceView) mActivity.getModuleLayoutRoot()
                .findViewById(R.id.preview_view);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSurfaceView.setVisibility(View.GONE);
        mSurfaceView.setVisibility(View.VISIBLE);
        requestCameraOpen();

        decodeFormats = null;
        characterSet = null;
        vibrate = true;
        mDataModuleCurrent.addListener(this);
        //Sprd:Fix bug771726
        mActivity.setSurfaceVisibility(false);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mSurfaceView.getLayoutParams();
        viewfinderView.setLayoutParams(params);

        mActivity.getCameraAppUI().setCenterScanText(mActivity.getResources().getString(R.string.scan_text));
    }

    private int setTransformMatrix(SurfaceView surfaceView) {
        CameraAppUI cameraAppUI = mActivity.getCameraAppUI();
        if (cameraAppUI == null) return -1;

        final RectF rect = cameraAppUI.getPreviewArea();
        final int spw = mCameraSettings.getCurrentPreviewSize().width();
        final int sph = mCameraSettings.getCurrentPreviewSize().width();
        final int previewWidth = Math.round(Math.min(spw, sph));
        final int previewHeight = Math.round(Math.max(spw, sph));
        final float aspectRatio = previewHeight * 1f / previewWidth;
        if (aspectRatio == 0) return -1;

        float scaledTextureWidth, scaledTextureHeight;

        if (previewWidth > previewHeight) {
            scaledTextureWidth = Math.min(previewWidth, (int) (previewHeight * aspectRatio));
            scaledTextureHeight = Math.min(previewHeight, (int) (previewWidth / aspectRatio));
        } else {
            scaledTextureWidth = Math.min(previewWidth, (int) (previewHeight / aspectRatio));
            scaledTextureHeight = Math.min(previewHeight, (int) (previewWidth * aspectRatio));
        }

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) surfaceView.getLayoutParams();
        params.width = (int) scaledTextureWidth;
        params.height = (int) scaledTextureHeight;
        params.setMargins(0, (int) rect.top, 0, 0);
        surfaceView.setLayoutParams(params);
        return params.height;
    }

    private void onPreviewStarted() {
        mActivity.onPreviewStarted();
    }

    @Override
    public void pause() {
        Log.i(TAG, "pause");
        isNotStartPrview = false;
        mCameraAvailable = false;
        isFirstShowToast = true;
        isBatteryAgainLowLevel = false;
        mPaused = true;
        resumeTextureView();
        if (mDataModuleCurrent != null) {
            mDataModuleCurrent.removeListener(this);
        }

        if (mCameraDevice != null) {
            mCameraDevice.cancelAutoFocus();
        }
        removeAutoFocusMoveCallback();
        stopPreview();

        closeCamera();
        mActivity.setSurfaceHolderListener(null);
        mAppController.getCameraAppUI().prepareForNextRoundOfSurfaceUpdate();
    }

    @Override
    public void destroy() {
        Log.i(TAG, "destroy");
        mActivity.getCameraAppUI().setCenterScanText(null);
    }

    public void handleDecode(Result result, Bitmap barcode) {
        playBeepSoundAndVibrate();
        String resultString = result.getText();

        if (resultString.equals("")) {
            resultString = "defaulturi";
        }
        Log.d(TAG,"handleDecode resultString： " + resultString);
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putString("result", resultString);
        intent.putExtras(bundle);
        if (resultString.contains("MECARD") || resultString.contains("VCARD")) {
            intent.setClass(mActivity, QrVcardResultActivity.class);
        } else {
            intent.setClass(mActivity, QrScanResultActivity.class);
        }
        startActivity(intent);

    }

    @Override
    public void enableTorchMode(boolean enable) {
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

        if (mCameraDevice != null) {
            mCameraDevice.applySettings(mCameraSettings);
            mCameraSettings = mCameraDevice.getSettings();
        }
    }

    public void initCamera(SurfaceHolder surfaceHolder) {
        if (!initialized) {
            initialized = true;
        }

        if (mPaused) {//SPRD：fix bug704259
            return;
        }
        qrCameraOpened();
        mCameraAvailable = true;
    }

    @Override
    protected void switchCamera() {
        Log.i(TAG, "switchCamera");

        freezeScreen(CameraUtil.isFreezeBlurEnable(),
                CameraUtil.isSwitchAnimationEnable());

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if (handler != null) {
                    mActivity.switchFrontAndBackMode();
                }
            }
        });
    }

    @Override
    public void freezeScreen(boolean needBlur, boolean needSwitch) {
        if (handler != null && handler.getHandler() !=null) {
            if (mFreezeBitmap != null && !mFreezeBitmap.isRecycled()) {
                mFreezeBitmap.recycle();
                mFreezeBitmap = null;
            }

            Rect previewRect = null;
            RectF previewArea = null;
            if (mSurfaceView != null) {
                previewRect = mSurfaceView.getHolder().getSurfaceFrame();
                previewArea = new RectF((float)previewRect.left, (float)previewRect.top, (float)previewRect.right, (float)previewRect.bottom);
            }
            mFreezeBitmap = ((QrDecodeHandler)handler.getHandler()).getPreviewBitmap(/*previewRect*/null);
            mFreezeBitmap = BitmapUtils.rotateBmpToDisplay(mActivity, mFreezeBitmap, 0, 0);

            if (needBlur) {
                mFreezeBitmap = CameraUtil.blurBitmap(CameraUtil.computeScale(mFreezeBitmap, 0.2f), (Context)mActivity);
            }
            mActivity.freezeScreenUntilPreviewReady(mFreezeBitmap, previewArea);
        }
    }

    @Override
    public void onDreamSettingChangeListener(HashMap<String, String> keys) {
        for (String key : keys.keySet()) {
            switch (key) {
            case Keys.KEY_DREAM_FLASH_GIF_PHOTO_MODE:
                updateQrCameraFlash();
                break;
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
            int height) {
        Log.i(TAG, "surfaceChanged: " + holder);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.i(TAG, "surfaceCreated: " + holder);
        if (!hasSurface) {
            hasSurface = true;
            startPreview(true);
        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i(TAG, "surfaceDestroyed: " + holder);
        hasSurface = false;

    }

    private void qrCameraOpened() {
        updateQrFlash();
    }

    private void updateQrFlash() {
        //SPRD:fix bug 607183 scan picture from gallery, the flash will flash
        if(isUpdateFlash){
            isUpdateFlash = false;
            return;
        }
        if (mFlashButton != null) {
            int state = mFlashButton.getState();
            if (state == 0 && !mActivity.getIsButteryLow()) {
                enableTorchMode(true);
            } else {
                enableTorchMode(false);
            }
        }
    }

    public ViewfinderView getViewfinderView() {
        return viewfinderView;
    }

    public Handler getHandler() {
        return handler;
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();
    }

    private static final long VIBRATE_DURATION = 200L;

    private void playBeepSoundAndVibrate() {
        if (vibrate) {
            Vibrator vibrator = (Vibrator) mActivity
                    .getSystemService(Service.VIBRATOR_SERVICE);
            vibrator.vibrate(VIBRATE_DURATION);
        }
    }

    private void checkPermissions() {
        if (!ApiHelper.isMOrHigher()) {
            mHasCriticalPermissions = true;
            return;
        }

        if (mActivity.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && mActivity
                        .checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            mHasCriticalPermissions = true;
        } else {
            mHasCriticalPermissions = false;
        }

        if (!mHasCriticalPermissions) {
            Intent cameraIntent = mActivity.getIntent();
            Bundle data = new Bundle();
            data.putParcelable("cameraIntent", cameraIntent);
            Intent intent = new Intent(mActivity, PermissionsActivity.class);
            intent.putExtras(data);

            if (!isCaptureIntent()) {
                startActivity(intent);
                mActivity.finish();
            } else {
                startActivityForResult(intent, 1);
            }
        }
    }

    private boolean isCaptureIntent() {
        if (MediaStore.ACTION_VIDEO_CAPTURE.equals(mActivity.getIntent()
                .getAction())
                || MediaStore.ACTION_IMAGE_CAPTURE.equals(mActivity.getIntent()
                        .getAction())
                || MediaStore.ACTION_IMAGE_CAPTURE_SECURE.equals(mActivity
                        .getIntent().getAction())) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int getModuleType() {
        return QR_MODULE;
    }

    protected boolean mCameraAvailable = false;

    @Override
    public boolean isCameraAvailable(){
        return mCameraAvailable;
    }

    private void startActivity(Intent intent){
        mActivity.startActivity(intent);
    }

    private void startActivityForResult(Intent intent, int requestCode){
        mActivity.startActivityForResult(intent, requestCode);
    }

    //SPRD: Fix bug 881427 add api2 support
    protected void requestCameraOpen() {
        /*
         * SPRD:Fix bug 513841 KEY_CAMERA_ID may be changed during the camera in
         * background if user do switch camera action in contacts, and then we
         * resume the camera, the init function will not be called, so the
         * mCameraId will not be the latest one. Which will cause the switch
         * camera function can not be used. @{
         */
        if (mCameraId != mDataModule.getInt(Keys.KEY_CAMERA_ID)) {
            mCameraId = mDataModule.getInt(Keys.KEY_CAMERA_ID);
            /*SPRD:Fix bug 673906 After switching camera under secure camera, start normal camera, mode list does not update */
            mActivity.getCameraAppUI().updateModeList();
        }

        if (mCameraId == DreamUtil.FRONT_CAMERA /*&& isSupportSensoSelfShotModule()*/) {
            mCameraId = CameraUtil.SENSOR_SELF_SHOT_CAMERA_ID;
        }
        /* @} */
        Log.i(TAG, "requestCameraOpen mCameraId:" + mCameraId);

        doCameraOpen(mCameraId);
    }

    protected void doCameraOpen(int cameraId){
        mActivity.getCameraProvider().requestCamera(mCameraId, useNewApi());
    }

    @Override
    public boolean useNewApi() {
        return GservicesHelper.useCamera2ApiThroughPortabilityLayer(mActivity.getContentResolver());
    }

    @Override
    public void onCameraAvailable(CameraProxy cameraProxy) {
        Log.i(TAG, "onCameraAvailable");
        if (mPaused) {
            return;
        }

        mCameraDevice = cameraProxy;
        CameraManager.get().openDriver(mCameraDevice);
        if(handler == null){
            handler = new QrCaptureActivityHandler(mActivity, QrCodePhotoModule.this,
                    decodeFormats, characterSet);
        }
        initializeCapabilities();
        // Do camera parameter dependent initialization.
        mCameraSettings = mCameraDevice.getSettings();
        CameraManager.getConfigManager().initFromCameraParameters(mCameraDevice,mCameraSettings,mCameraCapabilities,mAppController,mCameraId);
        setupCaptureParams();
        mCameraDevice.applySettings(mCameraSettings);
        // Set a default flash mode and focus mode
        if (mCameraSettings.getCurrentFlashMode() == null) {
            mCameraSettings.setFlashMode(CameraCapabilities.FlashMode.NO_FLASH);
        }
        if (mCameraSettings.getCurrentFocusMode() == null) {
            mCameraSettings.setFocusMode(CameraCapabilities.FocusMode.AUTO);
        }

        startPreview(true);
        mCameraAvailable = true;
        updateFlashTopButton();
        Log.i(TAG, "onCameraAvailable end");

        int surfaceViewHeight = setTransformMatrix(mSurfaceView);
        viewfinderView.setRealParentHeight(surfaceViewHeight);
    }

    public Rect getPictureRect() {
        return viewfinderView.getFramingRect();
    }

    protected void startPreview(boolean optimize) {
        if (isNotStartPrview) {
            return;
        }
        Log.i(TAG, "startPreview start! hasSurface = " + hasSurface);
        if (mCameraDevice == null || !hasSurface) {
            Log.i(TAG, "attempted to start preview before camera device");
            // do nothing
            return;
        }

        mCameraDevice.setPreviewDisplay(mSurfaceHolder);
        updateParametersAppModeId();
        updateQrCameraFlash();

        if(hasSurface){
            initCamera(mSurfaceHolder);
        }

        CameraAgent.CameraStartPreviewCallback startPreviewCallback = new CameraAgent.CameraStartPreviewCallback() {
            @Override
            public void onPreviewStarted() {
                /* SPRD:fix bug1113772 make sure reset frame count after surfacetexture of update @{ */
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        QrCodePhotoModule.this.onPreviewStarted();
                    }
                });
                /* @} */
                final Runnable hideModeCoverRunnable = new Runnable() {
                    @Override
                    public void run() {
                        if (!mPaused) {
                            final CameraAppUI cameraAppUI = mActivity.getCameraAppUI();
                            cameraAppUI.onSurfaceTextureUpdated(cameraAppUI.getSurfaceTexture());
                            cameraAppUI.pauseTextureViewRendering();
                        }
                    }
                };
                mCameraDevice.setSurfaceViewPreviewCallback(
                        new CameraAgent.CameraSurfaceViewPreviewCallback() {
                            @Override
                            public void onSurfaceUpdate() {
                                Log.d(TAG, "SurfaceView: CameraSurfaceViewPreviewCallback");
                                if (mPaused || mCameraDevice == null) {
                                    return;
                                }
                                mActivity.getCameraAppUI().hideModeCover();
                                mHandler.post(hideModeCoverRunnable);
                                // set callback to null
                                mCameraDevice.setSurfaceViewPreviewCallback(null);
                            }
                        });
                }
            };

        doStartPreviewSpecial(isCameraIdle(), isHdr(), mActivity, mCameraDevice, mHandler,
                mDisplayOrientation, isCameraFrontFacing(), mUI.getRootView(), mCameraSettings);//SPRD:fix bug624871
        doStartPreview(startPreviewCallback, mCameraDevice);
        Log.i(TAG, "startPreview end!");
    }

    protected void doStartPreviewSpecial(boolean isCameraIdle, boolean isHdrOn,
            CameraActivity activity, CameraAgent.CameraProxy cameraDevice, Handler h,
            int displayOrientation, boolean mirror, View rootView, CameraSettings cameraSettings) {
    }

    protected void doStartPreview(CameraAgent.CameraStartPreviewCallback startPreviewCallback, CameraAgent.CameraProxy cameraDevice) {
        if (useNewApi()) {
            mCameraDevice.startPreview();
            startPreviewCallback.onPreviewStarted();
        } else {
            mCameraDevice.startPreviewWithCallback(new Handler(Looper.getMainLooper()),
                    startPreviewCallback);
        }
        OrientationManager orientationManager = mAppController.getOrientationManager();
        mUI.onOrientationChanged(orientationManager.getDeviceOrientation().getDegrees());
    }

    protected void updateParametersAppModeId() {
        Log.i(TAG, "updateParametersAppModeId");
        mCameraSettings.setAppModeId(mActivity.getCurrentModuleIndex());
    }

    public void updateQrCameraFlash(){
        int state = mFlashButton.getState();
        if (state == 0 && !mActivity.getIsButteryLow()) {
            enableTorchMode(true);
        } else {
            enableTorchMode(false);
        }
    }
    public void stopPreview() {
        CameraProxy device = mCameraDevice;
        Log.i(TAG, "stopPreview start!mCameraDevice=" + device);
        if (device != null) {
            Log.i(TAG, "stopPreview");
            device.stopPreview();
            device.setPreviewDataCallback(null, null);//SPRD:fix bug914016
        }
    }

    protected void closeCamera() {
        Log.i(TAG, "closeCamera will! mCameraDevice=" + mCameraDevice);
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }

        mCameraAvailable = false;
        if (mCameraDevice == null) {
            Log.i(TAG, "closeCamera end!");
            return;
        }

        mActivity.getCameraProvider().releaseCamera(mCameraDevice.getCameraId());
        mCameraDevice = null;
    }

    private static class MainHandler extends Handler {
        private final WeakReference<QrCodePhotoModule> mModule;

        public MainHandler(QrCodePhotoModule module) {
            super(Looper.getMainLooper());
            mModule = new WeakReference<QrCodePhotoModule>(module);
        }
    }

    public boolean isCameraIdle() {
        return true;
    }

    private boolean isCameraFrontFacing() {
        return mAppController.getCameraProvider().getCharacteristics(mCameraId)
                .isFacingFront();
    }

    protected boolean isHdr() {
        return mDataModuleCurrent.isEnableSettingConfig(Keys.KEY_CAMERA_HDR)
                && mDataModuleCurrent.getBoolean(Keys.KEY_CAMERA_HDR);
    }

    public void applySettings() {
        if (mCameraDevice != null) {
            mCameraDevice.applySettings(mCameraSettings);
        }
    }

    public void setupCaptureParams(){
        mCameraPreviewWidth = CameraManager.getConfigManager().getCameraPreviewWidth();
        mCameraPreviewHeight = CameraManager.getConfigManager().getCameraPreviewHeight();
        mCameraSettings.setDefault(CameraUtil.VALUE_FRONT_FLASH_MODE_LCD == CameraUtil.getFrontFlashMode(), mCameraCapabilities);
        mCameraSettings.setPreviewSize(new Size(mCameraPreviewWidth, mCameraPreviewHeight).toPortabilitySize());
        //mCameraSettings.setCallbackSize(new Size(mCameraPreviewWidth, mCameraPreviewHeight).toPortabilitySize());
        mCameraSettings.setPhotoSize(new Size(mCameraPreviewWidth, mCameraPreviewHeight).toPortabilitySize());//must set PhotoSize, Otherwise can not applysetting
    }

    private void initializeCapabilities() {
        mCameraCapabilities = mCameraDevice.getCapabilities();
    }

    private boolean isCameraOpening() {
        return mCameraSettings == null || mCameraCapabilities == null;
    }

    private void removeAutoFocusMoveCallback() {
        if (mCameraDevice != null) {
            mCameraDevice.setAutoFocusMoveCallback(null, null);
        }
    }

    private void resumeTextureView() {
        if (mActivity.getCameraAppUI() != null) {
            /* SPRD: fix bug 700467 TextureView need cover by using SurfaceView @{ */
            mActivity.getCameraAppUI().refreshTextureViewCover();
            /* @} */
            mActivity.getCameraAppUI().resumeTextureViewRendering();
        }
    }
    public boolean isFirstShowToast = true;
    public boolean isBatteryAgainLowLevel = false;

    @Override
    public void updateBatteryLevel(int level) {
        int batteryLevel = CameraUtil.getLowBatteryNoFlashLevel();
        if (mAppController != null) {
            if (level <= batteryLevel) {
                mAppController.getButtonManager().disableButton(ButtonManagerDream.BUTTON_GIF_PHOTO_FLASH_DREAM);
                Log.i(TAG,"isFirstShowToast: " + isFirstShowToast + ", isBatteryAgainLowLevel: " + isBatteryAgainLowLevel);
                if(isFirstShowToast || (!isFirstShowToast && isBatteryAgainLowLevel)){
                    if(isFirstShowToast == false){
                        isBatteryAgainLowLevel = false;
                    }
                    isFirstShowToast = false;
                    //Sprd:fix bug932845
                    CameraUtil.toastHint(mActivity,R.string.battery_level_low);
                }
                updateQrCameraFlash();
            } else {
                isBatteryAgainLowLevel = true;
                mAppController.getButtonManager().enableButton(ButtonManagerDream.BUTTON_GIF_PHOTO_FLASH_DREAM);
                updateQrCameraFlash();
            }
        }
    }

    private void updateFlashTopButton() {
        if(isFlashSupported()){
            if(mDataModuleCurrent.isEnableSettingConfig(Keys.KEY_DREAM_FLASH_GIF_PHOTO_MODE)){
                mUI.setButtonVisibility(ButtonManagerDream.BUTTON_GIF_PHOTO_FLASH_DREAM,View.VISIBLE);
                return;
            }
        }
        Log.i(TAG, "Flash is not supported");
        mUI.setButtonVisibility(ButtonManagerDream.BUTTON_GIF_PHOTO_FLASH_DREAM,View.INVISIBLE);
    }

    public boolean isFlashSupported(){
        if (mCameraCapabilities != null){
            return CameraUtil.isFlashSupported(mCameraCapabilities, isCameraFrontFacing());
        } else
            return false;
    }

    @Override
    public boolean isUsingBottomBar() {
        // TODO Auto-generated method stub
        return true;
    }
}
