package com.playhaven.android.diagnostic;

import android.content.Context;
import android.content.res.Resources;
import android.widget.ArrayAdapter;

public class RequestTypeAdapter
extends ArrayAdapter<String>
{
    private boolean registered;

    /**
     * Constructor
     *
     * @param context            The current context.
     */
    public RequestTypeAdapter(Context context) {
        super(context, android.R.layout.simple_spinner_item);
        setGCM(context, false);
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
        notifyDataSetChanged();
    }
}
