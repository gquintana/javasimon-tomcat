package org.javasimon.extra.calltree;

import org.javasimon.Split;
import org.javasimon.callback.CallbackSkeleton;
import org.javasimon.utils.SimonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Callback which logs a call tree when a the main call is bigger than a threshold.
 * Call tree looks like this:
 * <code>
 * org.javasimon.web.Controller.execute 123ms
 *	org.javasimon.business.FirstService.work 75ms, 75% 
 *		org.javasimon.data.FirstDAO.findAll 50 ms, 82%
 *		org.javasimon.data.SecondDAO.findByRelation 20ms, 10%, 3 
 *	org.javasimon.business.SecodeService.do 10ms, 5%
 * </code>
 * @author gquintana
 */
public class CallTreeCallback extends CallbackSkeleton {
	private final ThreadLocal<CallTree> threadCallTree=new ThreadLocal<CallTree>();
	private static final Logger LOGGER=LoggerFactory.getLogger(CallTreeCallback.class);
	private long logThreshold=500;
	public CallTreeCallback() {
	}

	public CallTreeCallback(long logThreshold) {
		this.logThreshold=logThreshold;
	}
	
	public long getLogThreshold() {
		return logThreshold;
	}

	public void setLogThreshold(long logThreshold) {
		this.logThreshold = logThreshold;
	}

	private CallTree getOrCreateCallTree() {
		CallTree callTree=getCallTree();
		if (callTree==null) {
			callTree=new CallTree(){
				@Override
				protected void onRootStopwatchStop(CallTreeNode rootNode,Split split) {
					CallTreeCallback.this.onRootStopwatchStop(rootNode, split);
				}
			};
			threadCallTree.set(callTree);
		}
		return callTree;
	}

	private CallTree getCallTree() {
		return threadCallTree.get();
	}
	private void removeCallTree() {
		threadCallTree.remove();
	}
	@Override
	public void onStopwatchStart(Split split) {
		getOrCreateCallTree().onStopwatchStart(split);
	}

	@Override
	public void onStopwatchStop(Split split) {
		getCallTree().onStopwatchStop(split);
	}
	public void onRootStopwatchStop(CallTreeNode rootNode, Split split) {
		if (rootNode.getTotal()>logThreshold*SimonUtils.NANOS_IN_MILLIS) {
			LOGGER.warn("Call Tree alert\r\n"+rootNode.toString());
		}
		removeCallTree();
	}
	
}
