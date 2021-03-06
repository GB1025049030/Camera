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

package com.android.camera;

import android.view.View;

import com.android.camera.ShutterButton.OnShutterButtonListener;
import com.android.camera.settings.Keys;
import com.android.camera.util.CameraUtil;
import com.dream.camera.MakeupController.MakeupListener;
import com.dream.camera.settings.DataModuleManager;

public interface VideoController extends OnShutterButtonListener, MakeupListener{

    public void onReviewDoneClicked(View view);
    public void onReviewCancelClicked(View viwe);
    public void onReviewPlayClicked(View view);

    public boolean isVideoCaptureIntent();
    public boolean isInReviewMode();
    public void onZoomChanged(float ratio);

    public void onSingleTapUp(View view, int x, int y);

    public void stopPreview();

    public void updateCameraOrientation();
    public void updatePreviewAspectRatio(float aspectRatio);

    // Callbacks for camera preview UI events.
    public void onPreviewUIReady();
    public void onPreviewUIDestroyed();
    // make up
    public boolean isMakeUpEnable();
}
