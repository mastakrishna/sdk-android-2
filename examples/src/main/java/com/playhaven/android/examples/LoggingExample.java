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
import android.os.Bundle;
import android.util.Log;
import com.playhaven.android.PlayHaven;
import com.playhaven.android.PlayHavenException;
import com.playhaven.android.req.OpenRequest;
import com.playhaven.android.req.RequestListener;
import com.playhaven.android.view.FullScreen;

/**
 * The Content Request
 */
public class LoggingExample extends Activity
{
    /**
     * Unique name for logging
     */
    private static final String TAG = LoggingExample.class.getSimpleName();

    /**
     * Called when the Activity is created
     *
     * @param savedInstanceState from the previous run
     */
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        /**
         * Set our layout to src/main/android/res/layout/logging.xml
         */
        setContentView(R.layout.logging);

        try {
            /**
             * Here, we configure PlayHaven to use the Token and Secret specified in the Dashboard
             * @see <a href="https://dashboard.playhaven.com/">https://dashboard.playhaven.com/</a>
             *
             * In this example, we are grabbing the value of the token and secret from
             * src/main/android/res/values/strings.xml
             */
            PlayHaven.configure(this, R.string.token, R.string.secret);

            /**
             * Turn on DEBUG logging while we send an Open Request.
             */
            PlayHaven.setLogLevel(Log.VERBOSE);
            OpenRequest request = new OpenRequest();
            request.setResponseHandler(new RequestListener() {
                @Override
                public void handleResponse(String json) {
                    PlayHaven.setLogLevel(Log.INFO);
                }

                @Override
                public void handleResponse(PlayHavenException e) {
                    Log.e(TAG, "There was an error!", e);
                    PlayHaven.setLogLevel(Log.INFO);
                }
            });
            request.send(this);

            /**
             * Now we start the new Activity.
             *
             * We'll use PlayHaven's convenience method to create the appropriate intent.
             * The first parameter is our Context (ie: this Activity)
             * The second parameter is our placement tag as defined in the Dashboard
             * The last parameter is some display options (to be explained in a later tutorial).
             *
             * Once the ad is closed, this Activity will be resumed.
             */
            startActivity(FullScreen.createIntent(this, "content_example"));
        } catch (PlayHavenException e) {
            Log.e(TAG, "We have encountered an error", e);
        }
    }
}