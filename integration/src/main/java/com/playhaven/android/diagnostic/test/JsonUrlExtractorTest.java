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

import android.test.suitebuilder.annotation.SmallTest;
import com.playhaven.android.data.DataboundMapper;
import com.playhaven.android.data.JsonUrlExtractor;
import com.playhaven.android.diagnostic.Launcher;
import com.playhaven.android.req.model.ClientApiResponseModel;

import java.io.IOException;
import java.util.List;

/**
 * Validate the JsonUrlExtractor utility
 */
public class JsonUrlExtractorTest
        extends PHTestCase<Launcher>
{
    public JsonUrlExtractorTest() {
        super(Launcher.class);
    }

    @SmallTest
    public void testPrecache() throws InterruptedException, IOException {
        Launcher launcher = doActivityTestSetup();

        String json = getJSON(R.raw.precache);
        DataboundMapper mapper = new DataboundMapper();
        ClientApiResponseModel model = mapper.readValue(json, ClientApiResponseModel.class);

        List<String> urls = JsonUrlExtractor.getContentTemplates(model);
        assertNotNull(urls);
        assertTrue(urls.size() == 4);
        for(String url : urls)
        {
            assertNotNull(url);
            assertFalse(url.startsWith("\""));
            assertFalse(url.endsWith("\""));
            assertTrue(url.endsWith(".html.gz"));
        }

        launcher.finish();
    }
}
