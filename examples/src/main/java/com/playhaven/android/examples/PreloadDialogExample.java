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
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import com.playhaven.android.Placement;
import com.playhaven.android.PlacementListener;
import com.playhaven.android.PlayHaven;
import com.playhaven.android.PlayHavenException;
import com.playhaven.android.req.OpenRequest;
import com.playhaven.android.view.PlayHavenView;
import com.playhaven.android.view.Windowed;

/**
 * The Content Request w/ Preloading using Windowed dialogs
 */
public class PreloadDialogExample extends Activity
        implements PlacementListener
{
    /**
     * Unique name for logging
     */
    private static final String TAG = PreloadDialogExample.class.getSimpleName();

    /**
     * Placement as defined in the Dashboard
     */
    private Placement placement;

    /**
     * Handler to post to UI thread
     */
    private Handler handler;

    /**
     * Dialog to show the content
     */
    private Windowed dialog;

    /**
     * Called when the Activity is created
     *
     * @param savedInstanceState from the previous run
     */
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        /**
         * Instantiate our UI Handler
         */
        handler = new Handler();

        /**
         * Pass in the placement tag as defined in the Dashboard
         */
        placement = new Placement("content_example");

        /**
         * Set our layout to src/main/android/res/layout/preload.xml
         */
        setContentView(R.layout.preload2);

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
    public void contentLoaded(Placement placement)
    {
        /**
         * Since the content was loaded successfully, enable the 'displayAd' button.
         */
        handler.post(new Runnable() {
            @Override
            public void run() {
                ((Button) findViewById(R.id.displayAd)).setEnabled(true);
            }
        });
    }

    @Override
    public void contentFailed(Placement placement, PlayHavenException e) {
        /** Do something on failure... */
        final String msg = e.getMessage();
        handler.post(new Runnable(){
            public void run(){
                Toast.makeText(PreloadDialogExample.this, msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void contentDismissed(Placement placement, PlayHavenView.DismissType dismissType, Bundle data) {
        // We are just ignoring this
    }

    public void startPreloadingClicked(android.view.View target)
    {
        /**
         * Give it a listener.  This step is optional, but will let us ad logic to the contentLoaded.
         */
        placement.setListener(this);

        /**
         * Start preloading the content.  This will initiate network traffic but will not pause the game.
         */
        placement.preload(this);
    }

    public void displayAdClicked(android.view.View target)
    {
        if(placement.isLoaded())
        {
            /**
             * Now we create our popup Dialog
             *
             * The first parameter is our Context (ie: this Activity)
             * The second parameter is our placement as defined in the Dashboard
             *
             * Unlike with the FullScreen activity, our game is not paused.
             */
            dialog = new Windowed(this, placement);
            dialog.show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(dialog != null && dialog.isShowing())
            dialog.dismiss();
    }
}