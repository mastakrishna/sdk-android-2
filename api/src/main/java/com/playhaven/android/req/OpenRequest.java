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
package com.playhaven.android.req;

import android.content.Context;
import android.content.SharedPreferences;
import com.jayway.jsonpath.InvalidPathException;
import com.playhaven.android.PlayHaven;
import com.playhaven.android.PlayHavenException;
import com.playhaven.android.cache.Cache;
import com.playhaven.android.cache.CacheResponseHandler;
import com.playhaven.android.cache.CachedInfo;
import com.playhaven.android.data.JsonUrlExtractor;
import com.playhaven.android.util.JsonUtil;
import com.playhaven.android.util.KontagentUtil;
import com.playhaven.android.util.TimeZoneFormatter;
import org.springframework.http.HttpMethod;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static com.playhaven.android.compat.VendorCompat.ResourceType;

/**
 * Make an OPEN request to PlayHaven
 */
public class OpenRequest
extends PlayHavenRequest
{
    private static final String SCOUNT = "scount";
    private static final String SSUM = "ssum";
    private static final String STIME = "stime";
    private static final String SSTART = "sstart";
    private Cache cache;
    private Context ktContext;

    public OpenRequest()
    {
        super();
        setMethod(HttpMethod.POST);
    }

    @Override
    protected UriComponentsBuilder createUrl(android.content.Context context) throws PlayHavenException {
        UriComponentsBuilder builder = super.createUrl(context);
        builder.queryParam("tz", TimeZoneFormatter.getDefaultTimezone());

        String ktsid = KontagentUtil.getSenderId(context);
        if(ktsid == null)
        {
            ktsid = KontagentUtil.getSenderIdsAsJson(context);
            if(ktsid != null)
                builder.queryParam("ktsids", ktsid);
        }// else base request will add it in, don't duplicate

        SharedPreferences pref = PlayHaven.getPreferences(context);
        int scount = pref.getInt(SCOUNT, 0);
        long ssum = pref.getLong(SSUM, 0);
        long stime = pref.getLong(STIME, 0);
        ssum += stime;

        builder.queryParam(SCOUNT, scount);
        builder.queryParam(SSUM, ssum);

        SharedPreferences.Editor edit = pref.edit();
        // Increment count
        scount++;
        edit.putInt(SCOUNT, scount);
        // Clear current game time
        edit.putLong(STIME, 0);
        // Don't alter the previous sum until success
        // Save a start time
        Calendar rightNow = Calendar.getInstance();
        Date date = rightNow.getTime();
        edit.putLong(SSTART, date.getTime());
        // Commit changes
        edit.commit();

        return builder;
    }

    @Override
    protected void serverSuccess(android.content.Context context) {
        SharedPreferences pref = PlayHaven.getPreferences(context);
        SharedPreferences.Editor edit = pref.edit();
        // reset SCOUNT
        edit.putInt(SCOUNT, 1);
        // reset SSUM
        edit.putLong(SSUM, 0);
        // Commit changes
        edit.commit();

        // Temporarily hold onto this for updating Kontagent preferences from the Json response...
        ktContext = context;
    }

    protected void handleResponse(String json)
    {
        try{
            ArrayList<String> urls = new ArrayList<String>();
            urls.addAll(JsonUrlExtractor.getContentTemplates(json));
            cache.bulkRequest(new CacheResponseHandler() {
                @Override
                public void cacheSuccess(CachedInfo... cachedInfos) {
                    /* no-op, just don't precache */
                }

                @Override
                public void cacheFail(URL url, PlayHavenException exception) {
                    /* no-op, just don't precache */
                }
            }, urls);
        } catch (Exception e) {
            /* no-op, just don't precache */
        }

        /**
         * Setup Kontagent information, if available
         */
        try{
            String ktapi = JsonUtil.asString(json, "$.response.ktapi");
            String ktsid = JsonUtil.asString(json, "$.response.ktsid");
            KontagentUtil.setSenderId(ktContext, ktapi, ktsid);
        }catch(InvalidPathException e){
            // no-op
        }
        // And remove it to prevent any leak...
        ktContext = null;

        RequestListener handler = getResponseHandler();
        if(handler != null)
            handler.handleResponse(json);
    }

    @Override
    protected int getApiPath(Context context) 
    {
        return getCompat(context).getResourceId(context, ResourceType.string, "playhaven_request_open_v3");
    }
}
