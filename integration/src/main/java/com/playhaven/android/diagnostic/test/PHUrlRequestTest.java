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

import android.net.Uri;
import android.test.suitebuilder.annotation.SmallTest;
import com.playhaven.android.diagnostic.Launcher;
import com.playhaven.android.req.UrlRequest;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 *  Test relies on http://goo.gl shortened url persisting, it 
 *  should redirect to www.playhaven.com. 
 */
public class PHUrlRequestTest extends PHTestCase <Launcher> {
	// This must be an https url if using goo.gl else redirects won't be followed. 
    public static final String TEST_URL = "https://goo.gl/0inJ0";

    public PHUrlRequestTest() {
        super(Launcher.class);
    }

    @SmallTest
    public void testUrlRequest() throws Throwable {
        UrlRequest urlRequest = new UrlRequest(TEST_URL);
        ExecutorService pool = Executors.newSingleThreadExecutor();
        Future <String> uriFuture = pool.submit(urlRequest);

        // if call() throws an exception it'll happen here 
        String uriString = uriFuture.get();

        Uri parsedUri = Uri.parse(uriString);
        assertNotNull(parsedUri);
        assertEquals("www.playhaven.com", parsedUri.getHost());
    }
}