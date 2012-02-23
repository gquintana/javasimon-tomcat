package org.javasimon.extra.calltree;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import org.javasimon.Split;
import org.javasimon.utils.SimonUtils;

/**
 * Call tree node
 * @author gquintana
 */
public class CallTreeNode {
	/**
	 * Name, used as a key
	 */
	private final String name;
	/**
	 * Splits
	 */
	private final List<Split> splits=new ArrayList<Split>(1);
	/**
	 * Child tree nodes
	 */
	private Map<String,CallTreeNode> children;
	
	public CallTreeNode(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
	public void addSplit(Split split) {
		splits.add(split);
	}
	public int getCounter() {
		return splits.size();
	}
	public long getTotal() {
		long total=0;
		for(Split split:splits) {
			total+=split.runningFor();
		}
		return total;
	}
	public CallTreeNode addChild(String name) {
		if (children==null) {
			children=new HashMap<String, CallTreeNode>();
		}
		CallTreeNode child=new CallTreeNode(name);
		children.put(name,child);
		return child;
	}
	public CallTreeNode getChild(String name) {
		return (children==null)?null:children.get(name);
	}
	public Collection<CallTreeNode> getChildren() {
		return children==null?(List<CallTreeNode>)Collections.EMPTY_LIST:children.values();
	}
	public CallTreeNode getOrAddChild(String name) {
		CallTreeNode child=getChild(name);
		if (child==null) {
			child=addChild(name);
		}
		return child;
	}
	public void print(PrintWriter printWriter, String prefix, Long parentTotal) {
		long total=getTotal();
		printWriter.print(prefix);
		printWriter.print(name);
		printWriter.print(' ');
		if (parentTotal!=null) {
			printWriter.print(total*100/parentTotal);
			printWriter.print("%, ");
		}
		printWriter.print(SimonUtils.presentNanoTime(total));
		long counter=getCounter();
		if (counter>1) {
			printWriter.print(", "); 
			printWriter.print(counter);
		}
		printWriter.println();
		for(CallTreeNode child:getChildren()) {
			child.print(printWriter, prefix+"\t", total);
		}
	}
	@Override
	public String toString() {
		StringWriter stringWriter=new StringWriter();
		PrintWriter printWriter=new PrintWriter(stringWriter);
		print(printWriter,"",null);
		return stringWriter.toString();
	}
}
