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

import junit.framework.Assert;
import junit.framework.TestCase;

import org.jtrfp.trcl.pool.IndexPool.GrowthBehavior;
import org.jtrfp.trcl.pool.IndexPool.OutOfIndicesException;

public class IndexPoolTest extends TestCase {

    public void testPop() {
	final IndexPool subject = new IndexPool();
	for(int i=0; i<500; i++)
	    Assert.assertEquals(i, subject.pop());
    }

    public void testPopOrException() {
	final IndexPool subject = new IndexPool();
	subject.setHardLimit(500);
	for(int i=0; i<500; i++)
	    Assert.assertEquals(i, subject.pop());
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
	    Assert.assertEquals(Integer.valueOf(i), dest.get(i));
    }

    public void testFree() {
	final IndexPool subject = new IndexPool();
	for(int i=0; i<500; i++)
	    Assert.assertEquals(i, subject.pop());
	for(int i=0; i<250; i++)
	    subject.free(i);
	for(int i=0; i<250; i++)
	    Assert.assertEquals(i, subject.pop());
	Assert.assertEquals(500,subject.pop());
    }

    public void testSetGrowthBehavior() {
	final IndexPool subject = new IndexPool();
	Assert.assertEquals(1, subject.getMaxCapacity());
	subject.setGrowthBehavior(new GrowthBehavior(){
	    @Override
	    public int grow(int previousMaxCapacity) {
		return previousMaxCapacity+1;
	    }});
	subject.pop();
	subject.pop();
	Assert.assertEquals(2, subject.getMaxCapacity());
	subject.pop();
	Assert.assertEquals(3, subject.getMaxCapacity());
    }

    public void testPopConsecutive() {
	final IndexPool subject = new IndexPool();
	Assert.assertEquals(0, subject.popConsecutive(4));
	Assert.assertEquals(4, subject.popConsecutive(4));
    }

    public void testGetMaxCapacity() {
	final IndexPool subject =  new IndexPool();
	for(int i=0; i<60; i++)
	    subject.pop();
	//With default doubling growth, power-of-two behavior expected.
	Assert.assertEquals(64, subject.getMaxCapacity());
    }

    public void testGetHardLimit() {
	final IndexPool subject=  new IndexPool();
	subject.setHardLimit(5);
	Assert.assertEquals(5,subject.getHardLimit());
    }
    
    public void testFreeCollectionOfIntegersInt(){
	final IndexPool subject = new IndexPool();
	final ArrayList<Integer>dest = new ArrayList<Integer>();
	subject.pop(dest, 512);
	subject.free(dest);
	Assert.assertEquals(0, subject.pop());
    }

}
