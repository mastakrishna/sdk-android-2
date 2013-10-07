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
import com.jayway.jsonpath.JsonPath;
import com.playhaven.android.diagnostic.Launcher;
import com.playhaven.android.diagnostic.test.req.TestableContentRequest;
import com.playhaven.android.req.OpenRequest;
import com.playhaven.android.util.JsonUtil;

import java.util.List;

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
        clearAndConfigurePlayHaven();

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

        String model = req.getReturnedModel();
        assertNotNull(model);
        assertNull(JsonUtil.getPath(model, "$.error"));
        assertNotNull(JsonUtil.getPath(model, "$.response"));
        List<net.minidev.json.JSONObject> rewards = JsonUtil.getPath(model, "$.response.context.content.open_dispatch.parameters.rewards");
        assertNotNull(rewards);
        assertEquals(1, rewards.size());
        net.minidev.json.JSONObject reward = rewards.get(0);
        assertEquals("coins", JsonUtil.getPath(reward, "$.reward"));
        assertEquals(50, JsonUtil.<Double>getPath(reward, "$.quantity"));
        launcher.finish();

    }
}
