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
import android.test.suitebuilder.annotation.MediumTest;
import com.playhaven.android.PlayHaven;
import com.playhaven.android.PlayHavenException;
import com.playhaven.android.compat.VendorCompat;
import com.playhaven.android.diagnostic.Launcher;
import com.playhaven.android.req.OpenRequest;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.net.URI;

/**
 * Verify that the PluginIdentifer parameter is added to the URL
 */
public class PluginIdentifierTest
        extends PHTestCase<Launcher>
{
    private static final String TAG = PluginIdentifierTest.class.getSimpleName();
    private static final String PARAM_NAME = "plugin";

    private enum Plugin
    {
        ValidName("myplugin-1.2.3", "myplugin-1.2.3"),
        ValidCapital("MyPlugin-1.2.3", "MyPlugin-1.2.3"),
        ValidTypes("ABCabc123-._~", "ABCabc123-._~"),
        InvalidChar("ABC$123", "ABC123"),
        InvalidLength("1234567890123456789012345678901234567890123", "123456789012345678901234567890123456789012");
        Plugin(String requested, String corrected)
        {
            this.requested = requested;
            this.corrected = corrected;
        }
        public String requested, corrected;
    }

    public PluginIdentifierTest() {
        super(Launcher.class);
    }

    class TestOpenRequest
    extends OpenRequest
    {
        /**
         * To actually call the server, but with a mock URL - return it here
         *
         * @param context of the request
         * @return the url to call
         * @throws com.playhaven.android.PlayHavenException
         *          if there is a problem
         */
        @Override
        public String getUrl(Context context) throws PlayHavenException {
            return super.getUrl(context);
        }
    }

    @MediumTest
    public void testPermutations() throws Throwable{
        Launcher launcher = doActivityTestSetup();
        Context ctx = getTargetContext();

        for(Plugin plugin : Plugin.values())
        {
            clearPreferences();
            PlayHaven.setVendorCompat(ctx, new VendorCompat(plugin.requested));
            configurePlayHaven();

            TestOpenRequest req = new TestOpenRequest();
            String url = req.getUrl(ctx);
            String value = null;
            for(NameValuePair pair : URLEncodedUtils.parse(new URI(url), "UTF-8"))
            {
                if(!PARAM_NAME.equals(pair.getName())) continue;
                value = pair.getValue();
                break;
            }

            assertEquals(plugin.corrected, value);
        }

        launcher.finish();
    }
}
