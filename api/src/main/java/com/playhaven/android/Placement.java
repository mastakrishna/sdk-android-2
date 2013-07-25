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
package com.playhaven.android;

import android.content.Context;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import com.playhaven.android.cache.Cache;
import com.playhaven.android.cache.CacheResponseHandler;
import com.playhaven.android.cache.CachedInfo;
import com.playhaven.android.data.DataboundMapper;
import com.playhaven.android.data.JsonUrlExtractor;
import com.playhaven.android.req.ContentRequest;
import com.playhaven.android.req.NoContentException;
import com.playhaven.android.req.RequestListener;
import com.playhaven.android.req.model.ClientApiResponseModel;
import com.playhaven.android.req.model.Response;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

/**
 * A representation of a placement
 */
public class Placement
implements Parcelable, CacheResponseHandler, RequestListener
{
    /**
     * Placement tag as defined in the Dashboard
     *
     * @see <a href="https://dashboard.playhaven.com/">https://dashboard.playhaven.com/</a>
     */
    protected String placementTag;

    /**
     * Databound model returned from the server
     */
    private ClientApiResponseModel model;

    /**
     * Listener for successful loading of the content
     * NOT serialized
     */
    private PlacementListener listener;

    /**
     * Cache used for loading of the content
     * NOT serialized
     */
    protected Cache cache;

    /**
     * Flag to indicate whether this placement is in the process of loading
     * NOT serialized
     */
    protected boolean isLoading = false;

    /**
     * Construct a placement from a tag
     *
     * @param placementTag from the Dashboard
     * @see <a href="https://dashboard.playhaven.com/">https://dashboard.playhaven.com/</a>
     */
    public Placement(String placementTag)
    {
        this.placementTag = placementTag;
    }

    /**
     * Construct a placement from a Parcel
     *
     * @param in parcel containing this object
     */
    public Placement(Parcel in)
    {
        readFromParcel(in);
    }

    /**
     * Retrieve the placement tag as defined in the Dashboard
     *
     * @return placement tag
     * @see <a href="https://dashboard.playhaven.com/">https://dashboard.playhaven.com/</a>
     */
    public String getPlacementTag() {
        return placementTag;
    }

    /**
     * Set the placement tag as defined in the Dashboard
     *
     * @param placementTag to set
     * @see <a href="https://dashboard.playhaven.com/">https://dashboard.playhaven.com/</a>
     */
    public void setPlacementTag(String placementTag) {
        this.placementTag = placementTag;
    }

    /**
     * Retrieve the databound model returned from the server
     *
     * @return model
     */
    public ClientApiResponseModel getModel() {
        return model;
    }

    /**
     * Set the databound model returned from the server
     *
     * @param model to set
     */
    public void setModel(ClientApiResponseModel model) {
        this.model = model;
    }

    /**
     * Retrieve the listener used to detect successful loading of the content
     *
     * @return the listener
     */
    public PlacementListener getListener() {
        return listener;
    }

    /**
     * Set the listener used to detect successful loading of the content
     *
     * @param listener to set
     */
    public void setListener(PlacementListener listener) {
        this.listener = listener;
    }

    /**
     * Required Android annoyance used to create an instance of this class
     */
    public static final Parcelable.Creator<Placement> CREATOR = new Creator<Placement>()
    {
        public Placement createFromParcel(Parcel in){return new Placement(in);}
        public Placement[] newArray(int size){return new Placement[size];}
    };

    /**
     * Describe the kinds of special objects contained in this Parcelable's marshalled representation.
     *
     * @return a bitmask indicating the set of special object types marshalled by the Parcelable.
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Flatten this object in to a Parcel.
     *
     * @param dest  The Parcel in which the object should be written.
     * @param flags Additional flags about how the object should be written. May be 0 or Parcelable#PARCELABLE_WRITE_RETURN_VALUE.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(placementTag);

        if(model == null)
        {
            dest.writeString(null);
        }else{
            try{
                dest.writeString((new DataboundMapper()).writeValueAsString(model));
            }catch(Exception e){
                PlayHaven.w(e);
                dest.writeString(null);
            }
        }
    }

    /**
     * Deserialize this object from a Parcel
     *
     * @param in parcel to read from
     */
    protected void readFromParcel(Parcel in)
    {
        setPlacementTag(in.readString());

        String modelStr = in.readString();
        if(modelStr != null)
        {
            try{
                model = (new DataboundMapper()).readValue(modelStr, ClientApiResponseModel.class);
            }catch(Exception e){
                PlayHaven.w(e);
            }
        }
    }

    /**
     * Preload a content unit for this placement. 
     * @param context of the caller
     */
    public void preload(final Context context)
    {
        if(isLoading) return;

        if(placementTag == null)
        {
            // what to do?
            PlayHaven.d("Skipping 'null' placement");
            return;
        }

        PlayHaven.d("Start server call: %s", placementTag);
        isLoading = true;

        try{
            if(cache == null)
                cache = new Cache(context);
        } catch (PlayHavenException e) {
            contentFailed(e);
        }
        
        ContentRequest content = new ContentRequest(placementTag);
        content.setPreload(true);
        content.setResponseHandler(this);
        content.send(context);
    }

    /**
     * Notify listener that the content was loaded successfully
     */
    protected void contentLoaded() {
        isLoading = false;
        if(listener != null)
            listener.contentLoaded(this);
    }

    /**
     * Notify listener that the content failed to load successfully
     *
     * @param e containing the error that prevented loading
     */
    protected void contentFailed(PlayHavenException e) {
        isLoading = false;
        if(listener != null)
            listener.contentFailed(this, e);
    }

    /**
     * Called when a files are successfully retrieved
     *
     * @param cachedInfos of cached content
     */
    @Override
    public void cacheSuccess(CachedInfo... cachedInfos)
    {
        contentLoaded();
    }

    /**
     * Called when a file fails to be retrieved
     *
     * @param url       of the request
     * @param exception reason for the failure
     */
    @Override
    public void cacheFail(URL url, PlayHavenException exception) {
        contentFailed(exception);
    }

    /**
     * Is this placement in a state appropriate for displaying
     *
     * @return true if this placement is not empty and is already loaded
     */
    public boolean isDisplayable()
    {
        return isLoaded() && !isEmpty();
    }

    /**
     * Is this placement empty?
     *
     * @return if there is no response in the model, or the model has not yet been loaded from the server
     */
    public boolean isEmpty()
    {
        if(!isLoaded()) return true;
        return (model.getResponse() == null);
    }

    /**
     * Has this placement been loaded?
     *
     * @return true if the model has been loaded.
     */
    public boolean isLoaded()
    {
        return model != null;
    }

    /**
     * Is this placement in the process of loading
     *
     * @return true if this placement has started, but not yet completed, the loading process
     */
    public boolean isLoading()
    {
        return isLoading;
    }


    public void reset()
    {
        isLoading = false;
        model = null;
    }

    /**
     * Handle the databound model returned from the server call
     *
     * @param model bound to the json response from the server
     */
    @Override
    public void handleResponse(ClientApiResponseModel model) {
        setModel(model);
        if(model == null)
        {
            contentFailed(new PlayHavenException("No returned model"));
            return;
        }

        Object err = model.getError();
        if(err != null)
        {
            contentFailed(new PlayHavenException(err.toString()));
            return;
        }

        try{
            Response response = model.getResponse();
            if(response == null)
            {
                contentFailed(new NoContentException());
                return;
            }

            ArrayList<String> urls = new ArrayList<String>();
            urls.addAll(JsonUrlExtractor.getContentTemplates(model));
            // Pre API level 11 one cannot intercept resource requests, and 
            // so preloaded images aren't useful. 
            if(Build.VERSION.SDK_INT >= 11){
                urls.addAll(JsonUrlExtractor.getImages(model));
            }
            cache.bulkRequest(Placement.this, urls);
        }catch(PlayHavenException e){
            contentFailed(e);
        }catch(IOException e2){
            contentFailed(new PlayHavenException(e2));
        }catch (Exception e3){
            contentFailed(new PlayHavenException(e3.getMessage()));
        }
    }

    /**
     * Handle the exception that occurred while trying to retrieve the model from the server
     *
     * @param e that occurred
     */
    @Override
    public void handleResponse(PlayHavenException e) {
        contentFailed(e);
    }

}
