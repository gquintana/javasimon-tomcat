/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.javasimon.extra;

import org.javasimon.Simon;
import org.javasimon.Split;
import org.javasimon.Stopwatch;
import org.javasimon.callback.CallbackSkeleton;

/**
 * Callback retaining the last N stopwatch splits.
 * @author gquintana
 */
public class LastSplitsCallback extends CallbackSkeleton{
	/**
	 * Simon attribute name of the LastSplit object stored
	 * in Simons
	 */
	public static final String ATTR_NAME_LAST_SPLITS = "lastSplits";
	/**
	 * Number of splits retained in each Simon.
	 * Default 10
	 */
	private final int capacity;

	public LastSplitsCallback() {
		this.capacity=10;
	}

	public LastSplitsCallback(int capacity) {
		this.capacity=capacity;
	}

	private LastSplits getLastSplits(Stopwatch stopwatch) {
		return (LastSplits) stopwatch.getAttribute(ATTR_NAME_LAST_SPLITS);
	}
	/**
	 * When Stopwatch is created, a Last Splits attributes is added
	 */
	@Override
	public void onSimonCreated(Simon simon) {
		if (simon instanceof Stopwatch) {
			Stopwatch stopwatch=(Stopwatch) simon;
			stopwatch.setAttribute(ATTR_NAME_LAST_SPLITS, new LastSplits(capacity));
		}
		
	}

	/**
	 * When a Splits is stopped, it's added to the stopwatch a Last Splits attribute
	 */
	@Override
	public void onStopwatchStop(Split split) {
		LastSplits lastSplits=getLastSplits(split.getStopwatch());
		lastSplits.add(split);
		System.out.println(lastSplits.toString());
	}
	
	/**
	 * When the Stopwatch is reseted, the Last splits attribute as well 
	 */
	@Override
	public void onSimonReset(Simon simon) {
		if (simon instanceof Stopwatch) {
			getLastSplits((Stopwatch) simon).clear();
		}
	}
}
