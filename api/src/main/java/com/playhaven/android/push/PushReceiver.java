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
package com.playhaven.android.push;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import com.playhaven.android.Placement;
import com.playhaven.android.PlacementListener;
import com.playhaven.android.PlayHaven;
import com.playhaven.android.PlayHavenException;
import com.playhaven.android.req.PushTrackingRequest;
import com.playhaven.android.view.FullScreen;
import com.playhaven.android.view.PlayHavenView;
import com.playhaven.android.view.PlayHavenView.DismissType;

/**
 * Handles the Notification side of push notifications. 
 */
public class PushReceiver extends BroadcastReceiver implements PlacementListener {
	protected Context mContext;
	protected Bundle mBundle;
	
	/** The URIs we will receive and handle from push notifications.*/
	public enum UriTypes {
		DEFAULT,
		CUSTOM,
		MARKET,
		INVALID,
		PLACEMENT,
		ACTIVITY
	}
	
	/** Parameters to reuse for requests to PlayHaven. */
	public enum PushParams {
    	push_token, 
    	message_id, 
    	content_id
	}
	
	@Override
	public void contentLoaded(Placement placement) {
		PlayHaven.v("Have preloaded placement: %s", placement.getPlacementTag());
		
		Intent newIntent = new Intent(mContext, PushReceiver.class);
		newIntent.putExtra(PlayHavenView.BUNDLE_PLACEMENT, placement);
		newIntent.putExtras(mBundle);
		newIntent.putExtra(NotificationBuilder.Keys.URI.name(), mBundle.getString(NotificationBuilder.Keys.URI.name()));
		
		// Needed for instrumentation tests. 
		int requestCode;
		try {
			requestCode = Integer.parseInt(placement.getMessageId());
		} catch (NumberFormatException e){
			requestCode = this.hashCode();
		}
		
		PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, requestCode, newIntent, 0);
		Notification notification = new NotificationBuilder(mContext).makeNotification(mBundle, pendingIntent);
    	NotificationManager manager = (NotificationManager) mContext.getSystemService(Service.NOTIFICATION_SERVICE);
    	
    	manager.notify(requestCode, notification);
	}
	
	@Override
	public void contentFailed(Placement placement, PlayHavenException e) {
		PlayHaven.e("contentFailed() for placement \"%s\"", placement == null ? placement : placement.getPlacementTag());
		PlayHaven.e(e);
	}
	
	@Override
	/** The receiver probably won't be around to receive this event. */
	public void contentDismissed(Placement placement, DismissType dismissType, Bundle data) {
		PlayHaven.v("Placement dismissed with type %s", dismissType);
	}

	@Override
	/**
	 * Handles returning from Notification. See messaging-service-specific 
	 * classes for when system provides push notifications. 
	 * (Such as GCMBroadcastReceiver.) 
	 */
	public void onReceive(Context context, Intent intent) {
		Bundle bundle = intent.getExtras();
		if(bundle == null){
			// There is nothing more we can do. 
			PlayHaven.e("Received Notification with no extras.");
			return;
		}
		
		Uri uri = Uri.parse(bundle.getString(NotificationBuilder.Keys.URI.name()));
		Intent nextIntent = null;
		switch(checkUri(uri, context)){
			case ACTIVITY:
				// The publisher has provided a particular activity to launch after the notification.
				try {
					Class<?> activityClass = Class.forName(uri.getQueryParameter(PlayHaven.ACTION_ACTIVITY));
					nextIntent = new Intent(context, activityClass);
				} catch (ClassNotFoundException e) {
					PlayHaven.e(e);
				}
				break;
			case MARKET:
				// Launch Google Play. 
				nextIntent = new Intent(Intent.ACTION_VIEW, uri);
				break;
			case DEFAULT:
				// The default is to invoke the Application's default launcher after a notification. 
	    		PackageManager pm = context.getPackageManager();
				nextIntent = pm.getLaunchIntentForPackage(context.getPackageName());
				break;
			case PLACEMENT:
				// Launch a placement. 
				Placement placement = intent.getParcelableExtra(PlayHavenView.BUNDLE_PLACEMENT);
				nextIntent = FullScreen.createIntent(context, placement);
				break;
			default:
				break;
		}

		String message_id = bundle.getString(PushParams.message_id.name());
		String content_id = bundle.getString(PushParams.content_id.name());
		
		PushTrackingRequest trackingRequest = new PushTrackingRequest(context, message_id, content_id);
		trackingRequest.send(context);
		
		if(nextIntent != null) {
			nextIntent.putExtras(bundle);
			nextIntent.addFlags(0 | Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(nextIntent);
		}
	}
	
	/**
	 * Parses the intent provided from a push notification to create a 
	 * Notification (if appropriate) or perform other actions. 
	 */
	public void interpretPush(Intent intent, Context context) {
		mContext = context;
		mBundle = intent.getExtras();
		
		Uri uri = Uri.parse(intent.getStringExtra(NotificationBuilder.Keys.URI.name()));
		Notification notification = null;
		String messageId = intent.getStringExtra(PushParams.message_id.name());

		Intent newIntent = new Intent(context, PushReceiver.class);
		newIntent.putExtras(intent.getExtras());
		PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, getMessageId(intent), newIntent, 0);
		
		switch(checkUri(uri, mContext)){
			case DEFAULT:
				notification = new NotificationBuilder(mContext).makeNotification(mBundle, pendingIntent);
		    	break;
			case PLACEMENT:
				// Load a placement to show when the notification is clicked. 
				String placementTag = uri.getQueryParameter(PlayHaven.ACTION_PLACEMENT);
				Placement placement = new Placement(placementTag);
				placement.setListener(PushReceiver.this);
				placement.setMessageId(messageId);
				placement.preload(mContext);
				
				// Preserve these extras to pass to the pendingintent once the placement has loaded. 
				break;
			case CUSTOM:
				// The publisher has provided a Uri to broadcast. 
				Intent customIntent = new Intent();
				customIntent.setData(uri);
				mContext.sendBroadcast(customIntent);
				break;
			case MARKET: 
				notification = new NotificationBuilder(mContext).makeNotification(mBundle, pendingIntent);
				break;
			case ACTIVITY: 
				notification = new NotificationBuilder(mContext).makeNotification(mBundle, pendingIntent);
				break;
			default: 
				PlayHaven.e("An invalid URI was provided in a push notification: %s", uri);
				break;
		}
		
		if(notification != null){
	    	NotificationManager manager = (NotificationManager) mContext.getSystemService(Service.NOTIFICATION_SERVICE);
	    	manager.notify(getMessageId(intent), notification);
		}
	}
	
	/**
	 * Instrumentation needs to be able to retrieve the Notification from 
	 * the Notification Manager and so uses the message id to set the Notification id. 
	 * It requires an integer ID, which the message id might not always be if 
	 * coming from the PlayHaven server. 
	 */
	public int getMessageId(Intent intent){
		try {
			String messageId = intent.getStringExtra(PushParams.message_id.name());
			return Integer.parseInt(messageId);
		} catch (NumberFormatException e){
			return intent.hashCode();
		}
	}
    
    /**
     * Make sure this URI meets our security standards. 
     * We accept: 
     * 	market://<...>
     * 	playhaven://<application package name>
     * 	playhaven://com.playhaven.android/?<activity=x>|<placement=y>
     */
    public UriTypes checkUri(Uri uri, Context context){
    	String host = uri.getHost();
    	String scheme = uri.getScheme();
    	
    	if(PlayHaven.URI_SCHEME.equals(scheme)){
    		if(context.getPackageName().equals(host)){
    			return UriTypes.CUSTOM;
    		}
    		else if("com.playhaven.android".equals(host)){
    			if(uri.getQueryParameter(PlayHaven.ACTION_ACTIVITY) != null) {
    				return UriTypes.ACTIVITY;
    			}
    			if(uri.getQueryParameter(PlayHaven.ACTION_PLACEMENT) != null) {
    				return UriTypes.PLACEMENT;
    			}
    			return UriTypes.DEFAULT;
    		}
    	} else if("market".equals(scheme)){
    		return UriTypes.MARKET;
    	}
		
    	return UriTypes.INVALID;
    }
}
