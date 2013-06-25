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
package com.playhaven.android.view;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;
import com.playhaven.android.Placement;
import com.playhaven.android.PlayHaven;
import com.playhaven.android.PlayHavenException;
import com.playhaven.android.push.NotificationBuilder;
import com.playhaven.android.push.PushReceiver;
import com.playhaven.android.req.PushTrackingRequest;

import java.util.List;

public class FullScreen
extends Activity
implements PlayHavenListener
{
    private static final String TIMESTAMP = "closed.timestamp";

    /**
     * Result to send back to calling Activity
     */
    private Intent result;

    /**
     * Construct an Intent, used to display a PlayHaven FullScreen ad using the default display options
     *
     * @param context of the application
     * @param placementTag to be displayed
     * @return intent to call
     * @see <a href="https://dashboard.playhaven.com/">https://dashboard.playhaven.com/</a>
     * @see PlayHavenView#AUTO_DISPLAY_OPTIONS
     */
    public static Intent createIntent(Context context, String placementTag)
    {
        return createIntent(context, placementTag, PlayHavenView.AUTO_DISPLAY_OPTIONS);
    }

    /**
     * Construct an Intent, used to display a PlayHaven FullScreen ad
     *
     * @param context of the application
     * @param placementTag to be displayed
     * @param displayOptions to use
     * @return intent to call
     * @see <a href="https://dashboard.playhaven.com/">https://dashboard.playhaven.com/</a>
     * @see PlayHavenView#AUTO_DISPLAY_OPTIONS
     * @see PlayHavenView#NO_DISPLAY_OPTIONS
     * @see PlayHavenView#DISPLAY_OVERLAY
     * @see PlayHavenView#DISPLAY_ANIMATION
     */
    public static Intent createIntent(Context context, String placementTag, int displayOptions)
    {
        // This method is here instead of in PlayHavenView to prevent circular dependency
        Intent intent = new Intent(context, FullScreen.class);
        intent.putExtra(PlayHavenView.BUNDLE_PLACEMENT_TAG, placementTag);
        intent.putExtra(PlayHavenView.BUNDLE_DISPLAY_OPTIONS, displayOptions);
        return intent;
    }

    /**
     * Construct an Intent, used to display a PlayHaven FullScreen ad using the default display options
     *
     * @param context of the application
     * @param placement to be displayed
     * @return intent to call
     * @see <a href="https://dashboard.playhaven.com/">https://dashboard.playhaven.com/</a>
     * @see PlayHavenView#AUTO_DISPLAY_OPTIONS
     */
    public static Intent createIntent(Context context, Placement placement)
    {
        return createIntent(context, placement, PlayHavenView.AUTO_DISPLAY_OPTIONS);
    }

    /**
     * Construct an Intent, used to display a PlayHaven FullScreen ad
     *
     * @param context of the application
     * @param placement to be displayed
     * @param displayOptions to use
     * @return intent to call
     * @see <a href="https://dashboard.playhaven.com/">https://dashboard.playhaven.com/</a>
     * @see PlayHavenView#AUTO_DISPLAY_OPTIONS
     * @see PlayHavenView#NO_DISPLAY_OPTIONS
     * @see PlayHavenView#DISPLAY_OVERLAY
     * @see PlayHavenView#DISPLAY_ANIMATION
     */
    public static Intent createIntent(Context context, Placement placement, int displayOptions)
    {
        // This method is here instead of in PlayHavenView to prevent circular dependency
        Intent intent = new Intent(context, FullScreen.class);
        intent.putExtra(PlayHavenView.BUNDLE_PLACEMENT, placement);
        intent.putExtra(PlayHavenView.BUNDLE_DISPLAY_OPTIONS, displayOptions);
        return intent;
    }

    /**
     * Has enough time elapsed since the last FullScreen was displayed?
     *
     * @param context of the caller
     * @param durationMs required to be elapsed
     * @return true if durationMs has elapsed since the last FullScreen has returned
     */
    public static boolean timeElapsed(Context context, long durationMs)
    {
        SharedPreferences pref = PlayHaven.getPreferences(context);
        long timestamp = pref.getLong(TIMESTAMP, 0);
        if(timestamp == 0) return true;
        long now = System.currentTimeMillis();
        return (now - timestamp >= durationMs);
    }

    /**
     * Store the current timestamp
     *
     * @see FullScreen#timeElapsed(android.content.Context, long)
     */
    protected void storeTimestamp()
    {
        SharedPreferences pref = PlayHaven.getPreferences(this);
        SharedPreferences.Editor editor = pref.edit();
        editor.putLong(TIMESTAMP, System.currentTimeMillis());
        editor.commit();
    }

    /**
     * Create the FullScreen Activity
     *
     * @param savedInstanceState of the previous run
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        //Remove notification bar - Note: this broke adjustResize due to http://code.google.com/p/android/issues/detail?id=5497 
        //this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        int contentViewId = PlayHaven.getResId(getApplicationContext(), PlayHaven.ResourceTypes.layout, "playhaven_activity");
        setContentView(contentViewId);

        int activityViewId = PlayHaven.getResId(getApplicationContext(), PlayHaven.ResourceTypes.id, "playhaven_activity_view");
        PlayHavenView playHavenView = (PlayHavenView) findViewById(activityViewId);
        playHavenView.setPlayHavenListener(this);

        // If launched via Uri.parse, grab the parameters from the Uri
        Uri dataUri = getIntent().getData();
        if(dataUri != null)
        {
            List<String> path = dataUri.getPathSegments();
            if(path.size() == 1)
            {
                PlayHaven.d("path[0]: %s", path.get(0));
            }

            try{
                playHavenView.setDisplayOptions(Integer.parseInt(dataUri.getQueryParameter(PlayHavenView.BUNDLE_DISPLAY_OPTIONS)));
            }catch(NumberFormatException nfe){
                // no-op
            }
            playHavenView.setPlacementTag(dataUri.getQueryParameter(PlayHavenView.BUNDLE_PLACEMENT_TAG));
        }

        // If launched via FullScreen.createIntent, grab the parameters from the Bundle
        Bundle extras = getIntent().getExtras();
        if(extras != null)
        {
            playHavenView.setDisplayOptions(extras.getInt(PlayHavenView.BUNDLE_DISPLAY_OPTIONS));

            // Prefer an actual placement over the placement tag if both are provided
            Placement pl = extras.getParcelable(PlayHavenView.BUNDLE_PLACEMENT);
            if(pl != null)
            {
                playHavenView.setPlacement(pl);
            }else{
                String plId = extras.getString(PlayHavenView.BUNDLE_PLACEMENT_TAG);
                if(plId != null)
                    playHavenView.setPlacementTag(plId);
            }
            
            // If this happened as the result of a push notification, send a tracking request. 
            String message_id = extras.getString(PushReceiver.PushParams.message_id.name());
            if(message_id != null && pl != null)
            {
            	// TODO: when the server supports requesting content_id, change this. Until then, 
            	// use and track the placement tag. 
            	//String content_id = extras.getString(PushReceiver.PushParams.content_id.name());
            	String content_id = pl.getPlacementTag();
            	
            	PushTrackingRequest trackingRequest = new PushTrackingRequest(getApplicationContext(), message_id, content_id);
            	trackingRequest.send(getApplicationContext());
            }
        }
        

    }

    /**
     * Close this ad
     */
    @Override
    public void finish() 
    {
        if(result == null) 
        {
            // Default result...
            result = new Intent();
            int activityViewId = PlayHaven.getResId(getApplicationContext(), PlayHaven.ResourceTypes.id, "playhaven_activity_view");
            PlayHavenView playHavenView = (PlayHavenView) findViewById(activityViewId);
            result.putExtra(PlayHavenView.BUNDLE_DISMISS_TYPE, PlayHavenView.DismissType.SelfClose);
            doResult(RESULT_OK, result, playHavenView);
        }
        storeTimestamp();
        super.finish();
    }

    /**
     * Close this ad when the back button is pressed
     */
    @Override
    public void onBackPressed() 
    {
    	int activityViewId = PlayHaven.getResId(getApplicationContext(), PlayHaven.ResourceTypes.id, "playhaven_activity_view");
        viewDismissed((PlayHavenView) findViewById(activityViewId), PlayHavenView.DismissType.BackButton, null);
    }

    /**
     * The view failed to launch properly
     *
     * @param view that was attempted
     * @param exception that prevented loading of the view
     */
    @Override
    public void viewFailed(PlayHavenView view, PlayHavenException exception) 
    {
        result = new Intent();
        result.putExtra(PlayHavenView.BUNDLE_DISMISS_TYPE, PlayHavenView.DismissType.SelfClose);
        result.putExtra(PlayHavenView.BUNDLE_EXCEPTION, exception);
        doResult(RESULT_CANCELED, result, view);
        finish();
    }

    /**
     * The view was dismissed
     *
     * @param view that was dismissed
     * @param dismissType how it was dismissed
     * @param data additional data, depending on the content type
     */
    @Override
    public void viewDismissed(PlayHavenView view, PlayHavenView.DismissType dismissType, Bundle data) 
    {
        result = new Intent();
        result.putExtra(PlayHavenView.BUNDLE_DISMISS_TYPE, dismissType);
        if(data != null) 
        {
            result.putExtra(PlayHavenView.BUNDLE_DATA, data);
        }

        doResult(RESULT_OK, result, view);
        this.finish();
    }
    
    public void doResult(int resultCode, Intent result, PlayHavenView view) 
    {
        result.putExtra(PlayHavenView.BUNDLE_PLACEMENT, view.getPlacement());
        result.putExtra(PlayHavenView.BUNDLE_PLACEMENT_TAG, view.getPlacementTag());
        result.putExtra(PlayHavenView.BUNDLE_DISPLAY_OPTIONS, view.getDisplayOptions());
        
        // If this placement was launched as a result of a Notification, we want to 
        // launch the provided URI as an Intent or launch the providing Application. 
        String uriString = getIntent().getExtras().getString(NotificationBuilder.Keys.URI.name());
        if(uriString != null)
        {
        	PlayHaven.v("Provided URI was: %s", uriString);
        	// TODO: handle URIs not meant to launch the default application 
    		PackageManager pm = getPackageManager();
    		Intent newIntent = pm.getLaunchIntentForPackage(getPackageName());
    		
    		// Pass the uri parameters as extras to the Application as it launches (cleaner in API 11+, but...)
        	String[] params = uriString.split("&");
        	for(String param : params) {
        		String[] parts = param.split("=");
        		if(parts.length == 2){
        			newIntent.putExtra(param.split("=")[0], param.split("=")[1]);
        		}
        	}
    		startActivity(newIntent);
        }
    	// Provide this result to calling activity, if there was one. 
        setResult(resultCode, result);
    }
}
