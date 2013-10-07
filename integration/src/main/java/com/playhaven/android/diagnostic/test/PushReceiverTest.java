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
import android.test.InstrumentationTestCase;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.test.suitebuilder.annotation.SmallTest;
import com.playhaven.android.push.PushReceiver;
import com.playhaven.android.push.PushReceiver.UriTypes;

/**
 * Tests the Uri interpretation.
 */
public class PushReceiverTest extends InstrumentationTestCase {
    
    @SmallTest
    public void testCheckUri() throws Throwable {
        Instrumentation instrumentation = getInstrumentation();
        Context context = instrumentation.getTargetContext();
        
        PushReceiver receiver = new PushReceiver();
        receiver.onReceive(context, new Intent());
        
        Uri uri = Uri.parse("playhaven://com.playhaven.android/?activity=foo");
        UriTypes type = receiver.checkUri(uri, context);
        assertEquals(type, UriTypes.ACTIVITY);
        
        uri = Uri.parse("playhaven://com.playhaven.android/?placement=foo");
        type = receiver.checkUri(uri, context);
        assertEquals(type, UriTypes.PLACEMENT);
        
        uri = Uri.parse("playhaven://com.playhaven.android/?content_id=12345");
        type = receiver.checkUri(uri, context);
        assertEquals(type, UriTypes.PLACEMENT); // This is not a typo. 
        
        uri = Uri.parse("phdiagnostic://com.playhaven.android.diagnostic");
        type = receiver.checkUri(uri, context);
        assertEquals(type, UriTypes.CUSTOM);
        
        uri = Uri.parse("playhaven://com.playhaven.android");
        type = receiver.checkUri(uri, context);
        assertEquals(type, UriTypes.DEFAULT);
        
        uri = Uri.parse("");
        type = receiver.checkUri(uri, context);
        assertEquals(type, UriTypes.INVALID);
        
        uri = Uri.parse("market://details?id=com.foo.bar");
        type = receiver.checkUri(uri, context);
        assertEquals(type, UriTypes.CUSTOM);
        
        uri = Uri.parse("http://www.google.com");
        type = receiver.checkUri(uri, context);
        assertEquals(type, UriTypes.CUSTOM);
    }
}
