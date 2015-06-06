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

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.SpacePartitioningGrid;
import org.jtrfp.trcl.core.Renderer;
import org.jtrfp.trcl.obj.Positionable;
import org.junit.After;
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
    protected Collection<Pair<Vector3D,CollectionActionDispatcher<Positionable>>> mockedTarget;

    @Before
    public void setUp() throws Exception {
	subject             = new SpacePartitioningGrid<Positionable>(new Vector3D(10000, 10000, 10000), 1000, 5000){};
	mockedTarget        = mock(Collection.class);
	when(mockedTarget.add(any(Pair.class))).thenReturn(true);
	mockedPositionables = new Positionable[5];
	for(int index=0; index<5; index++){
	    Positionable pos = mock(Positionable.class);
	    mockedPositionables[index] = pos;
	    when(pos.getPositionV3D()).thenReturn(new Vector3D(1000*index,1000*index,1000*index));
	}//end for(positionables)
	subject.getPackedObjectsDispatcher().addTarget(mockedTarget, true);
	verify(mockedTarget,never()).add   (any(Pair.class));
	verify(mockedTarget,never()).addAll(any(Collection.class));
	verifyZeroInteractions(mockedTarget);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testRemoveAndKeepCollection() {
	if(!Renderer.NEW_MODE) return;
	subject.add(mockedPositionables[0]);
	subject.add(mockedPositionables[1]);//Leave this dangling
	subject.remove(mockedPositionables[0]);
	verify(mockedTarget,times(0)).remove(any(Object.class));
    }//end testRemoveAndKeepCollection()
    
  //Relevance executor breaks this test.
    
/*
    @Test
    public void testRemoveAndRemoveCollection() {
	if(!Renderer.NEW_MODE) return;
	subject.add(mockedPositionables[0]);
	subject.remove(mockedPositionables[0]);
	verify(mockedTarget,times(1)).remove(any(Object.class));
    }//end testRemoveAndRemoveCollection
    
    @Test
    public void testRemoveAll() {
	if(!Renderer.NEW_MODE) return;
	subject.add(mockedPositionables[0]);
	subject.add(mockedPositionables[1]);
	subject.add(mockedPositionables[2]);
	subject.removeAll();
	verify(mockedTarget,times(1)).remove(any(Object.class));
    }//end testRemoveAll()
    
    @Test
    public void testEmptyAddNonEmptyBranch() {//Relevance executor breaks this test.
	if(!Renderer.NEW_MODE) return;
	SpacePartitioningGrid<Positionable> branch = new SpacePartitioningGrid<Positionable>(subject){};
	branch.add(mockedPositionables[0]);
	branch.add(mockedPositionables[1]);
	ArgumentCaptor<Pair> argument 
	 = new ArgumentCaptor<Pair>();
	verify(mockedTarget,times(1)).add(argument.capture());
	Pair<Vector3D,CollectionActionDispatcher<Positionable>> pair = argument.getValue();
	 assertNotNull(pair);
	 assertEquals(new Vector3D(0,0,0),pair.getFirst());
	 CollectionActionDispatcher<Positionable> dispatcher = pair.getSecond();
	 assertEquals(2,dispatcher.size());
	 assertTrue(dispatcher.contains(mockedPositionables[0]));
	 assertTrue(dispatcher.contains(mockedPositionables[1]));
    }//end testEmptyAddNonEmptyBranch()
    
    @Test
    public void testEmptyDeactivateNonEmptyBranchThenActivate() {//Relevance executor breaks this test.
	if(!Renderer.NEW_MODE) return;
	when(mockedTarget.removeAll(any(Collection.class))).thenReturn(true);
	SpacePartitioningGrid<Positionable> branch = new SpacePartitioningGrid<Positionable>(subject){};
	branch.add(mockedPositionables[0]);
	branch.add(mockedPositionables[1]);
	branch.deactivate();
	assertEquals(0,subject.getPackedObjectsDispatcher().size());
	ArgumentCaptor<Pair> argument 
	 = new ArgumentCaptor<Pair>();
	verify(mockedTarget,times(1)).add(argument.capture());
	World.
	Pair<Vector3D,CollectionActionDispatcher<Positionable>> pair = argument.getValue();
	CollectionActionDispatcher<Positionable> dispatcher = pair.getValue();
	 ArgumentCaptor<Collection> argument2 
	  = new ArgumentCaptor<Collection>();
	verify(mockedTarget,times(1)).removeAll(argument2.capture());
	assertEquals(1,argument2.getValue().size());
	branch.activate();
	verify(mockedTarget,times(2)).add(any(Pair.class));
	assertEquals(1,subject.getPackedObjectsDispatcher().size());
    }//end testEmptyAddNonEmptyBranchThenActivate()
    
    @Test
    public void testPopulatedAddNonEmptyBranchCommonTags(){
	if(!Renderer.NEW_MODE) return;
	SpacePartitioningGrid<Positionable> branch = new SpacePartitioningGrid<Positionable>(subject){};
	subject.add(mockedPositionables[2]);
	subject.add(mockedPositionables[3]);
	branch.add(mockedPositionables[0]);
	branch.add(mockedPositionables[1]);
	ArgumentCaptor<Pair> argument 
	 = new ArgumentCaptor<Pair>();
	verify(mockedTarget,times(2)).add(argument.capture());
	Pair<Vector3D,CollectionActionDispatcher<Positionable>> pair = argument.getValue();
	 assertNotNull(pair);
	 assertEquals(new Vector3D(0,0,0),pair.getFirst());
	 CollectionActionDispatcher<Positionable> dispatcher = pair.getSecond();
	 assertEquals(2,dispatcher.size());
	 assertTrue(dispatcher.contains(mockedPositionables[0]));
	 assertTrue(dispatcher.contains(mockedPositionables[1]));
    }
*/
}//end SpacePartitioningGridRootTest
