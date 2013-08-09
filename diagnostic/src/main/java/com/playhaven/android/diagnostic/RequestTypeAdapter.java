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
