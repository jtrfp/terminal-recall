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
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.jtrfp.trcl.pool.PartitionedIndexPool.Entry;
import org.jtrfp.trcl.pool.PartitionedIndexPool.FlushBehavior;
import org.jtrfp.trcl.pool.PartitionedIndexPool.Partition;
import org.jtrfp.trcl.pool.PartitionedIndexPool.UnusedIndexLimitBehavior;

import com.ochafik.util.listenable.CollectionEvent;
import com.ochafik.util.listenable.CollectionListener;
import com.ochafik.util.listenable.ListenableCollection;
import com.ochafik.util.listenable.ListenableList;

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
	final ListenableList<TestObject> flatEntries = subject.getFlatEntries();
	Assert.assertNotNull(flatEntries);
	final Partition<TestObject> [] parts = new Partition[2];
	final TestObject [] to = new TestObject [] {new TestObject(),new TestObject()};
	final Entry<TestObject> [] entries = new Entry[4];
	final int [] added   = new int[1];
	final int [] removed = new int[1];
	flatEntries.addCollectionListener(new CollectionListener<TestObject>(){
	    @Override
	    public void collectionChanged(CollectionEvent<TestObject> evt) {
		switch(evt.getType()){
		case ADDED:
		    added[0]++;
		    break;
		case REMOVED:
		    removed[0]++;
		    break;
		case UPDATED:
		    fail("Unsupported enum: "+evt.getType());
		    break;
		default:
		    break;
		}//end EventType
	    }});
	parts[0] = subject.newPartition();
	parts[1] = subject.newPartition();
	entries[0] = parts[0].newEntry(to[0]);
	entries[1] = parts[1].newEntry(to[1]);
	Assert.assertEquals(2,flatEntries.size());
	Assert.assertTrue(flatEntries.contains(entries[0]));
	Assert.assertTrue(flatEntries.contains(entries[1]));
    }

    public void testGetTotalUnusedIndices() {
	Partition<TestObject> p0 = subject.newPartition();
	Assert.assertEquals(0, subject.getTotalUnusedIndices());
	Partition<TestObject> p1 = subject.newPartition();
	Assert.assertEquals(0, subject.getTotalUnusedIndices());
	
	Entry<TestObject> [] to = new Entry[2];
	
	p0.newEntry(new TestObject());
	Assert.assertEquals(0, subject.getTotalUnusedIndices());
	p0.newEntry(new TestObject());
	Assert.assertEquals(0, subject.getTotalUnusedIndices());
	to[0] = p0.newEntry(new TestObject());
	
	p1.newEntry(new TestObject());
	Assert.assertEquals(0, subject.getTotalUnusedIndices());
	p1.newEntry(new TestObject());
	Assert.assertEquals(0, subject.getTotalUnusedIndices());
	to[1] = p1.newEntry(new TestObject());
	Assert.assertEquals(0, subject.getTotalUnusedIndices());
	
	to[0].remove();
	Assert.assertEquals(1, subject.getTotalUnusedIndices());
	to[1].remove();
	Assert.assertEquals(2, subject.getTotalUnusedIndices());
	
	to[3] = p0.newEntry(new TestObject());
	Assert.assertEquals(1, subject.getTotalUnusedIndices());
	
	subject.defragment(1);
	Assert.assertEquals(1, subject.getTotalUnusedIndices());
	subject.defragment(0);
	Assert.assertEquals(0, subject.getTotalUnusedIndices());
    }

    public void testDefragment() {
	Partition<TestObject> p0 = subject.newPartition();
	Partition<TestObject> p1 = subject.newPartition();
	
	Entry<TestObject> [] to = new Entry[2];
	
	p0.newEntry(new TestObject());
	p0.newEntry(new TestObject());
	to[0] = p0.newEntry(new TestObject());
	
	p1.newEntry(new TestObject());
	p1.newEntry(new TestObject());
	to[1] = p1.newEntry(new TestObject());
	
	to[0].remove();
	to[1].remove();
	
	int unused = 0;
	for(TestObject obj:subject.getFlatEntries())
	    if(obj==null)unused++;
	Assert.assertEquals(2,unused);
	
	subject.defragment(1);
	unused = 0;
	for(TestObject obj:subject.getFlatEntries())
	    if(obj==null)unused++;
	Assert.assertEquals(1,unused);
	
	Assert.assertEquals(subject, subject.defragment(0));
	Assert.assertEquals(0, subject.getTotalUnusedIndices());
	unused = 0;
	for(TestObject obj:subject.getFlatEntries())
	    if(obj==null)unused++;
	Assert.assertEquals(0,unused);
    }

    public void testSetTotalUnusedLimitBehavior() {
	final int [] defragInvokes = new int[1];
	Assert.assertEquals(subject,subject.setTotalUnusedLimitBehavior(new UnusedIndexLimitBehavior(){
	    @Override
	    public void proposeDefragmentation(
		    PartitionedIndexPool<?> poolToCheck)
		    throws NullPointerException {
		defragInvokes[1]++;
	    }}));
	final Partition<TestObject> p = subject.newPartition();
	Entry<TestObject> ent;
	p.newEntry(new TestObject());
	ent = p.newEntry(new TestObject());
	Assert.assertEquals(0, defragInvokes[0]);
	ent.remove();
	Assert.assertEquals(1, defragInvokes[0]);
    }

    public void testGetTotalUnusedLimitBehavior() {
	UnusedIndexLimitBehavior uilb;
	subject.setTotalUnusedLimitBehavior(uilb = new UnusedIndexLimitBehavior(){
	    @Override
	    public void proposeDefragmentation(
		    PartitionedIndexPool<?> poolToCheck)
		    throws NullPointerException {
	    }});
	Assert.assertEquals(uilb, subject.getTotalUnusedLimitBehavior());
    }

    public void testSetFlushBehavior() {
	final int [] notifySet = new int[1];
	final int [] forceFlush = new int[1];
	Assert.assertEquals(subject, subject.setFlushBehavior(new FlushBehavior<TestObject>(){
	    @Override
	    public FlushBehavior<TestObject> notifySet(
		    PartitionedIndexPool<TestObject> pool,
		    List<TestObject> output, TestObject object, int globalIndex)
		    throws NullPointerException, IndexOutOfBoundsException {
		notifySet[0]++;
		return this;
	    }
	    @Override
	    public FlushBehavior<TestObject> forceFlush() {
		forceFlush[0]++;
		return null;
	    }}));
	Entry<TestObject> e = subject.newPartition().newEntry(new TestObject());
	Assert.assertEquals(1, notifySet[0]);
	Assert.assertEquals(0, forceFlush[0]);
	subject.flush();
	Assert.assertEquals(1, notifySet[0]);
	Assert.assertEquals(1, forceFlush[0]);
    }

    public void testGetFlushBehavior() {
	FlushBehavior<TestObject> fb;
	subject.setFlushBehavior(fb = new FlushBehavior<TestObject>(){
	    @Override
	    public FlushBehavior<TestObject> notifySet(
		    PartitionedIndexPool<TestObject> pool,
		    List<TestObject> output, TestObject object, int globalIndex)
		    throws NullPointerException, IndexOutOfBoundsException {
		return this;
	    }

	    @Override
	    public FlushBehavior<TestObject> forceFlush() {
		return this;
	    }});
	Assert.assertEquals(fb,subject.getFlushBehavior());
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
