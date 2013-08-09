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

package com.playhaven.android.diagnostic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.playhaven.android.PlayHaven;

public class InstrumentationReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent providedIntent) {
		Intent intent = new Intent(context, DiagnosticPreferences.class);
		
		// Get the extras from the URI. (Convenience method not available in Froyo.) 
		try {
			String[] queryParams = providedIntent.getData().getQuery().split("&");
	    	for(String param : queryParams) {
	    		String[] parts = param.split("=");
	    		if(parts.length == 2){
	    			intent.putExtra(parts[0], parts[1]);
	    		}
	    	}
		} catch (Exception e) {
			PlayHaven.e("Unable to extract extras from URI.");
		}
    	
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}
}
