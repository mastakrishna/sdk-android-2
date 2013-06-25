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
import android.test.suitebuilder.annotation.SmallTest;

import com.playhaven.android.PlayHaven.ResourceTypes;
import com.playhaven.android.diagnostic.Launcher;
import com.playhaven.android.PlayHaven;

/**
 * Test dynamic resource lookup from Diagnostic 
 */
public class ResourceTest
extends PHTestCase<Launcher>
{
    public ResourceTest()
    {
        super(Launcher.class);
    }

    @SmallTest
    public void testStart() throws Exception
    {
        Instrumentation instrumentation = getInstrumentation();
        Launcher launcher = startActivitySync(Launcher.class);

        // Make sure we can look up a resource belonging to the instrumentation apk. 
        int id = PlayHaven.getResId(instrumentation.getContext(), PlayHaven.ResourceTypes.string, "instrumentation_token");
        assertEquals(com.playhaven.android.diagnostic.test.R.string.instrumentation_token, id);
        
        // Now let's look up something in the PlayHaven SDK. 
        int apiServerResId = PlayHaven.getResId(instrumentation.getTargetContext(), ResourceTypes.string, "playhaven.public.api.server");
        assertEquals(com.playhaven.android.R.string.playhaven_public_api_server, apiServerResId);
        
        // How about a <declare-styleable> element? 
        int[] attrs = PlayHaven.getResStyleableArray(instrumentation.getContext(), "com_playhaven_android_view_Badge");
        PlayHaven.w("Styleable attrs has: %s elements.", attrs.length);
        assertTrue(attrs.length == 2);
        
        launcher.finish();
    }
}
