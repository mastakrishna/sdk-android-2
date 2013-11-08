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
import android.content.res.Resources;
import android.util.AttributeSet;
import android.widget.TextView;
import com.playhaven.android.req.PlayHavenRequest;

public class OutputBox extends TextView {
	String lastRequest;
	String lastResponse;
	String lastResult;

    public enum OutputType {
        Request(R.string.output_request),
        Response(R.string.output_response),
        Result(R.string.output_result);

        OutputType(int id) {
            this.id = id;
        }
        
        private int id;
        
        public String toString(Resources resources) {
        	return resources.getString(id);
        }
    }
	
	public OutputBox(Context context) {
		super(context);
	}
	
	public OutputBox(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public void updateRequest(PlayHavenRequest req) {
		try {
			updateRequest(req.getLastUrl().toString());
		} catch (Exception e) {
			updateRequest(e.getMessage());
		}
	}
	
	public void updateRequest(String req) {
		lastRequest = req;
		lastResponse = getContext().getString(R.string.output_hint_text);
		lastResult = getContext().getString(R.string.output_hint_text);
		
		updateDisplay(OutputType.Request);
	}

	public void updateResponse(Exception e) {
		updateResponse(e.getMessage());
	}
	
	public void updateResponse(String response) {
		lastResponse = response;
	}
	
	public void updateResult(String result) {
		lastResult = result;
	}
	
	public void updateDisplay(OutputType type) {
		switch(type) { 
			case Request:
				this.setText(lastRequest);
				break;
			case Response:
				this.setText(lastResponse);
				break;
			case Result:
				this.setText(lastResult);
				break;
		}
	}
}
