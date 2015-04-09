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

package org.jtrfp.trcl.pool;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.jtrfp.trcl.coll.ListActionDispatcher;
import org.jtrfp.trcl.dbg.PropertyChangeQueue;
import org.jtrfp.trcl.pool.EntryBasedIndexPool.Entry;
import org.jtrfp.trcl.pool.EntryBasedIndexPool.Entry.DeadEntry;
import org.jtrfp.trcl.pool.IndexPool.GrowthBehavior;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EntryBasedIndexPoolTest {
    private EntryBasedIndexPool<Integer> subject;
    
    @Before
    public void setUp() throws Exception {
	subject = new EntryBasedIndexPool<Integer>();
    }

    @After
    public void tearDown() throws Exception {
	subject = null;
    }

    @Test
    public void testPopEntry() {
	Entry<Integer> entry = subject.popEntry(0);
	assertNotNull(entry);
	assertEquals(0,(int)entry.getContained());
    }

    @Test
    public void testDefragment() {
	Entry<Integer> e0 = subject.popEntry(0);
	Entry<Integer> e1 = subject.popEntry(1);
	Entry<Integer> e2 = subject.popEntry(2);
	Entry<Integer> e3 = subject.popEntry(3);
	e1.free();
	e2.free();
	assertEquals(4,subject.getListActionDispatcher().size());
	assertEquals(0,(int)e0.getContained());
	assertEquals(3,(int)e3.getContained());
	subject.defragment();
	assertEquals(2,subject.getListActionDispatcher().size());
	assertEquals(0,(int)e0.getContained());
	assertEquals(3,(int)e3.getContained());
	assertEquals(0,e0.getPoolIndex());
	assertEquals(1,e3.getPoolIndex());
    }

    @Test
    public void testSetGrowthBehavior() {
	subject.setGrowthBehavior(new GrowthBehavior(){
	    @Override
	    public int grow(int previousMaxCapacity) {
		return previousMaxCapacity+1;
	    }

	    @Override
	    public int shrink(int minDesiredCapacity) {
		return minDesiredCapacity+1;
	    }});
	
    }

    @Test
    public void testSetHardLimit() {
	subject.setHardLimit(3);
	subject.popEntry(0);
	subject.popEntry(1);
	subject.popEntry(2);
	assertEquals(null,subject.popEntry(3));
    }

    @Test
    public void testGetListActionDispatcher() {
	final ListActionDispatcher<Entry<Integer>> lad = subject.getListActionDispatcher();
	assertNotNull(lad);
	Entry<Integer> e0 = subject.popEntry(0);
	subject.popEntry(1);
	subject.popEntry(2);
	final ArrayList<Entry<Integer>> target = new ArrayList<Entry<Integer>>();
	lad.addTarget(target, true);
	assertEquals(3,target.size());
	assertTrue(target.contains(e0));
	assertEquals(e0,target.get(0));
	e0.free();
	assertTrue(target.get(0) instanceof DeadEntry);
	subject.defragment();
	assertFalse(target.contains(e0));
	assertEquals(2,lad.size());
	assertEquals(2,target.size());
    }
    
    @Test
    public void testGetNumUnusedIndices(){
	subject.popEntry(5);
	Entry<Integer> e1 = subject.popEntry(6);
	assertEquals(0,subject.getNumUnusedIndices());
	e1.free();
	assertEquals(1,subject.getNumUnusedIndices());
    }
    
    @Test
    public void testGetNumUsedIndices(){
	subject.popEntry(5);
	Entry<Integer> e1 = subject.popEntry(6);
	assertEquals(0,subject.getNumUnusedIndices());
	e1.free();
	assertEquals(1,subject.getNumUnusedIndices());
    }
    
    @Test
    public void testGetNumUnusedIndicesPropertyChangeSupport(){
	PropertyChangeQueue queue = new PropertyChangeQueue();
	subject.addPropertyChangeListener(EntryBasedIndexPool.NUM_UNUSED_INDICES,queue);
	subject.popEntry(5);
	Entry<Integer> e1 = subject.popEntry(6);
	assertEquals(0,queue.size());
	e1.free();
	assertEquals(1,queue.size());
	assertEquals(1,queue.pop().getNewValue());
	subject.defragment();
	assertEquals(3,queue.size());
	assertEquals(2,queue.pop().getNewValue());
	assertEquals(1,queue.pop().getNewValue());
	assertEquals(0,queue.pop().getNewValue());
    }

}//end EntryBasedIndexPoolTest
