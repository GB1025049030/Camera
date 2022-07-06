package com.android.camera;

import android.graphics.Rect;
import android.graphics.YuvImage;
import android.graphics.ImageFormat;

import com.android.camera2.R;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActionBar;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
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
import android.view.Window;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.content.Intent;
import android.content.ContentResolver;
import android.graphics.BitmapFactory;
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
import android.content.DialogInterface;
import android.widget.ListView;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import android.view.LayoutInflater;
import java.util.HashMap;
import android.widget.SimpleAdapter;
import android.app.Dialog;
import java.util.Arrays;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class DulCameraPhotoEditActivity extends Activity implements Handler.Callback,
        DialogInterface.OnDismissListener{

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

    private int mCurrentTouchLocation_a_x;

    private int mCurrentTouchLocation_a_y;
    
    private int mCurrentTouchLocation_b_x;

    private int mCurrentTouchLocation_b_y;

    private int mOldTouchLocation_x;

    private int mOldTouchLocation_y;

    private byte[] mContent;

    private byte[] mMainYuvByte;

    private byte[] mDepth;

    private byte[] mRefocusEditedYuv;

    private Handler mHandler = null;

    private Handler mUiHandler = null;

    private HandlerThread mHandlerThread = null;

    private Refocus mRefocus = new Refocus();

    private boolean mIsSuccess = false;

    private ActionBar mActionBar;

    private MenuItem mUndoItem;

    private MenuItem mDulCameraSettings;

    private MenuItem mRedoItem;

    private MenuItem mSaveItem;

    private int mPhotoWidth;

    private int mPhotoHeigth;

    private int mBitmapWidth;

    private int mBitmapHeigth;

    private int mMainYuvheigth;

    private int mMainYuvWideth;

    private byte[] mOtpData;

    private static final int MSG_GET_MAIN_YUV_DATA_AND_DEPTH = 0;
    
    private boolean aPointNeedToMove = false;
    private boolean bPointNeedToMove = false;
    private ListView mListView;
    private int distance = 0;
    private int mVcm = 0;
    private final int testMainPictureWidth = 2592;
    private final int testMainPictureHeight = 1944;
    private double mDistance = 0.0;
    private final String TAG = "DulCameraPhotoEditActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.activity_dul_camera_photo_edit);
        screenWidth = getWindowManager().getDefaultDisplay().getWidth();
        text = (TextView) findViewById(R.id.current_value_distance);
        text.setText("0");
        mEditPicture = (ImageView) findViewById(R.id.refocus_edit_picture);

        getScreenWidthAndHeight();
        mHandlerThread = new HandlerThread(
                DulCameraPhotoEditActivity.class.getSimpleName() + "$Handler");
        mUiHandler = new Handler();
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper(), this);
        FrameLayout root = (FrameLayout)findViewById(R.id.root);
        mActionBar = getActionBar();
        mActionBar.setBackgroundDrawable(new ColorDrawable(0x00000000));
        //mActionBar.setDisplayShowTitleEnabled(false);
        //mActionBar.setDisplayShowHomeEnabled(false);

        ContentResolver resolver = getContentResolver();
        try {
            Uri originalUri = getIntent().getData();
            mContent = readStream(resolver.openInputStream(Uri
                    .parse(originalUri.toString())));
            myBitmap = getPicFromBytes(mContent, null);
            Log.i(TAG, "originalUri = " + originalUri.toString());
            Log.i(TAG, "myBitmap = " + myBitmap);
            Log.i(TAG, "mContent = " + mContent);
            mBitmapWidth = myBitmap.getWidth();
            mBitmapHeigth = myBitmap.getHeight();
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
        lp.height = (int)mBitmapHeigth*screenWidth/mBitmapWidth;
        root.setLayoutParams(lp);
    }

    private void getScreenWidthAndHeight() {
        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        screenWidth = display.getWidth();
        screenHeight = display.getHeight();
        Log.i(TAG, "screenWidth = " + screenWidth);
        Log.i(TAG, "screenHeight = " + screenHeight);
    }

    private class RefocusIconView extends View{
        int radius = 0;
        private Context context;
        private Paint paint;
        public RefocusIconView(Context context) {
            this(context, null);
        }

        public RefocusIconView(Context context, AttributeSet attrs) {
            super(context, attrs);
            mCurrentTouchLocation_a_x = screenWidth/2;
            mCurrentTouchLocation_a_y = mBitmapHeigth*screenWidth/(2*mBitmapWidth) - 200;
            mCurrentTouchLocation_b_x = screenWidth/2;
            mCurrentTouchLocation_b_y = mBitmapHeigth*screenWidth/(2*mBitmapWidth) + 200;
            Log.i(TAG, "mCurrentTouchLocation_a_x = " + mCurrentTouchLocation_a_x
                    + " mCurrentTouchLocation_a_y = " + mCurrentTouchLocation_a_y
                    + " mCurrentTouchLocation_b_x = " + mCurrentTouchLocation_b_x
                    + "mCurrentTouchLocation_b_y = " + mCurrentTouchLocation_b_y);
            this.context = context;
            this.paint = new Paint();
            this.paint.setAntiAlias(true);
            this.paint.setStyle(Paint.Style.STROKE);
        }
        @Override
        public boolean onTouchEvent(MotionEvent event) {
            int point_x = (int) event.getX();
            int point_y = (int) event.getY();
            Log.i(TAG, "onTouchEvent point_x = " + point_x + " point_y = " + point_y);
            int drawable_y = (int)mBitmapHeigth*screenWidth/mBitmapWidth;
            Log.i(TAG, "drawable_y = " + drawable_y);
            if (point_y <= 0||
                point_y >= drawable_y) {
                return true;
            }

            switch (event.getAction()&MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    point_x = (int) event.getX();
                    point_y = (int) event.getY();
                    int distance_a = (point_x - mCurrentTouchLocation_a_x)*(point_x - mCurrentTouchLocation_a_x) + (point_y - mCurrentTouchLocation_a_y)*(point_y - mCurrentTouchLocation_a_y);
                    int distance_b = (point_x - mCurrentTouchLocation_b_x)*(point_x - mCurrentTouchLocation_b_x) + (point_y - mCurrentTouchLocation_b_y)*(point_y - mCurrentTouchLocation_b_y);
                    Log.i(TAG, "ACTION_DOWN distance_a = " + distance_a + " distance_b = " + distance_b);
                    if (distance_a <= distance_b) {
                        aPointNeedToMove = true;
                        bPointNeedToMove = false;
                        Log.i(TAG, "ACTION_DOWN aPointNeedToMove = " + aPointNeedToMove + " bPointNeedToMove = " + bPointNeedToMove);
				    } else {
						aPointNeedToMove = false;
                        bPointNeedToMove = true;
                        Log.i(TAG, "ACTION_DOWN aPointNeedToMove = " + aPointNeedToMove + " bPointNeedToMove = " + bPointNeedToMove);
					}
                case MotionEvent.ACTION_UP:
                    point_x = (int) event.getX();
                    point_y = (int) event.getY();
                    if (aPointNeedToMove) {
                    	mCurrentTouchLocation_a_x = point_x;
                    	mCurrentTouchLocation_a_y = point_y;
                        Log.i(TAG, "ACTION_UP aPointNeedToMove = " + aPointNeedToMove + " bPointNeedToMove = " + bPointNeedToMove);
                    	invalidate();
                    } else if (bPointNeedToMove) {
                        mCurrentTouchLocation_b_x = point_x;
                    	mCurrentTouchLocation_b_y = point_y;
                        Log.i(TAG, "ACTION_UP aPointNeedToMove = " + aPointNeedToMove + " bPointNeedToMove = " + bPointNeedToMove);
                    	invalidate();
                    }
                case MotionEvent.ACTION_MOVE:
                    point_x = (int) event.getX();
                    point_y = (int) event.getY();
                    if (aPointNeedToMove) {
                    	mCurrentTouchLocation_a_x = point_x;
                    	mCurrentTouchLocation_a_y = point_y;
                        Log.i(TAG, "ACTION_MOVE aPointNeedToMove = " + aPointNeedToMove + " bPointNeedToMove = " + bPointNeedToMove);
                    	invalidate();
                    } else if (bPointNeedToMove) {
                        mCurrentTouchLocation_b_x = point_x;
                    	mCurrentTouchLocation_b_y = point_y;
                        Log.i(TAG, "ACTION_MOVE aPointNeedToMove = " + aPointNeedToMove + " bPointNeedToMove = " + bPointNeedToMove);
                    	invalidate();
                    }
            }
            updateUi();
            return true;
        };

        protected void onDraw(android.graphics.Canvas canvas) {
            int innerCircle = dip2px(context, 10);
            int outCircle = dip2px(context, 30);

            int ringWidth = outCircle - innerCircle;
/*
            this.paint.setARGB(155, 167, 190, 206);
            this.paint.setStrokeWidth(2);
            canvas.drawCircle(mCurrentTouchLocation_a_x, mCurrentTouchLocation_a_y, innerCircle, this.paint);
            
            this.paint.setARGB(155, 167, 190, 206);
            this.paint.setStrokeWidth(2);
            canvas.drawCircle(mCurrentTouchLocation_b_x, mCurrentTouchLocation_b_y, innerCircle, this.paint);
*/
            this.paint.setARGB(150, 212 ,225, 233);
            if (aPointNeedToMove) {
                this.paint.setARGB(150, 0 ,255, 0);
            }
            this.paint.setStrokeWidth(ringWidth);
            canvas.drawCircle(mCurrentTouchLocation_a_x, mCurrentTouchLocation_a_y, innerCircle + ringWidth/2, this.paint);
            
            this.paint.setARGB(150, 0 ,255, 0);
            if (aPointNeedToMove) {
                this.paint.setARGB(150, 212 ,225, 233);
            }
            this.paint.setStrokeWidth(ringWidth);
            canvas.drawCircle(mCurrentTouchLocation_b_x, mCurrentTouchLocation_b_y, innerCircle + ringWidth/2, this.paint);
/*
            this.paint.setARGB(155, 167, 190, 206);
            this.paint.setStrokeWidth(2);
            canvas.drawCircle(mCurrentTouchLocation_a_x, mCurrentTouchLocation_a_y, outCircle, this.paint);
            
            this.paint.setARGB(155, 167, 190, 206);
            this.paint.setStrokeWidth(2);
            canvas.drawCircle(mCurrentTouchLocation_b_x, mCurrentTouchLocation_b_y, outCircle, this.paint);
*/
            this.paint.setColor(Color.WHITE);
            this.paint.setStrokeWidth(5);
            canvas.drawLine(mCurrentTouchLocation_a_x, mCurrentTouchLocation_a_y, mCurrentTouchLocation_b_x, mCurrentTouchLocation_b_y, this.paint); 
            super.onDraw(canvas);
        }

        public int dip2px(Context context, float dpValue) {
            final float scale = context.getResources().getDisplayMetrics().density;
            return (int) (dpValue * scale + 0.5f);
        }
    }
/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_dul_camera_settings, menu);
        mDulCameraSettings = menu.findItem(R.id.dul_camera_settings);
        mDulCameraSettings.setEnabled(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                this.finish();
                break;
            case R.id.dul_camera_settings:
                showDialog();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
*/

    public static Bitmap getPicFromBytes(byte[] bytes, BitmapFactory.Options opts) {
        if (bytes != null)
            if (opts != null)
                return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opts);
            else
                return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        return null;
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
                | ((src[offset+1] & 0xFF)<<8)
                | ((src[offset+2] & 0xFF)<<16)
                | ((src[offset+3] & 0xFF)<<24));
        return value;
    }

    public static float byte2float(byte[] b, int index) {    
        int l;                                             
        l = b[index + 0];                                  
        l &= 0xff;                                         
        l |= ((long) b[index + 1] << 8);                   
        l &= 0xffff;                                       
        l |= ((long) b[index + 2] << 16);                  
        l &= 0xffffff;                                     
        l |= ((long) b[index + 3] << 24);                  
        return Float.intBitsToFloat(l);                    
    }

    private void getYuvByteFromBitmap() {
        if (mHandler != null) {
            mHandler.sendEmptyMessage(MSG_GET_MAIN_YUV_DATA_AND_DEPTH);
        }
    }

    private void updateUi() {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void ... arg) {
                float[] extra_params = new float[256];
                extra_params[0] = 2000f;
                extra_params[1] = 1f;
                extra_params[2] = 1f;
                extra_params[3] = 3f;
                extra_params[4] = 0f;
                extra_params[5] = 0f;
                extra_params[6] = 1f;
                extra_params[7] = 0f;
                extra_params[8] = 1f;
                extra_params[9] = 1f;
                int result = -1;
                int subHeigth = 960;
                int subWideth = 1280;
                int num = 256;
                Point aPoint = rotatePoint(mCurrentTouchLocation_a_x, mCurrentTouchLocation_a_y);
                Point bPoint = rotatePoint(mCurrentTouchLocation_b_x, mCurrentTouchLocation_b_y);
				int x1 = aPoint.x;
				int y1 = aPoint.y;
				int x2 = bPoint.x;
				int y2 = bPoint.y;
                mRefocus.distance(mDepth,subWideth,subHeigth,x1,y1,x2,y2,mVcm,mOtpData,mOtpData.length);
                mDistance = mRefocus.result();
               // result = mRefocus.alSDE2_DistanceMeasurement(mDepth, subWideth, subHeigth,
                        //(short) (aPoint.x + (short)10), (short) (aPoint.y + (short)10),
                        //(short) (bPoint.x + (short)10), (short) (bPoint.x + (short)10),
                 //       x1,y1,x2,y2,
                   //     mVcm, mOtpData, mOtpData.length);
                //distance = mDepthLib.DistanceCalc(mDepth, mMainYuvByte, mMainYuvheigth, mMainYuvWideth,
                        //subHeigth, subWideth, mMainYuvheigth, mMainYuvWideth, 
                        //aPoint.x, aPoint.y, bPoint.x, bPoint.y,
                        //mLocalLength, mLocalLength, mPixelSize, extra_params, num);
                Log.i(TAG, "result = " + result);
               // mDistance = mRefocus.DistanceResult();
                Log.i(TAG, "mDistance = " + mDistance);
                return true;
            }

            @Override
            protected void onPostExecute(Boolean succeed) {
                DecimalFormat df= new DecimalFormat("######0.00");
                text.setText(df.format(mDistance));
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
                    byte[] dulCameraData = Arrays.copyOfRange(mContent, lengthOfContent - 4,
                            lengthOfContent);
                    byte[] dimens = Arrays.copyOfRange(mContent, lengthOfContent - 32,
                            lengthOfContent - 4);
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
                            - otpSize - 32;
                    int lengthOfDulCameraData = dulCameraData.length;
                    mVcm = bytesToInt(dulCameraData, lengthOfDulCameraData - 4);
                    Log.i(TAG, "mVcm = " + mVcm);
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
                    //int ret = mRefocus.alRnBInit(null, 0, testMainPictureWidth,
                            //testMainPictureHeight, otpByte, otpSize);
                    //if (ret == 0) {
                       // int value = mRefocus
                                //.alRnBReFocusPreProcess(mMainYuvByte, mDepth, 1280, 960);
                        //mMainYuvByte = null;
                        //Log.i(TAG, "alRnBReFocusPreProcess value = " + value);
                    //} else {
                       // Log.e(TAG, "alRnBInit failed ret = " + ret);
                    //}
                    mOtpData = otpByte;
                    mMainYuvheigth = testMainPictureHeight;
                    mMainYuvWideth = testMainPictureWidth;
                }

                break;
            default:
                break;
        }
        Log.i(TAG, "handleMessage/out");
        return true;
    }

    private void showDialog() {
    }

    public void onDismiss(DialogInterface dialog) {
    }

    public Point rotatePoint(int px, int py) {
        Log.i(TAG, "origin px = " + px);
        Log.i(TAG, "origin py = " + py);

        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        int screenWidth = display.getWidth();
        int screenHeight = display.getHeight();
        int width = 1280 - 1;
        int height = 960 - 1;
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
