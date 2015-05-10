/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2012-2015 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/

package org.jtrfp.trcl.coll;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ListActionDispatcherTest {
    private ListActionDispatcher<Integer> subject;

    @Before
    public void setUp() throws Exception {
	subject = new ListActionDispatcher<Integer>(new ArrayList<Integer>());
    }// end setUp()

    @After
    public void tearDown() throws Exception {
	subject = null;
    }// end tearDown()

    @Test
    public void testAddTarget() {
	final List<Integer> l0 = new ArrayList<Integer>();
	assertTrue(subject.addTarget(l0, false));
	subject.add(0);
	subject.add(1);
	subject.add(2);
	assertEquals(0,(int)l0.get(0));
	assertEquals(2,(int)l0.get(2));
	assertEquals(3,l0.size());
	final List<Integer> l1 = new ArrayList<Integer>();
	assertTrue(subject.addTarget(l1, false));
	assertEquals(0,l1.size());
	
	subject.add(5);
	
	final List<Integer> l2 = new ArrayList<Integer>();
	assertTrue(subject.addTarget(l2, true));
	assertEquals(4,l2.size());
	assertEquals(4,subject.size());
	subject.remove(1);
	assertEquals(2,(int)l0.get(1));
	assertEquals(5,(int)l1.get(0));
	assertEquals(2,(int)l2.get(1));
    }//end testAddTarget())

    @Test
    public void testRemoveTarget() {
	final List<Integer> l0 = new ArrayList<Integer>();
	assertTrue(subject.addTarget(l0, true));
	subject.add(0);
	subject.add(1);
	subject.add(2);
	assertEquals(3,l0.size());
	assertTrue(subject.removeTarget(l0, true));
	assertEquals(0,l0.size());
	assertTrue(subject.addTarget(l0, false));
	assertEquals(0,l0.size());
    }// end testRemoveTarget()

    @Test
    public void testAddE() {
	assertTrue(subject.add(0));
    }// end testAddE()

    @Test
    public void testAddIntE() {
	final List<Integer> l0 = new ArrayList<Integer>();
	subject.addTarget(l0, true);
	for(int i=0; i<20; i++)
	    subject.add(i);
	subject.add(10, -5);
	assertEquals(-5,(int)l0.get(10));
	assertEquals(9,(int)l0.get(9));
	assertEquals(10,(int)l0.get(11));
    }// end testAddIntE()

    @Test
    public void testAddAllCollectionOfQextendsE() {
	final List<Integer> l0 = new ArrayList<Integer>();
	subject.addTarget(l0, true);
	final List<Integer> src = new ArrayList<Integer>();
	src.add(0);
	src.add(1);
	src.add(2);
	assertTrue (subject.addAll(src));
	l0.clear();
	assertTrue (l0.addAll(src));
    }// testAddAllCollectionOfQextendsE()

    @Test
    public void testAddAllIntCollectionOfQextendsE() {
	final List<Integer> l0 = new ArrayList<Integer>();
	subject.addTarget(l0, true);
	final List<Integer> src = new ArrayList<Integer>();
	src.add(0);
	src.add(1);
	src.add(2);
	subject.addAll(0,src);
	assertEquals(3,subject.size());
	assertEquals(2,(int)subject.get(2));
    }//end testAddAllIntColletionOfQextendsE()

    @Test
    public void testClear() {
	final List<Integer> l0 = new ArrayList<Integer>();
	subject.addTarget(l0, true);
	final List<Integer> src = new ArrayList<Integer>();
	src.add(0);
	src.add(1);
	src.add(2);
	subject.addAll(0,src);
	assertEquals(3,l0.size());
	subject.clear();
	assertEquals(0,l0.size());
    }//end testClear()

    @Test
    public void testContains() {
	final List<Integer> l0 = new ArrayList<Integer>();
	subject.addTarget(l0, true);
	subject.add(0);
	subject.add(1);
	subject.add(2);
	assertEquals(3,l0.size());
	assertTrue  (l0.contains(1));
	assertFalse (l0.contains(3));
    }//end testContains()

    @Test
    public void testContainsAll() {
	final List<Integer> l0 = new ArrayList<Integer>();
	final ArrayList<Integer> test = new ArrayList<Integer>();
	subject.addTarget(l0, true);
	subject.add(0);
	subject.add(1);
	subject.add(2);
	test.add(0);
	test.add(1);
	test.add(2);
	assertTrue  (l0.containsAll     (test));
	assertTrue  (subject.containsAll(test));
	test.add(5);
	assertFalse (l0.containsAll     (test));
	assertFalse (subject.containsAll(test));
    }//end testContainsAll()

    @Test
    public void testGet() {
	final List<Integer> l0 = new ArrayList<Integer>();
	subject.addTarget(l0, true);
	subject.add(0);
	subject.add(1);
	subject.add(2);
	assertEquals(1,(int)subject.get(1));
	assertEquals(2,(int)subject.get(2));
	assertEquals(1,(int)l0.get(1));
	assertEquals(2,(int)l0.get(2));
    }//end testGet()

    @Test
    public void testIndexOf() {
	final List<Integer> l0 = new ArrayList<Integer>();
	subject.addTarget(l0, true);
	subject.add(0);
	subject.add(1);
	subject.add(2);
	assertEquals(0,(int)subject.indexOf(0));
	assertEquals(2,(int)subject.indexOf(2));
	assertEquals(0,(int)l0.indexOf(0));
	assertEquals(2,(int)l0.indexOf(2));
    }//end testIndexOf()

    @Test
    public void testIsEmpty() {
	final List<Integer> l0 = new ArrayList<Integer>();
	subject.addTarget(l0, true);
	subject.add(0);
	subject.add(1);
	subject.add(2);
	assertTrue(subject.contains(0));
	assertTrue(subject.contains(2));
	assertTrue(l0.contains(0));
	assertTrue(l0.contains(2));
    }//end testIsEmpty()

    @Test
    public void testIterator() {
	final List<Integer> l0 = new ArrayList<Integer>();
	subject.addTarget(l0, true);
	subject.add(0);
	subject.add(1);
	subject.add(2);
	Iterator<Integer> sI = subject.iterator();
	assertNotNull(sI);
	assertTrue(sI.hasNext());
	assertEquals(0,(int)sI.next());
	assertTrue(sI.hasNext());
	assertEquals(1,(int)sI.next());
	assertTrue(sI.hasNext());
	assertEquals(2,(int)sI.next());
	Iterator<Integer> lI = l0.iterator();
	assertNotNull(lI);
	assertTrue(lI.hasNext());
	assertEquals(0,(int)lI.next());
	assertTrue(lI.hasNext());
	assertEquals(1,(int)lI.next());
	assertTrue(lI.hasNext());
	assertEquals(2,(int)lI.next());
    }//end testIterator()

    @Test
    public void testLastIndexOf() {
	final List<Integer> l0 = new ArrayList<Integer>();
	subject.addTarget(l0, true);
	subject.add(0);
	subject.add(1);
	subject.add(2);
	subject.add(1);
	assertEquals(3,(int)l0.lastIndexOf(1));
	assertEquals(3,(int)subject.lastIndexOf(1));
    }//end tesTLastIndexOf()

    @Test
    public void testListIterator() {
	final List<Integer> l0 = new ArrayList<Integer>();
	subject.addTarget(l0, true);
	subject.add(0);
	subject.add(1);
	subject.add(2);
	ListIterator<Integer> sI = subject.listIterator();
	assertNotNull(sI);
	assertTrue(sI.hasNext());
	assertEquals(0,(int)sI.next());
	assertTrue(sI.hasNext());
	assertEquals(1,(int)sI.next());
	assertTrue(sI.hasNext());
	assertEquals(2,(int)sI.next());
	ListIterator<Integer> lI = l0.listIterator();
	assertNotNull(lI);
	assertTrue(lI.hasNext());
	assertEquals(0,(int)lI.next());
	assertTrue(lI.hasNext());
	assertEquals(1,(int)lI.next());
	assertTrue(lI.hasNext());
	assertEquals(2,(int)lI.next());
    }//end testListIterator()

    @Test
    public void testListIteratorInt() {
	final List<Integer> l0 = new ArrayList<Integer>();
	subject.addTarget(l0, true);
	subject.add(0);
	subject.add(1);
	subject.add(2);
	ListIterator<Integer> sI = subject.listIterator(1);
	assertNotNull(sI);
	assertTrue(sI.hasNext());
	assertEquals(1,(int)sI.next());
	assertTrue(sI.hasNext());
	assertEquals(2,(int)sI.next());
	ListIterator<Integer> lI = l0.listIterator(1);
	assertNotNull(lI);
	assertTrue(lI.hasNext());
	assertEquals(1,(int)lI.next());
	assertTrue(lI.hasNext());
	assertEquals(2,(int)lI.next());
    }//end testListIteratorInt()

    @Test
    public void testRemoveObject() {
	final List<Integer> l0 = new ArrayList<Integer>();
	subject.addTarget(l0, true);
	subject.add(0);
	subject.add(1);
	subject.add(2);
	assertEquals(3,subject.size());
	assertEquals(3,l0.size());
	assertTrue(subject.contains(2));
	assertTrue(subject.remove((Integer)2));
	assertEquals(2,subject.size());
	assertEquals(2,l0.size());
	assertFalse(subject.contains(2));
	assertFalse(l0.contains(2));
    }//end testRemoveObjet

    @Test
    public void testRemoveInt() {
	final List<Integer> l0 = new ArrayList<Integer>();
	subject.addTarget(l0, true);
	subject.add(0);
	subject.add(1);
	subject.add(2);
	assertEquals(3,subject.size());
	assertEquals(3,l0.size());
	assertTrue(subject.contains(2));
	assertEquals(Integer.valueOf(2),subject.remove(2));
	assertEquals(2,subject.size());
	assertEquals(2,l0.size());
	assertFalse(subject.contains(2));
	assertFalse(l0.contains(2));
    }//end testRemoveInt()

    @Test
    public void testRemoveAll() {
	final List<Integer> l0 = new ArrayList<Integer>();
	subject.addTarget(l0, true);
	subject.add(0);
	subject.add(1);
	subject.add(2);
	final List<Integer> remove = new ArrayList<Integer>();
	remove.add(1);
	remove.add(2);
	assertTrue (subject.removeAll(remove));
	assertFalse(subject.removeAll(remove));
	assertEquals(1,subject.size());
	assertEquals(1,l0.size());
    }//end testRemoveAll()

    @Test
    public void testRetainAll() {
	final List<Integer> l0 = new ArrayList<Integer>();
	subject.addTarget(l0, true);
	subject.add(0);
	subject.add(1);
	subject.add(2);
	final List<Integer> retain = new ArrayList<Integer>();
	retain.add(1);
	retain.add(2);
	assertTrue (subject.retainAll(retain));
	assertFalse(subject.retainAll(retain));
    }//end testRetainAll()

    @Test
    public void testSet() {
	final List<Integer> l0 = new ArrayList<Integer>();
	subject.addTarget(l0, true);
	subject.add(0);
	subject.add(1);
	subject.add(2);
	assertEquals(1,(int)subject.set(1, 5));
	assertEquals(3,l0.size());
	assertEquals(3,subject.size());
	assertEquals(5,(int)subject.get(1));
	assertEquals(5,(int)l0.get(1));
    }//end testSet()

    @Test
    public void testSize() {
	final List<Integer> l0 = new ArrayList<Integer>();
	subject.addTarget(l0, true);
	subject.add(0);
	subject.add(1);
	subject.add(2);
	assertEquals(3,subject.size());
	assertEquals(3,l0.size());
	subject.remove(1);
	assertEquals(2,subject.size());
	assertEquals(2,l0.size());
	subject.add(7);
	subject.add(8);
	subject.add(9);
	assertEquals(5,subject.size());
	assertEquals(5,l0.size());
    }//end testSize()

    @Test
    public void testSubList() {
	final List<Integer> l0 = new ArrayList<Integer>();
	subject.addTarget(l0, true);
	subject.add(0);
	subject.add(1);
	subject.add(2);
	List<Integer> sl = subject.subList(1, 2);
	assertNotNull(sl);
	assertEquals(1,(int)sl.get(0));
    }//end testSubList()

    @Test
    public void testToArray() {
	final List<Integer> l0 = new ArrayList<Integer>();
	subject.addTarget(l0, true);
	subject.add(0);
	subject.add(1);
	subject.add(2);
	Object o = l0.toArray();
	assertNotNull(o);
	assertTrue(o instanceof Object[]);
	Object [] lA = (Object[])o;
	assertEquals(3,(int)lA.length);
	assertEquals(0,(int)((Integer)lA[0]));
	assertEquals(2,(int)((Integer)lA[2]));
	o = subject.toArray();
	assertNotNull(o);
	assertTrue(o instanceof Object[]);
	Object [] sA = (Object[])o;
	assertEquals(3,(int)sA.length);
	assertEquals(0,(int)(Integer)sA[0]);
	assertEquals(2,(int)(Integer)sA[2]);
    }//end testToArray()

    @Test
    public void testToArrayTArray() {
	final List<Integer> l0 = new ArrayList<Integer>();
	subject.addTarget(l0, true);
	subject.add(0);
	subject.add(1);
	subject.add(2);
	Object o = l0.toArray(new Integer[]{});
	assertNotNull(o);
	assertTrue(o instanceof Integer[]);
	Integer [] lA = (Integer[])o;
	assertEquals(3,(int)lA.length);
	assertEquals(0,(int)lA[0]);
	assertEquals(2,(int)lA[2]);
	o = subject.toArray(new Integer[]{});
	assertNotNull(o);
	assertTrue(o instanceof Integer[]);
	Integer [] sA = (Integer[])o;
	assertEquals(3,(int)sA.length);
	assertEquals(0,(int)sA[0]);
	assertEquals(2,(int)sA[2]);
    }//end testToArrayTArray()
    
    @Test
    public void testEquals(){
	assertTrue(this.equals(this));
    }

}//end ListActionDispatcherTest
