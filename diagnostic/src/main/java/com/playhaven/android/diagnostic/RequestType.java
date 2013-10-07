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
