package com.freeme.camera.ui;


import android.content.Context;
import android.os.Handler;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import com.android.camera.debug.Log;
import com.android.camera.ui.PreviewOverlay;

import java.util.ArrayList;
import java.util.List;

import com.android.camera.CameraActivity;
import com.android.camera.util.CameraUtil;


public class ModeScrollView extends HorizontalScrollView implements
        View.OnClickListener, View.OnTouchListener{

    private Handler handler;
    private static final Log.Tag TAG = new Log.Tag("ModeScrollView");
    private ModeScrollAdapter mAdapter;
    List<RecyclerView.ViewHolder> mViewHolders = new ArrayList<>();
    private final int mSlop;

    private int mHVSCurrentIndex = 0;

    private float mTouchDownX;
    private float mTouchDownY;

    private long nowScrollLeft = -1;

    private boolean isScrolling = false;
    private boolean isScrollable = true;
    private PreviewOverlay mPreviewOverlay;

    public ModeScrollView(Context context) {
        super(context);
        mSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }


    public ModeScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    public ModeScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override
    public void addView(View child) {
        super.addView(child);
    }

    public void scrollLeft() {
        //move to left
        boolean isRTL = (View.LAYOUT_DIRECTION_RTL == this.getResources().getConfiguration().getLayoutDirection());
        if ((isRTL ? (mAdapter == null || mHVSCurrentIndex >= mAdapter.getCount() - 1)
                : (mHVSCurrentIndex <= 0)) || !getCanScrollable()) {
            return;
        }
        final int scrollToIndex = isRTL ? mHVSCurrentIndex + 1 : mHVSCurrentIndex - 1;
        int position = getChildCenterPosition(scrollToIndex);
        setCurrent(scrollToIndex, position, true);
    }

    public void scrollRight() {
        boolean isRTL = (View.LAYOUT_DIRECTION_RTL == this.getResources().getConfiguration().getLayoutDirection());
        if ((isRTL ? (mHVSCurrentIndex <= 0)
                : (mAdapter == null || mHVSCurrentIndex >= mAdapter.getCount() - 1))
                || !getCanScrollable()) {
            return;
        }
        //move to right
        final int scrollToIndex = isRTL ? mHVSCurrentIndex - 1 : mHVSCurrentIndex + 1;
        int position = getChildCenterPosition(scrollToIndex);
        setCurrent(scrollToIndex, position, true);
    }

    public void smoothScrollAfterFreeze(final int smoothToPosition) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                smoothScrollTo(smoothToPosition, 0);
            }
        });
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }


    public void setAdapter(final ModeScrollAdapter adapter) {
        if(adapter == null || adapter.getCount() == 0){
            return;
        }
        mViewHolders.clear();
        removeAllViews();
        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setVerticalGravity(Gravity.CENTER);

        int size = adapter.getCount();
        for (int i = 0; i < size; i++) {
            mViewHolders.add(adapter.getItemView(i));
            mViewHolders.get(i).itemView.setTag(i);
            mViewHolders.get(i).itemView.setOnClickListener(ModeScrollView.this);
            linearLayout.addView(mViewHolders.get(i).itemView);
        }
        this.mAdapter = adapter;//bug 1395989 must be set after view holder added. or setCurrent may index out of bounds exception
        addView(linearLayout);
        init();
    }

    @Override
    public void onClick(View view) {
        if (!getCanScrollable() || (mPreviewOverlay != null && mPreviewOverlay.isZooming())) return;
        final int index = (int) view.getTag();
        final int position = getChildCenterPosition(index);
        Log.d(TAG, "onClick view=" + view + ", position=" + position);
        if(this.mHVSCurrentIndex == index){
            return;
        }
        setCurrent(index, position, true);
    }

    public void setScrollable(boolean isScrollabled) {
        isScrollable = isScrollabled;
    }


    private int getChildCenterPosition(int modeIndex) {
        if (getChildCount() <= 0) {
            return 0;
        }
        ViewGroup viewGroup = (ViewGroup) getChildAt(0);
        if (viewGroup == null || viewGroup.getChildCount() == 0) {
            return 0;
        }
        int offset_tmp = 0;
        boolean isRTL = (View.LAYOUT_DIRECTION_RTL == this.getResources().getConfiguration().getLayoutDirection());
        int groupChildCount = viewGroup.getChildCount();
        int indexInParent = isRTL ? (groupChildCount - 2/**/) - modeIndex : modeIndex + 1;
        int numCompute = indexInParent + 1;

        int startComputeIndex = isRTL ? groupChildCount - 1 : 0;
        int secondComputeIndex = isRTL ? startComputeIndex - 1 : startComputeIndex + 1;
        int offset_target = 0;
        for (int i = 0; i< numCompute; i++) {
            View child = viewGroup.getChildAt(isRTL ? startComputeIndex - i : i);
            int child_width = child.getWidth();
            int childStartW = 0;
            int childSecondW = 0;
            if (0 == child_width) {
                int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                child.measure(w, h);
                child_width = child.getMeasuredWidth();
                childStartW = viewGroup.getChildAt(startComputeIndex).getMeasuredWidth();
                childSecondW = viewGroup.getChildAt(secondComputeIndex).getMeasuredWidth();
            } else {
                childStartW = viewGroup.getChildAt(startComputeIndex).getWidth();
                childSecondW = viewGroup.getChildAt(secondComputeIndex).getWidth();
            }
            offset_tmp = offset_tmp + child_width;
            if (i == indexInParent) {
                offset_target = offset_tmp - child_width / 2 - childStartW - childSecondW / 2;
                return offset_target;
            }
        }

        return 0;
    }

    private boolean mScaleStarted = false;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {

        if (!getCanScrollable()) return false;
        if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN) {
            mTouchDownX = motionEvent.getX();
            mTouchDownY = motionEvent.getY();
        }
        return super.onInterceptTouchEvent(motionEvent);
    }

    private boolean getCanScrollable() {
        boolean result = !((CameraActivity) getContext()).isModeTransitionViewShow() && !((CameraActivity) getContext()).getCameraAppUI().unResponseClick()
                && isScrollable && (View.VISIBLE == getVisibility());
        return result;
    }
    @Override
    public boolean onTouch(View v, MotionEvent motionEvent) {
        if (!getCanScrollable()) return false;
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            isScrolling = true;
            mTouchDownX = motionEvent.getX();
            mTouchDownY = motionEvent.getY();
        }
        if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
            isScrolling = true;
        }
        if (motionEvent.getAction() == MotionEvent.ACTION_UP || motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {

            isScrolling = false;
            int deltaX = (int) (motionEvent.getX() - mTouchDownX);
            int deltaY = (int) (motionEvent.getY() - mTouchDownY);
            if (Math.abs(deltaX) > mSlop || Math.abs(deltaY) > mSlop) {
                // Calculate the direction of the swipe.
                if (deltaX >= Math.abs(deltaY)) {
                    // Swipe right.
                    scrollLeft();
                } else if (deltaX <= -Math.abs(deltaY)) {
                    // Swipe left.
                    scrollRight();
                }
            }
        }
        return true;

        // return (!mScaleStarted) && mGestureDetector.onTouchEvent(event);
    }

    /**
     * Sets a listener that listens to receive mode switch event.
     *
     * @param listener a listener that gets notified when mode changes.
     */
    public void setModeScrollListener(ModeScrollListener listener) {
        mModeScrollListener = listener;
    }

    public interface ModeScrollListener {
        public void onModeScrolled(int modeIndex);
    }

    private ModeScrollListener mModeScrollListener = null;

    private void onItemSelected(int index) {
        //int modeId = mModeScrollItems.get(index);
        //mModeScrollListener.onModeScrolled(modeId);
    }

    private void init() {
        setOnTouchListener(this);

        if (getChildCount() <= 0) {
            return;
        }
        ViewGroup viewGroup = (ViewGroup) getChildAt(0);
        if (viewGroup == null || viewGroup.getChildCount() == 0) {
            return;
        }
        //set padding, the first itemview and the last auto center
        View first = viewGroup.getChildAt(0);
        int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        first.measure(w, h);
        int first_width = first.getMeasuredWidth();
        View last = viewGroup.getChildAt(viewGroup.getChildCount() - 1);
        last.measure(w, h);
        int last_width = last.getMeasuredWidth();
        int mPaddingLeft = getScreenWidth(getContext()) / 2 - first_width /2;
        int mPaddingRight = getScreenWidth(getContext()) / 2 - last_width / 2;

        View startPaddingView = new View(getContext());
        final LayoutParams paramsL = generateDefaultLayoutParams();
        paramsL.width = mPaddingLeft;
        paramsL.height = first.getMeasuredHeight();
        viewGroup.addView(startPaddingView, 0, paramsL);

        View endPaddingView = new View(getContext());
        final LayoutParams paramsR = generateDefaultLayoutParams();
        paramsR.width = mPaddingRight;
        paramsR.height = first.getMeasuredHeight();
        viewGroup.addView(endPaddingView, paramsR);
        setCurrentIndex(mHVSCurrentIndex, false);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        nowScrollLeft = l;

    }

    @Override
    protected int computeHorizontalScrollRange() {
        int range = super.computeHorizontalScrollRange();
        return range;
    }

    @Override
    protected int computeHorizontalScrollOffset() {
        return super.computeHorizontalScrollOffset();
    }

    public void accordingToModuleIndexSmoothToPosition(int moduleIndex) {
        int index = CameraUtil.mModuleInfoResolve.getModuleScrollAdapterIndex(moduleIndex);
        int position = getChildCenterPosition(index);
        setCurrent(index, position, true);
    }

    private void setCurrent(int currentIndex, int smoothToPosition, boolean withCallBackable) {
        if (mAdapter != null && mAdapter.getCount() > 0 && currentIndex < mAdapter.getCount()) {
            mAdapter.onSelectStateChanged(mViewHolders.get(mHVSCurrentIndex), mHVSCurrentIndex, false);
            int lastIndex = mHVSCurrentIndex;
            this.mHVSCurrentIndex = currentIndex;
            mAdapter.onSelectStateChanged(mViewHolders.get(currentIndex), currentIndex, true);
            if (withCallBackable && onSelectChangeListener != null) {
                onSelectChangeListener.onSelectChange(lastIndex,currentIndex, smoothToPosition);
            }
        }
    }

    public void setCurrentIndex(int modeIndex, boolean withCallBackable) {
        setCurrent(modeIndex, 0, withCallBackable);
        if (getChildCount() <= 0) {
            return ;
        }
        ViewGroup viewGroup = (ViewGroup) getChildAt(0);
        if (viewGroup == null || viewGroup.getChildCount() == 0) {
            return ;
        }

        final int offset_target = getChildCenterPosition(modeIndex);
        smoothScrollAfterFreeze(offset_target);

    }

    public int getCurrentIndex() {
        return mHVSCurrentIndex;
    }

    public OnSelectChangeListener onSelectChangeListener;

    public void setOnSelectChangeListener(OnSelectChangeListener onSelectChangeListener) {
        this.onSelectChangeListener = onSelectChangeListener;
    }

    public static interface OnSelectChangeListener {
        void onSelectChange(int lastPosition, int position, int smoothToPosition);
    }

    public static int getScreenWidth(Context context) {
        //int switchMargin = context.getResources().getDimensionPixelSize(R.dimen.mode_switch_margin_left_right);//12dp
        return getDisplayMetrics(context).widthPixels;
    }

    public static DisplayMetrics getDisplayMetrics(Context context) {
        return context.getResources().getDisplayMetrics();
    }

    public void setHandler(Handler handler){
        this.handler = handler;
    }

    public void setPreviewOverlay(PreviewOverlay previewOverlay){
        mPreviewOverlay = previewOverlay;
    }
}
