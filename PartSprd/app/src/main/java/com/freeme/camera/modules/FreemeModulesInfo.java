
package com.freeme.camera.modules;

import android.content.Context;
import android.content.Intent;

import com.android.camera.app.AppController;
import com.android.camera.app.ModuleManager;
import com.android.camera.debug.Log;
import com.android.camera.module.ModuleController;
import com.dream.camera.modules.EffectVideo.EffectVideoModule;
import com.dream.camera.modules.slowvideo.SlowVideoModule;
import com.freeme.camera.modules.ikophoto.IKOPhotoModule;
import com.freeme.camera.modules.nightphoto.NightPhotoModule;
import com.freeme.camera.modules.openglfilter.depthblurphoto.DepthBlurPhotoModule;
import com.freeme.camera.modules.openglfilter.effectphoto.EffectPhotoModule;
import com.freeme.camera.modules.slrphoto.SlrPhotoModule;
import com.freeme.camera.settings.FreemeSettingsScopeNamespaces;
import com.freeme.utils.FreemeCameraUtil;

public class FreemeModulesInfo {
    private static final Log.Tag TAG = new Log.Tag("FreemeModulesInfo");

    public static void setupModules(Context context, ModuleManager moduleManager) {
        setupFreemeModules(context, moduleManager);
    }

    // Freeme Camera Modules
    public static void setupFreemeModules(Context context, ModuleManager moduleManager) {
        if (FreemeCameraUtil.isSlrPhotoEnabled()) {
            registerSlrPhotoModule(moduleManager, FreemeSettingsScopeNamespaces.SLR_PHOTO);
        }

        if (FreemeCameraUtil.isIKOPhotoEnabled()) {
            registerIKOPhotoModule(moduleManager, FreemeSettingsScopeNamespaces.IKO_PHOTO);
        }

        if(FreemeCameraUtil.isEffectVideoEnabled()) {
            registerEffectVideoModule(moduleManager, FreemeSettingsScopeNamespaces.EFFECT_VIDEO);
        }

        if(FreemeCameraUtil.isSlowVideoEnable()) {
            registerSlowVideoModule(moduleManager, FreemeSettingsScopeNamespaces.SLOW_VIDEO);
        }

        if (FreemeCameraUtil.isDepthBlurPhotoEnabled()) {
            registerDepthBlurPhotoModule(moduleManager, FreemeSettingsScopeNamespaces.DEPTH_BLUR_PHOTO);
        }

        if (FreemeCameraUtil.isByteDanceEnabled()) {
            registerEffectPhotoModule(moduleManager);
            registerNightPhotoModule(moduleManager);
        }
    }

    private static void registerIKOPhotoModule(ModuleManager moduleManager, final int moduleId) {
        moduleManager.registerModule(new ModuleManager.ModuleAgent() {
            @Override
            public int getModuleId() {
                return moduleId;
            }

            @Override
            public boolean requestAppForCamera() {
                return true;
            }

            @Override
            public ModuleController createModule(AppController app, Intent intent) {
                return new IKOPhotoModule(app);
            }
        });
    }

    private static void registerSlrPhotoModule(ModuleManager moduleManager, final int moduleId) {
        moduleManager.registerModule(new ModuleManager.ModuleAgent() {
            @Override
            public int getModuleId() {
                return moduleId;
            }

            @Override
            public boolean requestAppForCamera() {
                return true;
            }

            @Override
            public ModuleController createModule(AppController app, Intent intent) {
                return new SlrPhotoModule(app);
            }
        });
    }

    private static void registerEffectVideoModule(ModuleManager moduleManager, final int moduleId) {
        moduleManager.registerModule(new ModuleManager.ModuleAgent() {
            @Override
            public int getModuleId() {
                return moduleId;
            }

            @Override
            public boolean requestAppForCamera() {
                return true;
            }

            @Override
            public ModuleController createModule(AppController app, Intent intent) {
                return new EffectVideoModule(app);
            }
        });
    }

    private static void registerSlowVideoModule(ModuleManager moduleManager, final int moduleId) {
        moduleManager.registerModule(new ModuleManager.ModuleAgent() {
            @Override
            public int getModuleId() {
                return moduleId;
            }

            @Override
            public boolean requestAppForCamera() {
                return true;
            }

            @Override
            public ModuleController createModule(AppController app, Intent intent) {
                return new SlowVideoModule(app);
            }
        });
    }

    private static void registerDepthBlurPhotoModule(ModuleManager moduleManager, final int moduleId) {
        moduleManager.registerModule(new ModuleManager.ModuleAgent() {
            @Override
            public int getModuleId() {
                return moduleId;
            }

            @Override
            public boolean requestAppForCamera() {
                return true;
            }

            @Override
            public ModuleController createModule(AppController app, Intent intent) {
                return new DepthBlurPhotoModule(app);
            }
        });
    }

    private static void registerEffectPhotoModule(ModuleManager moduleManager) {
        moduleManager.registerModule(new ModuleManager.ModuleAgent() {
            @Override
            public int getModuleId() {
                return FreemeSettingsScopeNamespaces.EFFECT_PHOTO;
            }
            @Override
            public boolean requestAppForCamera() {
                return true;
            }
            @Override
            public ModuleController createModule(AppController app, Intent intent) {
                return new EffectPhotoModule(app);
            }
        });
    }

    private static void registerNightPhotoModule(ModuleManager moduleManager) {
        moduleManager.registerModule(new ModuleManager.ModuleAgent() {
            @Override
            public int getModuleId() {
                return FreemeSettingsScopeNamespaces.NIGHT_PHOTO;
            }
            @Override
            public boolean requestAppForCamera() {
                return true;
            }
            @Override
            public ModuleController createModule(AppController app, Intent intent) {
                return new NightPhotoModule(app);
            }
        });
    }
}
