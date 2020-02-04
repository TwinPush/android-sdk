package com.twincoders.twinpush.sdk.communications.requests.notifications;

import com.twincoders.twinpush.sdk.communications.DefaultRequestParam;
import com.twincoders.twinpush.sdk.communications.requests.TwinPushRequest;
import com.twincoders.twinpush.sdk.entities.InboxNotification;
import com.twincoders.twinpush.sdk.logging.Ln;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GetInboxRequest extends TwinPushRequest {

	public interface Listener extends ErrorListener {
		void onSuccess(List<InboxNotification> items, int totalPages);
	}

	/* Constants */
	private final static String SEGMENT = "inbox";
	/* Parameters */
	private final static String PAGE_KEY = "page";
	private final static String RESULTS_PER_PAGE_KEY = "per_page";
	private final static String TAGS_KEY = "tags";
	private final static String NO_TAGS_KEY = "no_tags";
	/* Response fields */
	private final static String RESPONSE_TOTAL_PAGES_KEY = "total_pages";
	private final static String RESPONSE_NOTIF_ARRAY_KEY = "objects";

	/* Properties */
	private Listener listener;

	public GetInboxRequest(String applicationId, String deviceId, List<String> tags, List<String> noTags, int page, int resultsPerPage, Listener listener) {
		super(applicationId, deviceId);
		this.listener = listener;
		this.httpMethod = HttpMethod.GET;
		// Segments
		addSegmentParam(SEGMENT);
		// Parameters
        if (tags != null) addParam(DefaultRequestParam.arrayParam(TAGS_KEY, tags));
        if (noTags != null) addParam(DefaultRequestParam.arrayParam(NO_TAGS_KEY, noTags));
		// Increase page value so first page is 1 
		addParam(PAGE_KEY, String.valueOf(page+1));
		addParam(RESULTS_PER_PAGE_KEY, String.valueOf(resultsPerPage));
	}
	
	@Override
	protected void onSuccess(JSONObject response) {
		int totalPages = 0;
		List<InboxNotification> items = new ArrayList<InboxNotification>();
		try {
			if (response.has(RESPONSE_TOTAL_PAGES_KEY)) {
				totalPages = response.getInt(RESPONSE_TOTAL_PAGES_KEY);
			}
			
			JSONArray jsonArray = response.getJSONArray(RESPONSE_NOTIF_ARRAY_KEY);
			for (int i=0; i<jsonArray.length(); i++) {
				JSONObject json = jsonArray.getJSONObject(i);
				InboxNotification item = parseInboxNotification(json);
				items.add(item);
			}
			getListener().onSuccess(items, totalPages);
		} catch (JSONException e) {
			Ln.e(e, "Error while trying to parse inbox notifications from response");
			getListener().onError(e);
		}
	}
	
	public Listener getListener() {
		return listener;
	}

}
