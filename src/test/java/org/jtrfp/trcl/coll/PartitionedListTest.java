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

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.jtrfp.trcl.coll.PartitionedList.Partition;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PartitionedListTest {
    private PartitionedList<Integer> subject;
    private ArrayList<Integer> list;

    @Before
    public void setUp() throws Exception {
	list    = new ArrayList<Integer>();
	subject = new PartitionedList<Integer>(list);
    }

    @After
    public void tearDown() throws Exception {
	subject = null;
	list    = null;
    }

    @Test
    public void testNewSubList() {
	List<Integer> pl0,pl1;
	assertNotNull(pl0 = subject.newSubList());
	pl0.add(0);
	pl0.add(1);
	pl0.add(2);
	assertNotNull(pl1 = subject.newSubList());
	pl1.add(3);
	pl1.add(4);
	pl1.add(5);
	assertEquals(6, list.size());
	// 0 1 2 3 4 5
	assertEquals(0,(int)list.get(0));
	assertEquals(2,(int)list.get(2));
	assertEquals(3,(int)list.get(3));
	assertEquals(5,(int)list.get(5));
	pl0.remove(1);
	// 0 2 3 4 5
	assertEquals(0,(int)list.get(0));
	assertEquals(2,(int)list.get(1));
	assertEquals(4,(int)list.get(3));
	pl1.remove(1);
	// 0 2 3 5
	assertEquals(0,(int)list.get(0));
	assertEquals(2,(int)list.get(1));
	assertEquals(3,(int)list.get(2));
	assertEquals(5,(int)list.get(3));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testRemoveSubList() {
	List<Integer> pl0,pl1;
	assertNotNull(pl0 = subject.newSubList());
	pl0.add(0);
	pl0.add(1);
	pl0.add(2);
	assertNotNull(pl1 = subject.newSubList());
	pl1.add(3);
	pl1.add(4);
	pl1.add(5);
	// 0 1 2 3 4 5
	assertEquals(0,(int)list.get(0));
	assertEquals(2,(int)list.get(2));
	assertEquals(3,(int)list.get(3));
	assertEquals(5,(int)list.get(5));
	subject.removeSubList((Partition)pl0);
	assertEquals(3,(int)list.size());
	// 3 4 5
	assertEquals(3,(int)list.get(0));
	assertEquals(4,(int)list.get(1));
	assertEquals(5,(int)list.get(2));
	subject.removeSubList((Partition)pl1);
	assertEquals(0,(int)list.size());
	
	try{ subject.removeSubList((Partition)pl1);}
	catch(IllegalStateException e){return;}//Good
	fail("Failed to throw expected IllegalStateException");
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testGetStartIndex() {
	List<Integer> pl0,pl1;
	assertNotNull(pl0 = subject.newSubList());
	pl0.add(0);
	pl0.add(1);
	pl0.add(2);
	assertNotNull(pl1 = subject.newSubList());
	pl1.add(3);
	pl1.add(4);
	pl1.add(5);
	// 0 1 2 3 4 5
	assertEquals(0,((Partition)pl0).getStartIndex());
	assertEquals(3,((Partition)pl1).getStartIndex());
	pl0.remove(1);
	// 0 2  3 4 5
	assertEquals(0,((Partition)pl0).getStartIndex());
	assertEquals(2,((Partition)pl1).getStartIndex());
	pl1.remove(1);
	// 0 2  3 5
	assertEquals(0,((Partition)pl0).getStartIndex());
	assertEquals(2,((Partition)pl1).getStartIndex());
    }//end testGetStartIndex()
    
    @Test
    public void testPartition() {
	final PartitionedList<Integer>.Partition p = subject.newSubList();
	p.add(0); p.add(1); p.add(2);
	p.remove(Integer.valueOf(0));
	p.remove(Integer.valueOf(1));
	p.remove(Integer.valueOf(2));
	assertEquals(0,list.size());
    }
    
    private void populate(int size, PartitionedList<Object>.Partition p, ArrayList<Object> testDest){
	//System.out.println("Populate "+size);
	for(int i=0; i<size; i++){
	    final Object obj = new Object();
	    p.add(obj);
	    testDest.add(obj);
	}
    }//end populate(...)
    
    private void depopulate(int size, PartitionedList<Object>.Partition p, ArrayList<Object> testDest){
	//System.out.println("Depopulate "+size);
	for(int i=0; i<size; i++){
	    final Object obj = testDest.get(0);
	    p.remove(obj);
	    testDest.remove(obj);
	}
    }//end depopulate(...)
    
    private void reset(PartitionedList<Object>.Partition part, ArrayList<Object> test){
	final int numIndicesToChange = (int)(Math.random()*test.size());
	for(int i = 0; i < numIndicesToChange; i++){
	    final int index  = (int)(Math.random()*numIndicesToChange);
	    final Object obj = new Object();
	    part.set(index, obj);
	    test.set(index, obj);
	}
    }//end reset(...)
    
    @SuppressWarnings("unchecked")
    private Object partitionedGet(int i, PartitionedList<Object>.Partition ... parts){
	int partIndex=0;
	PartitionedList<Object>.Partition partition = parts[0];
	while(partition.getStartIndex()+partition.getSize()<=i)
	    partition = parts[++partIndex];
	return partition.get(i-partition.getStartIndex());
    }
    
    @SafeVarargs
    @SuppressWarnings({ "rawtypes"})
    private void testValues(ArrayList<Object> intrinsic, PartitionedList<Object>.Partition ... parts){
	int size=0;
	for(Partition part:parts)
	    size+=part.getSize();
	assertEquals(size,intrinsic.size());
	for(int i=0; i<intrinsic.size(); i++)
	    assertEquals(partitionedGet(i,parts),intrinsic.get(i));
	for(int i=0; i<intrinsic.size(); i++)
	    assertEquals(partitionedGet(i,parts),intrinsic.get(i));
    }
    
    @Test
    public void testChangingState(){
	final int NUM_ITERATIONS = 15;
	final ArrayList<Object> subjectIntrinsic = new ArrayList<Object>();
	final PartitionedList<Object> subject = new PartitionedList<Object>(subjectIntrinsic);
	PartitionedList<Object>.Partition p0 = subject.newSubList();
	PartitionedList<Object>.Partition p1 = subject.newSubList();
	ArrayList<Object> test0 = new ArrayList<Object>();
	ArrayList<Object> test1 = new ArrayList<Object>();
	for(int iteration = 0; iteration < NUM_ITERATIONS; iteration++){
	    //System.out.println("iteration "+iteration);
	    final int popSize = (int)(Math.random()*10);
	    populate(popSize,p0,test0);
	    populate(popSize,p1,test1);
	    reset(p0,test0);
	    reset(p1,test1);
	    final int depopSize0 = (int)(Math.random()*test0.size());
	    final int depopSize1 = (int)(Math.random()*test1.size());
	    depopulate(depopSize0,p0,test0);
	    depopulate(depopSize1,p1,test1);
	    testValues(subjectIntrinsic,p0,p1);
	}//end for(NUM_ITERATIONS)
    }//end testChangingState()
    
    @Test
    public void testEquals(){
	assertTrue(subject.equals(subject));
    }
}//end PartitionedListTest
