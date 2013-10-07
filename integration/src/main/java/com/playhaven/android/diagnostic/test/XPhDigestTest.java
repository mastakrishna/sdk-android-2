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
import android.util.Log;
import com.playhaven.android.PlayHavenException;
import com.playhaven.android.diagnostic.Launcher;
import com.playhaven.android.req.OpenRequest;
import com.playhaven.android.req.SignatureException;

public class XPhDigestTest
extends PHTestCase<Launcher>
{
    private static final String TAG = XPhDigestTest.class.getSimpleName();

    public XPhDigestTest() {
        super(Launcher.class);
    }

    private class GoodOpenRequest extends OpenRequest
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

        public String getReturnedModel() {
            return returnedModel;
        }

        public Exception getReturnedException() {
            return returnedException;
        }

        @Override
        protected void handleResponse(String json) {
            Log.d(XPhDigestTest.this.getTag(), "handleResponse: model");
            this.returnedModel = json;
            XPhDigestTest.this.markReadyForTesting(this);
        }

        @Override
        protected void handleResponse(PlayHavenException e) {
            Log.d(XPhDigestTest.this.getTag(), "handleResponse: exception");
            this.returnedException = e;
            XPhDigestTest.this.markReadyForTesting(this);
        }

        @Override
        protected void validateSignatures(Context context, String xPhDigest, String json) throws SignatureException {
            super.validateSignatures(context, xPhDigest, json);
        }
    }

    private class BadOpenRequest extends GoodOpenRequest
    {
        @Override
        protected void validateSignatures(Context context, String xPhDigest, String json) throws SignatureException {
            char first = xPhDigest.charAt(0);
            xPhDigest = xPhDigest.replace(first, (char)(first+1));
            super.validateSignatures(context, xPhDigest, json);
        }
    }

    @SmallTest
    public void testGoodSignature() throws Throwable
    {
        Launcher launcher = doActivityTestSetup();
        clearAndConfigurePlayHaven();

        Context ctx = getTargetContext();

        GoodOpenRequest open = new GoodOpenRequest();
        enableThreadedTesting(open);
        open.send(ctx);
        waitForReady(open);

        if(open.getReturnedException() != null)
            fail(open.getReturnedException().getMessage());

        Log.d(TAG, "Successfully found GOOD signature");

        launcher.finish();

    }

    @SmallTest
    public void testBadSignature() throws Throwable
    {
        Launcher launcher = doActivityTestSetup();
        clearAndConfigurePlayHaven();

        Context ctx = getTargetContext();

        BadOpenRequest open = new BadOpenRequest();
        enableThreadedTesting(open);
        open.send(ctx);
        waitForReady(open);

        assertNotNull(open.getReturnedException());
        Log.d(TAG, "Successfully found BAD signature: " + open.getReturnedException().getMessage());

        launcher.finish();

    }
}
