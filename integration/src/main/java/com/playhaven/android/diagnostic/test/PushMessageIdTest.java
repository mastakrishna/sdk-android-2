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

import android.app.Instrumentation;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.test.suitebuilder.annotation.SmallTest;

import com.playhaven.android.Placement;
import com.playhaven.android.PushPlacement;
import com.playhaven.android.PlacementListener;
import com.playhaven.android.PlayHaven;
import com.playhaven.android.PlayHavenException;
import com.playhaven.android.diagnostic.Launcher;
import com.playhaven.android.view.PlayHavenView;
import com.playhaven.android.view.PlayHavenView.DismissType;

/**
 * Test the Diagnostic App Launcher activity
 */
public class PushMessageIdTest extends PHTestCase<Launcher> implements PlacementListener 
{
	private static final String MESSAGE_ID = "42";
    public PushMessageIdTest()
    {
        super(Launcher.class);
    }

    @SmallTest
    public void testStart() throws Exception
    {
    	clearAndConfigurePlayHaven();
		PushPlacement placement = new PushPlacement("main_menu");
		placement.setListener(PushMessageIdTest.this);
		placement.setMessageId(MESSAGE_ID);
		placement.preload(getInstrumentation().getTargetContext());
    }
    
	@Override
	public void contentLoaded(Placement placement) {
		assertEquals(((PushPlacement) placement).getMessageId(), MESSAGE_ID);
	}
	
	@Override
	public void contentFailed(Placement placement, PlayHavenException e) {
		PlayHaven.e(e);
		fail();
	}
	
	@Override
	public void contentDismissed(Placement placement, DismissType dismissType, Bundle data) { }
}
