/*
 * Copyright (C) 2007 The Android Open Source Project
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

package com.720p.camera;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;

/**
 * This activity plays a video from a specified URI.
 */
public class MovieView extends Activity  {
    private static final String TAG = "MovieView";

    private MovieViewControl mControl;
    private boolean mFinishOnCompletion;
    private boolean mUnregisterReceiver = false;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.movie_view);
        View rootView = findViewById(R.id.root);
        Intent intent = getIntent();
        mControl = new MovieViewControl(rootView, this, intent.getData()) {
            @Override
            public void onCompletion() {
                if (mFinishOnCompletion) {
                    finish();
                }
            }
        };
        if (intent.hasExtra(MediaStore.EXTRA_SCREEN_ORIENTATION)) {
            int orientation = intent.getIntExtra(
                    MediaStore.EXTRA_SCREEN_ORIENTATION,
                    ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            if (orientation != getRequestedOrientation()) {
                setRequestedOrientation(orientation);
            }
        }
        mFinishOnCompletion = intent.getBooleanExtra(
                MediaStore.EXTRA_FINISH_ON_COMPLETION, true);
    }

    @Override
    public void onPause() {
        mControl.onPause();
        super.onPause();

        if (mUnregisterReceiver == true) {
            unregisterReceiver(mReceiver);
            mUnregisterReceiver = false;
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus) {
            Log.v(TAG, "hasFocus");
            mControl.onResume();
            IntentFilter intentFilter =
                    new IntentFilter(Intent.ACTION_MEDIA_EJECT);
            intentFilter.addDataScheme("file");
            registerReceiver(mReceiver, intentFilter);
            mUnregisterReceiver = true;
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_MEDIA_EJECT)) {
                mControl.stopVideoPlayback();
                finish();
            }
        }
    };

}