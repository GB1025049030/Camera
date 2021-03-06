/*
 * Copyright (C) 2012 The Android Open Source Project
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

import com.android.camera.debug.Log;

import android.content.Intent;
import android.os.Bundle;
import  java.util.ArrayList;
import android.net.Uri;
import android.content.ContentUris;

// Use a different activity for secure camera only. So it can have a different
// task affinity from others. This makes sure non-secure camera activity is not
// started in secure lock screen.
public class SecureCameraActivity extends CameraActivity {
    private static final Log.Tag TAG = new Log.Tag("SecureCameraActivity Dream ");
    @Override
    public void onCreateTasks(Bundle state) {
        super.onCreateTasks(state);
        securePhotoList = new ArrayList<Long>();
    }

    @Override
    public void onNewIntentTasks(Intent intent) {
        super.onNewIntentTasks(intent);
        alreadyTriggerOnce = false;
        if(securePhotoList != null){
            securePhotoList.clear();
        }
        if(mDataAdapter != null){
            mDataAdapter.clear();
        }
    }

    @Override
    public void onDestroyTasks() {
        super.onDestroyTasks();
        securePhotoList.clear();
        securePhotoList = null;
    }
    @Override
    public void notifyNewMedia(final Uri uri) {
        if (isDestroyed()) return;
        super.notifyNewMedia(uri);
        securePhotoList.add(ContentUris.parseId(uri));
    }
}
