package org.javasimon.extra;

import org.javasimon.Split;
import org.javasimon.utils.SimonUtils;

/**
 * Object stored among Stopwatch's attributes in charge of <ul>
 * <li>Managing concurrent access to the inner ring buffer through synchronized blocks</li>
 * <li>
 * @author gquintana
 */
public class LastSplits {
	private final CircularList<Split> splits;
	
	public LastSplits(int capacity) {
		this.splits=new CircularList<Split>(capacity);
	}
	public void add(Split split) {
		synchronized(splits) {
			splits.add(split);
		}
	}
	public void clear() {
		synchronized(splits) {
			splits.clear();
		}
	}
	public int getCount() {
		synchronized(splits) {
			return splits.size();
		}
	}
	private <T> T processFunction(SplitFunction<T> function) {
		synchronized(splits) {
			if (splits.isEmpty()) {
				return null;
			}
			for(Split split:splits) {
				function.evaluate(split);
			}
			return function.result();
		}
	}
	private static interface SplitFunction<T> {
		void evaluate(Split split);
		T result();
	}
	private static abstract class AbstractSplitFunction<T> implements SplitFunction<T> {
		protected T result;
		public AbstractSplitFunction(T result) {
			this.result = result;
		}
		public abstract void evaluate(long runningFor);
		public final void evaluate(Split split) {
			evaluate(split.runningFor());
		}
		public T result() {
			return result;
		}
	}
	public Double getMean() {
		return processFunction(new AbstractSplitFunction<Double>(0.0D) {
			@Override
			public void evaluate(long runningFor) {
				result+=(double) runningFor;
			}

			@Override
			public Double result() {
				return result / (double) splits.size();
			}
			
		});
	}
	public Long getMin() {
		return processFunction(new AbstractSplitFunction<Long>(Long.MAX_VALUE) {
			@Override
			public void evaluate(long runningFor) {
				if (runningFor<result) {
					result=runningFor;
				}
			}
		});
	}
	public Long getMax() {
		return processFunction(new AbstractSplitFunction<Long>(Long.MIN_VALUE) {
			@Override
			public void evaluate(long runningFor) {
				if (runningFor>result) {
					result=runningFor;
				}
			}
		});
	}
	public Double getTrend() {
		return getTrend(1000);
	}
	public Double getTrend(final long timeDeltaThreshold) {
		return processFunction(new SplitFunction<Double>() {
			Split lastSplit;
			long result;
			int count;
			public void evaluate(Split split) {
				if (lastSplit==null) {
					lastSplit=split;
				} else {
					long timeDelta=split.getStart()-lastSplit.getStart();
					if (timeDelta>timeDeltaThreshold) {
						long durationDelta=split.runningFor()-lastSplit.runningFor();
						result += durationDelta;
						count ++;
						lastSplit=split;
					}
				}
			}

			public Double result() {
				return count>0?(result/((double) count)):null;
			}
		});
	}

	@Override
	public String toString() {
		int count;
		long min=0,mean=0,max=0,trend=0;
		// First extract data
		synchronized(splits) {
			count=getCount();
			if (count>0) {
				min=getMin();
				mean=getMean().longValue();
				max=getMax();
			}
			if (count>1) {
				trend=getTrend().longValue();
			}
		}			
		// Then free lock, and format data
		StringBuilder stringBuilder=new StringBuilder("LastSplits[size=");
		stringBuilder.append(count);
		if (count>0) {
			stringBuilder.append(",min=").append(SimonUtils.presentNanoTime(min))
				.append(",mean=").append(SimonUtils.presentNanoTime(mean))
				.append(",max=").append(SimonUtils.presentNanoTime(max));
		}
		if (count>1) {
			stringBuilder.append(",trend=").append(SimonUtils.presentNanoTime(trend));
		}
		stringBuilder.append("]");
		return stringBuilder.toString();
	}
}
