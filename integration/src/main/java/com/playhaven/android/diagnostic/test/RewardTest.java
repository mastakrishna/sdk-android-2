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
import com.playhaven.android.data.Reward;
import com.playhaven.android.diagnostic.Launcher;
import com.playhaven.android.diagnostic.test.req.TestableContentRequest;
import com.playhaven.android.req.OpenRequest;
import com.playhaven.android.req.model.Content;
import com.playhaven.android.req.model.OpenDispatch;
import com.playhaven.android.req.model.Response;
import com.playhaven.android.req.model.RewardParam;

import java.util.ArrayList;

/**
 * Validate Reward serialization
 */
public class RewardTest
        extends PHTestCase<Launcher>
{
    private static final String TAG = RewardTest.class.getSimpleName();

    public RewardTest() {
        super(Launcher.class);
    }

    @SmallTest
    public void testReward() throws Throwable
    {
        Launcher launcher = doActivityTestSetup();
        configurePlayHaven();

        Context ctx = getTargetContext();
        (new OpenRequest()).send(ctx);

        String json = getJSON(R.raw.reward);
        assertNotNull(json);

        TestableContentRequest<Launcher> req = TestableContentRequest.mock(this, "reward", json);
        enableThreadedTesting(req);
        req.send(ctx);
        waitForReady(req);

        if(req.getReturnedException() != null)
            fail(req.getReturnedException().getMessage());

        assertNotNull(req.getReturnedModel());
        assertNull(req.getReturnedModel().getError());
        Response response = req.getReturnedModel().getResponse();
        assertNotNull(response);
        com.playhaven.android.req.model.Context mCtx = response.getContext();
        assertNotNull(mCtx);
        Content cnt = mCtx.getContent();
        assertNotNull(cnt);
        OpenDispatch dispatch = cnt.getOpenDispatch();
        assertNotNull(dispatch);
        RewardParam param = dispatch.getParameters();
        assertNotNull(param);
        ArrayList<Reward> rewards = Reward.fromParameters(param);
        assertNotNull(rewards);
        assertEquals(1, rewards.size());
        Reward reward = rewards.get(0);
        assertEquals("coins", reward.getTag());
        assertEquals(Double.valueOf(50), reward.getQuantity());


        launcher.finish();

    }
}
