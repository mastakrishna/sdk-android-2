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
package com.playhaven.android.examples;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import com.playhaven.android.PlayHaven;
import com.playhaven.android.PlayHavenException;
import com.playhaven.android.data.Purchase;
import com.playhaven.android.req.OpenRequest;
import com.playhaven.android.req.PurchaseTrackingRequest;

import java.util.Date;
import java.util.Random;

/**
 * Demonstrates how to do IAP tracking without VGP
 */
public class IAPTrackingExample
extends Activity
{
    /**
     * Unique name for logging
     */
    private static final String TAG = IAPTrackingExample.class.getSimpleName();

    /**
     * For creating fake orderId and payload
     */
    private static final Random random = new Random(new Date().getTime());

    /**
     * Called when the Activity is created
     *
     * @param savedInstanceState from the previous run
     */
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        /**
         * Set our layout to src/main/android/res/layout/iap.xml
         */
        setContentView(R.layout.iap);

        try {
            PlayHaven.setLogLevel(Log.DEBUG);

            /**
             * Here, we configure PlayHaven to use the Token and Secret specified in the Dashboard
             * @see <a href="https://dashboard.playhaven.com/">https://dashboard.playhaven.com/</a>
             *
             * In this example, we are grabbing the value of the token and secret from
             * src/main/android/res/values/strings.xml
             */
            PlayHaven.configure(this, R.string.token, R.string.secret);

            /**
             * Create and send Open Request.
             */
            (new OpenRequest()).send(this);
        } catch (PlayHavenException e) {
            Log.e(TAG, "We have encountered an error", e);
        }
    }


    private void track(View target, Double price, Purchase.Result result)
    {
        Purchase purchase = new Purchase();
        purchase.setSKU(getResources().getResourceName(target.getId()));
        purchase.setPrice(price);
        purchase.setStore("OurCoolStore");
        String orderId = random.nextLong() + "ABC";
        purchase.setPayload(TAG + ":" + orderId);
        purchase.setOrderId(orderId);
        purchase.setQuantity(1);
        purchase.setResult(result);
        (new PurchaseTrackingRequest(purchase)).send(this);
    }

    public void buyableItemBought(View target)
    {
        track(target, 1.50, Purchase.Result.Bought);
    }

    public void alreadyOwnedItemFailed(View target)
    {
        track(target, 10.00, Purchase.Result.Owned);
    }

    public void itemNoLongerExists(View target)
    {
        track(target, 0.00, Purchase.Result.Invalid);
    }
}
