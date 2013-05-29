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
import com.playhaven.android.req.NoContentException;
import com.playhaven.android.req.OpenRequest;
import com.playhaven.android.view.PlayHavenListener;
import com.playhaven.android.view.PlayHavenView;
import com.playhaven.android.view.Windowed;

/**
 * Example showing how to capture "no content" in Dialog
 */
public class NoContent2Example extends Activity implements PlayHavenListener {
    /**
     * Unique name for logging
     */
    private static final String TAG = NoContent2Example.class.getSimpleName();

    /**
     * Called when the Activity is created
     *
     * @param savedInstanceState from the previous run
     */
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        /**
         * Set our layout to src/main/android/res/layout/nocontent.xml
         */
        setContentView(R.layout.nocontent);

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

            /**
             * There are different reasons why there might not be any content units available (like
             * targetting caps), but an invalid id is the easiest to reproduce.
             */
            Windowed dialog = new Windowed(this);
            dialog.setDisplayOptions(PlayHavenView.DISPLAY_OVERLAY | PlayHavenView.DISPLAY_ANIMATION);
            dialog.setPlacementTag("invalid_placement");
            dialog.setPlayHavenListener(this);
            dialog.show();
        } catch (PlayHavenException e) {
            Log.e(TAG, "We have encountered an error", e);
        }
    }

    @Override
    public void viewFailed(PlayHavenView view, PlayHavenException exception) {
        if(NoContentException.class.isInstance(exception))
            Log.d(TAG, view.getPlacementTag() + " had no content!");
        else
            Log.e(TAG, "Error showing " + view.getPlacementTag(), exception);
    }

    @Override
    public void viewDismissed(PlayHavenView view, PlayHavenView.DismissType dismissType, Bundle data) {
        Log.i(TAG, view.getPlacementTag() + " was dismissed: " + dismissType);
    }
}
