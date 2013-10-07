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
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.test.suitebuilder.annotation.SmallTest;

import android.util.AttributeSet;
import android.util.Xml;
import com.playhaven.android.compat.VendorCompat;
import com.playhaven.android.diagnostic.Launcher;
import com.playhaven.android.PlayHaven;
import org.xmlpull.v1.XmlPullParser;

import static com.playhaven.android.compat.VendorCompat.ResourceType;

/**
 * Test dynamic resource lookup from Diagnostic 
 */
public class ResourceTest
extends PHTestCase<Launcher>
{
    private Instrumentation instrumentation;
    private Launcher launcher;

    public ResourceTest()
    {
        super(Launcher.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        instrumentation = getInstrumentation();
        launcher = startActivitySync(Launcher.class);

    }

    @Override
    protected void tearDown() throws Exception {
        launcher.finish();
        super.tearDown();
    }

    @SmallTest
    public void testStart() throws Exception
    {
        VendorCompat compat = PlayHaven.getVendorCompat(instrumentation.getContext());

        // Make sure we can look up a resource belonging to the instrumentation apk.
        int id = compat.getResourceId(instrumentation.getContext(), ResourceType.string, "instrumentation_token");
        assertEquals(com.playhaven.android.diagnostic.test.R.string.instrumentation_token, id);

        // Now let's look up something in the PlayHaven SDK. 
        int apiServerResId = compat.getResourceId(instrumentation.getTargetContext(), ResourceType.string, "playhaven_public_api_server");
        assertEquals(com.playhaven.android.R.string.playhaven_public_api_server, apiServerResId);
    }

    @SmallTest
    public void testTypedArray() throws Exception
    {
        // How about a <declare-styleable> element?
        Context ctx = instrumentation.getContext();
        VendorCompat compat = PlayHaven.getVendorCompat(ctx);
        Resources res = ctx.getResources();
        XmlPullParser parser = res.getXml(R.layout.moregames);
        AttributeSet attrs = Xml.asAttributeSet(parser);
        TypedArray arr = compat.obtainStyledAttributes(ctx, attrs, VendorCompat.STYLEABLE.com_playhaven_android_view_Badge);
        PlayHaven.w("Resource attrs has: %s elements.", arr.length());
        assertTrue(arr.length() == 2);
    }
}
