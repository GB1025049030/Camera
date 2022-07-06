package com.freeme.camera.ui;

import android.content.Context;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.android.camera.CaptureLayoutHelper;
import com.android.camera.debug.Log;
import com.android.camera2.R;


/**
 * The goal of this class is to ensure mode options and capture indicator is
 * always laid out to the left of or above bottom bar in landscape or portrait
 * respectively. All the other children in this view group can be expected to
 * be laid out the same way as they are in a normal FrameLayout.
 */
public class StickyBottomModeScrollLayout extends FrameLayout {

    private final static Log.Tag TAG = new Log.Tag("StickyBotScrollLayout");
    private View mBottomBar;
    private ModeScrollView mBottomModeSwitchLayout;
    private LinearLayout mBottomModeSingleLayout;
    private LinearLayout mBottomPanel;
    private FrameLayout mBottomCancel;
    private CaptureLayoutHelper mCaptureLayoutHelper = null;
    private int mBottomModeSwitchHeight;

    public StickyBottomModeScrollLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
        mBottomModeSwitchHeight = getResources().getDimensionPixelSize(R.dimen.dream2_bottom_mode_switch_height);
        mBottomModeSwitchLayout = (ModeScrollView)findViewById(R.id.bottom_mode_switch);
        mBottomModeSingleLayout = (LinearLayout)findViewById(R.id.bottom_modeid_show_layout);
    }

    /**
     * Sets a capture layout helper to query layout rect from.
     */
    public void setCaptureLayoutHelper(CaptureLayoutHelper helper) {
        mCaptureLayoutHelper = helper;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (mCaptureLayoutHelper == null) {
            Log.e(TAG, "Capture layout helper needs to be set first.");
            return;
        }

        // Layout bottom bar.
        RectF bottomBarRect = mCaptureLayoutHelper.getBottomBarRect();
        //int switchMargin = getResources().getDimensionPixelSize(R.dimen.mode_switch_margin_left_right);//12dp
        mBottomModeSwitchLayout.layout((int) bottomBarRect.left, (int) bottomBarRect.top, (int) bottomBarRect.right, (int) bottomBarRect.top + mBottomModeSwitchHeight);
        mBottomModeSwitchLayout.setPaddingRelative(mBottomModeSwitchLayout.getPaddingStart(), mBottomModeSwitchLayout.getPaddingTop(), mBottomModeSwitchLayout.getPaddingEnd(), mBottomModeSwitchLayout.getPaddingBottom());

        int modeTextBgPaddingH = getResources().getDimensionPixelSize(R.dimen.mode_switch_padding_top);//24dp
        View modeText = findViewById(R.id.bottom_modeid_show_text);
        View modeCancel = findViewById(R.id.bottom_modeid_show_cancel);

        int modeTextW = modeText.getMeasuredWidth() + modeCancel.getMeasuredWidth();
        int modeShowLeft = ((int) bottomBarRect.width() - modeTextW) / 2;
        mBottomModeSingleLayout.layout(modeShowLeft, (int) bottomBarRect.top + modeTextBgPaddingH, modeShowLeft + modeTextW, (int) bottomBarRect.top + mBottomModeSwitchHeight);

    }

}