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
package com.playhaven.android.diagnostic;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.widget.Toast;

import com.playhaven.android.Version;
import com.playhaven.android.PlayHaven;
import com.playhaven.android.PlayHaven.Config;
import com.playhaven.android.PlayHavenException;

import static com.playhaven.android.diagnostic.DiagnosticPreferences.Pref.*;

/**
 * Preferences view for the Diagnostic Application
 */
public class DiagnosticPreferences extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener
{
    public enum Pref
    {
        pref_token,
        pref_secret,
        pref_server,
        pref_file,
        pref_log,
        pref_id,
        pref_sdkversion,
        pref_osversion,
        pref_osapi,
        pref_gcmacct,
        pref_projectid;

        public String getString(SharedPreferences pref)
        {
            return getString(pref, null);
        }
        public String getString(SharedPreferences pref, String defaultValue)
        {
            return pref.getString(toString(), defaultValue);
        }
    }

    public enum Category
    {
        pref_manual,
        pref_auto,
        pref_misc
    }
    
    /**
     * This is for instrumentation tests. 
     * @return
     */
    public String easterEggs = "";

    @Override
    @SuppressWarnings("deprecation")
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        /**
         * These methods are deprecated in API11 in favor of PreferenceFragment
         * But we're keeping this simple for the example
         */
        addPreferencesFromResource(R.layout.prefs);
        reset(getPreferenceScreen().getSharedPreferences());
        
        // Check for easter eggs. 
        Uri originatingUri = getIntent().getData();
        if(originatingUri != null) {
            easterEggs = getIntent().getData().getQueryParameter("easterEggs");
            if(easterEggs != null) {
            	Toast.makeText(this, String.format("Found easter eggs: %s", easterEggs), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void onDestroy() {
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Pref pref_key = Pref.valueOf(key);
        String value = pref_key.getString(sharedPreferences);

        // VALIDATION
        boolean altered = false;
        switch(pref_key)
        {
            case pref_server:
                if(value == null || value.length() == 0)
                {
                    setSummary(pref_key, null);
                    break;
                }
                if(!value.endsWith(".com"))
                {
                    value = null;
                    altered = true;
                    break;
                }
                if(!value.startsWith("http://"))
                {
                    value = "http://" + value;
                    altered = true;
                    break;
                }
                /**
                 * We *could* verify that the calling the url returns
                 * <code>
                 * {
                 *  "errobj": null,
                 *  "response": "PlayHaven Client API",
                 *  "error": null
                 * }
                 * </code>
                 */
                break;
            case pref_file:
                reset(sharedPreferences);
                break;
            default:
                break;
        }

        if(altered)
            setValue(sharedPreferences, key, value);

        setSummary(pref_key, value);
    }

    @SuppressWarnings("deprecation")
    protected void reset(SharedPreferences preferences)
    {
        preferences.unregisterOnSharedPreferenceChangeListener(this);
        for(Pref pref_key : Pref.values()){
            setSummary(pref_key, pref_key.getString(preferences));
        }

        String fileName = Pref.pref_file.getString(preferences);
        boolean fileEnabled = (fileName != null && fileName.length() > 0);
        findPreference(Category.pref_manual.toString()).setEnabled(!fileEnabled);
        if(fileEnabled)
        {
        	try {
            	PlayHaven.configure(this, fileName);
        	} catch (PlayHavenException e){
        		PlayHaven.e(e);
        	}
        	
			SharedPreferences storedPrefs = PlayHaven.getPreferences(this);
			setSummary(Pref.pref_projectid, storedPrefs.getString(Config.PushProjectId.name(), ""));
			setSummary(Pref.pref_secret, storedPrefs.getString(Config.Secret.name(), ""));
			setSummary(Pref.pref_token, storedPrefs.getString(Config.Token.name(), ""));
			setSummary(Pref.pref_server, storedPrefs.getString(Config.APIServer.name(), ""));
        }

        Preference pref = findPreference(pref_id.toString());
        pref.setSummary(new com.playhaven.android.DeviceId(this).toString());

        pref = findPreference(pref_sdkversion.toString());
        pref.setSummary(Version.PROJECT_VERSION);

        pref = findPreference(pref_osversion.toString());
        pref.setSummary(Build.VERSION.RELEASE);

        pref = findPreference(pref_osapi.toString());
        pref.setSummary(Build.VERSION.SDK_INT + "");

        pref = findPreference(pref_gcmacct.toString());
        android.accounts.AccountManager acctMgr = android.accounts.AccountManager.get(this);
        final android.accounts.Account[] accounts = acctMgr.getAccounts();
        if(accounts != null && accounts.length > 0)
        {
            for(final android.accounts.Account account : accounts)
            {
                if(account.type.equals("com.google"))
                {
                    pref.setSummary(account.name);
                    break;
                }
            }
        }

        preferences.registerOnSharedPreferenceChangeListener(this);
    }

    @SuppressWarnings("deprecation")
    protected void setSummary(Pref pref_key, String value)
    {
        Preference pref = findPreference(pref_key.toString());
        pref.setSummary(value == null ? "" : value);
    }

    protected void setValue(SharedPreferences sharedPreferences, Pref pref_key, String value)
    {
        setValue(sharedPreferences, pref_key.toString(), value);
    }
    
    protected void setValue(SharedPreferences sharedPreferences, String key, String value)
    {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if(value == null || value.length() == 0)
            editor.remove(key);
        else
            editor.putString(key, value);

        editor.commit();
    }
}
