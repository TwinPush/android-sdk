package com.twincoders.twinpush.sdk.communications.requests.statistics;

import com.twincoders.twinpush.sdk.communications.requests.TwinPushRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ReportStatisticsRequest extends TwinPushRequest {

	/* Constants */
	/* Segments */
	private final static String ACTION_SEGMENT = "report_statistics";
	/* Parameters */
	private final static String STATISTICS_KEY = "device";
	private final static String LATITUDE_KEY = "latitude";
	private final static String LONGITUDE_KEY = "longitude";
	
	/* Properties */
	DefaultListener listener;
	
	public ReportStatisticsRequest(String appId, String deviceId, double latitude, double longitude, DefaultListener listener) {
		super(appId, deviceId);
		this.listener = listener;
		this.httpMethod = HttpMethod.POST;
		// Segments
		addSegmentParam(ACTION_SEGMENT);
		// Parameters
		Map<String, Object> statistics = new HashMap<String, Object>();
		statistics.put(LATITUDE_KEY, latitude);
		statistics.put(LONGITUDE_KEY, longitude);
		addParam(STATISTICS_KEY, statistics);
	}

	@Override
	public ErrorListener getListener() {
		return listener;
	}

	@Override
	protected void onSuccess(JSONObject response) {
		listener.onSuccess();
	}
}
