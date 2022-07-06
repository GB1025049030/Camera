package com.freeme.camera.common;

import android.app.Application;
import android.os.Build;

import java.lang.reflect.InvocationTargetException;

public class Utils {

    private static Application sApplication;

    public static Application getApplication() {
        if (sApplication != null) {
            return sApplication;
        }
        Application app = getApplicationByReflect();
        init(app);
        return app;
    }

    private static Application getApplicationByReflect() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                //Reflection.exemptAll();
            }
            Class<?> activityThread = Class.forName("android.app.ActivityThread");
            Object thread = activityThread.getMethod("currentActivityThread").invoke(null);
            Object app = activityThread.getMethod("getApplication").invoke(thread);
            if (app == null) {
                throw new NullPointerException("u should init first");
            }
            return (Application) app;
        } catch (ClassNotFoundException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        throw new NullPointerException("u should init first");
    }

    private static void init(final Application app) {
        if (sApplication == null) {
            sApplication = app == null ? getApplicationByReflect() : app;
        } else {
            if (app != null && app.getClass() != sApplication.getClass()) {
                sApplication = app;
            }
        }
    }
}
