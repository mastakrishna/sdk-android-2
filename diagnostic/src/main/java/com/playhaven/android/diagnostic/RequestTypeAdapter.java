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

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.widget.ArrayAdapter;

public class RequestTypeAdapter
extends ArrayAdapter<String>
{
    private boolean registered;
    private static final String GCM_REGISTERED = "gcm.registered";
    private SharedPreferences pref;

    /**
     * Constructor
     *
     * @param context            The current context.
     */
    public RequestTypeAdapter(Context context) {
        super(context, android.R.layout.simple_spinner_item);
        pref = PreferenceManager.getDefaultSharedPreferences(context);
        setGCM(context, pref.getBoolean(GCM_REGISTERED, false));
    }

    public void setGCM(Context context, boolean registered)
    {
        Resources resources = context.getResources();
        clear();
        for(RequestType type : RequestType.values())
        {
            switch (type) {
                case GcmReg:
                    // Only show 'GcmReg' if we are NOT already registered
                    if(!registered)
                        add(type.toString(resources));

                    break;
                case GcmDereg:
                    // Only show 'GcmDereg' if we are already registered
                    if(registered)
                        add(type.toString(resources));

                    break;
                default:
                    add(type.toString(resources));
                    break;
            }
        }

        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(GCM_REGISTERED, registered);
        editor.commit();
        notifyDataSetChanged();
    }
}
