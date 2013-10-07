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

import java.util.LinkedHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.jayway.jsonpath.JsonPath;
import com.playhaven.android.PlayHaven;
import com.playhaven.android.PlayHavenException;
import com.playhaven.android.data.Purchase;
import com.playhaven.android.req.ContentRequest;
import com.playhaven.android.req.ContentUnitRequest;
import com.playhaven.android.req.MetadataRequest;
import com.playhaven.android.req.OpenRequest;
import com.playhaven.android.req.PurchaseTrackingRequest;
import com.playhaven.android.req.PushTrackingRequest;
import com.playhaven.android.req.RequestListener;
import com.playhaven.android.req.SubcontentRequest;
import com.playhaven.android.req.UrlRequest;

public class APIValidationIndicators extends Activity {
	private String mContentUnitId;
	private String mModel;
	private String mPlacementTag;
	
	// This keeps track of both the view ids and their default text. 
	private LinkedHashMap<Integer, String> indicatorIds;
	
	@Override
	public void onCreate(Bundle saved) {
		super.onCreate(saved);
        setContentView(R.layout.api_status);
        
        indicatorIds = new LinkedHashMap<Integer, String>();
        indicatorIds.put(R.id.open_req, "Open Request");
        indicatorIds.put(R.id.content_req, "Content Request");
        indicatorIds.put(R.id.content_unit_req, "Content Unit Request");
        indicatorIds.put(R.id.gcm_reg_req, "GCM Registration Request");
        indicatorIds.put(R.id.gcm_dereg_req, "GCM Deregistration Request");
        indicatorIds.put(R.id.purchase_req, "Purchase Request");
        indicatorIds.put(R.id.metadata_req, "Metadata Request");
        indicatorIds.put(R.id.sub_content_req, "Subcontent Request");
        indicatorIds.put(R.id.sub_content_unit_req, "Subcontent Unit Request");
        indicatorIds.put(R.id.url_req, "Url Request");

		for(Integer key : indicatorIds.keySet()) {
			((Button) findViewById(key)).setText(indicatorIds.get(key));
		}
	}
	
	public void startRequests(View goBtn) {
		String placementTag = ((EditText) findViewById(R.id.placementBox)).getText().toString();
		if(placementTag != null && placementTag.length() > 0) {
			mPlacementTag = placementTag;
		} else {
			((EditText) findViewById(R.id.placementBox)).setError(getString(R.string.go_error));
			return;
		}
		
		// These requests aren't dependent on other results. 
		final int[] freeRequests = {
				R.id.open_req,
				R.id.content_req,
				R.id.gcm_reg_req,
				R.id.gcm_dereg_req,
				R.id.purchase_req
		};
		
		Thread t = new Thread(){
			@Override
			public void run(){
				for(int id : freeRequests) {
					doRequest(findViewById(id));
				}
			}
		};
		t.start();
	}
	
	public void doRequest(View v) {
		updateUI(v.getId(), "Doing request: %s %s", "", R.drawable.test_new);

		switch(v.getId()){
			case R.id.open_req:
				OpenRequest open = new OpenRequest();
				open.setResponseHandler(new MListener(v.getId()));
				open.send(getApplicationContext());
				break;
			case R.id.gcm_reg_req:
	    		PushTrackingRequest deRegReq = new PushTrackingRequest("test", null, null);
	    		deRegReq.setResponseHandler(new MListener(v.getId()));
	    		deRegReq.send(getApplicationContext());
				break;
			case R.id.gcm_dereg_req:
	    		PushTrackingRequest regReq = new PushTrackingRequest("", null, null);
	    		regReq.setResponseHandler(new MListener(v.getId()));
	    		regReq.send(getApplicationContext());
				break;
			case R.id.purchase_req:
		        Purchase purchase = new Purchase();
		        purchase.setSKU("test");
		        purchase.setQuantity(1);
		        purchase.setResult(Purchase.Result.Bought);
		        
		        PurchaseTrackingRequest purchaseRequest = new PurchaseTrackingRequest(purchase);
		        purchaseRequest.setResponseHandler(new MListener(v.getId()));
		        purchaseRequest.send(getApplicationContext());
				break;
			case R.id.content_req:
				// The url request expects this to be a More Games request. 
				ContentRequest placementRequest = new ContentRequest(mPlacementTag);
				placementRequest.setResponseHandler(new MListener(v.getId()));
				placementRequest.send(getApplicationContext());
				break;
			case R.id.sub_content_req:
				// @TODO get this from the content request 
				SubcontentRequest subReq = new SubcontentRequest("{ \"url\": \"\", \"additional_parameters\": { \"skip_featured\": \"\", \"placement_id\": \"" + mPlacementTag + "\", \"args\": \"skip_featured\", \"skip_content\": \"\" } }");
				subReq.setResponseHandler(new MListener(v.getId()));
				subReq.send(getApplicationContext());
				break;
			case R.id.metadata_req:
				MetadataRequest metaReq = new MetadataRequest(mPlacementTag);
				metaReq.setResponseHandler(new MListener(v.getId()));
				metaReq.send(getApplicationContext());
				break;
			case R.id.url_req:
                String firstUrl = getJson(mModel, "$.response.context.content.items[0].buy_dispatch.parameters.url");
                UrlRequest urlRequest = new UrlRequest(firstUrl);
                final Long timestamp = System.currentTimeMillis();
                final Future<String> uriFuture = Executors.newSingleThreadExecutor().submit(urlRequest);
                
                String url = null;
                try {
                    url = uriFuture.get();
                	PlayHaven.v("UrlRequest retrieved: %s", url);
                } catch (Exception e) {
                    PlayHaven.e("Could not retrieve launch URL from server.");
                }
                if(url != null && url.startsWith("market:")){
                	String tsString = String.format("(%sms)", System.currentTimeMillis() - timestamp);
        	    	updateUI(R.id.url_req, "Success: %s %s", tsString, R.drawable.test_succeeded);
                } else {
        	    	updateUI(R.id.url_req, "Failure: %s, got: %s", url, R.drawable.test_failed);
                }
                break;
			case R.id.content_unit_req:
				ContentUnitRequest contentReq = new ContentUnitRequest(null);
				contentReq.setContentUnitId(mContentUnitId);
				contentReq.setMessageId("999999");
				contentReq.setResponseHandler(new MListener(v.getId()));
				contentReq.send(getApplicationContext());
				break;
			case R.id.sub_content_unit_req:
				SubcontentRequest subContentUnitReq = new SubcontentRequest("{ \"url\": \"\", \"additional_parameters\": {\"message_id\": \"00000\", \"content_id\": \"" + mContentUnitId + "\", \"skip_featured\": \"\", \"args\": \"skip_featured\", \"skip_content\": \"0\" } }");
				subContentUnitReq.setResponseHandler(new MListener(v.getId()));
				subContentUnitReq.send(getApplicationContext());
				break;
		}
	}
	
	private class MListener implements RequestListener {
		private int mId;
		private long mTimestamp;
		
		public MListener(int id){
			super();
			mId = id;
			mTimestamp = System.currentTimeMillis();
		}

		@Override
		public void handleResponse(String json) {
			boolean responseIsGood = true;
			String timeMsg = String.format("(%sms)", (System.currentTimeMillis() - mTimestamp));
			
			if(json != null) {
		    	// @TODO validate each of the responses more thoroughly, as with some kind of schema. <.<
		    	switch(mId) {
		    		case R.id.open_req:
		    			responseIsGood = (getJson(json, "$.response.precache") != null);
		    			break;
		    		case R.id.content_req:
                        if(getJson(json, "$.response.url") == null){
                            responseIsGood = false;
		    			} else {
		    				try {
                                Uri url = Uri.parse(getJson(json, "$.response.close_ping"));
					    		mContentUnitId = url.getQueryParameter("content_id");
		    				} catch (Exception e) {
		    					responseIsGood = false;
		    					break;
		    				}
		    				
				    		// These requests are dependent on the results of the first content request. 
				    		final int[] dependantRequests = {
				    				R.id.content_unit_req,
				    				R.id.url_req,
				    				R.id.metadata_req,
				    				R.id.sub_content_req,
				    				R.id.sub_content_unit_req
				    		};
				    		
				    		runOnUiThread(new Runnable() {
				    		    public void run() {
				    		    	for(Integer id : dependantRequests) {
				    		    		((Button) findViewById(id)).setText(indicatorIds.get(id));
				    		    	}
				    		    }
				    		});

				    		mModel = json;
		    		    	for(Integer id : dependantRequests) {
		    		    		doRequest(findViewById(id));
		    		    	}
		    			}
		    			break;
		    		case R.id.content_unit_req:
		    			responseIsGood = (getJson(json, "$.response.url") != null);
		    			break;
		    		case R.id.gcm_reg_req:
		    			responseIsGood = (getJson(json, "$.response.push_token") != null);
		    			break;
		    		case R.id.gcm_dereg_req:
		    			responseIsGood = (getJson(json, "$.response.push_token") == null);
                        break;
		    		case R.id.purchase_req:
		    			responseIsGood = (getJson(json, "$.response") != null);
		    			break;
		    		case R.id.metadata_req:
		    			responseIsGood = ("badge".equals(getJson(json, "$.response.notification.type")));
		    			break;
		    		case R.id.sub_content_req:
		    			responseIsGood = (getJson(json, "$.response.url") != null);
                        break;
		    		case R.id.sub_content_unit_req:
		    			responseIsGood = (getJson(json, "$.response.url") != null);
                        break;
		    	}
			}
			
			if(responseIsGood){
		    	updateUI(mId, "Success: %s %s", timeMsg, R.drawable.test_succeeded);
			} else {
		    	updateUI(mId, "Failure: %s%s", "'s response did not validate.", R.drawable.test_failed);
			}
		}

		@Override
		public void handleResponse(PlayHavenException e) {
	    	updateUI(mId, "Failure: %s %s", e.getMessage(), R.drawable.test_failed);
		}
	}
	
	private void updateUI(final int id, final String format, final String secondArg, final int drawable){
		runOnUiThread(new Runnable() {
		    public void run() {
				Button button = (Button) findViewById(id);
				button.setText(String.format(format, indicatorIds.get(id), secondArg));
				
				Drawable icon = getBaseContext().getResources().getDrawable(drawable);
				button.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null );
		    }
		});
	}
	
	private String getJson(String json, String path) {
		try {
			return JsonPath.read(json, path).toString();
		} catch (Exception e) {
			return null;
		}
	}
}