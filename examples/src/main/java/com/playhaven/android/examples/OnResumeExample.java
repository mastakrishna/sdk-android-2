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
import com.playhaven.android.view.FullScreen;
import com.playhaven.android.view.PlayHavenView;

/**
 * The Content Request
 */
public class OnResumeExample extends Activity
{
    /**
     * Unique name for logging
     */
    private static final String TAG = OnResumeExample.class.getSimpleName();

    /**
     * Called when the Activity is created
     *
     * @param savedInstanceState from the previous run
     */
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        /**
         * Set our layout to src/main/android/res/layout/onresume.xml
         */
        setContentView(R.layout.onresume);

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
             * Send an Open Request first.
             */
            (new OpenRequest()).send(this);

        } catch (PlayHavenException e) {
            Log.e(TAG, "We have encountered an error", e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(FullScreen.timeElapsed(this, 5000))
        {
            /**
             * Now we start the new Activity using the same logic as in the ContentExample.
             *
             * This time, we will specify Display Options.  The Display Options provide the
             * developer with a way to show feedback to the user while the content template
             * and images are being downloaded.
             *
             * DISPLAY_OVERLAY is used to display a partially transparent overlay.
             * DISPLAY_ANIMATION is used to display an indeterminate progress indicator.
             *
             * By specifying (DISPLAY_OVERLAY | DISPLAY_ANIMATION) we are telling the
             * system that we want to enable both options.
             */
            startActivity(FullScreen.createIntent(this, "content_example", PlayHavenView.DISPLAY_OVERLAY | PlayHavenView.DISPLAY_ANIMATION));
        }
    }
}