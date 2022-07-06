package com.freeme.camera.settings;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;

import com.android.camera.CameraActivity;
import com.freeme.camera.privacy.FreemePrivacyManager;
import com.freeme.updateself.update.UpdateMonitor;


public class FreemeUIPreferenceItemCheckUpdate extends Preference {
    public FreemeUIPreferenceItemCheckUpdate(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public FreemeUIPreferenceItemCheckUpdate(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public FreemeUIPreferenceItemCheckUpdate(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FreemeUIPreferenceItemCheckUpdate(Context context) {
        super(context);
    }

    @Override
    protected void onClick() {
        showAlertDialog();
    }

    public void showAlertDialog() {
        CameraActivity activity = (CameraActivity) getContext();
        if (!activity.isPrivacyEnable()) {
            activity.requestPrivacyEnable(state -> {
                if (state == FreemePrivacyManager.PRIVACY_RESULT_OK) {
                    startManualUpdate();
                }
            });
        }else {
            startManualUpdate();
        }
    }

    private void startManualUpdate() {
        UpdateMonitor.getAutoUpdate(getContext());
        UpdateMonitor.setAutoUpdate(getContext(), false);
        UpdateMonitor.doManualUpdate(getContext());
    }
}
