/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.camera;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;

import com.android.camera.debug.Log;
import com.android.camera.ui.RotateImageView;
import com.android.camera.ui.TouchCoordinate;

import java.util.ArrayList;
import java.util.List;

/**
 * A button designed to be used for the on-screen shutter button.
 * It's currently an {@code ImageView} that can call a delegate when the
 * pressed state changes.
 */

/*
 * SPRD Bug:519334 Refactor Rotation UI of Camera. @{
 * Original Android code:

public class ShutterButton extends ImageView {

 */
public class ShutterButton extends RotateImageView {
    private static final Log.Tag TAG = new Log.Tag("ShutterButton");
    public static final float ALPHA_WHEN_ENABLED = 1f;
    public static final float ALPHA_WHEN_DISABLED = 0.2f;
    private boolean mTouchEnabled = true;
    private TouchCoordinate mTouchCoordinate;
    private final GestureDetector mGestureDetector;

    private ObjectAnimator mScaleAnimation;

    /**
     * A callback to be invoked when a ShutterButton's pressed state changes.
     */
    public interface OnShutterButtonListener {
        /**
         * Called when a ShutterButton has been pressed.
         *
         * @param pressed The ShutterButton that was pressed.
         */
        void onShutterButtonFocus(boolean pressed);
        void onShutterCoordinate(TouchCoordinate coord);
        void onShutterButtonClick();

        /**
         * Called when shutter button is held down for a long press.
         */
        void onShutterButtonLongPressed();
    }

    /**
     * A gesture listener to detect long presses.
     */
    private class LongPressGestureListener extends SimpleOnGestureListener {
        @Override
        public void onLongPress(MotionEvent event) {
            for (OnShutterButtonListener listener : mListeners) {
                listener.onShutterButtonLongPressed();
            }
        }
    }

    private List<OnShutterButtonListener> mListeners
        = new ArrayList<OnShutterButtonListener>();
    private boolean mOldPressed;

    public ShutterButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        mGestureDetector = new GestureDetector(context, new LongPressGestureListener(), new Handler(context.getMainLooper()));
        mGestureDetector.setIsLongpressEnabled(true);
        PropertyValuesHolder scaleX =  PropertyValuesHolder.ofFloat("scaleX", 1.0f, 0.9f);
        PropertyValuesHolder scaleY =  PropertyValuesHolder.ofFloat("scaleY", 1.0f, 0.9f);
        mScaleAnimation = ObjectAnimator.ofPropertyValuesHolder(this, scaleX, scaleY);
        mScaleAnimation.setInterpolator(new AccelerateInterpolator());
        mScaleAnimation.setDuration(60);
    }

    /**
     * Add an {@link OnShutterButtonListener} to a set of listeners.
     */
    public void addOnShutterButtonListener(OnShutterButtonListener listener) {
        if (!mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }

    /**
     * Remove an {@link OnShutterButtonListener} from a set of listeners.
     */
    public void removeOnShutterButtonListener(OnShutterButtonListener listener) {
        if (mListeners.contains(listener)) {
            mListeners.remove(listener);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent m) {
        if (mTouchEnabled) {
            // Don't send ACTION_MOVE messages to gesture detector unless event motion is out of
            // shutter button view. A small motion resets the long tap status. A long tap should
            // be interpreted as the duration the finger is held down on the shutter button,
            // regardless of any small motions. If motion moves out of shutter button view, the
            // gesture detector needs to be notified to reset the long tap status.
            if (m.getActionMasked() != MotionEvent.ACTION_MOVE
                || m.getX() < 0 || m.getY() < 0
                || m.getX() >= getWidth() || m.getY() >= getHeight()) {
                mGestureDetector.onTouchEvent(m);
            }
            if (m.getActionMasked() == MotionEvent.ACTION_UP) {
                mTouchCoordinate = new TouchCoordinate(m.getX(), m.getY(), this.getMeasuredWidth(),
                        this.getMeasuredHeight());
            }
            return super.dispatchTouchEvent(m);
        } else {
            return false;
        }
    }

    public void enableTouch(boolean enable) {
        mTouchEnabled = enable;
    }
    public boolean isButtonPressed(){return isPressed();}
    /**
     * Hook into the drawable state changing to get changes to isPressed -- the
     * onPressed listener doesn't always get called when the pressed state
     * changes.
     */
    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        final boolean pressed = isPressed();

        if (pressed != mOldPressed) {
            if (!pressed) {
                // When pressing the physical camera button the sequence of
                // events is:
                //    focus pressed, optional camera pressed, focus released.
                // We want to emulate this sequence of events with the shutter
                // button. When clicking using a trackball button, the view
                // system changes the drawable state before posting click
                // notification, so the sequence of events is:
                //    pressed(true), optional click, pressed(false)
                // When clicking using touch events, the view system changes the
                // drawable state after posting click notification, so the
                // sequence of events is:
                //    pressed(true), pressed(false), optional click
                // Since we're emulating the physical camera button, we want to
                // have the same order of events. So we want the optional click
                // callback to be delivered before the pressed(false) callback.
                //
                // To do this, we delay the posting of the pressed(false) event
                // slightly by pushing it on the event queue. This moves it
                // after the optional click notification, so our client always
                // sees events in this sequence:
                //     pressed(true), optional click, pressed(false)
                post(new Runnable() {
                    @Override
                    public void run() {
                        callShutterButtonFocus(pressed);
                    }
                });
            } else {
                callShutterButtonFocus(pressed);
            }
            mOldPressed = pressed;
        }
    }

    private void callShutterButtonFocus(boolean pressed) {
        for (OnShutterButtonListener listener : mListeners) {
            listener.onShutterButtonFocus(pressed);
        }
        if (pressed) {
            mScaleAnimation.start();
        } else {
            mScaleAnimation.reverse();
        }
    }

    @Override
    public boolean performClick() {
        Log.i(TAG,"ShutterButton performClick");
        boolean result = super.performClick();
        // SPRD: Fix Bug 616857 At the same time click on the record video and thumbnail buttons,
        // recording video interface abnormalities
        if (getVisibility() == View.VISIBLE && isClickable()
                && (getContext() != null && !((CameraActivity) getContext()).isWaitToChangeMode())) {
            for (OnShutterButtonListener listener : mListeners) {
                listener.onShutterCoordinate(mTouchCoordinate);
                mTouchCoordinate = null;
                listener.onShutterButtonClick();
            }
        }
        return result;
    }

    /* SPRD:fix bug 473462 add burst capture @*/
    public boolean isOnTouchInside(MotionEvent ev, int pointId) {
        int pointIndex = ev.findPointerIndex(pointId);
        if (pointIndex < 0) return true;
        float currentX = ev.getX(pointIndex);
        float currentY = ev.getY(pointIndex);
        /*SPRD:modify for Coverity 108026
         * Orginal android code
        int width = getMeasuredWidth();
        int hight = getMeasuredHeight();
         */
        if (currentX > 0 && currentX < getMeasuredWidth() && currentY > 0 && currentY < getMeasuredHeight()) {
            return true;
        } else {
            return false;
        }
    }
    /* @} */

    /* SPRD:Add for bug 479120 wideangle module rotate issue @{ */
    @Override
    public void setOrientation(int degree, boolean animation) {
        super.setOrientation(degree, animation);
    }
    /* @} */
}
