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
/**
 * SPRD:fix bug474690 add for pano feature
 */
package com.android.camera;

public class Depth {

    static {

        System.loadLibrary("jni_depth");

    }

    //focal_length1 == focal_length2 
     
    //extra_param_len = 256
    public native int DistanceCalc (byte depth_array[] , byte yuv_array[], int h_yuv, int w_yuv,
     
            int h, int w, int h_org, int w_org, int x1, int y1, int x2, int y2, float focal_length1, float focal_length2, 
     
            float pixelSize, float extra_params[], int extra_params_len);
     
    public native int ResultDepth();
}

