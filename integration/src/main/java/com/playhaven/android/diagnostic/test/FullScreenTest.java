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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import android.test.suitebuilder.annotation.Suppress;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Instrumentation;
import android.app.Instrumentation.ActivityMonitor;
import android.content.Intent;
import android.os.Bundle;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;
import android.view.WindowManager;

import com.playhaven.android.Placement;
import com.playhaven.android.PlacementListener;
import com.playhaven.android.PlayHaven;
import com.playhaven.android.PlayHavenException;
import com.playhaven.android.diagnostic.Launcher;
import com.playhaven.android.view.FullScreen;
import com.playhaven.android.view.PlayHavenView;
import com.playhaven.android.view.PlayHavenView.DismissType;


/**
 * Tests whether a Fullscreen placement is shown with the FLAG_FULLSCREEN set on
 * its window, based on both whether or not it is set in the Activity it was launched 
 * from and on input from the server. 
 */
public class FullScreenTest extends PHTestCase<Launcher> {
	final CountDownLatch mSignal = new CountDownLatch(1);
	private Launcher mLauncher;
	
	public FullScreenTest() {
		super(Launcher.class);
	}
	
	@Override
	protected 
	void setUp() {
		try {
	    	clearAndConfigurePlayHaven();
	    	PlayHaven.setLogLevel(Log.VERBOSE);
		} catch (PlayHavenException e) {
			fail("Could not configure.");
		}
	}
	
	/**
	 * Test the situation in which a placement is not fullscreen compatible, 
	 * but is launched from a window with the fullscreen flag set. 
	 */
	@SmallTest @Suppress
	public void testLauncherNotPlacement() {
		mLauncher = startActivitySync(Launcher.class);
		mLauncher.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mLauncher.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			}
		});
	
	    Placement placement = new Placement("optinresize");
		placement.setListener(new TestPlacementListener(0));
		placement.preload(mLauncher);
		try {
			assertTrue(mSignal.await(30, TimeUnit.SECONDS));
		} catch (InterruptedException e) {
			fail("Content never loaded, interrupted.");
        }
	}


	
	/**
	 * Test the situation in which a placement is not fullscreen compatible, 
	 * and is launched from a window without the fullscreen flag set. 
	 */
	@SmallTest @Suppress
	public void testNotLauncherNotPlacement() {
		mLauncher = startActivitySync(Launcher.class);
		
	    Placement placement = new Placement("optinresize");
		placement.setListener(new TestPlacementListener(0));
		placement.preload(mLauncher);
		try {
			assertTrue(mSignal.await(30, TimeUnit.SECONDS));
		} catch (InterruptedException e) {
			fail("Content never loaded, interrupted.");
		}
	}

	/**
	 * Test the situation in which a placement is fullscreen compatible, 
	 * and launched from a window with the fullscreen flag set. 
	 */
	@SmallTest @Suppress
	public void testLauncherPlacement() {
		mLauncher = startActivitySync(Launcher.class);
    	
        Placement placement = new Placement("more_games");
		placement.setListener(new TestPlacementListener(1));
		placement.preload(mLauncher);
		try {
			assertTrue(mSignal.await(30, TimeUnit.SECONDS));
		} catch (InterruptedException e) {
			fail("Content never loaded, interrupted.");
		}
	}
	
	/**
	 * Test the situation in which a placement is fullscreen compatible, 
	 * but launched from a window without the fullscreen flag set. 
	 */
	@SmallTest @Suppress
	public void testNotLauncherPlacement() {
		mLauncher = startActivitySync(Launcher.class);
		mLauncher.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mLauncher.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			}
		});
    	
        Placement placement = new Placement("more_games");
		placement.setListener(new TestPlacementListener(1));
		placement.preload(mLauncher);
		try {
			assertTrue(mSignal.await(30, TimeUnit.SECONDS));
		} catch (InterruptedException e) {
			fail("Content never loaded, interrupted.");
		}
	}
	
	private class TestPlacementListener implements PlacementListener {
		
		// This can be removed when the resizable flag is supported server-side. 
		int mResizable;
		TestPlacementListener(int resizable) {
			mResizable = resizable;
		}

		@Override
		public void contentLoaded(Placement placement) {
			// This can be removed when the resizable flag is supported server-side.
			try {
				JSONObject json = new JSONObject(placement.getModel());
				JSONObject response = json.getJSONObject("response");
                response.put("resizable", mResizable);
                json.put("response", response);
                placement.setModel(json.toString());
            } catch (JSONException e) {
				e.printStackTrace();
			}
			
	        // Get the context of the window that is fullscreen or not, then kill it ...
            Intent intent = FullScreen.createIntent(getActivity(), placement);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ActivityMonitor monitor = getInstrumentation().addMonitor(FullScreen.class.getName(), null, false);

            boolean callingActivityIsFullscreen = (getActivity().getWindow().getAttributes().flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) != 0;
            mLauncher.finish();

            // ... to let us sync to the new one.
	        getInstrumentation().startActivitySync(intent);
            Activity startedActivity = monitor.waitForActivityWithTimeout(10000);
            assertNotNull(startedActivity);

            // Check if the placement setting & calling activity setting match the window.
	        boolean placementActivityIsFullscreen = (startedActivity.getWindow().getAttributes().flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) != 0;
            PlayHaven.v(
	        		"TestPlacementListener: placement: %s and launcher: %s => result: %s", 
	        		placement.isFullscreenCompatible(), 
	        		callingActivityIsFullscreen, 
	        		placementActivityIsFullscreen
	        );

            assertEquals((callingActivityIsFullscreen && placement.isFullscreenCompatible()), placementActivityIsFullscreen);

	        startedActivity.finish();
            mSignal.countDown();
        }

		@Override
		public void contentFailed(Placement placement, PlayHavenException e) {
			fail("Content failed.");
		}

		@Override
		public void contentDismissed(Placement placement, DismissType dismissType, Bundle data) { }
	}
}
