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

import android.util.Log;
import com.playhaven.android.PlayHavenException;
import com.playhaven.android.cache.CacheResponseHandler;
import com.playhaven.android.cache.CachedInfo;

import java.net.URL;

/**
 * Convenience class for using Cache inside of instrumentation tests
 */
class CacheTestResponseHandler
implements CacheResponseHandler
{
    // Test case owner
    private PHTestCase testCase;

    /**
     * ActivityInstrumentationTestCase2/JUnit doesn't work with assertions run in a background thread.
     * Save the cachedInfo for later processing.
     */
    public CachedInfo[] cachedInfos;

    /**
     * ActivityInstrumentationTestCase2/JUnit doesn't work with assertions run in a background thread.
     * Save the exception for later processing.
     */
    public Exception exception;


    /**
     * Construct a default handler
     *
     * @param testCase to own this handler
     */
    public CacheTestResponseHandler(PHTestCase testCase)
    {
        this.testCase = testCase;
    }

    @Override
    public void cacheSuccess(CachedInfo... cachedInfos)
    {
        for(CachedInfo info : cachedInfos)
            Log.d(testCase.TAG, "cacheSuccess: " + info.getFile().getAbsolutePath());

        this.cachedInfos = cachedInfos;
        testCase.markReadyForTesting(this);
    }

    @Override
    public void cacheFail(final URL url, final PlayHavenException exception)
    {
        Log.d(testCase.TAG, "cacheFail: " + url.toExternalForm());
        this.exception = exception;
        testCase.markReadyForTesting(this);
    }
}
