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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;

import junit.framework.TestCase;

import org.jtrfp.trcl.dbg.PropertyChangeQueue;
import org.jtrfp.trcl.pool.IndexPool.GrowthBehavior;
import org.jtrfp.trcl.pool.IndexPool.OutOfIndicesException;
import org.junit.Test;

public class IndexPoolTest extends TestCase {

    public void testPop() {
	final IndexPool subject = new IndexPool();
	for(int i=0; i<500; i++)
	    assertEquals(i, subject.pop());
    }

    public void testPopOrException() {
	final IndexPool subject = new IndexPool();
	subject.setHardLimit(500);
	for(int i=0; i<500; i++)
	    assertEquals(i, subject.pop());
	try {
	    subject.popOrException();
	    fail("Failed to throw expected OutOfIndicesException.");
	} catch (OutOfIndicesException e) {}//Good.
    }

    public void testPopCollectionOfIntegerInt() {
	final IndexPool subject = new IndexPool();
	final ArrayList<Integer>dest = new ArrayList<Integer>();
	subject.pop(dest, 512);
	for(int i=0; i<512; i++)
	    assertEquals(Integer.valueOf(i), dest.get(i));
	assertEquals(512,dest.size());
    }

    public void testFree() {
	final IndexPool subject = new IndexPool();
	for(int i=0; i<500; i++)
	    assertEquals(i, subject.pop());
	for(int i=0; i<250; i++)
	    subject.free(i);
	for(int i=0; i<250; i++)
	    assertEquals(i, subject.pop());
	assertEquals(500,subject.pop());
    }

    public void testSetGrowthBehavior() {
	final IndexPool subject = new IndexPool();
	assertEquals(1, subject.getMaxCapacity());
	subject.setGrowthBehavior(new GrowthBehavior(){
	    @Override
	    public int grow(int previousMaxCapacity) {
		return previousMaxCapacity+1;
	    }
	    public int shrink(int minDesiredCapacity){
		return minDesiredCapacity;
	    }});
	subject.pop();
	subject.pop();
	assertEquals(2, subject.getMaxCapacity());
	subject.pop();
	assertEquals(3, subject.getMaxCapacity());
    }

    public void testPopConsecutive() {
	final IndexPool subject = new IndexPool();
	assertEquals(0, subject.popConsecutive(4));
	assertEquals(4, subject.popConsecutive(4));
    }

    public void testGetMaxCapacity() {
	final IndexPool subject =  new IndexPool();
	for(int i=0; i<60; i++)
	    subject.pop();
	//With default incremental growth.
	assertEquals(60, subject.getMaxCapacity());
    }

    public void testGetHardLimit() {
	final IndexPool subject=  new IndexPool();
	subject.setHardLimit(5);
	assertEquals(5,subject.getHardLimit());
    }
    
    public void testFreeCollectionOfIntegersInt(){
	final IndexPool subject = new IndexPool();
	final ArrayList<Integer>dest = new ArrayList<Integer>();
	subject.pop(dest, 512);
	subject.free(dest);
	assertEquals(0, subject.pop());
    }
    
    public void testGetUsedIndices(){
	final IndexPool subject = new IndexPool();
	Queue<Integer> indices = subject.getUsedIndices();
	subject.pop();
	final ArrayList<Integer> dest = new ArrayList<Integer>();
	subject.pop(dest, 2);
	assertNotNull(indices);
	assertEquals(3,indices.size());
	Iterator<Integer> it = indices.iterator();
	assertEquals(0, (int)it.next());
	assertEquals(1, (int)it.next());
	assertEquals(2, (int)it.next());
	
	assertEquals(1, (int)dest.get(0));
	assertEquals(2, (int)dest.get(1));
    }//end testGetUsedIndices()
    
    public void testCompact(){
	final IndexPool subject = new IndexPool();
	subject.pop();
	subject.pop();
	subject.pop();
	subject.free(0);
	subject.free(1);
	// f f 2
	assertEquals(2, subject.getFreeIndices().size());
	assertEquals(1, subject.getUsedIndices().size());
	subject.compact();
	// f f 2
	assertEquals(2, subject.getFreeIndices().size());
	assertEquals(1, subject.getUsedIndices().size());
	assertEquals(0,subject.pop());
	subject.free(2);
	// 0 f f
	assertEquals(2, subject.getFreeIndices().size());
	assertEquals(1, subject.getUsedIndices().size());
	subject.compact();
	// 0
	assertEquals(0, subject.getFreeIndices().size());
	assertEquals(1, subject.getUsedIndices().size());
	// 0 1
	assertEquals(1,subject.pop());
	assertEquals(0, subject.getFreeIndices().size());
	assertEquals(2, subject.getUsedIndices().size());
	// 0 1 2
	assertEquals(2,subject.pop());
	assertEquals(0, subject.getFreeIndices().size());
	assertEquals(3, subject.getUsedIndices().size());
    }
    
    public void testGetNumUnusedIndices(){
	final IndexPool subject = new IndexPool();
	assertEquals(0,subject.getNumUnusedIndices());
	subject.pop();
	assertEquals(0,subject.getNumUnusedIndices());
	subject.free(0);
	assertEquals(1,subject.getNumUnusedIndices());
	subject.pop();
	assertEquals(0,subject.getNumUnusedIndices());
    }
    
    public void testGetNumUsedIndices(){
	final IndexPool subject = new IndexPool();
	assertEquals(0,subject.getNumUsedIndices());
	subject.pop();
	assertEquals(1,subject.getNumUsedIndices());
	subject.free(0);
	assertEquals(0,subject.getNumUsedIndices());
    }
    
    @Test
    public void testNumUsedIndicesPropertyChange(){
	final IndexPool         subject = new IndexPool();
	final PropertyChangeQueue queue = new PropertyChangeQueue();
	subject.addPropertyChangeListener(IndexPool.NUM_USED_INDICES,queue);
	assertEquals(0,queue.size());
	subject.pop();
	assertEquals(1,queue.size());
	assertEquals(1,queue.pop().getNewValue());
	subject.pop();
	assertEquals(1,queue.size());
	assertEquals(2,queue.pop().getNewValue());
    }
    
    @Test
    public void testNumUnusedIndicesPropertyChange(){
	final IndexPool         subject = new IndexPool();
	final PropertyChangeQueue queue = new PropertyChangeQueue();
	subject.addPropertyChangeListener(IndexPool.NUM_UNUSED_INDICES,queue);
	subject.pop();
	subject.pop();
	subject.pop();
	assertEquals(0,queue.size());
	subject.free(0);
	assertEquals(1,queue.size());
	assertEquals(1,queue.pop().getNewValue());
	subject.free(1);
	assertEquals(1,queue.size());
	assertEquals(2,queue.pop().getNewValue());
    }
    
    private void populate(IndexPool subject, int qty, List<Integer> dest){
	//System.out.println("Populate by "+qty);
	for(int i=0; i<qty; i++){
	    final int element = subject.pop();
	    dest.add(element);
	    assertTrue(subject.getUsedIndices().contains(element));
	    assertEquals(i,element);
	    }
    }//end populate()
    
    private void testIndices(List<Integer> dest){
	for(int i=0; i<dest.size(); i++)
	    assertEquals(i,dest.get(i).intValue());
    }
    
    private void depopulate(IndexPool subject, List<Integer> list){
	for(int i:list)
	    subject.free(i);
	assertEquals(0,subject.getNumUsedIndices());
    }
    
    @Test
    public void testChangingState(){
	final IndexPool subject = new IndexPool();
	final int NUM_ITERATIONS = 50;
	final ArrayList<Integer> indices = new ArrayList<Integer>();
	for(int iteration=0; iteration<NUM_ITERATIONS; iteration++){
	    //System.out.println("iteration "+iteration);
	    final int size = (int)(Math.random()*250);
	    indices.clear();
	    populate(subject,size,indices);
	    testIndices(indices);
	    depopulate(subject,indices);
	    assertEquals(size,subject.getNumUnusedIndices());
	    subject.compact();
	    assertEquals(0,subject.getNumUnusedIndices());
	}
    }//end testChangingState()

}//end IndexPoolTest
