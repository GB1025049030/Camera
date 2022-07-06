package com.freeme.camera.privacy;

import android.annotation.NonNull;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.android.camera2.R;
import com.dream.camera.settings.DataModuleManager;
import com.freeme.camera.settings.FreemeKeys;

import java.util.ArrayList;
import java.util.List;

public enum FreemePrivacyManager {
    I;
    public static final String ACTION_PRIVACY = "com.freeme.intent.action.PRIVACY";
    public static final String EXTRA_TIPS_CONTENT = "tipsContent";
    public static final String EXTRA_PRIVACY_CONTENT = "privacyContent";
    public static final String EXTRA_PACKAGE_NAME = "packageName";
    public static final int PRIVACY_REQUEST_CODE = 0x10;
    public static final int PRIVACY_RESULT_FIRST = 0;
    public static final int PRIVACY_RESULT_OK = 1;
    public static final int PRIVACY_RESULT_CANCELED = 2;

    private final List<OnStateChangeListener> mOnStateChangeListeners = new ArrayList<>();

    public int getState(@NonNull Context context) {
        return DataModuleManager.getInstance(context).getDataModuleCamera()
                .getInt(FreemeKeys.KEY_CAMERA_PRIVACY_ENABLE, PRIVACY_RESULT_FIRST);
    }

    public void setState(@NonNull Context context, int state) {
        setState(context, state, null);
    }


    public void setState(@NonNull Context context, int state, OnStateChangeCompleteListener complete) {
        if (getState(context) != state) {
            DataModuleManager.getInstance(context).getDataModuleCamera()
                    .set(FreemeKeys.KEY_CAMERA_PRIVACY_ENABLE, state);
            for (OnStateChangeListener listener : mOnStateChangeListeners) {
                listener.onChange(state);
            }
            if (complete != null) {
                complete.onChangeComplete(state);
            }
        }
    }

    public void checkState(@NonNull Context context) {
        if (getState(context) > PRIVACY_RESULT_CANCELED) {
            setState(context, PRIVACY_RESULT_CANCELED);
        }
    }

    public void registerListener(@NonNull Context context, @NonNull OnStateChangeListener listener) {
        if (listener != null) {
            listener.onChange(getState(context));
            mOnStateChangeListeners.add(listener);
        }
    }

    public void unregisterListener(@NonNull OnStateChangeListener listener) {
        if (listener != null) {
            mOnStateChangeListeners.remove(listener);
        }
    }

    //*/
    public boolean isPrivacyEnable(Activity activity) {
        Intent intent = new Intent(ACTION_PRIVACY);
        return intent.resolveActivity(activity.getPackageManager()) != null;
    }

    public void startPrivacyActivity(Activity activity) {
        Intent intent = new Intent(activity, FreemeCameraPrivacyActivity.class);
        intent.putExtra(EXTRA_TIPS_CONTENT, activity.getString(R.string.tips_content_text));
        intent.putExtra(EXTRA_PRIVACY_CONTENT, activity.getString(R.string.privacy_content_text_prefix)+
                activity.getString(R.string.privacy_content_text_main));
        intent.putExtra(EXTRA_PACKAGE_NAME, activity.getPackageName());
        activity.startActivityForResult(intent, FreemePrivacyManager.PRIVACY_REQUEST_CODE);
    }

    public interface OnStateChangeListener {
        /**
         * on privacy state change
         *
         * @param state current privacy state
         */
        void onChange(int state);
    }

    public interface OnStateChangeCompleteListener {
        /**
         * on privacy state change complete
         *
         * @param state current privacy state
         */
        void onChangeComplete(int state);
    }
}
