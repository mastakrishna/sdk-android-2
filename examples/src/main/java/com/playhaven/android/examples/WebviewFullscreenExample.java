/**
 * Copyright 2013 Medium Entertainment, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.playhaven.android.examples;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import com.playhaven.android.PlayHaven;
import com.playhaven.android.PlayHavenException;
import com.playhaven.android.req.OpenRequest;
import com.playhaven.android.view.FullScreen;

public class WebviewFullscreenExample extends Activity {
    public static final String TAG = WebviewFullscreenExample.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview_fullscreen);

        // Configure
        try {
            PlayHaven.configure(this, R.string.token, R.string.secret);
        } catch (PlayHavenException e) {
            e.printStackTrace();
        }

        // Send an open request 
        OpenRequest open = new OpenRequest();
        open.send(this);

        // TODO: option for preload 
    }

    public void launch(View launchButton) {
        Intent intent = FullScreen.createIntent(getApplicationContext(), "main_menu");
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.v(TAG, String.format("Got back: %s -> %s", requestCode, resultCode));
    }
}
