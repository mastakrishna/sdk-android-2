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
import android.widget.Toast;
import com.playhaven.android.PlayHaven;
import com.playhaven.android.PlayHavenException;
import com.playhaven.android.req.OpenRequest;
import com.playhaven.android.view.FullScreen;
import com.playhaven.android.view.PlayHavenView;

/**
 * The Content Request w/ a result listener
 */
public class ContentExample2 extends Activity
{
    /**
     * Unique name for logging
     */
    private static final String TAG = ContentExample2.class.getSimpleName();

    /**
     * A unique ID to correlate request with response
     */
    private static final int EXAMPLE2_REQUEST_CODE = 12345;

    /**
     * Called when the Activity is created
     *
     * @param savedInstanceState from the previous run
     */
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        /**
         * Set our layout to src/main/android/res/layout/content2.xml
         */
        setContentView(R.layout.content2);

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
             * Now we start the new Activity.
             *
             * We'll use PlayHaven's convenience method to create the appropriate intent.
             * The first parameter is our Context (ie: this Activity)
             * The second parameter is our placement tag as defined in the Dashboard
             *
             * Once the ad is closed, this Activity will be resumed.
             */
            startActivityForResult(FullScreen.createIntent(this, "content_example"), EXAMPLE2_REQUEST_CODE);
        } catch (PlayHavenException e) {
            Log.e(TAG, "We have encountered an error", e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode != EXAMPLE2_REQUEST_CODE)
            return;

        String placementTag = data.getStringExtra(PlayHavenView.BUNDLE_PLACEMENT_TAG);
        PlayHavenView.DismissType dismissType = (PlayHavenView.DismissType) data.getSerializableExtra(PlayHavenView.BUNDLE_DISMISS_TYPE);
        Toast.makeText(this, placementTag + " was dismissed: " + dismissType, Toast.LENGTH_LONG).show();
    }
}