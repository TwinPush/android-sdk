package com.twincoders.twinpush.sdk.communications.requests.statistics;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import com.twincoders.twinpush.sdk.communications.requests.TwinPushRequest;

public class ReportStatisticsRequest extends TwinPushRequest {

	/* Constants */
	/* Segments */
	private final static String ACTION_SEGMENT = "report_statistics";
	/* Parameters */
	private final static String DEVICE_ID_KEY = "id";
	private final static String STATISTICS_KEY = "device";
	private final static String LATITUDE_KEY = "latitude";
	private final static String LONGITUDE_KEY = "longitude";
	
	/* Properties */
	DefaultListener listener;
	
	public ReportStatisticsRequest(double latitude, double longitude, DefaultListener listener, String deviceId) {
		super();
		this.listener = listener;
		this.httpMethod = HttpMethod.POST;
		// Segments
		addSegmentParam(ACTION_SEGMENT);
		// Parameters
		addParam(DEVICE_ID_KEY, deviceId);
		Map<String, Object> statistics = new HashMap<String, Object>();
		statistics.put(LATITUDE_KEY, latitude);
		statistics.put(LONGITUDE_KEY, longitude);
		addParam(STATISTICS_KEY, statistics);
		
//		List<TwinRequestParam> statistics = new ArrayList<TwinRequestParam>();
//		statistics.add(DefaultRequestParam.simpleParam(LATITUDE_KEY, latitude));
//		statistics.add(DefaultRequestParam.simpleParam(LONGITUDE_KEY, longitude));
//		addParam(DefaultRequestParam.complexParam(STATISTICS_KEY, statistics));
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
