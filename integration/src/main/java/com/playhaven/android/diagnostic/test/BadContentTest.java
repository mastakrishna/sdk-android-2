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

import android.content.Context;
import android.test.suitebuilder.annotation.SmallTest;
import com.playhaven.android.diagnostic.Launcher;
import com.playhaven.android.diagnostic.test.req.TestableContentRequest;
import com.playhaven.android.req.OpenRequest;

/**
 * Verify that the application dismisses the dialog if we receive bad content from the (mock) server
 */
public class BadContentTest
        extends PHTestCase<Launcher>
{
    private static final String TAG = BadContentTest.class.getSimpleName();

    public BadContentTest()
    {
        super(Launcher.class);
    }

    @SmallTest
    public void testBadContent()
            throws Throwable
    {
        Launcher launcher = doActivityTestSetup();
        clearAndConfigurePlayHaven();

        Context ctx = getTargetContext();
        (new OpenRequest()).send(ctx);

        String json = getJSON(R.raw.badreq);
        assertNotNull(json);

        TestableContentRequest<Launcher> req = TestableContentRequest.mock(this, "content_example", json);
        enableThreadedTesting(req);
        req.send(ctx);
        waitForReady(req);

        assertNull(req.getReturnedModel());
        assertNotNull(req.getReturnedException());

        launcher.finish();
    }
}
