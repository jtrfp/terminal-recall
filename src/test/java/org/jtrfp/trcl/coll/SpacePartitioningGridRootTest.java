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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.SpacePartitioningGrid;
import org.jtrfp.trcl.World;
import org.jtrfp.trcl.core.CubeCoordinate;
import org.jtrfp.trcl.obj.Positionable;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.runners.MockitoJUnitRunner;

import com.ochafik.util.listenable.Pair;

@RunWith(MockitoJUnitRunner.class)
public class SpacePartitioningGridRootTest {
    protected SpacePartitioningGrid<Positionable> subject;
    protected Positionable [] mockedPositionables;
    protected Collection<Pair<CubeCoordinate,CollectionActionDispatcher<Positionable>>> mockedTarget;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception {
	subject             = new SpacePartitioningGrid<Positionable>(new Vector3D(10000, 10000, 10000), 1000, 5000){};
	mockedTarget        = mock(Collection.class);
	when(mockedTarget.add(any(Pair.class))).thenReturn(true);
	mockedPositionables = new Positionable[5];
	for(int index=0; index<5; index++){
	    Positionable pos = mock(Positionable.class);
	    mockedPositionables[index] = pos;
	    when(pos.getPosition()).thenReturn(new double[]{1000*index,1000*index,1000*index});
	}//end for(positionables)
	subject.getPackedObjectsDispatcher().addTarget(mockedTarget, true);
	verify(mockedTarget,never()).add   (any(Pair.class));
	verify(mockedTarget,never()).addAll(any(Collection.class));
	verifyZeroInteractions(mockedTarget);
	//subject.activate();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testRemoveAndKeepCollection() {
	subject.add(mockedPositionables[0]);
	subject.add(mockedPositionables[1]);//Leave this dangling
	subject.remove(mockedPositionables[0]);
	verify(mockedTarget,times(0)).remove(any(Object.class));
    }//end testRemoveAndKeepCollection()
    
    private void singleThreadExecutorBarrier(ExecutorService ex){
	try{ex.submit(new Runnable(){public void run(){}}).get();}
	catch(ExecutionException e){Assert.fail(e.getLocalizedMessage());}
	catch(InterruptedException e){Assert.fail(e.getLocalizedMessage());}
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testRemoveAndRemoveCollection() {
	subject.add(mockedPositionables[0]);
	subject.remove(mockedPositionables[0]);
	singleThreadExecutorBarrier(World.relevanceExecutor);
	verify(mockedTarget,times(1)).add(any(Pair.class));
    }//end testRemoveAndRemoveCollection
    
    /// Below is a random benchmark written to test (prove?) performance advantages of using exceptions with iterators
    /*
    private static final int NUM_ITERATIONS = 100000;
    
    @Test
    public void testLoopEscapePerformanceException(){
	Collection<Integer> ints = new ArrayList<Integer>();
	for(int i=0; i<NUM_ITERATIONS; i++)
	    ints.add((int)(Math.random()*1000.));
	
	long startTime = System.nanoTime();
	Iterator<Integer> iterator = ints.iterator();
	try{while(true)
	    if(iterator.next()>5000)throw new RuntimeException("This should never be thrown.");}
	catch(NoSuchElementException e){}//Naughty escape.
	long elapsed = System.nanoTime()-startTime;
	
	ints = new ArrayList<Integer>();
	for(int i=0; i<NUM_ITERATIONS; i++)
	    ints.add((int)(Math.random()*1000.));
	
	startTime = System.nanoTime();
	iterator = ints.iterator();
	try{while(true)
	    if(iterator.next()>5000)throw new RuntimeException("This should never be thrown.");}
	catch(NoSuchElementException e){}//Naughty escape.
	elapsed = System.nanoTime()-startTime;
	System.out.println("Exception Elapsed: "+elapsed);
    }
    
    @Test
    public void testLoopEscapePerformanceTraditional(){
	// WARMUP
	Collection<Integer> ints = new ArrayList<Integer>();
	for(int i=0; i<NUM_ITERATIONS; i++)
	    ints.add((int)(Math.random()*1000.));
	
	 long startTime = System.nanoTime();
	for(Integer i:ints)
	    if(i>5000)throw new RuntimeException("This should never be thrown.");
	 long elapsed = System.nanoTime()-startTime;
	// REAL
	ints = new ArrayList<Integer>();
	for(int i=0; i<NUM_ITERATIONS; i++)
	    ints.add((int)(Math.random()*1000.));
	
	startTime = System.nanoTime();
	for(Integer i:ints)
	    if(i>5000)throw new RuntimeException("This should never be thrown.");
	elapsed = System.nanoTime()-startTime;
	System.out.println("Traditional Elapsed: "+elapsed);
    }
    */
    
    @Test
    public void testRemoveAll() {
	verify(mockedTarget,times(0)).clear();
	subject.add(mockedPositionables[0]);
	subject.add(mockedPositionables[1]);
	subject.add(mockedPositionables[2]);
	subject.removeAll();
	singleThreadExecutorBarrier(World.relevanceExecutor);
	ArgumentCaptor<Pair> argument 
	 = ArgumentCaptor.forClass(Pair.class);
	verify(mockedTarget,times(1)).remove(argument.capture());
	Pair<Vector3D,CollectionActionDispatcher<Positionable>> pair = argument.getValue();
	 assertNotNull(pair);
	 assertEquals(new CubeCoordinate(0,0,0),pair.getFirst());
	 CollectionActionDispatcher<Positionable> dispatcher = pair.getSecond();
	 assertEquals(0,dispatcher.size());
	 assertTrue(subject.getPackedObjectsDispatcher().isEmpty());
    }//end testRemoveAll()
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testEmptyAddNonEmptyBranch() {//Relevance executor breaks this test.
	SpacePartitioningGrid<Positionable> branch = new SpacePartitioningGrid<Positionable>();
	subject.blockingAddBranch(branch);
	branch.add(mockedPositionables[0]);
	branch.add(mockedPositionables[1]);
	ArgumentCaptor<Pair> argument 
	 = ArgumentCaptor.forClass(Pair.class);
	singleThreadExecutorBarrier(World.relevanceExecutor);
	verify(mockedTarget,times(1)).add(argument.capture());
	Pair<Vector3D,CollectionActionDispatcher<Positionable>> pair = argument.getValue();
	 assertNotNull(pair);
	 assertEquals(new CubeCoordinate(0,0,0),pair.getFirst());
	 CollectionActionDispatcher<Positionable> dispatcher = pair.getSecond();
	 assertEquals(2,dispatcher.size());
	 assertTrue(dispatcher.contains(mockedPositionables[0]));
	 assertTrue(dispatcher.contains(mockedPositionables[1]));
    }//end testEmptyAddNonEmptyBranch()
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testEmptyDeactivateNonEmptyBranchThenActivate() {//Relevance executor breaks this test.
	when(mockedTarget.remove(any(Collection.class))).thenReturn(true);
	SpacePartitioningGrid<Positionable> branch = new SpacePartitioningGrid<Positionable>();
	subject.blockingAddBranch(branch);
	branch.add(mockedPositionables[0]);
	singleThreadExecutorBarrier(World.relevanceExecutor);
	assertEquals(1,subject.getPackedObjectsDispatcher().size());
	subject.blockingRemoveBranch(branch);
	branch.add(mockedPositionables[1]);
	assertEquals(0,subject.getPackedObjectsDispatcher().size());
	ArgumentCaptor<Pair> argument 
	 = ArgumentCaptor.forClass(Pair.class);
	singleThreadExecutorBarrier(World.relevanceExecutor);
	verify(mockedTarget,times(1)).add(argument.capture());
	 ArgumentCaptor<Collection> argument2 
	   = ArgumentCaptor.forClass(Collection.class);
	verify(mockedTarget,times(1)).remove(argument2.capture());
	assertEquals(argument.getValue(),argument2.getValue());
	subject.blockingAddBranch(branch);
	assertEquals(1,subject.getPackedObjectsDispatcher().size());
    }//end testEmptyAddNonEmptyBranchThenActivate()
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void testPopulatedAddNonEmptyBranchCommonTags(){
	SpacePartitioningGrid<Positionable> branch = new SpacePartitioningGrid<Positionable>();
	subject.blockingAddBranch(branch);
	subject.add(mockedPositionables[2]);
	subject.add(mockedPositionables[3]);
	branch.add(mockedPositionables[0]);
	branch.add(mockedPositionables[1]);
	singleThreadExecutorBarrier(World.relevanceExecutor);
	ArgumentCaptor<Pair> argument 
	 = ArgumentCaptor.forClass(Pair.class);
	verify(mockedTarget,times(2)).add(argument.capture());
	Pair<Vector3D,CollectionActionDispatcher<Positionable>> pair = argument.getValue();
	 assertNotNull(pair);
	 assertEquals(new CubeCoordinate(0,0,0),pair.getFirst());
	 CollectionActionDispatcher<Positionable> dispatcher = pair.getSecond();
	 assertEquals(2,dispatcher.size());
	 assertTrue(dispatcher.contains(mockedPositionables[0]));
	 assertTrue(dispatcher.contains(mockedPositionables[1]));
    }

}//end SpacePartitioningGridRootTest
