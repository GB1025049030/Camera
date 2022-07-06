package com.freeme.camera.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;

import com.android.camera.debug.Log;
import com.android.camera.util.CameraUtil;
import com.android.camera2.BuildConfig;
import com.android.camera2.R;
import com.android.camera.settings.SettingsScopeNamespaces;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.android.camera.app.CameraApp;
import com.android.camera.app.ModuleManagerImpl;
import com.freeme.camera.common.custom.CameraCustomManager;
import com.freeme.camera.settings.FreemeSettingsScopeNamespaces;

/**
 * Created by SPREADTRUM\matchbox.chang on 17-12-28.
 */

public class ModuleInfoResolve {

    public static class ModuleItem {
        public int moduelId;
        public int visible;
        public int cameraSupprot;
        public int modeSupport;
        //public String nameSpace;
        public String text;
        public String desc;
        public int unSelectIconId;
        public int selectIconId;
        public int coverIconId;
        public int captureIconId;
    }

    private static final Log.Tag TAG = new Log.Tag("ModuleInfoResolve");
    private final ArrayList<Integer> mVisibleModuleList;
    private final HashMap<Integer, ModuleItem> mModuleInfo;
    private final HashMap<Integer, Integer> mModuleScrollInfo;
    private int mDisplayModuleSize = 7;

    private static final int[] SLIDESHOW_STANDARD = new int[]{
            SettingsScopeNamespaces.MACRO_PHOTO,
            SettingsScopeNamespaces.TDNR_PHOTO,
            SettingsScopeNamespaces.PORTRAIT_PHOTO,
            SettingsScopeNamespaces.MANUAL,
            SettingsScopeNamespaces.AUTO_VIDEO,
            SettingsScopeNamespaces.CONTINUE,
            SettingsScopeNamespaces.FILTER,
            SettingsScopeNamespaces.REFOCUS,
            SettingsScopeNamespaces.DREAM_PANORAMA,
            SettingsScopeNamespaces.SLOWMOTION,
            SettingsScopeNamespaces.TIMELAPSE,
            SettingsScopeNamespaces.TDNR_VIDEO
    };

    private static final int[] SLIDESHOW_STANDARD_CARE = new int[]{
            SettingsScopeNamespaces.TDNR_PHOTO,
            SettingsScopeNamespaces.FILTER,
            SettingsScopeNamespaces.AUTO_PHOTO,
            SettingsScopeNamespaces.AUTO_VIDEO,
    };

    private int[] slideshow_standard;

    public ModuleInfoResolve() {
        mVisibleModuleList = new ArrayList<>();
        mModuleInfo = new HashMap<>();
        mModuleScrollInfo = new HashMap<>();
        if ("care".equals(BuildConfig.FLAVOR)) {
            slideshow_standard = SLIDESHOW_STANDARD_CARE;
        } else {
            slideshow_standard = SLIDESHOW_STANDARD;
        }
    }

    private ModuleManagerImpl mModuleManager;

    public void setModuleManager(ModuleManagerImpl moduleManager) {
        mModuleManager = moduleManager;
    }


    private void getModuleInfo(TypedArray moduleRes) {
        if (moduleRes == null) {
            Log.e(TAG, "resolve module return null");
            return;
        }
        int moduleId = -1;
        ModuleItem item = new ModuleItem();
        moduleId = moduleRes.getInteger(0, -1);
        if (1 == moduleRes.getInteger(1, 0)) {
            mVisibleModuleList.add(moduleId);
        }
        //init ModuleItem
        item.moduelId = moduleId;
        item.visible = moduleRes.getInteger(1, 0);
        item.cameraSupprot = moduleRes.getInteger(2, 0);
        item.modeSupport = moduleRes.getInteger(3, 0);
        //item.nameSpace = moduleRes.getString(4);
        item.text = moduleRes.getString(5);
        item.desc = moduleRes.getString(6);
        item.unSelectIconId = moduleRes.getResourceId(7, -1);
        item.selectIconId = moduleRes.getResourceId(8, -1);
        item.coverIconId = moduleRes.getResourceId(9, -1);
        item.captureIconId = moduleRes.getResourceId(10, -1);
        mModuleInfo.put(moduleId, item); //all module info from array: module_list
    }

    private void updateModuleInfo(Context context) {
        updatePortraitModuleInfo(context);
    }

    public void resolve(Context context) {
        synchronized (this) {
            mVisibleModuleList.clear();
            mModuleScrollInfo.clear();
            initModuleInfoFromArray(context); //only update once.
            updateModuleInfo(context); //update all module info base feature list.
        }
        initFreemeSlideshowStandard();
        buildModeSlideList(context);
        buildNonMainList();
    }

    private void initModuleInfoFromArray(Context context) {
        if (!CameraApp.backGroundConfigChanged && mModuleInfo.size() > 0) return;
        CameraApp.backGroundConfigChanged = false;
        mModuleInfo.clear();
        //update mModuleInfo, read from 'module_list'.
        Log.d(TAG, "resolve initModuleInfoFromArray update moduleinfo");
        TypedArray moduleListRes = context.getResources().obtainTypedArray(R.array.module_list);
        TypedArray moduleRes = null;
        if (moduleListRes == null) {
            Log.e(TAG, "resolve module list array return null");
            return;
        }
        for (int i = 0; i < moduleListRes.length(); i++) {
            int moduleResId = moduleListRes.getResourceId(i, -1);
            if (moduleResId < 0) {
                continue;
            }
            moduleRes = context.getResources().obtainTypedArray(moduleResId);
            getModuleInfo(moduleRes);
            moduleRes.recycle();
        }
        moduleListRes.recycle();
    }

    private final ArrayList<Integer> mNonMainList = new ArrayList<>();

    private void buildNonMainList() {
        synchronized (this) {
            mNonMainList.clear();
            int size = mModeSlideList.size();
            List<Integer> supportedModeIndexList = mModuleManager.getSupportedModeIndexList();
            int length = supportedModeIndexList.size();
            for (int i = 0; i < length; i++) {
                Integer objectValue = supportedModeIndexList.get(i);
                if (null == objectValue) continue;
                boolean canFind = false;
                for (int j = 0; j < size; j++) {
                    if (objectValue.equals(mModeSlideList.get(j))) {
                        canFind = true;
                    }
                }
                if (!canFind) mNonMainList.add(objectValue);
            }
        }
    }

    private final ArrayList<Integer> mModeSlideList = new ArrayList<>();

    public ArrayList<Integer> getModeSlideList() {
        ArrayList<Integer> tmpList;
        synchronized (this) {
            tmpList = new ArrayList<>(mModeSlideList);
        }
        return tmpList;
    }

    private void buildModeSlideList(Context context) {
        synchronized (this) {
            mModeSlideList.clear();
            ArrayList<Integer> tmpModeList = new ArrayList<>();
            List<Integer> supportedModeIndexList = mModuleManager.getSupportedModeIndexList();
            int normalListModuleSize;
            for (int value : slideshow_standard) {
                if (!supportedModeIndexList.contains(value)) {
                    if (SettingsScopeNamespaces.REFOCUS == value
                            && supportedModeIndexList.contains(SettingsScopeNamespaces.FRONT_BLUR)) {
                        value = SettingsScopeNamespaces.FRONT_BLUR;
                    } else if (SettingsScopeNamespaces.REFOCUS == value
                            && supportedModeIndexList.contains(FreemeSettingsScopeNamespaces.SLR_PHOTO)) {
                        value = FreemeSettingsScopeNamespaces.SLR_PHOTO;
                    } else if (SettingsScopeNamespaces.REFOCUS == value
                            && supportedModeIndexList.contains(FreemeSettingsScopeNamespaces.DEPTH_BLUR_PHOTO)){
                        value = FreemeSettingsScopeNamespaces.DEPTH_BLUR_PHOTO;
                    }else {
                        continue;
                    }
                }
                ModuleItem item = mModuleInfo.get(value);
                if (value == SettingsScopeNamespaces.AUTO_PHOTO) {
                    continue;
                }
                if (item == null || item.cameraSupprot == 999 || item.modeSupport == 999) {
                    //replace this null item with spare module.
                    continue;
                }
                tmpModeList.add(item.moduelId);
            }

            if (CameraCustomManager.I.isSupportMoreMode()) {
                //keep display module size odd limit(3,5,7)
                while (tmpModeList.size() < mDisplayModuleSize - 2) {
                    mDisplayModuleSize -= 2;
                    if (mDisplayModuleSize == 3) {
                        break;
                    }
                }
                normalListModuleSize = mDisplayModuleSize - 1;
            } else {
                //add auto photo
                normalListModuleSize = tmpModeList.size() + 1;
            }
            //keep auto photo in middle(auto photo module id is 0)
            tmpModeList.add(normalListModuleSize / 2, 0);

            for (int i = 0; i < normalListModuleSize; i++) {
                mModuleScrollInfo.put(tmpModeList.get(i)/*modeindex*/, i/*modeadapter index*/);
                mModeSlideList.add(tmpModeList.get(i));
            }
            if (CameraCustomManager.I.isSupportMoreMode()) {
                mModeSlideList.add(FreemeSettingsScopeNamespaces.MODE_MORE_TAG);
                mModuleScrollInfo.put(FreemeSettingsScopeNamespaces.MODE_MORE_TAG/*modeindex*/, mDisplayModuleSize/*modeadapter index*/);
            }
        }
    }

    private void initFreemeSlideshowStandard() {
        final int[] customSlideModes = getCustomSlideModeList();
        if (customSlideModes != null) {
            slideshow_standard = customSlideModes;
        } else {
            if (!CameraCustomManager.I.isSupportMoreMode()) {
                slideshow_standard = mModuleManager.getSupportedModeIndexList().stream().mapToInt(Integer::intValue).toArray();
            }
        }
    }

    public String getModuleText(int moduleId) {
        ModuleItem item = mModuleInfo.get(moduleId);
        if (item == null) {
            Log.e(TAG, "moduleId :" + moduleId + " is not support");
            return null;
        }
        return item.text;
    }

    public String getModuleDescription(int moduleId) {
        ModuleItem item = mModuleInfo.get(moduleId);
        if (item == null) {
            Log.e(TAG, "moduleId :" + moduleId + " is not support");
            return null;
        }
        return item.desc;
    }

    public int getModuleUnselectIcon(int moduleId) {
        ModuleItem item = mModuleInfo.get(moduleId);
        if (item == null) {
            Log.e(TAG, "moduleId :" + moduleId + " is not support");
            //return -1;
            return R.drawable.ic_auto_mode_sprd_unselected;
        }
        return item.unSelectIconId;
    }

    public int getModuleCoverIcon(int moduleId) {
        ModuleItem item = mModuleInfo.get(moduleId);
        if (item == null) {
            Log.e(TAG, "moduleId :" + moduleId + " is not support");
            //return -1;
            return R.drawable.ic_camera_blanket;
        }
        return item.coverIconId;
    }

    public int getModuleCaptureIcon(int moduleId) {
        ModuleItem item = mModuleInfo.get(moduleId);
        if (item == null) {
            Log.e(TAG, "moduleId :" + moduleId + " is not support");
            //return -1;
            return R.drawable.ic_capture_camera_sprd;
        }
        return item.captureIconId;
    }

    public int getModuleSupportMode(int moduleId) {
        ModuleItem item = mModuleInfo.get(moduleId);
        if (item == null) {
            Log.e(TAG, "moduleId :" + moduleId + " is not support");
            return -1;
        }
        return item.modeSupport;
    }

    public int getModuleSupportCamera(int moduleId) {
        ModuleItem item = mModuleInfo.get(moduleId);
        if (item == null) {
            Log.e(TAG, "moduleId :" + moduleId + " is not support");
            return -1;
        }
        return item.cameraSupprot;
    }

    public int getModuleScrollAdapterIndex(int moduleId) {
        Integer object = mModuleScrollInfo.get(moduleId);
        Integer objectDefault = mModuleScrollInfo.get(SettingsScopeNamespaces.AUTO_PHOTO);
        if (objectDefault == null) {
            return 0;
        }
        int modeidG = objectDefault;
        if (null != object) {
            modeidG = object;
        }
        return modeidG;
    }

    // update portrait module
    private void updatePortraitModuleInfo(Context context) {
        int moduleId = -1;
        TypedArray mModuleArray = null;
        mModuleArray = context.getResources().obtainTypedArray(R.array.portrait_photo_module);
        moduleId = mModuleArray.getInteger(0, -1);
        if (moduleId != -1) {
            ModuleItem item = mModuleInfo.get(moduleId);
            item.cameraSupprot = context.getResources().getInteger(R.integer.camera_support_none);
            if (CameraUtil.isPortraitPhotoEnable()) {
                item.cameraSupprot = context.getResources().getInteger(R.integer.camera_support_all);
            } else if (CameraUtil.isFrontPortraitPhotoEnable()) {
                item.cameraSupprot = context.getResources().getInteger(R.integer.camera_support_front);
            } else if (CameraUtil.isBackPortraitPhotoEnable()) {
                item.cameraSupprot = context.getResources().getInteger(R.integer.camera_support_back);
            }
        }
        mModuleArray.recycle();
    }

    public int[] getCustomSlideModeList() {
        final String[] splits = {",", "-", "_"};
        int[] slideModes = null;
        String customSlideModeArray = CameraCustomManager.I.getCustomSlideModeList();
        String[] tmpSlideModes;
        String splitStr = "";
        if (!TextUtils.isEmpty(customSlideModeArray)) {
            for (String split : splits) {
                if (customSlideModeArray.contains(split)) {
                    splitStr = split;
                }
            }
            if (!TextUtils.isEmpty(splitStr)) {
                tmpSlideModes = customSlideModeArray.split(splitStr);
                if (tmpSlideModes.length > 1) {
                    slideModes = new int[tmpSlideModes.length];
                    for (int i = 0; i < tmpSlideModes.length; i++) {
                        try {
                            slideModes[i] = Integer.parseInt(tmpSlideModes[i].trim());
                        } catch (NumberFormatException e) {
                            return null;
                        }
                    }
                }
            }
        }
        return slideModes;
    }
}
