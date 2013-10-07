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
package com.playhaven.android.diagnostic.test.req;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import com.playhaven.android.PlayHavenException;
import com.playhaven.android.diagnostic.test.PHTestCase;
import com.playhaven.android.req.ContentRequest;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * A ContentRequest designed to work with ActivityInstrumentationTestCase2
 */
public class TestableContentRequest<ACTIVITY extends Activity>
    extends ContentRequest
{
    private PHTestCase<ACTIVITY> testCase;

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

    public TestableContentRequest(PHTestCase<ACTIVITY> testCase, int placementResId) {
        super(placementResId);
        this.testCase = testCase;
    }

    public TestableContentRequest(PHTestCase<ACTIVITY> testCase, String placementTag) {
        super(placementTag);
        this.testCase = testCase;
    }

    public String getReturnedModel() {
        return returnedModel;
    }

    public Exception getReturnedException() {
        return returnedException;
    }

    @Override
    protected void handleResponse(String json) {
        Log.d(testCase.getTag(), "handleResponse: model");
        this.returnedModel = json;
        testCase.markReadyForTesting(this);
    }

    @Override
    protected void handleResponse(PlayHavenException e) {
        Log.d(testCase.getTag(), "handleResponse: exception");
        this.returnedException = e;
        testCase.markReadyForTesting(this);
    }

    public static <ACTIVITY extends Activity> TestableContentRequest<ACTIVITY> mock(final PHTestCase<ACTIVITY> testCase, final String placementTag, final String json)
    {
        return new TestableContentRequest<ACTIVITY>(testCase, placementTag)
        {
            @Override
            protected UriComponentsBuilder createUrl(Context context) throws PlayHavenException {
                return null;
            }

            /**
             * To pretend to call the server, and return a mock result - return the JSON result here
             * Note: This is used for testing
             *
             * @return json result
             */
            @Override
            protected String getMockJsonResponse() {
                return json;
            }
        };
    }
}
