package com.twincoders.twinpush.sdk.entities;

public enum LocationPrecision {
	
	/** Finer location accuracy */
	FINE	(TimeConstants.A_SECOND, 		5),
	/** High accuracy */
	HIGH	(TimeConstants.FIVE_SECONDS, 	10),
	/** Medium accuracy */ 
	MEDIUM	(TimeConstants.A_MINUTE, 		50),
	/** Low location accuracy */
	LOW		(TimeConstants.FIVE_MINUTES, 	500),
	/** Approximate accuracy */
	COARSE	(TimeConstants.AN_HOUR, 		1000);
	
	long minUpdateTime;
	int minUpdateDistance;
	
	private LocationPrecision(long minUpdateTime, int minUpdateDistance) {
		this.minUpdateTime = minUpdateTime;
		this.minUpdateDistance = minUpdateDistance;
	}

	/**
	 * Minimum time change to notify an update, in milliseconds
	 */
	public long getMinUpdateTime() {
		return minUpdateTime;
	}

	/**
	 * Minimum distance change to notify an update, in meters
	 */
	public int getMinUpdateDistance() {
		return minUpdateDistance;
	}
}
