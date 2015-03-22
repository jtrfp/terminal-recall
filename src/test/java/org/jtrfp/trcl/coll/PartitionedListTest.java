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
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.jtrfp.trcl.coll.PartitionedList.ListPartition;
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
	subject.removeSubList((ListPartition)pl0);
	assertEquals(3,(int)list.size());
	// 3 4 5
	assertEquals(3,(int)list.get(0));
	assertEquals(4,(int)list.get(1));
	assertEquals(5,(int)list.get(2));
	subject.removeSubList((ListPartition)pl1);
	assertEquals(0,(int)list.size());
	
	try{ subject.removeSubList((ListPartition)pl1);}
	catch(IllegalStateException e){return;}//Good
	fail("Failed to throw expected IllegalStateException");
    }

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
	assertEquals(0,((ListPartition)pl0).getStartIndex());
	assertEquals(3,((ListPartition)pl1).getStartIndex());
	pl0.remove(1);
	// 0 2  3 4 5
	assertEquals(0,((ListPartition)pl0).getStartIndex());
	assertEquals(2,((ListPartition)pl1).getStartIndex());
	pl1.remove(1);
	// 0 2  3 5
	assertEquals(0,((ListPartition)pl0).getStartIndex());
	assertEquals(2,((ListPartition)pl1).getStartIndex());
    }//end testGetStartIndex()

}//end PartitionedListTest
