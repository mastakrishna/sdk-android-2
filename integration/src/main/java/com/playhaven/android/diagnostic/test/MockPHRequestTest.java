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
import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;
import com.jayway.jsonpath.JsonPath;
import com.playhaven.android.PlayHavenException;
import com.playhaven.android.diagnostic.Launcher;
import com.playhaven.android.req.ContentRequest;
import com.playhaven.android.req.OpenRequest;
import com.playhaven.android.util.JsonUtil;
import org.springframework.web.util.UriComponentsBuilder;

import static java.lang.Thread.sleep;

/**
 * Validate ability to Mock PlayHavenRequest
 */
public class MockPHRequestTest
extends PHTestCase<Launcher>
{
    private static final String TAG = MockPHRequestTest.class.getSimpleName();

    public MockPHRequestTest()
    {
        super(Launcher.class);
    }

    class MockOpenRequest
    extends OpenRequest
    {
        /**
         * ActivityInstrumentationTestCase2/JUnit doesn't work with assertions run in a background thread.
         * Save the model for later processing.
         */
        private String returnedModel;

        /**
         * ActivityInstrumentationTestCase2/JUnit doesn't work with assertions run in a background thread.
         * Save the exception for later processing.
         */
        private Exception returnedException;

        public MockOpenRequest() {
            super();
        }

        @Override
        protected void handleResponse(String json) {
            Log.d(TAG, "handleResponse: model");
            super.handleResponse(json);
            this.returnedModel = json;
            markReadyForTesting(this);
        }

        @Override
        protected void handleResponse(PlayHavenException e) {
            Log.d(TAG, "handleResponse: exception");
            super.handleResponse(e);
            this.returnedException = e;
            markReadyForTesting(this);
        }

        @Override
        protected String getMockJsonResponse() {
            return "{\"error\":null,\"errobj\":null}";
        }
    }

    @SmallTest
    public void testMockOpen() throws Exception
    {
        Instrumentation instrumentation = getInstrumentation();
        Launcher launcher = startActivitySync(Launcher.class);
        sleep(250);

        MockOpenRequest req = new MockOpenRequest();
        enableThreadedTesting(req);
        req.send(instrumentation.getTargetContext());
        waitForReady(req);

        if(req.returnedException != null)
            fail(req.returnedException.getMessage());

        String model = req.returnedModel;

        assertNotNull(model);
        assertFalse(JsonUtil.hasPath(model, "$.error"));
        assertFalse(JsonUtil.hasPath(model, "$.response.context.content"));

        launcher.finish();
    }

    class MockContentRequest
            extends ContentRequest
    {
        /**
         * ActivityInstrumentationTestCase2/JUnit doesn't work with assertions run in a background thread.
         * Save the model for later processing.
         */
        private String returnedModel;

        /**
         * ActivityInstrumentationTestCase2/JUnit doesn't work with assertions run in a background thread.
         * Save the exception for later processing.
         */
        private Exception returnedException;

        public MockContentRequest()
        {
            super("more_games");
        }

        @Override
        protected UriComponentsBuilder createUrl(Context context) throws PlayHavenException {
            UriComponentsBuilder builder = super.createUrl(context);
            builder.queryParam("metadata", 1);
            return builder;
        }

        @Override
        protected void handleResponse(String json) {
            Log.d(TAG, "handleResponse: model");
            this.returnedModel = json;
            markReadyForTesting(this);
        }

        @Override
        protected void handleResponse(PlayHavenException e) {
            Log.d(TAG, "handleResponse: exception");
            this.returnedException = e;
            markReadyForTesting(this);
        }

        @Override
        protected String getMockJsonResponse() {
            return "{\"errobj\": null,\"response\": {\"content\": \"more_games\",\"notification\": {\"type\": \"badge\",\"value\": \"9\"}},\"error\": null}";
        }
    }

    @SmallTest
    public void testMockMoreGames() throws Exception
    {
        Instrumentation instrumentation = getInstrumentation();
        Launcher launcher = startActivitySync(Launcher.class);
        sleep(250);

        MockContentRequest req = new MockContentRequest();
        enableThreadedTesting(req);
        req.send(instrumentation.getTargetContext());
        waitForReady(req);

        if(req.returnedException != null)
            fail(req.returnedException.getMessage());

        String model = req.returnedModel;
        assertNotNull(model);
        assertNull(JsonUtil.getPath(model, "$.error"));
        assertEquals("more_games", JsonUtil.getPath(model, "$.response.content"));

        launcher.finish();
    }
}
