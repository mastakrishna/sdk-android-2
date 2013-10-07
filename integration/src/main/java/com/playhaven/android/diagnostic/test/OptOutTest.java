package com.playhaven.android.diagnostic.test;

import android.content.Context;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;

import com.playhaven.android.diagnostic.Launcher;
import com.playhaven.android.PlayHaven;
import com.playhaven.android.PlayHavenException;

public class OptOutTest extends PHTestCase<Launcher> {

	public OptOutTest() {
		super(Launcher.class);
	}

	public void setUp() {
		try {
	    	clearAndConfigurePlayHaven();
			PlayHaven.setLogLevel(Log.VERBOSE);
		} catch (PlayHavenException e) {
			fail("Could not configure.");
		}
	}
	
	@SmallTest
	public void testOptOutSettingTrue() {
		Context context = getInstrumentation().getTargetContext();
		try {
			PlayHaven.setOptOut(context, true);
			assertTrue(PlayHaven.getOptOut(context));
		} catch (PlayHavenException e) {
			fail();
		}
	}
	
	@SmallTest
	public void testOptOutSettingFalse() {
		Context context = getInstrumentation().getTargetContext();
		try {
			PlayHaven.setOptOut(context, false);
			assertFalse(PlayHaven.getOptOut(context));
		} catch (PlayHavenException e) {
			fail();
		}
	}
}
