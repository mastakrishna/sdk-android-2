package com.playhaven.android.req;

import org.springframework.web.util.UriComponentsBuilder;

import android.content.Context;

import com.playhaven.android.PlayHavenException;
import com.playhaven.android.push.PushReceiver;

public class ContentUnitRequest extends ContentRequest {
	private String messageId;
	private String contentUnitId;

	public ContentUnitRequest(String placementTag) {
		super(placementTag);
	}

    @Override
    protected UriComponentsBuilder createUrl(Context context) throws PlayHavenException {
    	UriComponentsBuilder builder = super.createUrl(context);
    	builder.queryParam(PushReceiver.PushParams.message_id.name(), messageId);
    	builder.queryParam(PushReceiver.PushParams.content_id.name(), contentUnitId);
    	return builder;
    }

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public String getContentUnitId() {
		return contentUnitId;
	}

	public void setContentUnitId(String contentUnitId) {
		this.contentUnitId = contentUnitId;
	}
}
