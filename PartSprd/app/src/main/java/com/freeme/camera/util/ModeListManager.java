package com.freeme.camera.util;

import android.util.SparseArray;

import com.android.camera.settings.SettingsScopeNamespaces;
import com.freeme.camera.settings.FreemeSettingsScopeNamespaces;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class ModeListManager {
    private static final int[] MODES = {
            SettingsScopeNamespaces.DREAM_PANORAMA,
            SettingsScopeNamespaces.REFOCUS,
            SettingsScopeNamespaces.AUTO_PHOTO,
            SettingsScopeNamespaces.MANUAL,
            SettingsScopeNamespaces.CONTINUE,
            SettingsScopeNamespaces.INTERVAL,
            SettingsScopeNamespaces.SCENE,
            SettingsScopeNamespaces.PIP,
            SettingsScopeNamespaces.GCAM,
            SettingsScopeNamespaces.AUTO_VIDEO,
            SettingsScopeNamespaces.VIV,
            SettingsScopeNamespaces.TIMELAPSE,
            FreemeSettingsScopeNamespaces.SLR_PHOTO,
            FreemeSettingsScopeNamespaces.DEPTH_BLUR_PHOTO,
            SettingsScopeNamespaces.SLOWMOTION,
            SettingsScopeNamespaces.AUDIO_PICTURE,
            SettingsScopeNamespaces.FILTER,
            SettingsScopeNamespaces.QR_CODE,
            FreemeSettingsScopeNamespaces.IKO_PHOTO,
            SettingsScopeNamespaces.FRONT_BLUR,
            SettingsScopeNamespaces.TDNR_PHOTO,
            SettingsScopeNamespaces.TDNR_VIDEO,
            SettingsScopeNamespaces.INTENTCAPTURE,
            SettingsScopeNamespaces.INTENTVIDEO,
            SettingsScopeNamespaces.AR_PHOTO,
            SettingsScopeNamespaces.AR_VIDEO,
            SettingsScopeNamespaces.BACK_ULTRA_WIDE_ANGLE,
            SettingsScopeNamespaces.PORTRAIT_PHOTO,
            SettingsScopeNamespaces.HIGH_RESOLUTION_PHOTO,
            SettingsScopeNamespaces.MACRO_PHOTO,
            SettingsScopeNamespaces.MACRO_VIDEO,
            SettingsScopeNamespaces.IR_PHOTO,
            FreemeSettingsScopeNamespaces.EFFECT_VIDEO,
            FreemeSettingsScopeNamespaces.EFFECT_PHOTO,
            FreemeSettingsScopeNamespaces.NIGHT_PHOTO
    };

    private static final SparseArray<Integer> MODE_ARRAY = new SparseArray<Integer>() {
        {
            for (int i = 0; i < MODES.length; i++) {
                put(MODES[i], i);
            }
        }
    };

    public static ArrayList<Integer> sortModeSupportList(List<Integer> supportedModes) {
        TreeMap<Integer, Integer> result = new TreeMap<>();
        int maxId = Math.max(MODES.length, supportedModes.size());
        for (int i = 0; i < supportedModes.size(); i++) {
            int modeId = supportedModes.get(i);
            if (isContains(MODE_ARRAY, modeId)) {
                result.put(MODE_ARRAY.get(modeId), modeId);
            } else {
                result.put(i + maxId, modeId);
            }
        }
        return new ArrayList<>(result.values());
    }

    public static boolean isContains(SparseArray array, int key){
        return array.indexOfKey(key) >= 0;
    }
}

