/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.freeme.camera.settings;

public final class FreemeSettingsScopeNamespaces {

    // max id in SettingsScopeNamespaces.java
    public static final int MAX_NAMESPACE = 33;

    // slr
    public static final int SLR_PHOTO = MAX_NAMESPACE + 1;
    public static final int ORDER_SLR_PHOTO = SLR_PHOTO;

    // iko
    public static final int IKO_PHOTO = MAX_NAMESPACE + 2;
    public static final int ORDER_IKO_PHOTO = IKO_PHOTO;

    // effect video
    public static final int EFFECT_VIDEO = MAX_NAMESPACE + 3;
    public static final int ORDER_EFFECT_VIDEO = EFFECT_VIDEO;

    // slow video
    public static final int SLOW_VIDEO= MAX_NAMESPACE + 4;
    public static final int ORDER_SLOW_VIDEO = SLOW_VIDEO;

    //depth blur
    public static final int DEPTH_BLUR_PHOTO = MAX_NAMESPACE + 5;

    // effect photo
    public static final int EFFECT_PHOTO = MAX_NAMESPACE + 6;

    // night photo
    public static final int NIGHT_PHOTO = MAX_NAMESPACE + 7;

    public static final int MODE_MORE_TAG = 999;
}
