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
import com.playhaven.android.data.Purchase;
import com.playhaven.android.diagnostic.Launcher;
import com.playhaven.android.diagnostic.test.req.TestablePurchaseTrackingRequest;
import com.playhaven.android.req.OpenRequest;

/**
 * Validate handling of server error responses
 */
public class ServerErrorTest
        extends PHTestCase<Launcher>
{
    private static final String TAG = ServerErrorTest.class.getSimpleName();

    public ServerErrorTest() {
        super(Launcher.class);
    }

    @SmallTest
    public void testBadIAPQty() throws Throwable{
        Launcher launcher = doActivityTestSetup();
        clearAndConfigurePlayHaven();

        Context ctx = getTargetContext();
        (new OpenRequest()).send(ctx);

        Purchase badQty = new Purchase();
        badQty.setSKU("badQty");
        badQty.setPrice(1.00);
        badQty.setStore("IntegrationTest");
        badQty.setPayload(TAG);
        badQty.setOrderId(TAG+":badQty");
        badQty.setQuantity(-1);
        badQty.setResult(Purchase.Result.Bought);

        TestablePurchaseTrackingRequest<Launcher> req = new TestablePurchaseTrackingRequest<Launcher>(this, badQty);
        enableThreadedTesting(req);
        req.send(ctx);
        waitForReady(req);

        assertNull(req.getReturnedModel());
        assertNotNull(req.getReturnedException());

        String msg = req.getReturnedException().getMessage();
        assertNotNull(msg);
        assertTrue(msg.contains("quantity"));

        launcher.finish();
    }
}
