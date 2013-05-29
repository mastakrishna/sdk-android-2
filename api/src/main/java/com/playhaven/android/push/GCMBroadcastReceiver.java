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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.playhaven.android.PlayHaven;
import com.playhaven.android.req.PushTrackingRequest;

/**
 * Handles REGISTRATION and RECEIVE Intents from the system 
 * targeted for the application. 
 */
public class GCMBroadcastReceiver extends PushReceiver {
	public static final String REGID = "registration_id";
	public static final String ERROR = "error";
	public static final String UNREGISTERED = "unregistered";
	public static final String C2DM_REGISTRATION = "com.google.android.c2dm.intent.REGISTRATION";
	public static final String C2DM_RECEIVE = "com.google.android.c2dm.intent.RECEIVE";
	public static final String C2DM_REGISTER = "com.google.android.c2dm.intent.REGISTER";
	public static final String SERVICE_NOT_AVAILABLE = "SERVICE_NOT_AVAILABLE";
	
    @Override
    public final void onReceive(Context context, Intent intent) {
		mContext = context;
		mBundle = intent.getExtras();
		
        String action = intent.getAction();
    	PlayHaven.v("Received broadcast: %s.", action);
		
        if (C2DM_REGISTRATION.equals(action)) {
        	registration(intent, context);
        } else if (C2DM_RECEIVE.equals(action)) {
        	this.interpretPush(intent, context);
        }
    }
    
    /**
     * Handle a registration event sent from Google's servers. 
     */
    private void registration(Intent intent, Context context) {
        String registrationId = intent.getStringExtra(REGID);
        String error = intent.getStringExtra(ERROR);
        String unregistered = intent.getStringExtra(UNREGISTERED); 

        if (registrationId != null) {
            SharedPreferences pref = PlayHaven.getPreferences(context);
            SharedPreferences.Editor edit = pref.edit();
            edit.putString(GCMBroadcastReceiver.REGID, registrationId);
            edit.commit();
            
    		String message_id = mBundle.getString(PushParams.message_id.name());
    		String content_id = mBundle.getString(PushParams.content_id.name());
    		
    		PushTrackingRequest trackingRequest = new PushTrackingRequest(registrationId, message_id, content_id);
    		trackingRequest.send(context);
        }

        if (unregistered != null) {
        	PlayHaven.v("GCM registration was unregistered: %s", unregistered);
        	// TODO: unregistration request? 
        }

        if (error != null) {
            if (SERVICE_NOT_AVAILABLE.equals(error)) {
            	PlayHaven.e("GCM registration service unavailable."); 
            } else {
                PlayHaven.e("GCM registration error: " + error);
            }
        }
    }
}