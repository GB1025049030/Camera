
package com.android.camera;

import android.graphics.Rect;
import android.graphics.YuvImage;
import android.graphics.ImageFormat;
import android.graphics.Point;

import com.android.camera2.R;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActionBar;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.view.ViewGroup;
import android.util.Log;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;

import android.view.Window;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.graphics.Bitmap;
import android.content.Intent;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import android.database.Cursor;
import android.provider.MediaStore;
import android.view.WindowManager;
import android.view.Display;
import android.os.AsyncTask;
import android.widget.FrameLayout;
import android.view.ViewGroup.LayoutParams;

/**
 * An example full-screen activity that shows and hides the system UI (i.e. status bar and
 * navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class RefocusPhotoEditActivity extends Activity implements Handler.Callback {
    private final String TAG = "CAM_RefocusPhotoEditActivity";

    private SeekBar seekbar;

    private TextView startValue;

    private TextView endValue;

    private TextView text;

    private int totalLength;

    private int screenWidth;

    private int screenHeight;

    private double moveStep;

    private RefocusIconView mRefocusView;

    private int mProgress;

    private int mOldProgress;

    private ImageView mEditPicture;

    private Bitmap myBitmap;

    private int mCurrentTouchLocation_x;

    private int mCurrentTouchLocation_y;

    private int mOldTouchLocation_x;

    private int mOldTouchLocation_y;

    private byte[] mContent;

    private byte[] mOriginByte;

    private byte[] mMainYuvByte;

    private byte[] mSubYuvByte;

    private byte[] mOtpByte;

    private byte[] mDepth;

    private byte[] mRefocusEditedYuv;

    private Handler mHandler = null;

    private Handler mUiHandler = null;

    private HandlerThread mHandlerThread = null;

    private Refocus mRefocus = new Refocus();

    private boolean mIsSuccess = false;

    private ActionBar mActionBar;

    private MenuItem mUndoItem;

    private MenuItem mRedoItem;

    private MenuItem mSaveItem;

    private int mPhotoWidth;

    private int mPhotoHeigth;

    private int mBitmapWidth;

    private int mBitmapHeigth;

    private int mMainYuvheigth;

    private int mMainYuvWideth;

    private final int testMainPictureWidth = 2592;
    private final int testMainPictureHeight = 1944;

    private static final int MSG_GET_MAIN_YUV_DATA_AND_DEPTH = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.activity_refocus_photo_edit);
        screenWidth = getWindowManager().getDefaultDisplay().getWidth();
        seekbar = (SeekBar) findViewById(R.id.refocus_edit_seekbar);
        startValue = (TextView) findViewById(R.id.start_value_refocus);
        startValue.setText("F8.0");
        endValue = (TextView) findViewById(R.id.end_value_refocus);
        endValue.setText("F2.0");
        text = (TextView) findViewById(R.id.current_value_refocus);
        text.setText("F8.0");
        mEditPicture = (ImageView) findViewById(R.id.refocus_edit_picture);

        getScreenWidthAndHeight();
        mHandlerThread = new HandlerThread(
                RefocusPhotoEditActivity.class.getSimpleName() + "$Handler");
        mUiHandler = new Handler();
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper(), this);
        seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListenerImp());
        FrameLayout root = (FrameLayout) findViewById(R.id.root);
        mActionBar = getActionBar();
        mActionBar.setBackgroundDrawable(new ColorDrawable(0x00000000));
        // mActionBar.setDisplayShowTitleEnabled(false);
        // mActionBar.setDisplayShowHomeEnabled(false);

        ContentResolver resolver = getContentResolver();
        try {
            Uri originalUri = getIntent().getData();
            mContent = readStream(resolver.openInputStream(Uri
                    .parse(originalUri.toString())));
            // readRawFile();
            // mContent = mOriginByte;
            myBitmap = getPicFromBytes(mContent, null, true);
            Log.i(TAG, "originalUri = " + originalUri.toString());
            Log.i(TAG, "myBitmap = " + myBitmap);
            Log.i(TAG, "mContent = " + mContent);
            mBitmapWidth = myBitmap.getWidth();
            mBitmapHeigth = myBitmap.getHeight();
            // mBitmapWidth = 4160;
            // mBitmapHeigth = 3120;
            Log.i(TAG, "mBitmapWidth = " + mBitmapWidth);
            Log.i(TAG, "mBitmapHeigth = " + mBitmapHeigth);
            mEditPicture.setImageBitmap(myBitmap);
            getYuvByteFromBitmap();
        } catch (Exception e) {
            Log.i(TAG, "Exception" + e);
        }
        mRefocusView = new RefocusIconView(this);
        root.addView(mRefocusView);
        LayoutParams lp = root.getLayoutParams();
        lp.width = screenWidth;
        lp.height = (int) mBitmapHeigth * screenWidth / mBitmapWidth;
        root.setLayoutParams(lp);
    }

    @Override
    protected void onStop() {
        if (mRefocus != null) {
            mRefocus.alRnBClose();
        }
        super.onStop();
    }

    private void getScreenWidthAndHeight() {
        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        screenWidth = display.getWidth();
        screenHeight = display.getHeight();
        Log.i(TAG, "screenWidth = " + screenWidth);
        Log.i(TAG, "screenHeight = " + screenHeight);
    }

    private class OnSeekBarChangeListenerImp implements
            SeekBar.OnSeekBarChangeListener {

        public void onProgressChanged(SeekBar seekBar, int progress,
                boolean fromUser) {
            seekbar.setProgress(progress);
            mProgress = progress;
            float percent = progress / 255f;
            float blur = 8f - (percent * 6f);
            DecimalFormat df = new DecimalFormat(".0");
            text.setText("F" + df.format(blur));
            Log.i(TAG, "mProgress = " + mProgress);
            mRefocusView.invalidate();
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
            updateUi();
        }
    }

    private class RefocusIconView extends View {
        int radius = 0;
        private Context context;
        private Paint paint;

        public RefocusIconView(Context context) {
            this(context, null);
        }

        public RefocusIconView(Context context, AttributeSet attrs) {
            super(context, attrs);
            mCurrentTouchLocation_x = screenWidth / 2;
            mCurrentTouchLocation_y = mBitmapHeigth * screenWidth / (2 * mBitmapWidth);
            Log.i(TAG, "mCurrentTouchLocation_x = " + mCurrentTouchLocation_x
                    + " mCurrentTouchLocation_y = " + mCurrentTouchLocation_y);
            this.context = context;
            this.paint = new Paint();
            this.paint.setAntiAlias(true);
            this.paint.setStyle(Paint.Style.STROKE);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            int point_x = (int) event.getX();
            int point_y = (int) event.getY();
            int drawable_y = (int) mBitmapHeigth * screenWidth / mBitmapWidth;
            Log.i(TAG, "drawable_y = " + drawable_y);
            if (point_y <= 0 ||
                    point_y >= drawable_y) {
                return true;
            }
            mCurrentTouchLocation_x = point_x;
            mCurrentTouchLocation_y = point_y;
            Log.i(TAG, "mCurrentTouchLocation_x = " + mCurrentTouchLocation_x
                    + " mCurrentTouchLocation_y = " + mCurrentTouchLocation_y);
            invalidate();
            if (MotionEvent.ACTION_UP == event.getActionMasked()) {
                updateUi();
            }
            return true;
        };

        protected void onDraw(android.graphics.Canvas canvas) {
            int innerCircle = dip2px(context, (int) ((256 - mProgress) / 8) + 5);
            int outCircle = dip2px(context, 40);

            int ringWidth = outCircle - innerCircle;
            Log.i(TAG, "innerCircle = " + innerCircle);

            this.paint.setARGB(155, 167, 190, 206);
            this.paint.setStrokeWidth(2);
            canvas.drawCircle(mCurrentTouchLocation_x, mCurrentTouchLocation_y, innerCircle,
                    this.paint);

            int alpha = 100 + 155 * innerCircle / (dip2px(context, (int) (256 / 8) + 5));
            this.paint.setARGB(alpha, 212, 225, 233);
            this.paint.setStrokeWidth(ringWidth);
            canvas.drawCircle(mCurrentTouchLocation_x, mCurrentTouchLocation_y, innerCircle
                    + ringWidth / 2, this.paint);

            this.paint.setARGB(155, 167, 190, 206);
            this.paint.setStrokeWidth(2);
            canvas.drawCircle(mCurrentTouchLocation_x, mCurrentTouchLocation_y, outCircle,
                    this.paint);

            super.onDraw(canvas);
        }

        public int dip2px(Context context, float dpValue) {
            final float scale = context.getResources().getDisplayMetrics().density;
            return (int) (dpValue * scale + 0.5f);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_undo_redo_save, menu);
        mUndoItem = menu.findItem(R.id.refocus_edit_undo);
        mRedoItem = menu.findItem(R.id.refocus_edit_redo);
        mSaveItem = menu.findItem(R.id.refocus_edit_save);
        mUndoItem.setEnabled(false);
        mRedoItem.setEnabled(false);
        mSaveItem.setEnabled(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                this.finish();
                break;
            case R.id.refocus_edit_undo:
                resetToDefult();
                break;
            case R.id.refocus_edit_redo:
                redoEdit();
                updateUi();
                break;
            case R.id.refocus_edit_save:
                saveJpeg(mRefocusEditedYuv, "Bokeh_result");
                this.finish();
                // MosaicJpeg jpeg = generateFinalMosaic();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void resetToDefult() {
        mUndoItem.setEnabled(false);
        mRedoItem.setEnabled(true);
        mSaveItem.setEnabled(false);
        mOldTouchLocation_x = mCurrentTouchLocation_x;
        mOldTouchLocation_y = mCurrentTouchLocation_y;
        mOldProgress = mProgress;
        mCurrentTouchLocation_x = screenWidth / 2;
        mCurrentTouchLocation_y = mBitmapHeigth * screenWidth / (2 * mBitmapWidth);
        seekbar.setProgress(0);
        myBitmap = getPicFromBytes(mContent, null, true);
        mEditPicture.setImageBitmap(myBitmap);
        mRefocusView.invalidate();
    }

    private void redoEdit() {
        mUndoItem.setEnabled(true);
        mRedoItem.setEnabled(false);
        mSaveItem.setEnabled(true);
        mCurrentTouchLocation_x = mOldTouchLocation_x;
        mCurrentTouchLocation_y = mOldTouchLocation_y;
        mProgress = mOldProgress;
        seekbar.setProgress(mProgress);
        mRefocusView.invalidate();
    }

    private MosaicJpeg generateFinalMosaic() {
        YuvImage yuvimage = new YuvImage(mRefocusEditedYuv, ImageFormat.NV21, mPhotoWidth,
                mPhotoHeigth, null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvimage.compressToJpeg(new Rect(0, 0, mPhotoWidth, mPhotoHeigth), 100, out);
        try {
            out.close();
        } catch (Exception e) {
            return new MosaicJpeg();
        }
        return new MosaicJpeg(out.toByteArray(), mPhotoWidth, mPhotoHeigth);
    }

    public Bitmap getPicFromBytes(byte[] bytes, BitmapFactory.Options opts, boolean isJpeg) {
        if (bytes == null) {
            Log.e(TAG, "getPicFromBytes bytes is null !");
            return null;
        }
        if (isJpeg) {
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opts);
        } else {
            YuvImage yuvimage = new YuvImage(bytes, ImageFormat.NV21, testMainPictureWidth,
                    testMainPictureHeight, null);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            yuvimage.compressToJpeg(new Rect(0, 0, testMainPictureWidth, testMainPictureHeight),
                    80, out);
            byte[] jdata = out.toByteArray();
            try {
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return BitmapFactory.decodeByteArray(jdata, 0, jdata.length, opts);
        }

    }

    public static byte[] readStream(InputStream inStream) throws Exception {
        byte[] buffer = new byte[1024];
        int len = -1;
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        while ((len = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        byte[] data = outStream.toByteArray();
        outStream.close();
        inStream.close();
        return data;
    }

    private static int bytesToInt(byte[] src, int offset) {
        int value;
        value = (int) ((src[offset] & 0xFF)
                | ((src[offset + 1] & 0xFF) << 8)
                | ((src[offset + 2] & 0xFF) << 16)
                | ((src[offset + 3] & 0xFF) << 24));
        return value;
    }

    private void getYuvByteFromBitmap() {
        if (mHandler != null) {
            mHandler.sendEmptyMessage(MSG_GET_MAIN_YUV_DATA_AND_DEPTH);
        }
    }

    private void updateUi() {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... arg) {
                if (mIsSuccess) {
                    Point alPoint = rotatePoint(mCurrentTouchLocation_x,
                            mCurrentTouchLocation_y);
                    mRefocusEditedYuv = mRefocus.alRnBReFocusGen(
                            mRefocusEditedYuv,
                            mProgress,
                            alPoint.x,
                            alPoint.y,
                            0);
                    // saveBytes(mRefocusEditedYuv, "alRnBReFocusGen");
                    Log.i(TAG, "convert yuv to bitmap for display start...");
                    myBitmap = getPicFromBytes(mRefocusEditedYuv, null, false);
                    Log.i(TAG, "convert yuv to bitmap for display end...");
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean succeed) {
                mUndoItem.setEnabled(true);
                mRedoItem.setEnabled(false);
                mSaveItem.setEnabled(true);
                if (mIsSuccess) {
                    mEditPicture.setImageBitmap(myBitmap);
                }
            }
        }.execute();
    }

    public boolean handleMessage(Message msg) {
        Log.i(TAG, "handleMessage/in");
        switch (msg.what) {
            case MSG_GET_MAIN_YUV_DATA_AND_DEPTH:
                /*
                 * // simulation capture Raw4160x3120 Yuv1280x960 String version =
                 * mRefocus.alSDE2VersionInfoGet(); Log.i(TAG,"alSDE2VersionInfoGet = " + version);
                 * int res = -1; res = mRefocus.alSDE2Init(null, 0, 1280, 960, mOtpByte,
                 * mOtpByte.length); if (res == 0) { mDepth = mRefocus.alSDE2Run(mDepth,
                 * mSubYuvByte, mMainYuvByte, 1); //saveBytes(mDepth,"depth"); } else {
                 * Log.e(TAG,"alSDE2Init failed res = " + res); return false; }
                 * //saveBytes(mOtpByte,"otp"); res = mRefocus.alRnBInit(null, 0, 4160, 3120,
                 * mOtpByte, mOtpByte.length); if (res == 0) { int ret =
                 * mRefocus.alRnBReFocusPreProcess(mOriginByte, mDepth, 1280, 960); //release memory
                 * mOriginByte = null; Log.i(TAG, "alRnBReFocusPreProcess ret = " + ret); mIsSuccess
                 * = true; }else { Log.e(TAG,"alRnBInit failed res = " + res); return false; }
                 * mMainYuvheigth = 3120; mMainYuvWideth = 4160;
                 */
                if (mContent != null) {
                    int lengthOfContent = mContent.length;
                    byte[] dimens = Arrays.copyOfRange(mContent, lengthOfContent - 28,
                            lengthOfContent);
                    int lengthOfDimens = dimens.length;
                    Log.i(TAG, "lengthOfDimens = " + lengthOfDimens);
                    int lengthOfDepth = bytesToInt(dimens, lengthOfDimens - 4);
                    int lengthOfMainYuv = bytesToInt(dimens, lengthOfDimens - 8);
                    int otpSize = bytesToInt(dimens, lengthOfDimens - 12);
                    int subYuvheigth = bytesToInt(dimens, lengthOfDimens - 16);
                    int subYuvWideth = bytesToInt(dimens, lengthOfDimens - 20);
                    int mainYuvheigth = bytesToInt(dimens, lengthOfDimens - 24);
                    int mainYuvWideth = bytesToInt(dimens, lengthOfDimens - 28);
                    int lengthOfMainJpeg = lengthOfContent - lengthOfMainYuv - lengthOfDepth
                            - otpSize - 28;
                    Log.i(TAG, "lengthOfContent = " + lengthOfContent);
                    Log.i(TAG, "lengthOfDepth = " + lengthOfDepth);
                    Log.i(TAG, "lengthOfMainYuv = " + lengthOfMainYuv);
                    Log.i(TAG, "otpSize = " + otpSize);
                    Log.i(TAG, "subYuvheigth = " + subYuvheigth);
                    Log.i(TAG, "subYuvWideth = " + subYuvWideth);
                    Log.i(TAG, "mainYuvheigth = " + mainYuvheigth);
                    Log.i(TAG, "mainYuvWideth = " + mainYuvWideth);
                    Log.i(TAG, "lengthOfMainJpeg = " + lengthOfMainJpeg);
                    mRefocusEditedYuv = new byte[lengthOfMainYuv];
                    // separate yuv of main camera fron the current bitmap
                    byte[] mainYuvByte = new byte[lengthOfMainYuv];
                    for (int i = 0; i < lengthOfMainYuv; i++) {
                        mainYuvByte[i] = mContent[lengthOfMainJpeg + i];
                    }
                    mMainYuvByte = mainYuvByte;
                    // saveBytes(mMainYuvByte, "orign");
                    // separate depth byte[] fron the current bitmap
                    byte[] depthByte = new byte[lengthOfDepth];
                    for (int i = 0; i < lengthOfDepth; i++) {
                        depthByte[i] = mContent[lengthOfMainJpeg + lengthOfMainYuv + i];
                    }
                    mDepth = depthByte;
                    // saveBytes(mDepth, "mDepth");
                    // separate OTP byte[] fron the current bitmap
                    byte[] otpByte = new byte[otpSize];
                    for (int i = 0; i < otpSize; i++) {
                        otpByte[i] = mContent[lengthOfMainJpeg + lengthOfMainYuv + lengthOfDepth
                                + i];
                    }
                    // saveBytes(otpByte, "otp");
                    int ret = mRefocus.alRnBInit(null, 0, testMainPictureWidth,
                            testMainPictureHeight, otpByte, otpSize);
                    if (ret == 0) {
                        int value = mRefocus
                                .alRnBReFocusPreProcess(mMainYuvByte, mDepth, 1280, 960);
                        mMainYuvByte = null;
                        Log.i(TAG, "alRnBReFocusPreProcess value = " + value);
                    } else {
                        Log.e(TAG, "alRnBInit failed ret = " + ret);
                    }
                    mMainYuvheigth = testMainPictureHeight;
                    mMainYuvWideth = testMainPictureWidth;
                    if (ret == 0) {
                        mIsSuccess = true;
                    }
                }

                break;
            default:
                break;
        }
        Log.i(TAG, "handleMessage/out");
        return true;
    }

    private class MosaicJpeg {
        public MosaicJpeg(byte[] data, int width, int height) {
            this.data = data;
            this.width = width;
            this.height = height;
            this.isValid = true;
        }

        public MosaicJpeg() {
            this.data = null;
            this.width = 0;
            this.height = 0;
            this.isValid = false;
        }

        public final byte[] data;
        public final int width;
        public final int height;
        public final boolean isValid;
    }

    // For 13M Test
    // private void readRawFile()
    // {
    // Resources resources = this.getResources();
    // InputStream originStream = null;
    // InputStream mainStream = null;
    // InputStream subStream = null;
    // InputStream otpStream = null;
    // InputStream depthStream = null;
    //
    // try {
    // originStream = resources.openRawResource(R.raw.origin_data);
    // mOriginByte = new byte[originStream.available()];
    // mRefocusEditedYuv = new byte[originStream.available()];
    // Log.i(TAG, "originStream.available() = " + originStream.available());
    // originStream.read(mOriginByte);
    // Log.i(TAG, "read mOriginByte:" + mOriginByte);
    //
    // mainStream = resources.openRawResource(R.raw.main_data);
    // mMainYuvByte = new byte[mainStream.available()];
    // Log.i(TAG, "mainStream.available() = " + mainStream.available());
    // mainStream.read(mMainYuvByte);
    // Log.i(TAG, "read mMainYuvByte:" + mMainYuvByte);
    //
    // subStream = resources.openRawResource(R.raw.sub_data);
    // mSubYuvByte = new byte[subStream.available()];
    // Log.i(TAG, "subStream.available() = " + subStream.available());
    // subStream.read(mSubYuvByte);
    // Log.i(TAG, "read mSubYuvByte:" + mSubYuvByte);
    //
    // otpStream = resources.openRawResource(R.raw.otp);
    // mOtpByte = new byte[otpStream.available()];
    // Log.i(TAG, "otpStream.available() = " + otpStream.available());
    // otpStream.read(mOtpByte);
    // Log.i(TAG, "read mOtpByte:" + mOtpByte);
    //
    // depthStream = resources.openRawResource(R.raw.depth_data);
    // mDepth = new byte[depthStream.available()];
    // Log.i(TAG, "depthStream.available() = " + depthStream.available());
    // depthStream.read(mDepth);
    // Log.i(TAG, "read mDepth:" + mDepth);
    //
    // } catch (IOException e)
    // {
    // Log.e(TAG, "write file", e);
    // } finally
    // {
    // if (originStream != null)
    // {
    // try {
    // originStream.close();
    // } catch (IOException e)
    // {
    // Log.e(TAG, "close file", e);
    // }
    // }
    // if (mainStream != null)
    // {
    // try {
    // mainStream.close();
    // } catch (IOException e)
    // {
    // Log.e(TAG, "close file", e);
    // }
    // }
    // if (subStream != null)
    // {
    // try {
    // subStream.close();
    // } catch (IOException e)
    // {
    // Log.e(TAG, "close file", e);
    // }
    // }
    // if (otpStream != null)
    // {
    // try {
    // otpStream.close();
    // } catch (IOException e)
    // {
    // Log.e(TAG, "close file", e);
    // }
    // }
    // if (depthStream != null)
    // {
    // try {
    // depthStream.close();
    // } catch (IOException e)
    // {
    // Log.e(TAG, "close file", e);
    // }
    // }
    // }
    // }

    private void saveBytes(byte[] bytes, String name) {
        OutputStream output = null;
        try {
            File file = new File(Environment.getExternalStorageDirectory() + "/DCIM", name
                    + "_bytes");
            output = new FileOutputStream(file);
            output.write(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != output) {
                try {
                    output.close();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
    }

    private void saveJpeg(byte[] bytes, String name) {
        OutputStream output = null;
        try {
            YuvImage yuvimage = new YuvImage(bytes, ImageFormat.NV21, testMainPictureWidth,
                    testMainPictureHeight, null);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            yuvimage.compressToJpeg(new Rect(0, 0, testMainPictureWidth, testMainPictureHeight),
                    100, out);
            byte[] jdata = out.toByteArray();
            SimpleDateFormat sdf = new SimpleDateFormat("_yyyyMMddmmss");
            java.util.Date date = new java.util.Date();
            String strDate = sdf.format(date);
            File file = new File(Environment.getExternalStorageDirectory() + "/DCIM", name
                    + strDate + ".jpg");
            output = new FileOutputStream(file);
            output.write(jdata);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != output) {
                try {
                    output.close();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
    }

    private byte[] rotateYUV420Degree90(byte[] data, int imageWidth, int imageHeight)
    {
        byte[] yuv = new byte[imageWidth * imageHeight * 3 / 2];
        // Rotate the Y luma
        int i = 0;
        for (int x = 0; x < imageWidth; x++)
        {
            for (int y = imageHeight - 1; y >= 0; y--)
            {
                yuv[i] = data[y * imageWidth + x];
                i++;
            }
        }
        // Rotate the U and V color components
        i = imageWidth * imageHeight * 3 / 2 - 1;
        for (int x = imageWidth - 1; x > 0; x = x - 2)
        {
            for (int y = 0; y < imageHeight / 2; y++)
            {
                yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth) + x];
                i--;
                yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth) + (x - 1)];
                i--;
            }
        }
        return yuv;
    }

    public Point rotatePoint(int px, int py) {
        Log.i(TAG, "origin px = " + px);
        Log.i(TAG, "origin py = " + py);

        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        int screenWidth = display.getWidth();
        int screenHeight = display.getHeight();
        int width = testMainPictureWidth - 1;
        int height = testMainPictureHeight - 1;
        if (width != 0) {
            screenHeight = (width * screenWidth) / height;
        }

        int xRotated = (py * width) / screenHeight;
        int yRotated = height - ((px * height) / screenWidth);
        Log.i(TAG, "rotated px = " + xRotated);
        Log.i(TAG, "rotated py = " + yRotated);
        return new Point(xRotated, yRotated);
    }

}
