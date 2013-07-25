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
import android.content.SharedPreferences;
import android.test.suitebuilder.annotation.SmallTest;
import com.playhaven.android.PlayHaven;
import com.playhaven.android.diagnostic.Launcher;

/**
 * Test DeviceId
 */
public class DeviceIdTest
        extends PHTestCase<Launcher>
{
    public DeviceIdTest() {
        super(Launcher.class);
    }

    @SmallTest
    public void testDeviceId() throws Exception
    {
        Instrumentation instrumentation = getInstrumentation();
        Launcher launcher = startActivitySync(Launcher.class);
        clearAndConfigurePlayHaven();
        SharedPreferences pref = PlayHaven.getPreferences(instrumentation.getTargetContext());
        String devId = pref.getString(PlayHaven.Config.DeviceId.toString(), null);
        assertNotNull(devId);
        assertNotSame("", devId);
        launcher.finish();
    }

}
