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
package com.playhaven.android.util;

import org.hamcrest.core.IsEqual;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.junit.Assert.*;

/**
 * Tests TimeZoneFormatter
 */
@RunWith(JUnit4.class)
public class TimeZoneTest
{
    @Test
    public void pdt()
    {
        // Not using US/Pacific in case it is currently PST
        String result = TimeZoneFormatter.getTimezone("GMT-7");
        assertNotNull(result);
        assertEquals("-7.00", result);
    }

    @Test
    public void pst()
    {
        // Not using US/Pacific in case it is currently PDT
        String result = TimeZoneFormatter.getTimezone("GMT-8");
        assertNotNull(result);
        assertEquals("-8.00", result);
    }

    @Test
    public void caracas()
    {
        String result = TimeZoneFormatter.getTimezone("America/Caracas");
        assertNotNull(result);
        assertEquals("-4.30", result);
    }

    @Test
    public void kiev()
    {
        String result = TimeZoneFormatter.getTimezone("Europe/Kiev");
        assertNotNull(result);
        IsEqual<String> winter = new IsEqual<String>("2.00");
        IsEqual<String> summer = new IsEqual<String>("3.00");
        assertThat(result, anyOf(winter, summer));
    }
}
