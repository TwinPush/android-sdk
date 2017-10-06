package com.twincoders.twinpush.sdk.entities;

/**
 * Summary information of the alias inbox
 */

public class InboxSummary {

    private int totalCount, unopenedCount;

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getUnopenedCount() {
        return unopenedCount;
    }

    public void setUnopenedCount(int unopenedCount) {
        this.unopenedCount = unopenedCount;
    }
}
