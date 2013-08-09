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
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.test.suitebuilder.annotation.SmallTest;
import com.playhaven.android.diagnostic.Launcher;
import com.playhaven.android.push.PushReceiver;
import com.playhaven.android.push.NotificationBuilder;
import com.playhaven.android.push.NotificationBuilder.Keys;

public class NotificationBuilderTest extends PHTestCase <Launcher>{
	
    public NotificationBuilderTest() {
        super(Launcher.class);
    }
    
    @SmallTest
    public void testNotificationBuilder() throws Throwable {
        Instrumentation instrumentation = getInstrumentation();
        Context context = instrumentation.getTargetContext();
        
    	Notification notification = null;
    	PendingIntent pendingIntent = PendingIntent.getActivity(context, this.hashCode(), new Intent(), 0);
    	
        Bundle bundle = new Bundle();

        // An empty bundle will not be enough to create a Notification. 
        notification = new NotificationBuilder(context).makeNotification(bundle, pendingIntent);
        assertNull(notification);
        
        // Now there should be a valid notification. 
    	bundle.putString(Keys.TITLE.name(), "Test Title");
    	bundle.putString(Keys.TEXT.name(), "Test Message");
        notification = new NotificationBuilder(context).makeNotification(bundle, pendingIntent);
        assertNotNull(notification);
        assertNotNull(notification.contentIntent);
    }
    
    /*
     * Tests the method that validates (and uppercases) the keys for building a notification. 
     */
    @SmallTest
    public void testCaseInsensitivity() throws Throwable {
        Instrumentation instrumentation = getInstrumentation();
        Context context = instrumentation.getTargetContext();
    	
        Bundle bundle = new Bundle();

    	bundle.putString(Keys.TITLE.name(), "Test Title");
    	bundle.putString(Keys.TEXT.name().toLowerCase(), "Test Message");
    	bundle.putString(Keys.URI.name().toLowerCase(), "playhaven://com.android.playhaven/?");
    	bundle = PushReceiver.validatePushKeys(bundle);

    	PendingIntent pendingIntent = PendingIntent.getActivity(context, this.hashCode(), new Intent(), 0);
        Notification notification = new NotificationBuilder(context).makeNotification(bundle, pendingIntent);
        assertNotNull(notification);
        assertNotNull(notification.contentIntent);
    }
}
