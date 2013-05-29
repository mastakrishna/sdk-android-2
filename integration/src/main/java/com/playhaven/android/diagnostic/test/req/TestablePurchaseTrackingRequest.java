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
import android.util.Log;
import com.playhaven.android.PlayHavenException;
import com.playhaven.android.data.Purchase;
import com.playhaven.android.diagnostic.test.PHTestCase;
import com.playhaven.android.req.PurchaseTrackingRequest;
import com.playhaven.android.req.model.ClientApiResponseModel;

/**
 * A PurchaseTrackingRequest designed to work with ActivityInstrumentationTestCase2
 */
public class TestablePurchaseTrackingRequest<ACTIVITY extends Activity>
    extends PurchaseTrackingRequest
{
    private PHTestCase<ACTIVITY> testCase;

    /**
     * ActivityInstrumentationTestCase2/JUnit doesn't work with assertions run in a background thread.
     * Save the model for later processing.
     */
    private ClientApiResponseModel returnedModel;

    /**
     * ActivityInstrumentationTestCase2/JUnit doesn't work with assertions run in a background thread.
     * Save the exception for later processing.
     */
    private Exception returnedException;

    public TestablePurchaseTrackingRequest(PHTestCase<ACTIVITY> testCase, Purchase purchase) {
        super(purchase);
        this.testCase = testCase;
    }

    public ClientApiResponseModel getReturnedModel() {
        return returnedModel;
    }

    public Exception getReturnedException() {
        return returnedException;
    }

    @Override
    protected void handleResponse(ClientApiResponseModel model) {
        Log.d(testCase.getTag(), "handleResponse: model");
        this.returnedModel = model;
        testCase.markReadyForTesting(this);
    }

    @Override
    protected void handleResponse(PlayHavenException e) {
        Log.d(testCase.getTag(), "handleResponse: exception");
        this.returnedException = e;
        testCase.markReadyForTesting(this);
    }
}
