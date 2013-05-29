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

import static java.lang.Thread.sleep;

/**
 * Temporarily using the Diagnostic App to test the Cache directly
 */
public class PHCacheTest
extends PHTestCase<Launcher>
{
    /**
     * URL to download
     */
    public static final String TEST_URL = "http://www.playhaven.com/favicon.ico";

    /**
     * Size of content at the URL
     */
    private static final int EXPECTED_SIZE = 1286;

    /**
     * Small repeated pattern in the URL
     */
    private static final int[] EXPECTED_BYTES = new int[]{0xA3, 0x8D, 0x23};

    /**
     * A few places the pattern repeats in the URL
     */
    private static final int[] EXPECTED_OFFSETS = new int[]{0x82, 0x10A, 0x212, 0x31A, 0x452};

    public PHCacheTest()
    {
        super(Launcher.class);
    }

    @SmallTest
    public void testRequest() throws Exception
    {
        Instrumentation instrumentation = getInstrumentation();

//        screenshot("pre-launch");
        Launcher launcher = startActivitySync(Launcher.class);
        sleep(250);
//        screenshot("post-launch");

        Cache cache = new Cache(instrumentation.getTargetContext());
//        screenshot("cache created");
        CacheTestResponseHandler handler = new CacheTestResponseHandler(this);
        enableThreadedTesting(handler);
        cache.request(TEST_URL, handler);
//        screenshot("content requested");
        waitForReady(handler);

        if(handler.exception != null)
            fail(handler.exception.getMessage());

        try {
            // There should be 1 entry
            assertNotNull(handler.cachedInfos);
            assertTrue(handler.cachedInfos.length == 1);
            CachedInfo info = handler.cachedInfos[0];
            // Of a specific size
            byte[] buf = readFile(info.getFile());
            assertEquals("Wrong size", EXPECTED_SIZE, buf.length);

            // With specific bytes repeated throughout
            for(int offsetIndex=0; offsetIndex < EXPECTED_OFFSETS.length; offsetIndex++)
            {
                int offset = EXPECTED_OFFSETS[offsetIndex];
                for(int blockIndex=0; blockIndex < EXPECTED_BYTES.length; blockIndex++)
                {
                    int value = (buf[offset + blockIndex] & 0xFF);
                    assertEquals("Data mismatch (" + offset + ":" + blockIndex + ")", EXPECTED_BYTES[blockIndex], value);
                }
            }
        } catch (IOException e) {
            fail(e.getMessage());
        }

//        screenshot("done");
        cache.close();
//        screenshot("closed");
        launcher.finish();
//        screenshot("finished");
    }

}
