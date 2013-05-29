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
import com.playhaven.android.cache.Cache;
import com.playhaven.android.cache.CachedInfo;
import com.playhaven.android.diagnostic.Launcher;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static java.lang.Thread.sleep;

/**
 * Temporarily using the Diagnostic App to test the Cache directly
 */
public class PHCacheBulkTest
extends PHTestCase<Launcher>
{
    // URL of favicon from PlayHaven website
    private URL faviconUrl;

    // URL of We're Hiring logo from PlayHaven website
    private URL hiringUrl;

    // Cache handler
    private CacheTestResponseHandler handler;

    public PHCacheBulkTest() throws MalformedURLException {
        super(Launcher.class);
        faviconUrl = new URL("http://www.playhaven.com/favicon.ico");
        hiringUrl = new URL("http://www.playhaven.com/images/page_elements/werehiring_home.png");
        handler = new CacheTestResponseHandler(this);
    }

    @SmallTest
    public void testBulkRequest() throws Exception
    {
        Instrumentation instrumentation = getInstrumentation();

        Launcher launcher = startActivitySync(Launcher.class);
        sleep(250);

        Cache cache = new Cache(instrumentation.getTargetContext());
        enableThreadedTesting(handler);
        cache.bulkRequest(handler, faviconUrl, hiringUrl);
        waitForReady(handler);

        if(handler.exception != null)
            fail(handler.exception.getMessage());

        try {
            // There should be 2 entries
            assertNotNull(handler.cachedInfos);
            assertEquals(2, handler.cachedInfos.length);

            // FavIcon should be smaller than We're Hiring
            int faviconLength = -1;
            int hiringLength = -2;

            for(CachedInfo info : handler.cachedInfos)
            {
                if(faviconUrl.equals(info.getURL()))
                {
                    faviconLength = readFile(info.getFile()).length;
                }else if(hiringUrl.equals(info.getURL())){
                    hiringLength = readFile(info.getFile()).length;
                }else{
                    fail("Unexpected content: " + info.getURL().toExternalForm());
                }
            }
            assertTrue(faviconLength < hiringLength);
        } catch (IOException e) {
            fail(e.getMessage());
        }
        cache.close();
        launcher.finish();
    }
}
