package com.playtech.casino.common;

import java.io.Serializable;

public class QueryStatistics implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8411470440262576449L;
	private int noOfQueriesProcessed;
	private Double maxProcessTime, minProcessTime, averageProcessTime;

	public int getNoOfQueriesProcessed() {
		return noOfQueriesProcessed;
	}

	public void setNoOfQueriesProcessed(int noOfQueriesProcessed) {
		this.noOfQueriesProcessed = noOfQueriesProcessed;
	}

	public Double getMaxProcessTime() {
		return maxProcessTime;
	}

	public void setMaxProcessTime(Double maxProcessTime) {
		this.maxProcessTime = maxProcessTime;
	}

	public Double getMinProcessTime() {
		return minProcessTime;
	}

	public void setMinProcessTime(Double minProcessTime) {
		this.minProcessTime = minProcessTime;
	}

	public Double getAverageProcessTime() {
		return averageProcessTime;
	}

	public void setAverageProcessTime(Double averageProcessTime) {
		this.averageProcessTime = averageProcessTime;
	}

	@Override
	public String toString() {
		return "QueryStatistics [noOfQueriesProcessed=" + noOfQueriesProcessed + ", maxProcessTime=" + maxProcessTime
				+ ", minProcessTime=" + minProcessTime + ", averageProcessTime=" + averageProcessTime + "]";
	}

}
