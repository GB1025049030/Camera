/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.camera.ui;

import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.android.camera.settings.SettingsScopeNamespaces;
import com.android.camera2.R;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.SystemClock;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.TextView;

import com.android.camera.CameraActivity;
import com.android.camera.CaptureLayoutHelper;
import com.android.camera.app.CameraAppUI;
import com.android.camera.debug.Log;
import com.android.camera.util.AndroidServices;
import com.android.camera.util.CameraUtil;
import com.android.camera.util.Gusterpolator;
import com.android.camera.widget.AnimationEffects;
import com.android.camera.widget.SettingsCling;
import com.dream.camera.DreamOrientation;
import com.freeme.camera.common.custom.CameraCustomManager;


/**
 * ModeListView class displays all camera modes and settings in the form
 * of a list. A swipe to the right will bring up this list. Then tapping on
 * any of the items in the list will take the user to that corresponding mode
 * with an animation. To dismiss this list, simply swipe left or select a mode.
 */
/*
 * SPRD Bug:519334 Refactor Rotation UI of Camera. @{
 * Original Android code:

public class ModeListView extends FrameLayout
        implements ModeSelectorItem.VisibleWidthChangedListener,
        PreviewStatusListener.PreviewAreaChangedListener {

 */
public class ModeListView extends FrameLayout
        implements ModeSelectorItem.VisibleWidthChangedListener,
        PreviewStatusListener.PreviewAreaChangedListener, Rotatable {

    private static final Log.Tag TAG = new Log.Tag("ModeListView");

    // Animation Durations
    private static final int DEFAULT_DURATION_MS = 200;
    private static final int FLY_IN_DURATION_MS = 0;
    private static final int HOLD_DURATION_MS = 0;
    private static final int FLY_OUT_DURATION_MS = 850;
    private static final int START_DELAY_MS = 100;
    private static final int TOTAL_DURATION_MS = FLY_IN_DURATION_MS + HOLD_DURATION_MS
            + FLY_OUT_DURATION_MS;
    private static final int HIDE_SHIMMY_DELAY_MS = 1000;
    // Assumption for time since last scroll when no data point for last scroll.
    private static final int SCROLL_INTERVAL_MS = 50;
    // Last 20% percent of the drawer opening should be slow to ensure soft landing.
    private static final float SLOW_ZONE_PERCENTAGE = 0.2f;

    private static final int NO_ITEM_SELECTED = -1;

    // Scrolling delay between non-focused item and focused item
    private static final int DELAY_MS = 30;
    // If the fling velocity exceeds this threshold, snap to full screen at a constant
    // speed. Unit: pixel/ms.
    private static final float VELOCITY_THRESHOLD = 2f;

    /**
     * A factor to change the UI responsiveness on a scroll.
     * e.g. A scroll factor of 0.5 means UI will move half as fast as the finger.
     */
    private static final float SCROLL_FACTOR = 0.5f;
    // 60% opaque black background.
    private static final int BACKGROUND_TRANSPARENTCY = (int) (0.6f * 255);
    private static final int PREVIEW_DOWN_SAMPLE_FACTOR = 4;
    // Threshold, below which snap back will happen.
    private static final float SNAP_BACK_THRESHOLD_RATIO = 0.33f;

    private static final int WIDEANGLE_MODE_INDEX = 4;//SPRD:fix bug474690
    private final GestureDetector mGestureDetector;
    private final CurrentStateManager mCurrentStateManager = new CurrentStateManager();
    private final int mSettingsButtonMargin;
    private long mLastScrollTime;
    private int mListBackgroundColor;
    //private GridLayout mListView;
    private RecyclerView mListView;
    private TextView mTitle;
    private View mSettingsButton;
    private int mTotalModes;
    //private ModeSelectorItem[] mModeSelectorItems;
    private LinkedHashMap<Integer,ModeSelectorItem> mModeSelectorItems;
    private AnimatorSet mAnimatorSet;
    private int mFocusItem = NO_ITEM_SELECTED;
    private ModeListOpenListener mModeListOpenListener;
    private ModeListVisibilityChangedListener mVisibilityChangedListener;
    private CameraAppUI.CameraModuleScreenShotProvider mScreenShotProvider = null;
    private int[] mInputPixels;
    private int[] mOutputPixels;
    private float mModeListOpenFactor = 1f;

    private View mChildViewTouched = null;
    private MotionEvent mLastChildTouchEvent = null;
    private int mVisibleWidth = 0;

    // Width and height of this view. They get updated in onLayout()
    // Unit for width and height are pixels.
    private int mWidth;
    private int mHeight;
    private float mScrollTrendX = 0f;
    private float mScrollTrendY = 0f;
    private ModeSwitchListener mModeSwitchListener = null;
    private ArrayList<Integer> mSupportedModes;
    private final LinkedList<TimeBasedPosition> mPositionHistory
            = new LinkedList<TimeBasedPosition>();
    private long mCurrentTime;
    private float mVelocityX; // Unit: pixel/ms.
    private long mLastDownTime = 0;
    private CaptureLayoutHelper mCaptureLayoutHelper = null;
    private SettingsCling mSettingsCling = null;
    private boolean mTouchable = true;//SPRD:Fix bug428712
    private int mCount = 0;

    private class CurrentStateManager {
        private ModeListState mCurrentState;

        ModeListState getCurrentState() {
            return mCurrentState;
        }

        void setCurrentState(ModeListState state) {
            mCurrentState = state;
            state.onCurrentState();
        }
    }

    /**
     * ModeListState defines a set of functions through which the view could manage
     * or change the states. Sub-classes could selectively override these functions
     * accordingly to respect the specific requirements for each state. By overriding
     * these methods, state transition can also be achieved.
     */
    private abstract class ModeListState implements GestureDetector.OnGestureListener {
        protected AnimationEffects mCurrentAnimationEffects = null;

        /**
         * Called by the state manager when this state instance becomes the current
         * mode list state.
         */
        public void onCurrentState() {
            // Do nothing.
            showSettingsClingIfEnabled(false);
        }

        /**
         * If supported, this should show the mode switcher and starts the accordion
         * animation with a delay. If the view does not currently have focus, (e.g.
         * There are popups on top of it.) start the delayed accordion animation
         * when it gains focus. Otherwise, start the animation with a delay right
         * away.
         */
        public void showSwitcherHint() {
            // Do nothing.
        }

        /**
         * Gets the currently running animation effects for the current state.
         */
        public AnimationEffects getCurrentAnimationEffects() {
            return mCurrentAnimationEffects;
        }

        /**
         * Returns true if the touch event should be handled, false otherwise.
         *
         * @param ev motion event to be handled
         * @return true if the event should be handled, false otherwise.
         */
        public boolean shouldHandleTouchEvent(MotionEvent ev) {
            return true;
        }

        /**
         * Handles touch event. This will be called if
         * {@link ModeListState#shouldHandleTouchEvent(android.view.MotionEvent)}
         * returns {@code true}
         *
         * @param ev touch event to be handled
         * @return always true
         */
        public boolean onTouchEvent(MotionEvent ev) {
            return true;
        }

        /**
         * Gets called when the window focus has changed.
         *
         * @param hasFocus whether current window has focus
         */
        public void onWindowFocusChanged(boolean hasFocus) {
            // Default to do nothing.
        }

        /**
         * Gets called when back key is pressed.
         *
         * @return true if handled, false otherwise.
         */
        public boolean onBackPressed() {
            return false;
        }

        /**
         * Gets called when menu key is pressed.
         *
         * @return true if handled, false otherwise.
         */
        public boolean onMenuPressed() {
            return false;
        }

        /**
         * Gets called when there is a {@link View#setVisibility(int)} call to
         * change the visibility of the mode drawer. Visibility change does not
         * always make sense, for example there can be an outside call to make
         * the mode drawer visible when it is in the fully hidden state. The logic
         * is that the mode drawer can only be made visible when user swipe it in.
         *
         * @param visibility the proposed visibility change
         * @return true if the visibility change is valid and therefore should be
         *         handled, false otherwise.
         */
        public boolean shouldHandleVisibilityChange(int visibility) {
            return true;
        }

        /**
         * If supported, this should start blurring the camera preview and
         * start the mode switch.
         *
         * @param selectedItem mode item that has been selected
         */
        public void onItemSelected(ModeSelectorItem selectedItem) {
            // Do nothing.
        }

        /**
         * This gets called when mode switch has finished and UI needs to
         * pinhole into the new mode through animation.
         */
        public void startModeSelectionAnimation() {
            // Do nothing.
        }

        /**
         * Hide the mode drawer and switch to fully hidden state.
         */
        public void hide() {
            // Do nothing.
        }

        /**
         * Hide the mode drawer (with animation, if supported)
         * and switch to fully hidden state.
         * Default is to simply call {@link #hide()}.
         */
        public void hideAnimated() {
            hide();
        }

        /***************GestureListener implementation*****************/
        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {
            // Do nothing.
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            // Do nothing.
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }
    }

    /**
     * Fully hidden state. Transitioning to ScrollingState and ShimmyState are supported
     * in this state.
     */
    private class FullyHiddenState extends ModeListState {
        private Animator mAnimator = null;
        private boolean mShouldBeVisible = false;

        public FullyHiddenState() {
            reset();
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            mShouldBeVisible = true;
            // Change visibility, and switch to scrolling state.
            resetModeSelectors();
            //mCurrentStateManager.setCurrentState(new ScrollingState());
            return true;
        }

        @Override
        public void showSwitcherHint() {
            mShouldBeVisible = true;
            mCurrentStateManager.setCurrentState(new ShimmyState());
        }

        @Override
        public boolean shouldHandleTouchEvent(MotionEvent ev) {
            return true;
        }

        @Override
        public boolean onTouchEvent(MotionEvent ev) {
            if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
                mFocusItem = getFocusItem(ev.getX(), ev.getY());
                setSwipeMode(true);
            }
            return true;
        }

        @Override
        public boolean onMenuPressed() {
            if (mAnimator != null) {
                return false;
            }
            //snapOpenAndShow();
            mShouldBeVisible =true;
            return true;
        }

        @Override
        public boolean shouldHandleVisibilityChange(int visibility) {
            if (mAnimator != null) {
                return false;
            }
            if (visibility == VISIBLE && !mShouldBeVisible) {
                return false;
            }
            return true;
        }

        private void updateOpenTag (boolean visible) {
            mShouldBeVisible = visible;
        }

        /**
         * Snaps open the mode list and go to the fully shown state.
         */
        private void snapOpenAndShow() {
            mShouldBeVisible = true;
            setVisibility(VISIBLE);

            mAnimator = snapToFullScreen();
            if (mAnimator != null) {
                mAnimator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mAnimator = null;
                        mCurrentStateManager.setCurrentState(new FullyShownState());
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
            } else {
                mCurrentStateManager.setCurrentState(new FullyShownState());
            }
        }

        @Override
        public void onCurrentState() {
            super.onCurrentState();
            announceForAccessibility(
                    getContext().getResources().getString(R.string.accessibility_mode_list_hidden));
            Log.i(TAG, "modelistview invisible");
        }
    }

    /**
     * Fully shown state. This state represents when the mode list is entirely shown
     * on screen without any on-going animation. Transitions from this state could be
     * to ScrollingState, SelectedState, or FullyHiddenState.
     */
    private class FullyShownState extends ModeListState {
        private Animator mAnimator = null;

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            // Go to scrolling state.
            /*if (distanceX > 0) {
                // Swipe out
                cancelForwardingTouchEvent();
                mCurrentStateManager.setCurrentState(new ScrollingState());
            }*/
            return true;
        }

        @Override
        public boolean shouldHandleTouchEvent(MotionEvent ev) {
            if (mAnimator != null && mAnimator.isRunning()) {
                return false;
            }
            return true;
        }

        @Override
        public boolean onTouchEvent(MotionEvent ev) {
            if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
                final LinearLayoutManager layoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
                int pagerPos = 0;
                if (null != layoutManager) {
                    pagerPos = layoutManager.findLastVisibleItemPosition();
                }
                int itemSelected = getFocusItemByPos(ev.getX(), ev.getY(), pagerPos);
                if (itemSelected != NO_ITEM_SELECTED) {
                    //ModeSelectorItem item = mModeSelectorItems[itemSelected];
                    ModeSelectorItem item = mModeSelectorItems.get(itemSelected);
                    if (item.isShown()) {
                        mChildViewTouched = item;
                    }
                }
            }
            forwardTouchEventToChild(ev);
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent ev) {
            return true;
        }

        @Override
        public boolean onBackPressed() {
            snapBackAndHide();
            return true;
        }

        @Override
        public boolean onMenuPressed() {
            snapBackAndHide();
            return true;
        }

        @Override
        public void onItemSelected(ModeSelectorItem selectedItem) {
            selectedItem.setSelected(true);
            Log.i(TAG, "FullyShownState onItemSelected");
            mCurrentStateManager.setCurrentState(new SelectedState(selectedItem));
        }

        /**
         * Snaps back the mode list and go to the fully hidden state.
         */
        private void snapBackAndHide() {
            mAnimator = snapBack(true);
            if (mAnimator != null) {
                mAnimator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mAnimator = null;
                        mCurrentStateManager.setCurrentState(new FullyHiddenState());
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
            } else {
                mCurrentStateManager.setCurrentState(new FullyHiddenState());
            }
        }

        @Override
        public void hide() {
            if (mAnimator != null) {
                mAnimator.cancel();
            } else {
                mCurrentStateManager.setCurrentState(new FullyHiddenState());
            }
        }

        @Override
        public void onCurrentState() {
            announceForAccessibility(
                    getContext().getResources().getString(R.string.accessibility_mode_list_shown));
            /**
             * SPRD:fix bug474690 add for pano
            showSettingsClingIfEnabled(true);
             */
            if (mModeSwitchListener != null && mModeSwitchListener.getCurrentModeIndex() != WIDEANGLE_MODE_INDEX) {
                showSettingsClingIfEnabled(true);
            }
        }
    }

    /**
     * Shimmy state handles the specifics for shimmy animation, including
     * setting up to show mode drawer (without text) and hide it with shimmy animation.
     *
     * This state can be interrupted when scrolling or mode selection happened,
     * in which case the state will transition into ScrollingState, or SelectedState.
     * Otherwise, after shimmy finishes successfully, a transition to fully hidden
     * state will happen.
     */
    private class ShimmyState extends ModeListState {

        private boolean mStartHidingShimmyWhenWindowGainsFocus = false;
        private Animator mAnimator = null;
        private final Runnable mHideShimmy = new Runnable() {
            @Override
            public void run() {
                startHidingShimmy();
            }
        };

        public ShimmyState() {
            setVisibility(VISIBLE);
            mSettingsButton.setVisibility(INVISIBLE);
            mModeListOpenFactor = 0f;
            onModeListOpenRatioUpdate(0);
            int moduleId = -1;
            moduleId = mSupportedModes.get(0);
            int maxVisibleWidth = mModeSelectorItems.get(moduleId).getMaxVisibleWidth();
            ModeSelectorItem item = null;
            Iterator ite = mModeSelectorItems.entrySet().iterator();
            while(ite.hasNext()){
                Map.Entry entry = (Map.Entry)ite.next();
                item = (ModeSelectorItem)entry.getValue();
                item.setVisibleWidth(maxVisibleWidth);
            }
            if (hasWindowFocus()) {
                hideShimmyWithDelay();
            } else {
                mStartHidingShimmyWhenWindowGainsFocus = true;
            }
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            // Scroll happens during accordion animation.
            cancelAnimation();
            cancelForwardingTouchEvent();
            // Go to scrolling state
            mCurrentStateManager.setCurrentState(new ScrollingState());
            return true;
        }

        @Override
        public boolean shouldHandleTouchEvent(MotionEvent ev) {
            if (MotionEvent.ACTION_DOWN == ev.getActionMasked()) {
                if (isTouchInsideList(ev) &&
                        ev.getX() <= mModeSelectorItems.get(mSupportedModes.get(0)).getMaxVisibleWidth()) {
                    int itemSelected = getFocusItem(ev.getX(), ev.getY());
                    if (itemSelected != NO_ITEM_SELECTED) {
                        mChildViewTouched = mModeSelectorItems.get(itemSelected);
                        return true;
                    }
                }
                // If shimmy is on-going, reject the first down event, so that it can be handled
                // by the view underneath. If a swipe is detected, the same series of touch will
                // re-enter this function, in which case we will consume the touch events.
                if (mLastDownTime != ev.getDownTime()) {
                    mLastDownTime = ev.getDownTime();
                    return false;
                }
            }
            return true;
        }

        @Override
        public boolean onTouchEvent(MotionEvent ev) {
            if (MotionEvent.ACTION_DOWN == ev.getActionMasked()) {
                if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    mFocusItem = getFocusItem(ev.getX(), ev.getY());
                    setSwipeMode(true);
                }
            }
            forwardTouchEventToChild(ev);
            return true;
        }

        @Override
        public void onItemSelected(ModeSelectorItem selectedItem) {
            cancelAnimation();
            Log.i(TAG, "ShimmyState onItemSelected");
            mCurrentStateManager.setCurrentState(new SelectedState(selectedItem));
        }

        private void hideShimmyWithDelay() {
            postDelayed(mHideShimmy, HIDE_SHIMMY_DELAY_MS);
        }

        @Override
        public void onWindowFocusChanged(boolean hasFocus) {
            if (mStartHidingShimmyWhenWindowGainsFocus && hasFocus) {
                mStartHidingShimmyWhenWindowGainsFocus = false;
                hideShimmyWithDelay();
            }
        }

        /**
         * This starts the accordion animation, unless it's already running, in which
         * case the start animation call will be ignored.
         */
        private void startHidingShimmy() {
            if (mAnimator != null) {
                return;
            }
            int maxVisibleWidth = mModeSelectorItems.get(mSupportedModes.get(0)).getMaxVisibleWidth();
            mAnimator = animateListToWidth(START_DELAY_MS * (-1), TOTAL_DURATION_MS,
                    Gusterpolator.INSTANCE, maxVisibleWidth, 0);
            mAnimator.addListener(new Animator.AnimatorListener() {
                private boolean mSuccess = true;
                @Override
                public void onAnimationStart(Animator animation) {
                    // Do nothing.
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mAnimator = null;
                    ShimmyState.this.onAnimationEnd(mSuccess);
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    mSuccess = false;
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                    // Do nothing.
                }
            });
        }

        /**
         * Cancels the pending/on-going animation.
         */
        private void cancelAnimation() {
            removeCallbacks(mHideShimmy);
            if (mAnimator != null && mAnimator.isRunning()) {
                mAnimator.cancel();
            } else {
                mAnimator = null;
                onAnimationEnd(false);
            }
        }

        @Override
        public void onCurrentState() {
            super.onCurrentState();
            ModeListView.this.disableA11yOnModeSelectorItems();
        }
        /**
         * Gets called when the animation finishes or gets canceled.
         *
         * @param success indicates whether the animation finishes successfully
         */
        private void onAnimationEnd(boolean success) {
            if (mSettingsButton.getLayerType() == View.LAYER_TYPE_HARDWARE) {
                Log.v(TAG, "Disabling hardware layer for the Settings Button. (onAnimationEnd)");
                mSettingsButton.setLayerType(View.LAYER_TYPE_NONE, null);
            }

            /**
             *
            mSettingsButton.setVisibility(VISIBLE);
             */
            if (mModeSwitchListener.getCurrentModeIndex() != WIDEANGLE_MODE_INDEX) {
                mSettingsButton.setVisibility(VISIBLE);
            }
            // If successfully finish hiding shimmy, then we should go back to
            // fully hidden state.
            if (success) {
                ModeListView.this.enableA11yOnModeSelectorItems();
                mModeListOpenFactor = 1;
                mCurrentStateManager.setCurrentState(new FullyHiddenState());
                return;
            }

            // If the animation was canceled before it's finished, animate the mode
            // list open factor from 0 to 1 to ensure a smooth visual transition.
            final ValueAnimator openFactorAnimator = ValueAnimator.ofFloat(mModeListOpenFactor, 1f);
            openFactorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mModeListOpenFactor = (Float) openFactorAnimator.getAnimatedValue();
                    onVisibleWidthChanged(mVisibleWidth);
                }
            });
            openFactorAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    // Do nothing.
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mModeListOpenFactor = 1f;
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    // Do nothing.
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                    // Do nothing.
                }
            });
            openFactorAnimator.start();
        }

        @Override
        public void hide() {
            cancelAnimation();
            mCurrentStateManager.setCurrentState(new FullyHiddenState());
        }

        @Override
        public void hideAnimated() {
            cancelAnimation();
            animateListToWidth(0).addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mCurrentStateManager.setCurrentState(new FullyHiddenState());
                }
            });
        }
    }

    /**
     * When the mode list is being scrolled, it will be in ScrollingState. From
     * this state, the mode list could transition to fully hidden, fully open
     * depending on which direction the scrolling goes.
     */
    private class ScrollingState extends ModeListState {
        private Animator mAnimator = null;

        public ScrollingState() {
            setVisibility(VISIBLE);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            // Scroll based on the scrolling distance on the currently focused
            // item.
            scroll(mFocusItem, distanceX * SCROLL_FACTOR,
                    distanceY * SCROLL_FACTOR);
            return true;
        }

        @Override
        public boolean shouldHandleTouchEvent(MotionEvent ev) {
            // If the snap back/to full screen animation is on going, ignore any
            // touch.
            if (mAnimator != null) {
                return false;
            }
            return true;
        }

        @Override
        public boolean onTouchEvent(MotionEvent ev) {
            if (ev.getActionMasked() == MotionEvent.ACTION_UP ||
                    ev.getActionMasked() == MotionEvent.ACTION_CANCEL) {
                final boolean shouldSnapBack = shouldSnapBack();
                if (shouldSnapBack) {
                    mAnimator = snapBack();
                } else {
                    mAnimator = snapToFullScreen();
                }
                mAnimator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mAnimator = null;
                        mFocusItem = NO_ITEM_SELECTED;
                        if (shouldSnapBack) {
                            mCurrentStateManager.setCurrentState(new FullyHiddenState());
                        } else {
                            mCurrentStateManager.setCurrentState(new FullyShownState());
                        }
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
            }
            return true;
        }
    }

    /**
     * Mode list gets in this state when a mode item has been selected/clicked.
     * There will be an animation with the blurred preview fading in, a potential
     * pause to wait for the new mode to be ready, and then the new mode will
     * be revealed through a pinhole animation. After all the animations finish,
     * mode list will transition into fully hidden state.
     */
    private class SelectedState extends ModeListState {

        public SelectedState(ModeSelectorItem selectedItem) {
            final int modeId = selectedItem.getModeId();
            ModeSelectorItem item = null;
            Iterator ite = mModeSelectorItems.entrySet().iterator();
            while(ite.hasNext()){
                Map.Entry entry = (Map.Entry)ite.next();
                item = (ModeSelectorItem)entry.getValue();
                item.setSelected(false);
            }

            // Fixme: top 2 hotspot method in this method
            PeepholeAnimationEffect effect = new PeepholeAnimationEffect();
            effect.setSize(mWidth, mHeight);

            // Calculate the position of the icon in the selected item, and
            // start animation from that position.
            int[] location = new int[2];
            // Gets icon's center position in relative to the window.
            selectedItem.getIconCenterLocationInWindow(location);
            int iconX = location[0];
            int iconY = location[1];
            // Gets current view's top left position relative to the window.
            getLocationInWindow(location);
            // Calculate icon location relative to this view
            iconX -= location[0];
            iconY -= location[1];

            effect.setAnimationStartingPosition(iconX, iconY);
            effect.setModeSpecificColor(selectedItem.getHighlightColor());

            // SPRD: Fix bug474690 add for pano
            if (CameraUtil.isModeSwitchAnimationEnabled()) {
                if (mScreenShotProvider != null) {
                    if (modeId == WIDEANGLE_MODE_INDEX
                            || mModeSwitchListener.getCurrentModeIndex() == WIDEANGLE_MODE_INDEX) {
                        effect.setBackground(
                                // SPRD: Fix bug 612561, get wrong background
                                mScreenShotProvider.getBlackPreviewFrame(PREVIEW_DOWN_SAMPLE_FACTOR),
                                mCaptureLayoutHelper.getPreviewRect());
                        effect.setBackgroundOverlay(mScreenShotProvider
                                .getPreviewOverlayAndControls());
                    } else {
                        effect.setBackground(mScreenShotProvider
                                        .getPreviewFrame(PREVIEW_DOWN_SAMPLE_FACTOR),
                                mCaptureLayoutHelper.getPreviewRect());
                        effect.setBackgroundOverlay(mScreenShotProvider
                                .getPreviewOverlayAndControls());
                    }
                }
            } else {
                Bitmap screenshot = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(screenshot);
                canvas.drawARGB(0, 0, 0, 0);
                effect.setBackground(screenshot, mCaptureLayoutHelper.getPreviewRect());
            }

            mCurrentAnimationEffects = effect;
            effect.startFadeoutAnimation(null, selectedItem, iconX, iconY, modeId);
            invalidate();
        }

        @Override
        public boolean shouldHandleTouchEvent(MotionEvent ev) {
            return false;
        }

        @Override
        public void startModeSelectionAnimation() {
            /*
             * SPRD: Fix bug 598593 that mode switch is slow @{
             * Original Code
             *
            mCurrentAnimationEffects.startAnimation(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mCurrentAnimationEffects = null;
                    mCurrentStateManager.setCurrentState(new FullyHiddenState());
                }
            });
             */
            if (CameraUtil.isModeSwitchAnimationEnabled()) {
                mCurrentAnimationEffects.startAnimation(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mCurrentAnimationEffects = null;
                        mCurrentStateManager.setCurrentState(new FullyHiddenState());
                    }
                });
            } else {
                mCurrentAnimationEffects = null;
                mCurrentStateManager.setCurrentState(new FullyHiddenState());
            }
            /* @} */
        }

        @Override
        public void hide() {
            if (!mCurrentAnimationEffects.cancelAnimation()) {
                mCurrentAnimationEffects = null;
                mCurrentStateManager.setCurrentState(new FullyHiddenState());
            }
        }
    }

    public interface ModeSwitchListener {
        public void onModeButtonPressed(int modeIndex);
        public void onModeSelected(int modeIndex);
        public int getCurrentModeIndex();
        public void onSettingsSelected();
        void onCameraIdUpdate(int modeIndex);
    }

    public interface ModeListOpenListener {
        /**
         * Mode list will open to full screen after current animation.
         */
        public void onOpenFullScreen();

        /**
         * Updates the listener with the current progress of mode drawer opening.
         *
         * @param progress progress of the mode drawer opening, ranging [0f, 1f]
         *                 0 means mode drawer is fully closed, 1 indicates a fully
         *                 open mode drawer.
         */
        public void onModeListOpenProgress(float progress);

        /**
         * Gets called when mode list is completely closed.
         */
        public void onModeListClosed();
    }

    public static abstract class ModeListVisibilityChangedListener {
        private Boolean mCurrentVisibility = null;

        /** Whether the mode list is (partially or fully) visible. */
        public abstract void onVisibilityChanged(boolean visible);

        /**
         * Internal method to be called by the mode list whenever a visibility
         * even occurs.
         * <p>
         * Do not call {@link #onVisibilityChanged(boolean)} directly, as this
         * is only called when the visibility has actually changed and not on
         * each visibility event.
         *
         * @param visible whether the mode drawer is currently visible.
         */
        private void onVisibilityEvent(boolean visible) {
            if (mCurrentVisibility == null || mCurrentVisibility != visible) {
                mCurrentVisibility = visible;
                onVisibilityChanged(visible);
            }
        }
    }

    /**
     * This class aims to help store time and position in pairs.
     */
    private static class TimeBasedPosition {
        private final float mPosition;
        private final long mTimeStamp;
        public TimeBasedPosition(float position, long time) {
            mPosition = position;
            mTimeStamp = time;
        }

        public float getPosition() {
            return mPosition;
        }

        public long getTimeStamp() {
            return mTimeStamp;
        }
    }

    /**
     * This is a highly customized interpolator. The purpose of having this subclass
     * is to encapsulate intricate animation timing, so that the actual animation
     * implementation can be re-used with other interpolators to achieve different
     * animation effects.
     *
     * The accordion animation consists of three stages:
     * 1) Animate into the screen within a pre-specified fly in duration.
     * 2) Hold in place for a certain amount of time (Optional).
     * 3) Animate out of the screen within the given time.
     *
     * The accordion animator is initialized with 3 parameter: 1) initial position,
     * 2) how far out the view should be before flying back out,  3) end position.
     * The interpolation output should be [0f, 0.5f] during animation between 1)
     * to 2), and [0.5f, 1f] for flying from 2) to 3).
     */
    private final TimeInterpolator mAccordionInterpolator = new TimeInterpolator() {
        @Override
        public float getInterpolation(float input) {

            float flyInDuration = (float) FLY_OUT_DURATION_MS / (float) TOTAL_DURATION_MS;
            float holdDuration = (float) (FLY_OUT_DURATION_MS + HOLD_DURATION_MS)
                    / (float) TOTAL_DURATION_MS;
            if (input == 0) {
                return 0;
            } else if (input < flyInDuration) {
                // Stage 1, project result to [0f, 0.5f]
                input /= flyInDuration;
                float result = Gusterpolator.INSTANCE.getInterpolation(input);
                return result * 0.5f;
            } else if (input < holdDuration) {
                // Stage 2
                return 0.5f;
            } else {
                // Stage 3, project result to [0.5f, 1f]
                input -= holdDuration;
                input /= (1 - holdDuration);
                float result = Gusterpolator.INSTANCE.getInterpolation(input);
                return 0.5f + result * 0.5f;
            }
        }
    };

    /**
     * The listener that is used to notify when gestures occur.
     * Here we only listen to a subset of gestures.
     */
    private final GestureDetector.OnGestureListener mOnGestureListener
            = new GestureDetector.SimpleOnGestureListener(){
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {
            mCurrentStateManager.getCurrentState().onScroll(e1, e2, distanceX, distanceY);
            mLastScrollTime = System.currentTimeMillis();
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent ev) {
            mCurrentStateManager.getCurrentState().onSingleTapUp(ev);
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            // Cache velocity in the unit pixel/ms.
            mVelocityX = velocityX / 1000f * SCROLL_FACTOR;
            mCurrentStateManager.getCurrentState().onFling(e1, e2, velocityX, velocityY);
            return true;
        }

        @Override
        public boolean onDown(MotionEvent ev) {
            mVelocityX = 0;
            mCurrentStateManager.getCurrentState().onDown(ev);
            return true;
        }
    };

    /**
     * Gets called when a mode item in the mode drawer is clicked.
     *
     * @param selectedItem the item being clicked
     */
    private void onItemSelected(ModeSelectorItem selectedItem) {
        int modeId = selectedItem.getModeId();
        mModeSwitchListener.onModeButtonPressed(modeId);

        mCurrentStateManager.getCurrentState().onItemSelected(selectedItem);
    }

    /**
     * Checks whether a touch event is inside of the bounds of the mode list.
     *
     * @param ev touch event to be checked
     * @return whether the touch is inside the bounds of the mode list
     */
    private boolean isTouchInsideList(MotionEvent ev) {
        // Ignore the tap if it happens outside of the mode list linear layout.
        float x = ev.getX() - mListView.getX();
        float y = ev.getY() - mListView.getY();
        if (x < 0 || x > mListView.getWidth() || y < 0 || y > mListView.getHeight()) {
            return false;
        }
        return true;
    }

    public ModeListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mGestureDetector = new GestureDetector(context, mOnGestureListener);
        mListBackgroundColor = getResources().getColor(R.color.mode_list_background);
        mSettingsButtonMargin = getResources().getDimensionPixelSize(
                R.dimen.mode_list_settings_icon_margin);
    }

    private void disableA11yOnModeSelectorItems() {
        ModeSelectorItem item = null;
        Iterator ite = mModeSelectorItems.entrySet().iterator();
        while(ite.hasNext()){
            Map.Entry entry = (Map.Entry)ite.next();
            item = (ModeSelectorItem)entry.getValue();
            item.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        }
    }

    private void enableA11yOnModeSelectorItems() {
        ModeSelectorItem item = null;
        Iterator ite = mModeSelectorItems.entrySet().iterator();
        while(ite.hasNext()){
            Map.Entry entry = (Map.Entry)ite.next();
            item = (ModeSelectorItem)entry.getValue();
            item.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_AUTO);
        }
    }

    /**
     * Sets the alpha on the list background. This is called whenever the list
     * is scrolling or animating, so that background can adjust its dimness.
     *
     * @param alpha new alpha to be applied on list background color
     */
    private void setBackgroundAlpha(int alpha) {
        // Make sure alpha is valid.
        alpha = alpha & 0xFF;
        // Change alpha on the background color.
        mListBackgroundColor = mListBackgroundColor & 0xFFFFFF;
        mListBackgroundColor = mListBackgroundColor | (alpha << 24);
        // Set new color to list background.
        setBackgroundColor(mListBackgroundColor);
    }
    /**
     * Sets the screen shot provider for getting a preview frame and a bitmap
     * of the controls and overlay.
     */
    public void setCameraModuleScreenShotProvider(
            CameraAppUI.CameraModuleScreenShotProvider provider) {
        mScreenShotProvider = provider;
    }

    /**
     * Maps between the UI mode selector index to the actual mode id.
     *
     * @param modeSelectorIndex the index of the UI item
     * @return the index of the corresponding camera mode
     */
    private int getModeIndex(int modeSelectorIndex) {
        if (modeSelectorIndex < mTotalModes && modeSelectorIndex >= 0) {
            return mSupportedModes.get(modeSelectorIndex);
        }
        Log.e(TAG, "Invalid mode selector index: " + modeSelectorIndex + ", total modes: " +
                mTotalModes);
        return SettingsScopeNamespaces.AUTO_PHOTO;
    }

    /** Notify ModeSwitchListener, if any, of the mode change. */
    private void onModeSelected(int modeIndex) {
        if (mModeSwitchListener != null) {
            mModeSwitchListener.onModeSelected(modeIndex);
        }
    }

    private void onCameraIdUpdate(int modeIndex) {
        if (mModeSwitchListener != null) {
            mModeSwitchListener.onCameraIdUpdate(modeIndex);
        }
    }

    /**
     * Sets a listener that listens to receive mode switch event.
     *
     * @param listener a listener that gets notified when mode changes.
     */
    public void setModeSwitchListener(ModeSwitchListener listener) {
        mModeSwitchListener = listener;
    }

    /**
     * Sets a listener that gets notified when the mode list is open full screen.
     *
     * @param listener a listener that listens to mode list open events
     */
    public void setModeListOpenListener(ModeListOpenListener listener) {
        mModeListOpenListener = listener;
    }

    /**
     * Sets or replaces a listener that is called when the visibility of the
     * mode list changed.
     */
    public void setVisibilityChangedListener(ModeListVisibilityChangedListener listener) {
        mVisibilityChangedListener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (!mTouchable) {//SPRD:Fix bug428712
            return false;
        }
        // Reset touch forward recipient
        if (MotionEvent.ACTION_DOWN == ev.getActionMasked()) {
            mChildViewTouched = null;
        }

        if (!mCurrentStateManager.getCurrentState().shouldHandleTouchEvent(ev)) {
            /**
             * SPRD: fix bug599272 touch event shouldn't deliver to next view when at select state
             * Original code
            return false;
             */
            if (mCurrentStateManager.getCurrentState() instanceof SelectedState
                || mCurrentStateManager.getCurrentState() instanceof ScrollingState) {
                return true;
            } else {
                return false;
            }
        }
        getParent().requestDisallowInterceptTouchEvent(true);
        super.onTouchEvent(ev);
        // Pass all touch events to gesture detector for gesture handling.
        mGestureDetector.onTouchEvent(ev);
        mCurrentStateManager.getCurrentState().onTouchEvent(ev);
        return true;
    }

    public void setTouchable(boolean enabled) {// SPRD:Fix bug428712
        mTouchable = enabled;
    }

    /**
     * Forward touch events to a recipient child view. Before feeding the motion
     * event into the child view, the event needs to be converted in child view's
     * coordinates.
     */
    private void forwardTouchEventToChild(MotionEvent ev) {
        if (mChildViewTouched != null) {
            float x = ev.getX() - mListView.getX();
            float y = ev.getY() - mListView.getY();
            x -= mChildViewTouched.getLeft();
            y -= mChildViewTouched.getTop();
            mLastChildTouchEvent = MotionEvent.obtain(ev);
            mLastChildTouchEvent.setLocation(x, y);
            mChildViewTouched.onTouchEvent(mLastChildTouchEvent);
        }
    }

    /**
     * Sets the swipe mode to indicate whether this is a swiping in
     * or out, and therefore we can have different animations.
     *
     * @param swipeIn indicates whether the swipe should reveal/hide the list.
     */
    private void setSwipeMode(boolean swipeIn) {
        ModeSelectorItem item = null;
        Iterator ite = mModeSelectorItems.entrySet().iterator();
        while(ite.hasNext()){
            Map.Entry entry = (Map.Entry)ite.next();
            item = (ModeSelectorItem)entry.getValue();
            item.onSwipeModeChanged(swipeIn);
        }
    }

    public void setShouldBeVisible(boolean swipeIn) {
        if (mCurrentStateManager.getCurrentState() instanceof FullyHiddenState)
            ((FullyHiddenState)mCurrentStateManager.getCurrentState()).updateOpenTag(swipeIn);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mWidth = right - left;
        mHeight = bottom - top - getPaddingTop() - getPaddingBottom();

        updateModeListLayout();

        if (mCurrentStateManager.getCurrentState().getCurrentAnimationEffects() != null) {
            mCurrentStateManager.getCurrentState().getCurrentAnimationEffects().setSize(
                    mWidth, mHeight);
        }
    }

    /**
     * Sets a capture layout helper to query layout rect from.
     */
    public void setCaptureLayoutHelper(CaptureLayoutHelper helper) {
        mCaptureLayoutHelper = helper;
    }

    @Override
    public void onPreviewAreaChanged(RectF previewArea) {
        if (getVisibility() == View.VISIBLE && !hasWindowFocus()) {
            // When the preview area has changed, to avoid visual disruption we
            // only make corresponding UI changes when mode list does not have
            // window focus.
            updateModeListLayout();
        }
    }

    private void updateModeListLayout() {
        if (mCaptureLayoutHelper == null) {
            Log.e(TAG, "Capture layout helper needs to be set first.");
            return;
        }
        /*
        // Center mode drawer in the portion of camera preview that is not covered by
        // bottom bar.
        RectF uncoveredPreviewArea = mCaptureLayoutHelper.getUncoveredPreviewRect();
        mTitle.setTranslationX(uncoveredPreviewArea.left + gridTranslationX);
        // Align left:
        mListView.setTranslationX(uncoveredPreviewArea.left + gridTranslationX);
        // Align center vertical:
        // mListView.setTranslationY(uncoveredPreviewArea.centerY()
        // - mListView.getMeasuredHeight() / 2);

        updateSettingsButtonLayout(uncoveredPreviewArea);
        */
        mTitle.setVisibility(VISIBLE);
    }

    private void updateSettingsButtonLayout(RectF uncoveredPreviewArea) {
        if (mWidth > mHeight) {
            // Align to the top right.
            mSettingsButton.setTranslationX(uncoveredPreviewArea.right - mSettingsButtonMargin
                    - mSettingsButton.getMeasuredWidth());
            mSettingsButton.setTranslationY(uncoveredPreviewArea.top + mSettingsButtonMargin);
        } else {
            // Align to the bottom right.
            mSettingsButton.setTranslationX(uncoveredPreviewArea.right - mSettingsButtonMargin
                    - mSettingsButton.getMeasuredWidth());
            mSettingsButton.setTranslationY(uncoveredPreviewArea.bottom - mSettingsButtonMargin
                    - mSettingsButton.getMeasuredHeight());
        }
        if (mSettingsCling != null) {
            mSettingsCling.updatePosition(mSettingsButton);
        }
    }

    @Override
    public void draw(Canvas canvas) {

        ModeListState currentState = mCurrentStateManager.getCurrentState();
        AnimationEffects currentEffects = currentState.getCurrentAnimationEffects();
        if (currentEffects != null) {
            currentEffects.drawBackground(canvas);
            if (currentEffects.shouldDrawSuper()) {
                super.draw(canvas);
            }
            currentEffects.drawForeground(canvas);
        } else {
            super.draw(canvas);
        }
    }

    /**
     * Sets whether a cling for settings button should be shown. If not, remove
     * the cling from view hierarchy if any. If a cling should be shown, inflate
     * the cling into this view group.
     *
     * @param show whether the cling needs to be shown.
     */
    public void setShouldShowSettingsCling(boolean show) {
        if (show) {
            if (mSettingsCling == null) {
                inflate(getContext(), R.layout.settings_cling, this);
                mSettingsCling = (SettingsCling) findViewById(R.id.settings_cling);
            }
        } else {
            if (mSettingsCling != null) {
                // Remove settings cling from view hierarchy.
                removeView(mSettingsCling);
                mSettingsCling = null;
            }
        }
    }

    /**
     * Show or hide cling for settings button. The cling will only be shown if
     * settings button has never been clicked. Otherwise, cling will be null,
     * and will not show even if this method is called to show it.
     */
    private void showSettingsClingIfEnabled(boolean show) {
        if (mSettingsCling != null) {
            int visibility = show ? VISIBLE : INVISIBLE;
            mSettingsCling.setVisibility(visibility);
        }
    }

    /**
     * This shows the mode switcher and starts the accordion animation with a delay.
     * If the view does not currently have focus, (e.g. There are popups on top of
     * it.) start the delayed accordion animation when it gains focus. Otherwise,
     * start the animation with a delay right away.
     */
    public void showModeSwitcherHint() {
        mCurrentStateManager.getCurrentState().showSwitcherHint();
    }

    /**
     * Hide the mode list immediately (provided the current state allows it).
     */
    public void hide() {
        mCurrentStateManager.getCurrentState().hide();
    }

    /**
     * Hide the mode list with an animation.
     */
    public void hideAnimated() {
        mCurrentStateManager.getCurrentState().hideAnimated();
    }

    /**
     * Resets the visible width of all the mode selectors to 0.
     */
    private void resetModeSelectors() {
        ModeSelectorItem item = null;
        Iterator ite = mModeSelectorItems.entrySet().iterator();
        while(ite.hasNext()){
            Map.Entry entry = (Map.Entry)ite.next();
            item = (ModeSelectorItem)entry.getValue();
            item.setVisibleWidth(0);
        }
    }

    private boolean isRunningAccordionAnimation() {
        return mAnimatorSet != null && mAnimatorSet.isRunning();
    }

    /**
     * Calculate the mode selector item in the list that is at position (x, y).
     * If the position is above the top item or below the bottom item, return
     * the top item or bottom item respectively.
     *
     * @param x horizontal position
     * @param y vertical position
     * @return index of the item that is at position (x, y)
     */
    private int getFocusItem(float x, float y) {
        // Convert coordinates into child view's coordinates.
        x -= mListView.getX();
        y -= mListView.getY();
        ModeSelectorItem item = null;
        Iterator ite = mModeSelectorItems.entrySet().iterator();
        while(ite.hasNext()){
            Map.Entry entry = (Map.Entry)ite.next();
            item = (ModeSelectorItem)entry.getValue();
            if(item.getParent() == null){
                continue;
            }
            float left = item.getLeft();
            float right = item.getRight();
            float top = item.getTop();
            float bottom = item.getBottom();

            if (x > left && x <= right && y > top && y <= bottom) {
                Log.d(TAG, "returned i=" + entry.getKey() + "," + left + "," + right + "," + top + "," + bottom);
                return (int) entry.getKey();
            }
        }
        return NO_ITEM_SELECTED;
    }

    private int mInsideGridMargin = 0;

    private int getFocusItemByPos(float x, float y, int pagerPos) {
        int needReadItemEndNum = (pagerPos + 1) * 12 - 1;//11, 23...
        int needReadItemStartNum = pagerPos * 12;//0, 12...
        int rememberSelectorItemsNumber = -1;

        // Convert coordinates into child view's coordinates.
        x -= mListView.getX();
        y -= mListView.getY();
        ModeSelectorItem item = null;
        Iterator ite = mModeSelectorItems.entrySet().iterator();
        while(ite.hasNext()){
            Map.Entry entry = (Map.Entry)ite.next();
            rememberSelectorItemsNumber++;
            if (needReadItemStartNum <= rememberSelectorItemsNumber && rememberSelectorItemsNumber <= needReadItemEndNum){
                item = (ModeSelectorItem)entry.getValue();
                if(item.getParent() == null){
                    continue;
                }
                float left = item.getLeft(); // getLeft() is relative item's parent.
                float right = item.getRight();
                float top = item.getTop();
                float bottom = item.getBottom();
                if (x > left && x <= right && y > top && y <= bottom) {
                    return (int) entry.getKey();
                }
            }
        }
        return NO_ITEM_SELECTED;
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        mCurrentStateManager.getCurrentState().onWindowFocusChanged(hasFocus);
    }

    @Override
    public void onVisibilityChanged(View v, int visibility) {
        super.onVisibilityChanged(v, visibility);
        if (visibility == VISIBLE ) {
            if (mModeSelectorItems != null) {
                resetSelectedState();
            }
            /*
            // Highlight current module
            if (mModeSwitchListener != null && mModeSelectorItems != null) {
                int modeId = mModeSwitchListener.getCurrentModeIndex();
                if(mModeSelectorItems.size() > 0) {
                    if (CameraUtil.mModuleInfoResolve.isModuleVisible(modeId)) {
                        mModeSelectorItems.get(modeId).setSelected(true);
                    } else {
                        mModeSelectorItems.get(modeId).setSelected(false);
                    }
                    //SPRD:bug905793 mModeSelectorItems may be null in monkey test.
                    if (modeId == SettingsScopeNamespaces.FRONT_BLUR || modeId == SettingsScopeNamespaces.REFOCUS ||
                            modeId == SettingsScopeNamespaces.BACK_ULTRA_WIDE_ANGLE) {
                        mModeSelectorItems.get(SettingsScopeNamespaces.AUTO_PHOTO).setSelected(true);
                    }
                }
            }

             */
            updateModeListLayout();
            mCurrentStateManager.setCurrentState(new FullyShownState());
        } else {
            if (mModeListOpenListener != null) {
                mModeListOpenListener.onModeListClosed();
            }
        }

        if (mVisibilityChangedListener != null) {
            mVisibilityChangedListener.onVisibilityEvent(getVisibility() == VISIBLE);
        }
    }

    private void resetSelectedState() {
        ModeSelectorItem item = null;
        Iterator ite = mModeSelectorItems.entrySet().iterator();
        while(ite.hasNext()) {
            Map.Entry entry = (Map.Entry) ite.next();
            item = (ModeSelectorItem) entry.getValue();
            item.setSelected(false);
        }
    }

    @Override
    public void setVisibility(int visibility) {
        ModeListState currentState = mCurrentStateManager.getCurrentState();
        if (currentState != null && !currentState.shouldHandleVisibilityChange(visibility)) {
            return;
        }
        if (getContext() != null && getContext() instanceof CameraActivity
                && ((CameraActivity) getContext()).getCameraAppUI() != null) {
            if (visibility == View.VISIBLE) {
                ((CameraActivity) getContext()).getCameraAppUI().onOpenModeList();
            } else {
                ((CameraActivity) getContext()).getCameraAppUI().onCloseModeList();
            }
        }
        super.setVisibility(visibility);
    }

    private void scroll(int itemId, float deltaX, float deltaY) {
        // Scrolling trend on X and Y axis, to track the trend by biasing
        // towards latest touch events.
        mScrollTrendX = mScrollTrendX * 0.3f + deltaX * 0.7f;
        mScrollTrendY = mScrollTrendY * 0.3f + deltaY * 0.7f;

        // TODO: Change how the curve is calculated below when UX finalize their design.
        mCurrentTime = SystemClock.uptimeMillis();
        float longestWidth;
        if (itemId != NO_ITEM_SELECTED) {
            longestWidth = mModeSelectorItems.get(itemId).getVisibleWidth();
        } else {
            longestWidth = mModeSelectorItems.get(0).getVisibleWidth();
        }
        float newPosition = longestWidth - deltaX;
        int maxVisibleWidth = mModeSelectorItems.get(0).getMaxVisibleWidth();
        newPosition = Math.min(newPosition, getMaxMovementBasedOnPosition((int) longestWidth,
                maxVisibleWidth));
        newPosition = Math.max(newPosition, 0);
        insertNewPosition(newPosition, mCurrentTime);

//        for (int i = 0; i < mModeSelectorItems.length; i++) {
//            mModeSelectorItems[i].setVisibleWidth((int) newPosition);
//        }
            ModeSelectorItem item = null;
            Iterator ite = mModeSelectorItems.entrySet().iterator();
            while(ite.hasNext()){
                Map.Entry entry = (Map.Entry)ite.next();
                item = (ModeSelectorItem)entry.getValue();
                item.setVisibleWidth((int) newPosition);
            }
    }

    /**
     * Insert new position and time stamp into the history position list, and
     * remove stale position items.
     *
     * @param position latest position of the focus item
     * @param time  current time in milliseconds
     */
    private void insertNewPosition(float position, long time) {
        // TODO: Consider re-using stale position objects rather than
        // always creating new position objects.
        mPositionHistory.add(new TimeBasedPosition(position, time));

        // Positions that are from too long ago will not be of any use for
        // future position interpolation. So we need to remove those positions
        // from the list.
        long timeCutoff = time - (mTotalModes - 1) * DELAY_MS;
        while (mPositionHistory.size() > 0) {
            // Remove all the position items that are prior to the cutoff time.
            TimeBasedPosition historyPosition = mPositionHistory.getFirst();
            if (historyPosition.getTimeStamp() < timeCutoff) {
                mPositionHistory.removeFirst();
            } else {
                break;
            }
        }
    }

    /**
     * Gets the interpolated position at the specified time. This involves going
     * through the recorded positions until a {@link TimeBasedPosition} is found
     * such that the position the recorded before the given time, and the
     * {@link TimeBasedPosition} after that is recorded no earlier than the given
     * time. These two positions are then interpolated to get the position at the
     * specified time.
     */
    private float getPosition(long time, float currentPosition) {
        int i;
        for (i = 0; i < mPositionHistory.size(); i++) {
            TimeBasedPosition historyPosition = mPositionHistory.get(i);
            if (historyPosition.getTimeStamp() > time) {
                // Found the winner. Now interpolate between position i and position i - 1
                if (i == 0) {
                    // Slowly approaching to the destination if there isn't enough data points
                    float weight = 0.2f;
                    return historyPosition.getPosition() * weight + (1f - weight) * currentPosition;
                } else {
                    TimeBasedPosition prevTimeBasedPosition = mPositionHistory.get(i - 1);
                    // Start interpolation
                    float fraction = (float) (time - prevTimeBasedPosition.getTimeStamp()) /
                            (float) (historyPosition.getTimeStamp() - prevTimeBasedPosition.getTimeStamp());
                    float position = fraction * (historyPosition.getPosition()
                            - prevTimeBasedPosition.getPosition()) + prevTimeBasedPosition.getPosition();
                    return position;
                }
            }
        }
        // It should never get here.
        Log.e(TAG, "Invalid time input for getPosition(). time: " + time);
        if (mPositionHistory.size() == 0) {
            Log.e(TAG, "TimeBasedPosition history size is 0");
        } else {
            Log.e(TAG, "First position recorded at " + mPositionHistory.getFirst().getTimeStamp()
            + " , last position recorded at " + mPositionHistory.getLast().getTimeStamp());
        }
        assert (i < mPositionHistory.size());
        return i;
    }

    private void reset() {
        resetModeSelectors();
        mScrollTrendX = 0f;
        mScrollTrendY = 0f;
        setVisibility(INVISIBLE);
    }

    /**
     * When visible width of list is changed, the background of the list needs
     * to darken/lighten correspondingly.
     */
    @Override
    public void onVisibleWidthChanged(int visibleWidth) {
        mVisibleWidth = visibleWidth;

        // When the longest mode item is entirely shown (across the screen), the
        // background should be 50% transparent.
        int maxVisibleWidth = mModeSelectorItems.get(0).getMaxVisibleWidth();
        visibleWidth = Math.min(maxVisibleWidth, visibleWidth);
        if (visibleWidth != maxVisibleWidth) {
            // No longer full screen.
            cancelForwardingTouchEvent();
        }
        float openRatio = (float) visibleWidth / maxVisibleWidth;
        // onModeListOpenRatioUpdate(openRatio * mModeListOpenFactor);
        gridTranslationX = (int) ((openRatio - 1) * mListView.getWidth());
        RectF uncoveredPreviewArea = mCaptureLayoutHelper.getUncoveredPreviewRect();
        // Align left:
        mListView.setTranslationX(uncoveredPreviewArea.left + gridTranslationX);
        mTitle.setTranslationX(uncoveredPreviewArea.left + gridTranslationX);
    }

    private int gridTranslationX = -2048;//Bug747883

    /**
     * Gets called when UI elements such as background and gear icon need to adjust
     * their appearance based on the percentage of the mode list opening.
     *
     * @param openRatio percentage of the mode list opening, ranging [0f, 1f]
     */
    private void onModeListOpenRatioUpdate(float openRatio) {
            ModeSelectorItem item = null;
            Iterator ite = mModeSelectorItems.entrySet().iterator();
            while(ite.hasNext()){
                Map.Entry entry = (Map.Entry)ite.next();
                item = (ModeSelectorItem)entry.getValue();
                item.setTextAlpha(1);
            }
        setBackgroundAlpha((int) (BACKGROUND_TRANSPARENTCY * openRatio));
        if (mModeListOpenListener != null) {
            mModeListOpenListener.onModeListOpenProgress(openRatio);
        }
        if (mSettingsButton != null) {
            // Disable the hardware layer when the ratio reaches 0.0 or 1.0.
            if (openRatio >= 1.0f || openRatio <= 0.0f) {
                if (mSettingsButton.getLayerType() == View.LAYER_TYPE_HARDWARE) {
                    Log.v(TAG, "Disabling hardware layer for the Settings Button. (via alpha)");
                    mSettingsButton.setLayerType(View.LAYER_TYPE_NONE, null);
                }
            } else {
                if (mSettingsButton.getLayerType() != View.LAYER_TYPE_HARDWARE) {
                    Log.v(TAG, "Enabling hardware layer for the Settings Button.");
                    mSettingsButton.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                }
            }

            mSettingsButton.setAlpha(openRatio);
        }
    }

    /**
     * Cancels the touch event forwarding by sending a cancel event to the recipient
     * view and resetting the touch forward recipient to ensure no more events
     * can be forwarded in the current series of the touch events.
     */
    private void cancelForwardingTouchEvent() {
        if (mChildViewTouched != null) {
            mLastChildTouchEvent.setAction(MotionEvent.ACTION_CANCEL);
            mChildViewTouched.onTouchEvent(mLastChildTouchEvent);
            mChildViewTouched = null;
        }
    }

    @Override
    public void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility != VISIBLE  && mCurrentStateManager.getCurrentState() != null) {
            mCurrentStateManager.getCurrentState().hide();
        }
    }

    /**
     * Defines how the list view should respond to a menu button pressed
     * event.
     */
    public boolean onMenuPressed() {
        return mCurrentStateManager.getCurrentState().onMenuPressed();
    }

    /**
     * The list view should either snap back or snap to full screen after a gesture.
     * This function is called when an up or cancel event is received, and then based
     * on the current position of the list and the gesture we can decide which way
     * to snap.
     */
    private void snap() {
        if (shouldSnapBack()) {
            snapBack();
        } else {
            snapToFullScreen();
        }
    }

    private boolean shouldSnapBack() {
        int itemId = Math.max(0, mFocusItem);
        if (Math.abs(mVelocityX) > VELOCITY_THRESHOLD) {
            // Fling to open / close
            return mVelocityX < 0;
        } else if (mModeSelectorItems.get(itemId).getVisibleWidth()
                < mModeSelectorItems.get(itemId).getMaxVisibleWidth() * SNAP_BACK_THRESHOLD_RATIO) {
            return true;
        } else if (Math.abs(mScrollTrendX) > Math.abs(mScrollTrendY) && mScrollTrendX > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Snaps back out of the screen.
     *
     * @param withAnimation whether snapping back should be animated
     */
    public Animator snapBack(boolean withAnimation) {
        if (withAnimation) {
            if (mVelocityX > -VELOCITY_THRESHOLD * SCROLL_FACTOR) {
                return animateListToWidth(0);
            } else {
                return animateListToWidthAtVelocity(mVelocityX, 0);
            }
        } else {
            setVisibility(INVISIBLE);
            resetModeSelectors();
            return null;
        }
    }

    /**
     * Snaps the mode list back out with animation.
     */
    private Animator snapBack() {
        return snapBack(true);
    }

    private Animator snapToFullScreen() {
        Animator animator;
        int focusItem = mFocusItem == NO_ITEM_SELECTED ? 0 : mFocusItem;
        int fullWidth = mModeSelectorItems.get(focusItem).getMaxVisibleWidth();
        if (mVelocityX <= VELOCITY_THRESHOLD) {
            animator = animateListToWidth(fullWidth);
        } else {
            // If the fling velocity exceeds this threshold, snap to full screen
            // at a constant speed.
            animator = animateListToWidthAtVelocity(VELOCITY_THRESHOLD, fullWidth);
        }
        if (mModeListOpenListener != null) {
            mModeListOpenListener.onOpenFullScreen();
        }
        return animator;
    }

    /**
     * Overloaded function to provide a simple way to start animation. Animation
     * will use default duration, and a value of <code>null</code> for interpolator
     * means linear interpolation will be used.
     *
     * @param width a set of values that the animation will animate between over time
     */
    private Animator animateListToWidth(int... width) {
        return animateListToWidth(0, DEFAULT_DURATION_MS, null, width);
    }

    /**
     * Animate the mode list between the given set of visible width.
     *
     * @param delay start delay between consecutive mode item. If delay < 0, the
     *              leader in the animation will be the bottom item.
     * @param duration duration for the animation of each mode item
     * @param interpolator interpolator to be used by the animation
     * @param width a set of values that the animation will animate between over time
     */
    private Animator animateListToWidth(int delay, int duration,
                                    TimeInterpolator interpolator, int... width) {
        if (mAnimatorSet != null && mAnimatorSet.isRunning()) {
            mAnimatorSet.end();
        }

        ArrayList<Animator> animators = new ArrayList<Animator>();
        boolean animateModeItemsInOrder = true;
        if (delay < 0) {
            animateModeItemsInOrder = false;
        }

        for (int i = 0; i < mTotalModes; i++) {
            int moduleId = 0;
            ObjectAnimator animator;
            if (animateModeItemsInOrder) {
                moduleId = mSupportedModes.get(i);
            }else {
                moduleId = mSupportedModes.get(mTotalModes - 1 - i);
            }
            animator = ObjectAnimator.ofInt(mModeSelectorItems.get(moduleId),"visibleWidth",width);
            animator.setDuration(duration);
            animators.add(animator);
        }

        mAnimatorSet = new AnimatorSet();
        mAnimatorSet.playTogether(animators);
        mAnimatorSet.setInterpolator(interpolator);
        mAnimatorSet.start();

        return mAnimatorSet;
    }

    /**
     * Animate the mode list to the given width at a constant velocity.
     *
     * @param velocity the velocity that animation will be at
     * @param width final width of the list
     */
    private Animator animateListToWidthAtVelocity(float velocity, int width) {
        if (mAnimatorSet != null && mAnimatorSet.isRunning()) {
            mAnimatorSet.end();
        }

        ArrayList<Animator> animators = new ArrayList<Animator>();
            ModeSelectorItem item = null;
            Iterator ite = mModeSelectorItems.entrySet().iterator();
            while(ite.hasNext()){
                Map.Entry entry = (Map.Entry)ite.next();
                item = (ModeSelectorItem)entry.getValue();
                ObjectAnimator animator = ObjectAnimator.ofInt(item,"visibleWidth",width);
                int duration = (int) (width / velocity);
                animator.setDuration(duration);
                animators.add(animator);
            }

        mAnimatorSet = new AnimatorSet();
        mAnimatorSet.playTogether(animators);
        mAnimatorSet.setInterpolator(null);
        mAnimatorSet.start();

        return mAnimatorSet;
    }

    /**
     * Called when the back key is pressed.
     *
     * @return Whether the UI responded to the key event.
     */
    public boolean onBackPressed() {
        return mCurrentStateManager.getCurrentState().onBackPressed();
    }

    public void startModeSelectionAnimation() {
        mCurrentStateManager.getCurrentState().startModeSelectionAnimation();
    }

    public float getMaxMovementBasedOnPosition(int lastVisibleWidth, int maxWidth) {
        int timeElapsed = (int) (System.currentTimeMillis() - mLastScrollTime);
        if (timeElapsed > SCROLL_INTERVAL_MS) {
            timeElapsed = SCROLL_INTERVAL_MS;
        }
        float position;
        int slowZone = (int) (maxWidth * SLOW_ZONE_PERCENTAGE);
        if (lastVisibleWidth < (maxWidth - slowZone)) {
            position = VELOCITY_THRESHOLD * timeElapsed + lastVisibleWidth;
        } else {
            float percentageIntoSlowZone = (lastVisibleWidth - (maxWidth - slowZone)) / slowZone;
            float velocity = (1 - percentageIntoSlowZone) * VELOCITY_THRESHOLD;
            position = velocity * timeElapsed + lastVisibleWidth;
        }
        position = Math.min(maxWidth, position);
        return position;
    }

    private class PeepholeAnimationEffect extends AnimationEffects {

        private final static int UNSET = -1;
        private final static int PEEP_HOLE_ANIMATION_DURATION_MS = 500;

        private final Paint mMaskPaint = new Paint();
        private final RectF mBackgroundDrawArea = new RectF();

        private int mPeepHoleCenterX = UNSET;
        private int mPeepHoleCenterY = UNSET;
        private float mRadius = 0f;
        private ValueAnimator mPeepHoleAnimator;
        private ValueAnimator mFadeOutAlphaAnimator;
        private ValueAnimator mRevealAlphaAnimator;
        private Bitmap mBackground;
        private Bitmap mBackgroundOverlay;

        private Paint mCirclePaint = new Paint();
        private Paint mCoverPaint = new Paint();

        private TouchCircleDrawable mCircleDrawable;

        public PeepholeAnimationEffect() {
            mMaskPaint.setAlpha(0);
            mMaskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

            mCirclePaint.setColor(0);
            mCirclePaint.setAlpha(0);

            mCoverPaint.setColor(0);
            mCoverPaint.setAlpha(0);

            setupAnimators();
        }

        private void setupAnimators() {
            mFadeOutAlphaAnimator = ValueAnimator.ofInt(0, 255);
            mFadeOutAlphaAnimator.setDuration(100);
            mFadeOutAlphaAnimator.setInterpolator(Gusterpolator.INSTANCE);
            mFadeOutAlphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mCoverPaint.setAlpha((Integer) animation.getAnimatedValue());
                    invalidate();
                }
            });
            mFadeOutAlphaAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    // Sets a HW layer on the view for the animation.
                    setLayerType(LAYER_TYPE_HARDWARE, null);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    // Sets the layer type back to NONE as a workaround for b/12594617.
                    setLayerType(LAYER_TYPE_NONE, null);
                }
            });

            /////////////////

            mRevealAlphaAnimator = ValueAnimator.ofInt(255, 0);
            /* SPRD: fix bug552200 disable the animation to let the preview showed as early as possible @{
             * orginal code
            mRevealAlphaAnimator.setDuration(PEEP_HOLE_ANIMATION_DURATION_MS);
             */
            mRevealAlphaAnimator.setDuration(PEEP_HOLE_ANIMATION_DURATION_MS);
            /* @} */
            mRevealAlphaAnimator.setInterpolator(Gusterpolator.INSTANCE);
            mRevealAlphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int alpha = (Integer) animation.getAnimatedValue();
                    mCirclePaint.setAlpha(alpha);
                    mCoverPaint.setAlpha(alpha);
                }
            });
            mRevealAlphaAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    // Sets a HW layer on the view for the animation.
                    setLayerType(LAYER_TYPE_HARDWARE, null);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    // Sets the layer type back to NONE as a workaround for b/12594617.
                    setLayerType(LAYER_TYPE_NONE, null);
                }
            });

            ////////////////

            int horizontalDistanceToFarEdge = Math.max(mPeepHoleCenterX, mWidth - mPeepHoleCenterX);
            int verticalDistanceToFarEdge = Math.max(mPeepHoleCenterY, mHeight - mPeepHoleCenterY);
            int endRadius = (int) (Math.sqrt(horizontalDistanceToFarEdge * horizontalDistanceToFarEdge
                    + verticalDistanceToFarEdge * verticalDistanceToFarEdge));
            int startRadius = getResources().getDimensionPixelSize(
                    R.dimen.mode_selector_icon_block_width) / 2;

            mPeepHoleAnimator = ValueAnimator.ofFloat(startRadius, endRadius);
            /* SPRD: fix bug552200 disable the animation to let the preview showed as early as possible @{
             * orginal code
            mPeepHoleAnimator.setDuration(PEEP_HOLE_ANIMATION_DURATION_MS);
             */
            mPeepHoleAnimator.setDuration(PEEP_HOLE_ANIMATION_DURATION_MS);
            /* @} */
            mPeepHoleAnimator.setInterpolator(Gusterpolator.INSTANCE);
            mPeepHoleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    // Modify mask by enlarging the hole
                    mRadius = (Float) mPeepHoleAnimator.getAnimatedValue();
                    invalidate();
                }
            });
            mPeepHoleAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    // Sets a HW layer on the view for the animation.
                    setLayerType(LAYER_TYPE_HARDWARE, null);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    // Sets the layer type back to NONE as a workaround for b/12594617.
                    setLayerType(LAYER_TYPE_NONE, null);
                }
            });

            ////////////////
            int size = getContext().getResources()
                    .getDimensionPixelSize(R.dimen.mode_selector_icon_block_width);
            mCircleDrawable = new TouchCircleDrawable(getContext().getResources());
            mCircleDrawable.setSize(size, size);
            mCircleDrawable.setUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    invalidate();
                }
            });
        }

        @Override
        public void setSize(int width, int height) {
            mWidth = width;
            mHeight = height;
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            return true;
        }

        @Override
        public void drawForeground(Canvas canvas) {
            // Draw the circle in clear mode
            if (mPeepHoleAnimator != null) {
                // Draw a transparent circle using clear mode
                canvas.drawCircle(mPeepHoleCenterX, mPeepHoleCenterY, mRadius, mMaskPaint);
                canvas.drawCircle(mPeepHoleCenterX, mPeepHoleCenterY, mRadius, mCirclePaint);
            }
        }

        public void setAnimationStartingPosition(int x, int y) {
            mPeepHoleCenterX = x;
            mPeepHoleCenterY = y;
        }

        public void setModeSpecificColor(int color) {
            mCirclePaint.setColor(color & 0x00ffffff);
        }

        /**
         * Sets the bitmap to be drawn in the background and the drawArea to draw
         * the bitmap.
         *
         * @param background image to be drawn in the background
         * @param drawArea area to draw the background image
         */
        public void setBackground(Bitmap background, RectF drawArea) {
            mBackground = background;
            mBackgroundDrawArea.set(drawArea);
        }

        /**
         * Sets the overlay image to be drawn on top of the background.
         */
        public void setBackgroundOverlay(Bitmap overlay) {
            mBackgroundOverlay = overlay;
        }

        @Override
        public void drawBackground(Canvas canvas) {
            canvas.drawARGB(0, 0, 0, 0);
            if (mBackground != null) {
                canvas.drawBitmap(mBackground, null, mBackgroundDrawArea, null);
                canvas.drawPaint(mCoverPaint);
                if (mBackgroundOverlay != null) {
                    canvas.drawBitmap(mBackgroundOverlay, 0, 0, null);
                }

                if (mCircleDrawable != null) {
                    mCircleDrawable.draw(canvas);
                }
            }
        }

        @Override
        public boolean shouldDrawSuper() {
            // No need to draw super when mBackgroundOverlay is being drawn, as
            // background overlay already contains what's drawn in super.
            return (mBackground == null || mBackgroundOverlay == null);
        }

        public void startFadeoutAnimation(Animator.AnimatorListener listener,
                final ModeSelectorItem selectedItem,
                int x, int y, final int modeId) {
            Log.i(TAG, "mode item was selected");
            final boolean modeSwitchAnimationEnabled = CameraUtil.isModeSwitchAnimationEnabled();
            final Runnable modeSelectRunnable = new Runnable() {
                @Override
                public void run() {
                    // SPRD Bug:512729 Callback after Activity's destroying or crash.
                    if ((ModeListView.this.getContext() == null)
                            || (ModeListView.this.getContext() != null
                            && ModeListView.this.getContext() instanceof CameraActivity
                            && ((CameraActivity) ModeListView.this.getContext()).isPaused())) {
                        // SPRD: Fix bug 683394 should change modelist state when paused
                        hide();
                        return;
                    }
                    // Select the focused item.
                    selectedItem.setSelected(true);
                    onCameraIdUpdate(modeId);
                    onModeSelected(modeId);
                }
            };


            mCircleDrawable.setIconDrawable(
                    selectedItem.getIcon().getIconDrawableClone(),
                    selectedItem.getIcon().getIconDrawableSize());
            mCircleDrawable.setCenter(new Point(x, y));
            mCircleDrawable.setColor(selectedItem.getHighlightColor());
            mCircleDrawable.setAnimatorListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    // Post mode selection runnable to the end of the message queue
                    // so that current UI changes can finish before mode initialization
                    // clogs up UI thread.
                    post(modeSelectRunnable);
                }
            });

            // add fade out animator to a set, so we can freely add
            // the listener without having to worry about listener dupes
            /*
             * SPRD: Fix bug 681215 optimization about that DV change to DC is slow
             */
            mCoverPaint.setColor(0);
            if (modeSwitchAnimationEnabled) {
                mCoverPaint.setAlpha(0);

                AnimatorSet s = new AnimatorSet();
                s.play(mFadeOutAlphaAnimator);
                if (listener != null) {
                    s.addListener(listener);
                }
                s.start();

                mCircleDrawable.animate();

            } else {
                // ignore FadeOutAlphaAnimator that set alpha from 0-255 during 100ms,
                // and init alpha start 255
                mCoverPaint.setAlpha(255);
                // next invalidate() will draw the selected icon immediately
                selectedItem.setSelected(true);
                // post here if modeSwitchAnimationEnabled is false
                post(modeSelectRunnable);
            }
            /* @} */
        }

        @Override
        public void startAnimation(Animator.AnimatorListener listener) {
            if (mPeepHoleAnimator != null && mPeepHoleAnimator.isRunning()) {
                return;
            }
            if (mPeepHoleCenterY == UNSET || mPeepHoleCenterX == UNSET) {
                mPeepHoleCenterX = mWidth / 2;
                mPeepHoleCenterY = mHeight / 2;
            }

            mCirclePaint.setAlpha(255);
            mCoverPaint.setAlpha(255);

            // add peephole and reveal animators to a set, so we can
            // freely add the listener without having to worry about
            // listener dupes
            AnimatorSet s = new AnimatorSet();
            s.play(mPeepHoleAnimator).with(mRevealAlphaAnimator);
            if (listener != null) {
                s.addListener(listener);
            }
            s.start();
        }

        @Override
        public void endAnimation() {
        }

        @Override
        public boolean cancelAnimation() {
            if (mPeepHoleAnimator == null || !mPeepHoleAnimator.isRunning()) {
                return false;
            } else {
                mPeepHoleAnimator.cancel();
                return true;
            }
        }
    }

    /*
     * SPRD Bug:519334 Refactor Rotation UI of Camera. @{
     */
    private int mDegree;

    @Override
    public void setOrientation(int degree, boolean animation) {
        mDegree = degree;
    }
    /* @} */

    public void updateList(int mode, int camera) {
        /*
        mCount = 0;
        if (mModeSelectorItems != null && mModeSelectorItems.size() > 0) {
            mListView.removeAllViews();
            Iterator ite = mModeSelectorItems.entrySet().iterator();
            while(ite.hasNext()){
                Map.Entry entry = (Map.Entry)ite.next();
                final ModeSelectorItem selectorItem = (ModeSelectorItem)entry.getValue();
                //if (selectorItem != null && selectorItem.isSupportMC(mode, camera)) {
                if (selectorItem != null) {
                    mCount = mCount + 1;
                    GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                    params.width = selectorItem.getMaxVisibleWidth();
                    params.height = getContext().getResources().getDimensionPixelSize(R.dimen.mode_selector_item_height);
                    selectorItem.setOrientation(mDegree, false);


                    if (selectorItem.getModeId() == INTENTCAPTURE || selectorItem.getModeId() == INTENTVIDEO || selectorItem.getModeId() == REFOCUS || selectorItem.getModeId() == PORTRAIT_PHOTO) {
                    } else {
                        if (camera == 1) {
                            if (selectorItem.getModeId() == QR_CODE || selectorItem.getModeId() == DREAM_PANORAMA) {
                            } else {
                                mListView.addView(selectorItem, params);
                            }
                        } else {
                            mListView.addView(selectorItem, params);
                        }
                    }
                }
            }
        }

         */
    }

    public int getModeListSize() {
        return mCount;
    }

    public  LinkedHashMap<Integer,ModeSelectorItem> getModeSelectorItems() {
        return mModeSelectorItems;
    }

    public boolean isModeInSelector(int modeId) {
        if (mModeSelectorItems != null && null != mModeSelectorItems.get(modeId)) {
            return true;
        }
        return false;
    }

    public boolean isSupportMC(int modeIndex, int camera) {
        int supportCamera = CameraUtil.mModuleInfoResolve.getModuleSupportCamera(modeIndex);
        return supportCamera == camera || -1 == supportCamera;
    }

    /**
     * SPRD: Fix bug 659315 optimize camera launch time @{
     * @param modeListHelper a container that contains supported modes
     */
    public void init(ModeListView.ModeListViewHelper modeListHelper) {
        if (!mHasInitialized) {
            mHasInitialized = true;
            mTitle = (TextView) findViewById(R.id.mode_list_title);
            mSettingsButton = findViewById(R.id.settings_button);
            mSettingsButton.setVisibility(View.GONE);
        }
        Log.d(TAG, "INIT BY helper : " + modeListHelper);
        if (modeListHelper == null) {
            mSupportedModes = new ArrayList<>(0);
            mTotalModes = 0;
            //mCameraSupportList = null;

            LayoutInflater inflater = AndroidServices.instance().provideLayoutInflater();
            final ModeSelectorItem selectorItem = (ModeSelectorItem) inflater
                    .inflate(R.layout.dream_mode_selector, null);
            //mModeSelectorItems = new ModeSelectorItem[] {selectorItem};
            mModeSelectorItems = new LinkedHashMap<>();
            if (mCurrentStateManager.getCurrentState() == null) {
                mCurrentStateManager.setCurrentState(new DummyState());
            }
            return;
        }

        mSupportedModes = modeListHelper.mSupportedModes;
        mTotalModes = modeListHelper.mTotalModes;
        //mCameraSupportList = modeListHelper.mCameraSupportList;
        mModeSelectorItems = modeListHelper.mModeSelectorItems;
        mTotalModes = mModeSelectorItems.size();
        /*
        for (int i = 0; i < mTotalModes; i++) {
            int moduleId = mSupportedModes.get(i);
            final ModeSelectorItem selectorItem = mModeSelectorItems.get(moduleId);
            selectorItem.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemSelected(selectorItem);
                }
            });
        }
         */
        // During drawer opening/closing, we change the visible width of the mode
        // items in sequence, so we listen to the last item's visible width change
        // for a good timing to do corresponding UI adjustments.
        //mModeSelectorItems.get(mSupportedModes.get(mTotalModes - 1)).setVisibleWidthChangedListener(this);

        mCurrentStateManager.setCurrentState(new FullyHiddenState());

        onModeListOpenRatioUpdate(0);
        initRecycleView(modeListHelper);
        mInitDone = true;
    }

    private RecyclerView mRecyclerView;

    public void initRecycleView (ModeListView.ModeListViewHelper modeListHelper) {
        mInsideGridMargin = getResources().getDimensionPixelSize(R.dimen.mode_selector_margin_left_right);
        mRecyclerView = (RecyclerView) findViewById(R.id.rv_main);
        mListView = mRecyclerView;

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(linearLayoutManager);

        MyRVAdapter2 adapter2 = new MyRVAdapter2(getContext());
        adapter2.setModeListSelectors(mModeSelectorItems, 0);
        PagerSnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(mRecyclerView);
        mRecyclerView.setAdapter(adapter2);
        mRecyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                event.setLocation(event.getX() - mInsideGridMargin, event.getY());
                onTouchEvent(event);
                return false;
            }
        });
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {

            }

            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (null != recyclerView && null != recyclerView.getAdapter()) {
                    if (recyclerView.getChildCount() > 1 && recyclerView.getChildCount() == recyclerView.getAdapter().getItemCount()) {
                        DreamOrientation.setOrientation(recyclerView, mDegree, false);
                    }
                }
                //Log.i(TAG, "recyclerView getChildCount=" + recyclerView.getChildCount() +
                // "getChild=" +  getChildAt(0));
            }
        });
    }

    public class MyRVAdapter2 extends RecyclerView.Adapter<MyRVAdapter2.MyTVHolder> {
        private LinkedHashMap<Integer, ModeSelectorItem> mModeSelectorItems;
        private final LayoutInflater mLayoutInflater;
        private final Context mContext;

        public MyRVAdapter2(Context context) {
            mLayoutInflater = LayoutInflater.from(context);
            mContext = context;
        }

        public void setModeListSelectors(LinkedHashMap<Integer, ModeSelectorItem> items, int pos) {
            mModeSelectorItems = items;
        }

        private boolean mModePagerInitialized = false;

        private void updateItemViews(GridLayout parent, int pageNumber) {
            int mCount = 0;
            LinkedHashMap<Integer, ModeSelectorItem> items;
            if (mModeSelectorItems == null) return;
            int totaleModeCount = mModeSelectorItems.size();
            if (totaleModeCount == 0) return;

            int needReadItemEndNum = (pageNumber + 1) * 12 - 1;//11, 23...
            int needReadItemStartNum = pageNumber * 12;//0, 12...

            parent.removeAllViews();
            Iterator ite = mModeSelectorItems.entrySet().iterator();
            int rememberSelectorItemsNumber = -1;
            Log.i(TAG, "updateList inAPP needReadItemEndNum=" + needReadItemEndNum + ", needReadItemStartNum=" + needReadItemStartNum);
            while (ite.hasNext()) {
                Map.Entry entry = (Map.Entry) ite.next();
                rememberSelectorItemsNumber++;
                if (needReadItemStartNum <= rememberSelectorItemsNumber && rememberSelectorItemsNumber <= needReadItemEndNum) {
                    final ModeSelectorItem selectorItem = (ModeSelectorItem) entry.getValue();
                    if (selectorItem != null) {
                        //&& selectorItem.isSupportMC(mode, camera)) {
                        mCount = mCount + 1;
                        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                        params.width = selectorItem.getMaxVisibleWidth();
                        /* SPRD: Fix bug 1404942 selectorItem incomplete display @{ */
                        //params.height = getContext().getResources().getDimensionPixelSize(R.dimen.mode_selector_item_height);

                        params.height = (int) (mCaptureLayoutHelper.getPreviewRect().width() * 4 / 3 / parent.getRowCount());
                        //int grid_margin = getContext().getResources().getDimensionPixelSize(R.dimen.mode_selector_grid_margin);
                        //int bottomBarTop = (int)mCaptureLayoutHelper.getBottomBarRect().top;
                        //params.height = (bottomBarTop - grid_margin)/parent.getRowCount();
                        /* @} */
                        selectorItem.setOrientation(mDegree, false);
                        //selectorItem params
                        selectorItem.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                onItemSelected(selectorItem);
                            }
                        });

                        parent.addView(selectorItem, params);
                    }
                }

            }
            Log.i(TAG, "updateList inAPP mListView.mCount=" + mCount);
        }

        @Override
        public MyRVAdapter2.MyTVHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            MyRVAdapter2.MyTVHolder holder = new MyRVAdapter2.MyTVHolder(mLayoutInflater.inflate(R.layout.item_mode_grid, parent, false));
            /*
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) parent.getLayoutParams();
            params.gravity = Gravity.TOP | Gravity.CENTER;
            parent.setLayoutParams(params);
            */
            //holder.setIsRecyclable(false);
            return holder;
        }

        @Override
        public void onBindViewHolder(final MyRVAdapter2.MyTVHolder holder, int pos) {
            updateItemViews(holder.modeList, pos);
            //holder.modeList.requestFocus();
        }

        @Override
        public int getItemCount() {
            return mModeSelectorItems.size() % 12 > 0 ? mModeSelectorItems.size() / 12 + 1 : mModeSelectorItems.size() / 12;
        }

        class MyTVHolder extends RecyclerView.ViewHolder {
            TextView mTextView;
            GridLayout modeList;

            MyTVHolder(View itemView) {
                super(itemView);
                modeList = (GridLayout) itemView.findViewById(R.id.mode_list);
            }
        }
    }

    private boolean mHasInitialized = false;
    private boolean mInitDone = false;

    public boolean isInitDone(){
        return mInitDone;
    }

    private class DummyState extends ModeListState {

    }

    // container that contains supported modes
    public static class ModeListViewHelper {
        private ArrayList<Integer> mSupportedModes;
        private int mTotalModes;
        //private ModeSelectorItem[] mModeSelectorItems;
        private LinkedHashMap<Integer,ModeSelectorItem> mModeSelectorItems;
        //private int[] mCameraSupportList;

        public ModeListViewHelper(Context context, List<Integer> modeIndexList) {
            mModeSelectorItems = new LinkedHashMap<>();
            mSupportedModes = new ArrayList<>();
            mSupportedModes.addAll(modeIndexList);
            mTotalModes = mSupportedModes.size();
            initializeModeSelectorItems(context);
        }

        private void initializeModeSelectorItems(Context context) {
            Log.d(TAG, "initializeModeSelectorItems :" + mTotalModes);
            LayoutInflater inflater = AndroidServices.instance().provideLayoutInflater();
            for (int i = 0; i < mTotalModes; i++) {
                int modeId = mSupportedModes.get(i);
                if (modeId == SettingsScopeNamespaces.INTENTCAPTURE || modeId == SettingsScopeNamespaces.INTENTVIDEO ||
                        modeId == SettingsScopeNamespaces.BACK_ULTRA_WIDE_ANGLE) {
                    continue;
                }
                boolean canFind = false;
                ArrayList<Integer> panelShowList = CameraUtil.mModuleInfoResolve.getModeSlideList();
                int panelSize = panelShowList.size();
                if (CameraCustomManager.I.isSupportMoreMode()){
                    panelSize = Math.min(panelShowList.size(), 7);
                }
                for (int j = 0; j < panelSize; j++) {
                    if (modeId == panelShowList.get(j)) {
                        canFind = true;
                    }
                }
                if (canFind) continue;

                int cameraSupport = CameraUtil.mModuleInfoResolve.getModuleSupportCamera(modeId);
                if (cameraSupport == 999/* none camera*/) continue;

                int[] mc = {
                        CameraUtil.mModuleInfoResolve.getModuleSupportMode(modeId),
                        CameraUtil.mModuleInfoResolve.getModuleSupportCamera(modeId),
                };
                final ModeSelectorItem selectorItem = (ModeSelectorItem) inflater.inflate(R.layout.dream_mode_selector, null);
                selectorItem.setMC(mc);
                selectorItem.setUnSelectedImageResource(CameraUtil.mModuleInfoResolve.getModuleUnselectIcon(modeId));
                //selectorItem.setSelectedImageResource(CameraUtil.mModuleInfoResolve.getModuleSelectIcon(modeId));

                // Set text
                String textString = CameraUtil.mModuleInfoResolve.getModuleText(modeId);
                selectorItem.setText(textString);
                selectorItem.setContentDescription(CameraUtil.mModuleInfoResolve.getModuleDescription(modeId));
                selectorItem.setModeId(modeId);
                selectorItem.setSelected(false);

                selectorItem.setDefaultBackgroundColor(context.getResources()
                        .getColor(R.color.dream_background_mode_item));

                //Log.i(TAG, "mModeSelectorItems put: id=" + modeId + ", name=" + textString);
                mModeSelectorItems.put(modeId, selectorItem);
            }
        }
    }
    /* @} */
}
