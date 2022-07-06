package com.freeme.camera.modules.slrphoto;

import android.annotation.Nullable;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.android.camera.debug.Log;
import com.android.camera.bitmap.bitmappool.BitmapPoolManager;
import com.android.util.libyuv.YUVManager;
import com.freeme.camera.common.help.TransformHelp;
import com.android.camera2.R;
import com.android.slrblur.BlurInfo;
import com.android.slrblur.SmoothBlurJni;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BVirtualView extends BVirtualViewAbs {
    private static final Log.Tag TAG = new Log.Tag("BVirtualView");
    private static final int SUPPORT_MAX_ASPECT_SIZE = 720;
    private static final float IN_SHARPNESS_RADIUS_SCALE = 0.25f;
    private static final float OUT_SHARPNESS_RADIUS_SCALE = 0.35f;

    private final PaintFlagsDrawFilter mPaintFlagsDrawFilter;
    private final Context mContext;
    private final BlurInfo mBlurInfo;
    private final RectF mPreviewArea = new RectF(0f, 120f, 720f, 1380f);
    private final Matrix mRotationMatrix;
    private final Matrix mTranslateMatrix;
    private final Bitmap mPartBitmap;
    private final Bitmap mSeekBarPoint;
    private final Bitmap mCircleOutGreen;
    private final Paint mPaint;
    private final Path mPath;
    private final RectF mSeekBarArea = new RectF();
    private final float mPartRadius;
    private final float mSphereRadius;
    private final float mCircleOutRadius;
    private final float mCircleInRadius;
    private final float mSeekBarLineRadius;
    private final GestureDetector mGestureDetector;
    private final TransformHelp<BVirtualHolder, Bitmap> mTransformHelp;
    private final ConcurrentLinkedQueue<BVirtualHolder> mBVirtualHolders;
    private final BVirtualHolder mCurrentBVirtualHolder;
    private final PointF mPointF = new PointF();
    private final float[] mPoint = new float[2];
    private final int mDefaultWidth;
    private Matrix mMatrix;
    private boolean mStillDown;
    private boolean mIsChangeCenter;
    private boolean mIsInSeekBarArea;
    private int mWidth;
    private int mHeight;
    private int mPreviewAreaWidth;
    private int mPreviewAreaHeight;
    private int mOrientation;
    private int mBlurDegree;
    private int mOnSingleX;
    private int mOnSingleY;
    private float mTranslateYProgress;
    private float mCenterX;
    private float mCenterY;
    private byte[] mRGBAArray;
    private Bitmap mBlueBgBitmap;

    public BVirtualView(Context context) {
        this(context, null);
    }

    public BVirtualView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BVirtualView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mPaintFlagsDrawFilter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

        // Bitmap
        mContext = context;
        mBlurInfo = new BlurInfo();
        mMatrix = new Matrix();
        mBlurDegree = 4;
        mTranslateYProgress = 0.5f;

        mPartBitmap = ((BitmapDrawable) context.getResources().getDrawable(R.drawable.freeme_vb_diaphragm_part, null)).getBitmap();
        mSeekBarPoint = ((BitmapDrawable) context.getResources().getDrawable(R.drawable.freeme_seekbar_scroll_point, null)).getBitmap();
        mCircleOutGreen = ((BitmapDrawable) context.getResources().getDrawable(R.drawable.freeme_circle_out_green, null)).getBitmap();
        mRotationMatrix = new Matrix();
        mTranslateMatrix = new Matrix();

        mSphereRadius = mSeekBarPoint.getWidth() >> 1;
        mCircleOutRadius = mCircleOutGreen.getWidth() >> 1;
        mCircleInRadius = mCircleOutRadius * 0.75f;
        mPartRadius = mCircleOutGreen.getWidth() * 0.15f;
        mSeekBarLineRadius = /*mCircleOutGreen.getWidth() * 0.75f*/mCircleOutRadius + mPartRadius - mSphereRadius;

        mPaint = new Paint();
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mPaint.setAntiAlias(true);

        mPath = new Path();
        mPath.addCircle(mCircleOutRadius, mCircleOutRadius, mCircleInRadius, Path.Direction.CCW);

        // Main
        mGestureDetector = new GestureDetector(context, this);

        mCurrentBVirtualHolder = new BVirtualHolder();
        mBVirtualHolders = new ConcurrentLinkedQueue<>();
        mTransformHelp = new TransformHelp<>(new TransformHelp.Callback<BVirtualHolder, Bitmap>() {
            @Override
            public Bitmap getResult(BVirtualHolder holder) {
                holder.copyTo(mCurrentBVirtualHolder);
                return getSmoothBlurPreviewBitmap(mCurrentBVirtualHolder);
            }

            @Override
            public void onResultChange(boolean state) {
                if (state) postInvalidate();
            }
        });

        mDefaultWidth = getScreenWidth(context);
    }

    private int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }

    public void start() {
        setBitmapSize(0, 0);
        if (mTransformHelp != null) {
            mTransformHelp.start();
        }
    }

    public void stop() {
        if (mTransformHelp != null) {
            mTransformHelp.stop();
        }
        if (mBVirtualHolders != null) {
            mBVirtualHolders.clear();
        }
        YUVManager.I.destroy();
    }

    @Override
    public void setOrientation(int orientation, boolean animation) {
        setDisplayOrientation(orientation);
    }

    public void setDisplayOrientation(int orientation) {
        orientation = orientation % 360;
        if (mOrientation != orientation) {
            mOrientation = orientation;
            invalidate();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int mode = getSpecMode(widthMeasureSpec);
        int width = getMeasuredWidth(widthMeasureSpec, mode);
        int height = getMeasuredHeight(heightMeasureSpec, width, mode);
        setMeasuredDimension(width, height);
    }

    private int getSpecMode(final int widthSpec) {
        return MeasureSpec.getMode(widthSpec);
    }

    private int getMeasuredWidth(int spec, final int specMode) {
        final int specSize = MeasureSpec.getSize(spec);
        final int minSize = (int) Math.max(mCircleOutRadius * 2 + mPartRadius * 2
                + mSphereRadius, mSeekBarLineRadius * 2 + mSphereRadius);
        int width = Math.max(minSize, mDefaultWidth);
        if (specMode == MeasureSpec.EXACTLY) {
            width = Math.max(minSize, specSize);
        } else {
            width = Math.min(width, specSize);
        }
        return width;
    }

    private int getMeasuredHeight(int spec, final int width, final int specMode) {
        final int specSize = MeasureSpec.getSize(spec);
        final int minSize = (int) Math.max(mCircleOutRadius * 2 + mPartRadius * 2
                + mSphereRadius, mSeekBarLineRadius * 2 + mSphereRadius);
        int height = Math.max(minSize, Math.round(mDefaultWidth * mPreviewArea.height() / mPreviewArea.width()));
        if (specMode == MeasureSpec.EXACTLY) {
            height = Math.max(minSize, Math.round(width * mPreviewArea.height() / mPreviewArea.width()));
        } else {
            height = Math.min(height, specSize);
        }
        return height;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        int x = (right - left) >> 1;
        int y = (bottom - top) >> 1;
        setSinglePoint(x, y);
        setCenterPoint(x, y);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                setState(true);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                setState(false);
                break;
            default:
                break;
        }
        mGestureDetector.onTouchEvent(event);
        return true;
    }

    private void setState(boolean down) {
        if (down == mStillDown) {
            return;
        }
        mStillDown = down;
        if (!down) {
            onUp();
        }
    }

    public void onUp() {
        setIsInSeekBarArea(false);
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        if (!mIsInSeekBarArea) {
            setIsInSeekBarArea(isInSeekBarSlideArea(mTranslateYProgress, motionEvent.getX(), motionEvent.getY()));
        }
        return super.onDown(motionEvent);
    }

    private boolean isInSeekBarSlideArea(float progress, float x, float y) {
        PointF pointF = completeSeekBarCoordinate(isScreenPortrait(), progress, mCenterX, mCenterY);
        float size = 3.0f * mSphereRadius * 2;
        mSeekBarArea.set(pointF.x - size, pointF.y - size, pointF.x + size, pointF.y + size);
        return mSeekBarArea.contains(x, y);
    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        if (mPreviewArea.contains(motionEvent.getX(), motionEvent.getY())) {
            int x = (int) motionEvent.getX();
            int y = (int) motionEvent.getY();
            setCenterPoint(x, y);
            setSinglePoint(x, y);
            setIsInSeekBarArea(false);
        }
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        if (mIsInSeekBarArea) {
            float threshold = Math.min(getMeasuredHeight(), getMeasuredWidth());
            float step = v1 / threshold;
            boolean isPortrait = isScreenPortrait();
            if (isPortrait) {
                if (Math.abs(v1) > Math.abs(v)) {
                    updateTranslateYProgress(-step * 1.5f);
                }
            } else {
                step = v / threshold;
                if (Math.abs(v) > Math.abs(v1)) {
                    updateTranslateYProgress(-step * 1.5f);
                }
            }
            return true;
        }
        return super.onScroll(motionEvent, motionEvent1, v, v1);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.setDrawFilter(mPaintFlagsDrawFilter);
        drawTrueBgVirtualWithCanvas(canvas);
        drawDiaphragm(canvas, isScreenPortrait(), mTranslateYProgress, mCenterX, mCenterY);
    }

    private void drawTrueBgVirtualWithCanvas(Canvas canvas) {
        int previewSize = Math.min((int) mPreviewArea.width(), (int) mPreviewArea.height());
        boolean useTransform = isNeedTransform(previewSize);
        float aspectScale = useTransform ? 2.0f : 1.0f;
        if (mBlueBgBitmap != null) {
            BitmapPoolManager.I.put(mBlueBgBitmap);
        }
        mBlueBgBitmap = mTransformHelp.getData();
        if (mBlueBgBitmap != null) {
            canvas.drawBitmap(mBlueBgBitmap, getMatrix(aspectScale), null);
        }
    }

    private void drawDiaphragm(Canvas canvas, boolean isPortrait, float progress, float centerX, float centerY) {
        mRotationMatrix.setScale(0.9f, 0.9f, mCircleOutRadius, mCircleOutRadius);
        mRotationMatrix.postTranslate(centerX - mCircleOutRadius, centerY - mCircleOutRadius);
        canvas.drawBitmap(mCircleOutGreen, mRotationMatrix, null);
        PointF pointF = completeSeekBarCoordinate(isPortrait, progress, centerX, centerY);
        canvas.drawBitmap(mSeekBarPoint, pointF.x - mSphereRadius, pointF.y - mSphereRadius, null);
        final float l = progress * mSeekBarLineRadius * 2;
        if (isPortrait) {
            if (pointF.y > centerY - (mSeekBarLineRadius - mSphereRadius)) {
                canvas.drawLine(pointF.x, pointF.y - l, pointF.x, pointF.y - mSphereRadius, mPaint);
            }
            if (pointF.y < centerY + (mSeekBarLineRadius - mSphereRadius)) {
                canvas.drawLine(pointF.x, pointF.y + mSphereRadius, pointF.x, pointF.y + mSeekBarLineRadius * 2 - l, mPaint);
            }
        } else {
            if (pointF.x > centerX - (mSeekBarLineRadius - mSphereRadius)) {
                canvas.drawLine(pointF.x - l, pointF.y, pointF.x - mSphereRadius, pointF.y, mPaint);
            }
            if (pointF.x < centerX + (mSeekBarLineRadius - mSphereRadius)) {
                canvas.drawLine(pointF.x + mSphereRadius, pointF.y, pointF.x + mSeekBarLineRadius * 2 - l, pointF.y, mPaint);
            }
        }

        if (mIsInSeekBarArea) {
            canvas.save();
            canvas.translate(centerX - mCircleOutRadius, centerY - mCircleOutRadius);
            canvas.setDrawFilter(mPaintFlagsDrawFilter);
            if (Build.VERSION.SDK_INT >= 28) {
                canvas.clipPath(mPath);
            } else {
                canvas.clipPath(mPath, Region.Op.REPLACE);
            }

            for (int i = 0; i < 360; i += 60) {
                progress = progress < 0.1f ? 0.1f : Math.min(progress, 0.9f);
                mPoint[0] = -mCircleInRadius * (1 - progress);
                mPoint[1] = 0;
                mRotationMatrix.setRotate(i, mCircleOutRadius, mCircleOutRadius);
                mTranslateMatrix.setRotate(i + 120);
                mTranslateMatrix.mapPoints(mPoint);
                mRotationMatrix.postTranslate(mPoint[0], mPoint[1]);
                canvas.drawBitmap(mPartBitmap, mRotationMatrix, mPaint);
            }
            canvas.restore();
        }
    }

    private PointF completeSeekBarCoordinate(boolean isPortrait, float progress, float centerX, float centerY) {
        int minSize = (int) (mCircleOutRadius + mPartRadius + mSphereRadius * 2);
        float position = mSeekBarLineRadius * 2 * Math.abs(progress - 0.5f) * (progress < 0.5f ? -1 : 1);
        if (isPortrait) {
            mPointF.x = centerX + (mCircleOutRadius + mPartRadius + mSphereRadius) * (getMeasuredWidth() - centerX < minSize ? -1 : 1);
            mPointF.y = centerY + position;
        } else {
            mPointF.x = centerX + position;
            mPointF.y = centerY + (mCircleOutRadius + mPartRadius + mSphereRadius) * (getMeasuredHeight() - centerY < minSize ? -1 : 1);
        }
        return mPointF;
    }

    private boolean isScreenPortrait() {
        return mOrientation != 90 && mOrientation != 270;
    }

    private boolean isNeedTransform(int previewSize) {
        return previewSize > SUPPORT_MAX_ASPECT_SIZE;
    }

    private int getBlurDegree(float progress) {
        float degree = 1 - progress;
        degree = degree < 0.1f ? 0.1f : Math.min(degree, 0.8f);
        return (int) (degree * 10);
    }

    private Matrix getMatrix(float aspectScale) {
        if (mMatrix == null) {
            mMatrix = new Matrix();
        }
        //mMatrix.setScale(aspectScale, aspectScale);
        return mMatrix;
    }

    private void setIsInSeekBarArea(boolean newValue) {
        if (mIsInSeekBarArea != newValue) {
            mIsInSeekBarArea = newValue;
        }
    }

    private void setCenterPoint(float pointX, float pointY) {

        if (pointX < (mCircleOutRadius + mPartRadius)) {
            pointX = (mCircleOutRadius + mPartRadius);
        } else if (pointX > (getMeasuredWidth() - (mCircleOutRadius + mPartRadius))) {
            pointX = getMeasuredWidth() - (mCircleOutRadius + mPartRadius);
        }

        if (pointY < (mSeekBarLineRadius + mSphereRadius)) {
            pointY = mSeekBarLineRadius + mSphereRadius;
        } else if (pointY > getMeasuredHeight() - (mSeekBarLineRadius + mSphereRadius)) {
            pointY = getMeasuredHeight() - (mSeekBarLineRadius + mSphereRadius);
        }

        if (mCenterX != pointX || mCenterY != pointY) {
            mCenterX = pointX;
            mCenterY = pointY;
        }
    }


    private void setSinglePoint(int singleX, int singleY) {
        if (mOnSingleX != singleX || mOnSingleY != singleY) {
            mOnSingleX = singleX;
            mOnSingleY = singleY;
        }
    }

    private void updateTranslateYProgress(float changeValue) {
        mTranslateYProgress += changeValue;
        if (mTranslateYProgress < 0f) {
            mTranslateYProgress = 0f;
        } else if (mTranslateYProgress > 1f) {
            mTranslateYProgress = 1f;
        }
    }

    @Override
    public void onPreviewAreaChanged(RectF previewArea) {
        super.onPreviewAreaChanged(previewArea);
        if (mPreviewArea != previewArea) {
            mPreviewArea.set(previewArea);
            mPreviewAreaWidth = Math.round(previewArea.right - previewArea.left);
            mPreviewAreaHeight = Math.round(previewArea.bottom - previewArea.top);
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) getLayoutParams();
            params.setMargins((int) mPreviewArea.left, (int) mPreviewArea.top, 0, 0);
            setLayoutParams(params);
        }
    }

    public void setPreviewData(byte[] nv21Data, int dataWidth, int dataHeight) {
        setPreviewData(nv21Data, dataWidth, dataHeight, mPreviewAreaHeight, mPreviewAreaWidth);
    }

    private void setPreviewData(byte[] nv21Data, int dataWidth, int dataHeight, int targetWidth, int targetHeight) {
        if (mBVirtualHolders != null) {
            int previewSize = Math.min((int) mPreviewArea.width(), (int) mPreviewArea.height());
            mBlurDegree = getBlurDegree(mTranslateYProgress);
            BVirtualHolder holder;
            if (mBVirtualHolders.size() < 2) {
                holder = new BVirtualHolder(nv21Data, dataWidth, dataHeight, targetWidth, targetHeight, mBlurDegree,
                        mBlurInfo.update(mOnSingleX, mOnSingleY,
                                (int) (IN_SHARPNESS_RADIUS_SCALE * previewSize),
                                (int) (OUT_SHARPNESS_RADIUS_SCALE * previewSize)
                        ));
            } else {
                holder = mBVirtualHolders.poll();
                if (holder != null) {
                    holder.update(nv21Data, dataWidth, dataHeight, targetWidth, targetHeight, mBlurDegree,
                            mBlurInfo.update(mOnSingleX, mOnSingleY,
                                    (int) (IN_SHARPNESS_RADIUS_SCALE * previewSize),
                                    (int) (OUT_SHARPNESS_RADIUS_SCALE * previewSize)
                            ));
                } else {
                    holder = new BVirtualHolder(nv21Data, dataWidth, dataHeight, targetWidth, targetHeight, mBlurDegree,
                            mBlurInfo.update(mOnSingleX, mOnSingleY,
                                    (int) (IN_SHARPNESS_RADIUS_SCALE * previewSize),
                                    (int) (OUT_SHARPNESS_RADIUS_SCALE * previewSize)
                            ));
                }
            }
            mBVirtualHolders.offer(holder);
            mTransformHelp.setData(holder);
        }
    }

    private Bitmap getPreviewBitmap(final BVirtualHolder holder) {
        if (holder != null) {
            if (holder.getData() != null && holder.getDataWidth() > 0 && holder.getDataHeight() > 0
                    && (holder.getTargetWidth() > 0 && holder.getTargetHeight() > 0
                    || holder.getTargetWidth() == -1 && holder.getTargetHeight() == -1)) {
                return holder.getTargetWidth() == -1
                        ? nv21ToBitmap(holder.getData(), holder.getDataWidth(), holder.getDataHeight())
                        : nv21ToBitmap(holder.getData(), holder.getDataWidth(), holder.getDataHeight(),
                        holder.getTargetWidth(), holder.getTargetHeight());
            }
        }
        return null;
    }

    private Bitmap getSmoothBlurPreviewBitmap(final BVirtualHolder holder) {
        Bitmap blueBgBitmap = null;
        Bitmap previewBitmap = getPreviewBitmap(holder);
        if (previewBitmap != null) {
            blueBgBitmap = BlurUtils.I.getBitmap(mContext, previewBitmap, holder.getBlurDegree());
            if (holder.getBlurInfo() != null) {
                SmoothBlurJni.smoothRender(blueBgBitmap, previewBitmap, holder.getBlurInfo());
                BitmapPoolManager.I.put(previewBitmap);
            }
        }
        return blueBgBitmap;
    }

    private void setBitmapSize(int width, int height) {
        this.mWidth = width;
        this.mHeight = height;
    }

    private Bitmap nv21ToBitmap(final byte[] data, int dataWidth, int dataHeight) {
        return nv21ToBitmap(data, dataWidth, dataHeight, dataWidth, dataHeight);
    }

    private Bitmap nv21ToBitmap(final byte[] data, int dataWidth, int dataHeight, int targetWidth, int targetHeight) {
        long startTime = System.currentTimeMillis();
        //*/ 平均每帧处理时间　5ms
        if (mWidth != dataWidth || mHeight != dataHeight) {
            setBitmapSize(targetWidth, targetHeight);
            mRGBAArray = new byte[targetWidth * targetHeight * 4];
            YUVManager.I.update(dataWidth, dataHeight, targetWidth, targetHeight);
        }
        if (dataWidth == targetWidth && dataHeight == targetHeight) {
            YUVManager.I.transformNV21ToRGBA(mRGBAArray, data, dataWidth, dataHeight, 90);
        } else {
            YUVManager.I.compressNV21ToRGBA(mRGBAArray, data, dataWidth, dataHeight, targetWidth, targetHeight, 90);
        }
        Bitmap bitmap = BitmapPoolManager.I.get(targetHeight, targetWidth, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(mRGBAArray));
        Log.d(TAG, "nv21ToBitmap cost: " + (System.currentTimeMillis() - startTime));
        return bitmap;
    }

    public byte[] blendJpegData(final byte[] jpegData) {
        return blendJpegData(jpegData, new BVirtualHolder(mCurrentBVirtualHolder));
    }

    private byte[] blendJpegData(final byte[] jpegData, BVirtualHolder holder) {
        synchronized (BVirtualView.this) {
            Bitmap src_bitmap = decodeByteArray(jpegData);
            if (src_bitmap == null) {
                Log.e(TAG, "src_bitmap = null, blendOutput() fail!");
                return null;
            }

            final int previewSize = (int) Math.min(holder.getDataWidth(), holder.getDataHeight());
            final int pictureSize = Math.min(src_bitmap.getWidth(), src_bitmap.getHeight());
            final float pictureScale = pictureSize / (float) previewSize;

            Bitmap bgBlurBitmap = BlurUtils.I.getBitmap(mContext, src_bitmap, holder.getBlurDegree(), previewSize);
            SmoothBlurJni.smoothRender(
                    bgBlurBitmap,
                    src_bitmap,
                    holder.getBlurInfo().update(
                            Math.round(holder.getBlurInfo().x * pictureScale),
                            Math.round(holder.getBlurInfo().y * pictureScale),
                            Math.round(holder.getBlurInfo().inRadius * pictureScale),
                            Math.round(holder.getBlurInfo().outRadius * pictureScale)
                    )
            );

            Canvas canvas = new Canvas(bgBlurBitmap);
            canvas.save(Canvas.ALL_SAVE_FLAG);
            canvas.restore();
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            bgBlurBitmap.compress(Bitmap.CompressFormat.JPEG, 95, os);
            byte[] retData = os.toByteArray();
            src_bitmap.recycle();
            BitmapPoolManager.I.put(bgBlurBitmap);
            return retData;
        }
    }

    private Bitmap decodeByteArray(final byte[] jpegData) {
        Bitmap src_bitmap;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inMutable = true;
            src_bitmap = BitmapFactory.decodeByteArray(jpegData, 0, jpegData.length, options);
        } catch (OutOfMemoryError e) {
            Log.e(TAG, "blendOutput() fail!", e);
            return null;
        }
        return src_bitmap;
    }
}
