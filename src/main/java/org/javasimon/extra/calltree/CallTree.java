package org.javasimon.extra.calltree;

import java.util.LinkedList;
import org.javasimon.Split;

/**
 * Call tree contains the root call tree node and the current call stack
 * @author gquintana
 */
public class CallTree {
	private final LinkedList<CallTreeNode> callStack=new LinkedList<CallTreeNode>();
	public void onStopwatchStart(Split split) {
		final String name=split.getStopwatch().getName();
		CallTreeNode currentNode;
		if (callStack.isEmpty()) {
			// Root tree node
			currentNode=new CallTreeNode(name);
		} else {
			// Child node
			currentNode=callStack.getLast().getOrAddChild(name);
		}
		callStack.addLast(currentNode);
	}
	public void onStopwatchStop(Split split) {
		CallTreeNode currentNode=callStack.removeLast();
		currentNode.addSplit(split);
		if (callStack.isEmpty()) {
			onRootStopwatchStop(currentNode, split);
		}
	}
	protected void onRootStopwatchStop(CallTreeNode callTreeNode,Split split) {
	}
}
