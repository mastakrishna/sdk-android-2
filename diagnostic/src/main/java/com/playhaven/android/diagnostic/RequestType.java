package com.playhaven.android.diagnostic;

import android.content.res.Resources;

public enum RequestType
{
    Open(R.string.req_open),
    Preload(R.string.req_preload),
    Content(R.string.req_content),
    Metadata(R.string.req_meta),
    GcmReg(R.string.req_push_reg),
    GcmDereg(R.string.req_push_dereg);

    RequestType(int id)
    {
        this.id = id;
    }
    private int id;
    public String toString(Resources resources){return resources.getString(id);}
}
