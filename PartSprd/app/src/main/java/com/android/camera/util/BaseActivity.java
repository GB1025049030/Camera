package com.android.camera.util;

import android.app.Activity;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.view.Display;

import java.lang.reflect.Method;

public class BaseActivity extends Activity {
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (newConfig.fontScale != 1) getResources();
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        if (res.getConfiguration().fontScale != 1) {
            Configuration newConfig = new Configuration();
            newConfig.setToDefaults();
            newConfig.densityDpi = getDefaultDisplayDensity();
            res.updateConfiguration(newConfig, res.getDisplayMetrics());
        }
        return res;
    }

    private static int getDefaultDisplayDensity() {
        try {
            Class clazz = Class.forName("android.view.WindowManagerGlobal");
            Method method = clazz.getMethod("getWindowManagerService");
            method.setAccessible(true);
            Object iwm = method.invoke(clazz);
            Method getInitialDisplayDensity = iwm.getClass().getMethod("getInitialDisplayDensity", int.class);
            getInitialDisplayDensity.setAccessible(true);
            Object densityDpi = getInitialDisplayDensity.invoke(iwm, Display.DEFAULT_DISPLAY);
            return (int)densityDpi;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

}
