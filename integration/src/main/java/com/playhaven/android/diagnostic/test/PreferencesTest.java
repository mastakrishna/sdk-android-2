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

import android.preference.EditTextPreference;
import android.test.suitebuilder.annotation.SmallTest;

import com.playhaven.android.diagnostic.DiagnosticPreferences;
import com.playhaven.android.PlayHaven;

/**
 * Test the Diagnostic App Launcher activity
 */
public class PreferencesTest extends PHTestCase<DiagnosticPreferences>
{
	private enum EditTextPrefsToCheckFor {
		pref_token,
		pref_secret,
		pref_server,
		pref_projectid,
		pref_file
	}
	
    public PreferencesTest()
    {
        super(DiagnosticPreferences.class);
    }
    
    /**
     * Just makes sure the editable preferences are there. 
     */
    @SmallTest
    public void testStart() throws Exception
    {
        DiagnosticPreferences activity = startActivitySync(DiagnosticPreferences.class);
        
        EditTextPreference editTextPref;
        for(EditTextPrefsToCheckFor prefKey : EditTextPrefsToCheckFor.values()){
        	editTextPref = null;
        	editTextPref = (EditTextPreference) activity.findPreference(prefKey.name());
        	PlayHaven.i("EditTextPreference found: %s.", editTextPref.getTitle());
            assertNotNull(editTextPref);
        }
        
        activity.finish();
    }
}
