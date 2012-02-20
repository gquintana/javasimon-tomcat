package org.javasimon.extra;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.javasimon.SimonManager;
import org.javasimon.Split;
import org.javasimon.Stopwatch;
import static org.junit.Assert.*;
import org.junit.Test;
/**
 *
 * @author gquintana
 */
public class CircularListTest {
	@Test
	public void testAddAndSize() {
		CircularList<String> list=new CircularList<String>(5);
		assertEquals(0,list.size());
		list.add("A"); assertEquals(1,list.size());
		list.add("B"); assertEquals(2,list.size());
		list.add("C"); assertEquals(3,list.size());
		list.add("D"); assertEquals(4,list.size());
		list.add("E"); assertEquals(5,list.size());
		list.add("F"); assertEquals(5,list.size());
		list.add("G"); assertEquals(5,list.size());
	}
	private String toString(List<?> elements) {
		StringBuilder stringBuilder=new StringBuilder();
		boolean first=true;
		for(Object element:elements) {
			if (first) {
				first=false;
			} else {
				stringBuilder.append(",");
			}
			stringBuilder.append(element.toString());
		}
		return stringBuilder.toString();
	}
	@Test
	public void testIterator() {
		CircularList<String> list=new CircularList<String>(5);
		assertEquals("", toString(list));
		list.addAll(Arrays.asList("A"));
		assertEquals("A", toString(list));
		list.addAll(Arrays.asList("B","C"));
		assertEquals("A,B,C", toString(list));
		list.addAll(Arrays.asList("D","E"));
		assertEquals("A,B,C,D,E", toString(list));
		list.addAll(Arrays.asList("F","G","H"));
		assertEquals("D,E,F,G,H", toString(list));
		list.addAll(Arrays.asList("I","J"));
		assertEquals("F,G,H,I,J", toString(list));
	}
	private void assertArrayEquals(String[] expected, Object[] actual) {
		assertEquals("Element count",expected.length, actual.length);
		for (int i = 0; i < expected.length; i++) {
			assertEquals("Element #"+i, expected[i], actual[i]);
		}
	}
	@Test
	public void testToArray() {
		CircularList<String> list=new CircularList<String>(5);
		assertArrayEquals(new String[0], list.toArray());
		list.addAll(Arrays.asList("A","B","C"));
		assertArrayEquals(new String[]{"A","B","C"}, list.toArray());
		list.addAll(Arrays.asList("D","E"));
		assertArrayEquals(new String[]{"A","B","C","D","E"}, list.toArray());
		list.addAll(Arrays.asList("F","G"));
		assertArrayEquals(new String[]{"C","D","E","F","G"}, list.toArray());
	}
	@Test
	public void testAddPerformance() {
		final int iterations=1000000;
		List<Integer> circularList=new CircularList<Integer>(10);
		Stopwatch stopwatch=SimonManager.getStopwatch(getClass().getName()+".testAddPerformance");
		Split split=stopwatch.start();
		for(int i=0;i<iterations;i++) {
			circularList.add(i);
		}
		long circular=split.stop().runningFor();
		LinkedList<Integer> linkedList=new LinkedList<Integer>();
		split=stopwatch.start();
		for(int i=0;i<iterations;i++) {
			linkedList.add(i);
			if (linkedList.size()>10) {
				linkedList.removeFirst();
			}
		}
		long linked=split.stop().runningFor();
		System.out.println("Circular "+circular+" /Linked "+linked +" "+((linked-circular)*100/circular)+"%");
	}
}
