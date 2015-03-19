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
import java.util.Queue;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.jtrfp.trcl.pool.IndexPool.GrowthBehavior;
import org.jtrfp.trcl.pool.IndexPool.OutOfIndicesException;

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
	//With default doubling growth, power-of-two behavior expected.
	assertEquals(64, subject.getMaxCapacity());
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
	subject.pop();
	subject.free(2);
	// 0 f f
	assertEquals(2, subject.getFreeIndices().size());
	assertEquals(1, subject.getUsedIndices().size());
	subject.compact();
	// 0
	assertEquals(0, subject.getFreeIndices().size());
	assertEquals(1, subject.getUsedIndices().size());
    }

}//end IndexPoolTest
