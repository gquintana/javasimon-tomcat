package org.javasimon.extra;

import org.javasimon.SimonManager;
import org.javasimon.Split;
import org.javasimon.Stopwatch;
import org.javasimon.utils.SimonUtils;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author gquintana
 */
public class LastSplitsCallbackTest {
	private Stopwatch getStopwatch() {
		return SimonManager.getStopwatch(getClass().getName()+".stopwatch");
	}
	private LastSplits getLastSplits() {
		return (LastSplits) getStopwatch().getAttribute(LastSplitsCallback.ATTR_NAME_LAST_SPLITS);
	}
	private Split addSplit(long length) {
		Split split=getStopwatch().start();
		try {
			Thread.sleep(length);
		} catch (InterruptedException interruptedException) {
		}
		split.stop();
		return split;
	}
	private static LastSplitsCallback lastSplitsCallback=new LastSplitsCallback(5);
	@BeforeClass
	public static void addCallcack() {
		SimonManager.manager().callback().addCallback(lastSplitsCallback);
	}
	@AfterClass
	public static void removeCallcack() {
		SimonManager.manager().callback().removeCallback(lastSplitsCallback);
	}
	@Before
	public void resetStopwatch() {
		getStopwatch().reset();
	}
	private void assertTimeEquals(String name, long expected, long actual) {
		assertTrue(name, Math.abs(expected-actual)<5);
	}
	@Test
	public void testAddSplit() {
		addSplit(100L);
		addSplit(150L);
		addSplit(125L);
		addSplit(150L);
		LastSplits lastSplits=getLastSplits();
		assertTimeEquals("Min", 100L, lastSplits.getMin().longValue()/SimonUtils.NANOS_IN_MILLIS);
		assertTimeEquals("Max", 150L, lastSplits.getMax().longValue()/SimonUtils.NANOS_IN_MILLIS);
		assertTimeEquals("Mean", (150L*2+125L+100L)/4L, lastSplits.getMean().longValue()/SimonUtils.NANOS_IN_MILLIS);
	}
	@Test
	public void testTrend() {
		addSplit(100L);
		addSplit(125L);
		addSplit(150L);
		LastSplits lastSplits=getLastSplits();
		assertTrue("Positive trend", lastSplits.getTrend()>0);
		resetStopwatch();
		addSplit(150L);
		addSplit(125L);
		addSplit(100L);
		lastSplits=getLastSplits();
		assertTrue("Negative trend", lastSplits.getTrend()<0);
	}
}
