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
import com.playhaven.android.diagnostic.Launcher;
import com.playhaven.android.req.UserAgent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserAgentTest extends PHTestCase <Launcher> {
	
    public UserAgentTest() {
        super(Launcher.class);
    }

    @SmallTest
    public void testUserAgent() throws Throwable {
    	Matcher matcher = Pattern.compile("[a-zA-Z\\-\\.0-9]*[\\/][a-zA-Z\\-\\.0-9]*[ ][(][a-zA-Z\\-\\.0-9 ]*[)]").matcher(UserAgent.USER_AGENT);
    	if(!matcher.matches()){
    		fail("User agent string not as expected.");
    	}
    }
}
