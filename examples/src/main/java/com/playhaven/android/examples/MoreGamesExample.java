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
import com.playhaven.android.view.MoreGames;

/**
 * Demonstrate the More Games button
 */
public class MoreGamesExample
extends Activity
implements RequestListener
{
    /**
     * Unique name for logging
     */
    private static final String TAG = MoreGamesExample.class.getSimpleName();

    /**
     * Called when the Activity is created
     *
     * @param savedInstanceState from the previous run
     */
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

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
            OpenRequest open = new OpenRequest();
            open.setResponseHandler(this);
            open.send(this);
        } catch (PlayHavenException e) {
            Log.e(TAG, "We have encountered an error", e);
        }

        /**
         * Set our layout to src/main/android/res/layout/moregames.xml
         */
        setContentView(R.layout.moregames);
    }


    public void moreGamesClicked(android.view.View target)
    {
        /**
         * Now we start the new Activity.
         * Once the ad is closed, this Activity will be resumed.
         */
        startActivity(FullScreen.createIntent(this, getResources().getString(R.string.moregames_tag)));
    }

    @Override
    public void handleResponse(String json) {
        /**
         * Now that the Open is complete, tell the Badge to update
         */
        ((MoreGames) findViewById(R.id.more)).load(this);
    }

    @Override
    public void handleResponse(PlayHavenException e) {
        Log.d(TAG, "Error during open request", e);
    }
}
