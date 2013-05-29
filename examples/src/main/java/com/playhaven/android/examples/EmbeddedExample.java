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
import android.view.Window;
import android.view.WindowManager;
import com.playhaven.android.PlayHaven;
import com.playhaven.android.PlayHavenException;
import com.playhaven.android.req.OpenRequest;
import com.playhaven.android.view.PlayHavenListener;
import com.playhaven.android.view.PlayHavenView;

/**
 * An example of embedded an ad in another screen
 */
public class EmbeddedExample extends Activity implements PlayHavenListener {
    /**
     * Unique name for logging
     */
    private static final String TAG = EmbeddedExample.class.getSimpleName();

    /**
     * Called when the Activity is created
     *
     * @param savedInstanceState from the previous run
     */
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

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
             * Send an Open Request.
             */
            (new OpenRequest()).send(this);
        } catch (PlayHavenException e) {
            Log.e(TAG, "We have encountered an error", e);
        }

        /**
         * Set our layout to src/main/android/res/layout/embedded.xml
         */
        setContentView(R.layout.embedded);

        ((PlayHavenView)findViewById(R.id.ad)).setPlayHavenListener(this);
    }

    public void doRefresh(android.view.View target)
    {
        ((PlayHavenView)findViewById(R.id.ad)).reload();
    }

    @Override
    public void viewFailed(PlayHavenView view, PlayHavenException exception) {
        // no-op
    }

    @Override
    public void viewDismissed(PlayHavenView view, PlayHavenView.DismissType dismissType, Bundle data) {
        // Let's load the next one
        doRefresh(view);
    }
}
