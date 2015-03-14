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
import java.util.Collection;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.jtrfp.trcl.pool.PartitionedIndexPool.Partition;

import com.ochafik.util.listenable.CollectionEvent;
import com.ochafik.util.listenable.CollectionListener;
import com.ochafik.util.listenable.ListenableCollection;

public abstract class PartitionedIndexPoolTest extends TestCase {
    
    protected PartitionedIndexPool<TestObject> subject;

    protected void setUp() throws Exception {
	super.setUp();
    }

    protected void tearDown() throws Exception {
	super.tearDown();
    }
    
    protected class TestObject{
    }//end TestObject
    
    protected abstract PartitionedIndexPool<TestObject> newSubject();

    public void testNewPartition() {
	Partition<TestObject> tp = subject.newPartition();
	Assert.assertNotNull(tp);
    }

    public void testRemovePartition() {
	Partition<TestObject> tp = subject.newPartition();
	Assert.assertTrue(subject.getPartitions().contains(tp));
	Assert.assertTrue(tp.isValid());
	Assert.assertEquals(subject,subject.removePartition(tp));
	Assert.assertFalse(subject.getPartitions().contains(tp));
	Assert.assertFalse(tp.isValid());
    }

    public void testRemoveAllPartitions() {
	for(int i=0; i<10; i++)
	    subject.newPartition();
	Assert.assertEquals(10, subject.getPartitions().size());
	Collection<Partition<TestObject>> parts = new ArrayList<Partition<TestObject>>();
	parts.addAll(subject.getPartitions());
	Assert.assertEquals(subject,subject.removeAllPartitions());
	Assert.assertEquals(0, subject.getPartitions().size());
	for(Partition<TestObject> p:parts)
	    Assert.assertFalse(p.isValid());
    }

    public void testGetPartitions() {
	for(int i=0; i<10; i++)
	    subject.newPartition();
	Assert.assertEquals(10,subject.getPartitions().size());
	Partition<TestObject> np = subject.newPartition();
	ListenableCollection<Partition<TestObject>> partitions;
	partitions = subject.getPartitions();
	Assert.assertNotNull(partitions);
	Assert.assertTrue((partitions).contains(np));
	final int [] added   = new int[]{0};
	final int [] removed = new int[]{0};
	partitions.addCollectionListener(new CollectionListener<Partition<TestObject>>(){
	    @Override
	    public void collectionChanged(
		    CollectionEvent<Partition<TestObject>> evt) {
		switch(evt.getType()){
		case ADDED:
		    added[0]++;
		    break;
		case REMOVED:
		    removed[0]++;
		    break;
		case UPDATED:
		    fail("Items should not update.");
		    break;
		default:
		    fail("Unsupported enum: "+evt.getType());
		    break;
		}
	    }});
	Assert.assertEquals(0, added[0]);
	Assert.assertEquals(0, removed[0]);
	Partition<TestObject> np1 = subject.newPartition();
	Assert.assertEquals(1, added[0]);
	Assert.assertEquals(0, removed[0]);
	subject.removePartition(np1);
	Assert.assertEquals(1, added[0]);
	Assert.assertEquals(1, removed[0]);
	subject.removeAllPartitions();
	Assert.assertEquals(1, added[0]);
	Assert.assertEquals(12, removed[0]);
	Assert.assertTrue(partitions.isEmpty());
    }//end testGetPartitions()

    public void testGetFlatEntries() {
	fail("Not yet implemented"); // TODO
    }

    public void testGetTotalUnusedIndices() {
	fail("Not yet implemented"); // TODO
    }

    public void testDefragment() {
	fail("Not yet implemented"); // TODO
    }

    public void testSetTotalUnusedLimitBehavior() {
	fail("Not yet implemented"); // TODO
    }

    public void testGetTotalUnusedLimitBehavior() {
	fail("Not yet implemented"); // TODO
    }

    public void testSetFlushBehavior() {
	fail("Not yet implemented"); // TODO
    }

    public void testGetFlushBehavior() {
	fail("Not yet implemented"); // TODO
    }

    public void testFlush() {
	fail("Not yet implemented"); // TODO
    }

    public void testAddPropertyChangeListenerPropertyChangeListener() {
	fail("Not yet implemented"); // TODO
    }

    public void testAddPropertyChangeListenerStringPropertyChangeListener() {
	fail("Not yet implemented"); // TODO
    }

    public void testRemovePropertyChangeListenerPropertyChangeListener() {
	fail("Not yet implemented"); // TODO
    }

    public void testRemovePropertyChangeListenerStringPropertyChangeListener() {
	fail("Not yet implemented"); // TODO
    }

    public void testGetPropertyChangeListeners() {
	fail("Not yet implemented"); // TODO
    }

    public void testGetPropertyChangeListenersString() {
	fail("Not yet implemented"); // TODO
    }

    public void testHasListeners() {
	fail("Not yet implemented"); // TODO
    }

}
