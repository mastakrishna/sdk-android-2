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
package com.playhaven.android.diagnostic.test;

import static com.github.rtyley.android.screenshot.celebrity.Screenshots.poseForScreenshotNamed;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Instrumentation.ActivityMonitor;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;

import com.playhaven.android.push.GCMBroadcastReceiver;
import com.playhaven.android.push.PushReceiver;
import com.playhaven.android.diagnostic.Launcher;
import com.playhaven.android.push.GCMRegistrationRequest;
import com.playhaven.android.view.FullScreen;
import com.playhaven.android.PlayHaven;

/**
 * Validate receipt of push notifications that create Notifications. 
 */
public class PushTest extends PHTestCase<Launcher> {
	
    String[] URIS = {
    		"market://details?id=com.bitwisedesign.SolRunner", // MARKET
    		"playhaven://com.playhaven.android", // DEFAULT
    		"playhaven://com.playhaven.android.diagnostic", // CUSTOM 
    		"playhaven://com.playhaven.android/?placement=more_games", // PLACEMENT 
    		"playhaven://com.playhaven.android/?activity=Preferences", // ACTIVITY 
    };
    
    public PushTest() {
        super(Launcher.class);
    }
    
    @MediumTest
    public void test_a_start() throws Throwable {
    	PlayHaven.setLogLevel(Log.VERBOSE);
    	
    	// Make sure that some account is set up. 
        Context ctx = getTargetContext();
    	AccountManager acctManager = AccountManager.get(ctx);
    	Account[] accounts = acctManager.getAccounts();
    	assertNotNull(accounts);
    	if(! (accounts.length > 0)){
    		fail("There were no Google accounts configured.");
    	}
    	
    	getInstrumentation().waitForIdleSync();
        (new GCMRegistrationRequest()).register(ctx);

        // Register.
        String regId = null;
        int count = 0;
        while (regId == null && count < 30) { // It doesn't usually take that long, but ...
            regId = PlayHaven.getPreferences(ctx).getString(GCMBroadcastReceiver.REGID, null);
            if(regId == null){
                count += 1;
                Thread.sleep(1000);
            }
        }
        assertNotNull(regId);
    }
    
    @SmallTest
    public void test_b_default() throws Throwable {
    	int testId = 40;
        Context context = getTargetContext();

        // Send a push *TO* GCM.
        boolean went = go(context, URIS[1], "Default test.", testId);
        assertTrue(went);

        // See if there is a notification with the identifier we just sent. 
        PendingIntent pending = getPushReceiverIntent(context, testId);
        assertNotNull(pending);
        
        // Watch to see if Launcher gets launched. 
        verifyActivity(Launcher.class.getName(), pending);
    }
    
    @SmallTest
    public void test_c_market() throws Throwable {
    	int testId = 41;
        Context context = getTargetContext();

        // Send a push *TO* GCM.
        boolean went = go(context, URIS[0], "Market test.", testId);
        assertTrue(went);

        // See if there is a notification with the identifier we just sent. 
        PendingIntent pending = getPushReceiverIntent(context, testId);
        assertNotNull(pending);
        
        // Since we're not instrumenting Play, we can't do much validation here. 
        pending.send();
        Thread.sleep(5000);
        poseForScreenshotNamed(TAG + ": market appearance");
    }
    
    @SmallTest
    public void test_d_custom() throws Throwable {
    	// TODO - what would a valid test for this even be? 
    }
    
    @SmallTest
    public void test_e_placement() throws Throwable {
    	int testId = 43;
        Context context = getTargetContext();
        
        // Send a push *TO* GCM.
        boolean went = go(context, URIS[3], "Placement test.", testId);
        assertTrue(went);
        
        // With this test, we may have to wait some time for the 
        // placement to get downloaded. 
        PendingIntent pending = null;
        int count = 0;
        while(pending == null && count < 4){
        	count += 1;
        	pending = getPushReceiverIntent(context, testId); // This can take up to 30 seconds. 
        }
        assertNotNull(pending);

        // Watch to see if FullScreen gets launched. 
        verifyActivity(FullScreen.class.getName(), pending);
    }
    
	public PendingIntent getPushReceiverIntent(Context context, int requestCode) throws InterruptedException {
		Intent intent = new Intent(context, PushReceiver.class);
		
        int count = 0;
        PendingIntent pending = null;
        while (pending == null && count < 30) { // It doesn't usually take that long, but ...
        	pending = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_NO_CREATE);
        	if(pending == null){
                count += 1;
                Thread.sleep(1000);
        	}
        }
        
		return pending;
	}

    private boolean go(Context ctx, String uri, String text, int msgId) {
        try {
            Resources res = this.getInstrumentation().getContext().getResources();

            URL url = new URL(res.getString(R.string.gcm_url));
            HttpsURLConnection request = (HttpsURLConnection) url.openConnection();
            request.setDoOutput(true);
            request.setDoInput(true);
            request.setRequestProperty("Content-type", "application/json");
            request.setRequestProperty("Authorization", "key=" + res.getString(R.string.gcm_api_key));
            request.setRequestMethod("POST");

            OutputStreamWriter post = new OutputStreamWriter(request.getOutputStream());
            String data = "{\"data\": {\"message_id\": \"" + msgId + "\", \"TEXT\": \"" + text + "\", \"TITLE\": \"The Best Title\", \"URI\": \"" + uri + "\"}, \"registration_ids\": [\"" + PlayHaven.getPreferences(ctx).getString(GCMBroadcastReceiver.REGID, null) + "\"]}";
            post.write(data);
            post.flush();

            BufferedReader in = new BufferedReader(new InputStreamReader(request.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                PlayHaven.i(inputLine.replace("%", " "));
            }
            post.close();
            in.close();
        } catch (Exception e) {
            PlayHaven.e(e);
            return false;
        }
        return true;
    }
    
    @SmallTest
    public void test_f_deregistration() throws Throwable {
        Context context = getTargetContext();
        (new GCMRegistrationRequest()).deregister(context);
        
        // If deregistration completes sucessfully, we'll have no token stored soon. 
        SharedPreferences pref = PlayHaven.getPreferences(context);
        String regId = pref.getString(GCMBroadcastReceiver.REGID, null);
        
        int count = 0;
        while (regId != null && count < 30) { // It doesn't usually take that long, but ...
            regId = PlayHaven.getPreferences(context).getString(GCMBroadcastReceiver.REGID, null);
            if(regId != null){
                count += 1;
                Thread.sleep(1000);
            }
        }
        
        assertNull(regId);
    }
    
    /**
     * Wait to see if the specified Activity gets launched, then close it. 
     */
    public void verifyActivity(String className, PendingIntent pending){
        ActivityMonitor monitor = getInstrumentation().addMonitor(className, null, false);
        try {
			pending.send();
		} catch (CanceledException e) {
			fail("Couldn't send PendingIntent.");
		}
        Activity startedActivity = monitor.waitForActivityWithTimeout(10000);
        assertNotNull(startedActivity);
        startedActivity.finish();
    }
}
