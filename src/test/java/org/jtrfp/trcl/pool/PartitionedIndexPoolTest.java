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
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jtrfp.trcl.pool.PartitionedIndexPool.Entry;
import org.jtrfp.trcl.pool.PartitionedIndexPool.FlushBehavior;
import org.jtrfp.trcl.pool.PartitionedIndexPool.Partition;
import org.jtrfp.trcl.pool.PartitionedIndexPool.UnusedIndexLimitBehavior;

import com.ochafik.util.listenable.CollectionEvent;
import com.ochafik.util.listenable.CollectionListener;
import com.ochafik.util.listenable.ListenableCollection;
import com.ochafik.util.listenable.ListenableList;

public abstract class PartitionedIndexPoolTest {
    
    protected PartitionedIndexPool<TestObject> subject;
    
    protected class TestObject{
    }//end TestObject
    
    protected abstract PartitionedIndexPool<TestObject> newSubject();

    public void testNewPartition() {
	Partition<TestObject> tp = subject.newPartition();
	assertNotNull(tp);
    }

    public void testRemovePartition() {
	Partition<TestObject> tp = subject.newPartition();
	assertTrue(subject.getPartitions().contains(tp));
	assertTrue(tp.isValid());
	assertEquals(subject,subject.removePartition(tp));
	assertFalse(subject.getPartitions().contains(tp));
	assertFalse(tp.isValid());
    }

    public void testRemoveAllPartitions() {
	for(int i=0; i<10; i++)
	    subject.newPartition();
	assertEquals(10, subject.getPartitions().size());
	Collection<Partition<TestObject>> parts = new ArrayList<Partition<TestObject>>();
	parts.addAll(subject.getPartitions());
	assertEquals(subject,subject.removeAllPartitions());
	assertEquals(0, subject.getPartitions().size());
	for(Partition<TestObject> p:parts)
	    assertFalse(p.isValid());
    }

    public void testGetPartitions() {
	for(int i=0; i<10; i++)
	    subject.newPartition();
	assertEquals(10,subject.getPartitions().size());
	Partition<TestObject> np = subject.newPartition();
	ListenableCollection<Partition<TestObject>> partitions;
	partitions = subject.getPartitions();
	assertNotNull(partitions);
	assertTrue((partitions).contains(np));
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
	assertEquals(0, added[0]);
	assertEquals(0, removed[0]);
	Partition<TestObject> np1 = subject.newPartition();
	assertEquals(1, added[0]);
	assertEquals(0, removed[0]);
	subject.removePartition(np1);
	assertEquals(1, added[0]);
	assertEquals(1, removed[0]);
	subject.removeAllPartitions();
	assertEquals(1, added[0]);
	assertEquals(12, removed[0]);
	assertTrue(partitions.isEmpty());
    }//end testGetPartitions()

    public void testGetFlatEntries() {
	final ListenableList<TestObject> flatEntries = subject.getFlatEntries();
	assertNotNull(flatEntries);
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
	assertEquals(2,flatEntries.size());
	assertTrue(flatEntries.contains(entries[0]));
	assertTrue(flatEntries.contains(entries[1]));
    }

    public void testGetTotalUnusedIndices() {
	Partition<TestObject> p0 = subject.newPartition();
	assertEquals(0, subject.getTotalUnusedIndices());
	Partition<TestObject> p1 = subject.newPartition();
	assertEquals(0, subject.getTotalUnusedIndices());
	
	Entry<TestObject> [] to = new Entry[2];
	
	p0.newEntry(new TestObject());
	assertEquals(0, subject.getTotalUnusedIndices());
	p0.newEntry(new TestObject());
	assertEquals(0, subject.getTotalUnusedIndices());
	to[0] = p0.newEntry(new TestObject());
	
	p1.newEntry(new TestObject());
	assertEquals(0, subject.getTotalUnusedIndices());
	p1.newEntry(new TestObject());
	assertEquals(0, subject.getTotalUnusedIndices());
	to[1] = p1.newEntry(new TestObject());
	assertEquals(0, subject.getTotalUnusedIndices());
	
	to[0].remove();
	assertEquals(1, subject.getTotalUnusedIndices());
	to[1].remove();
	assertEquals(2, subject.getTotalUnusedIndices());
	
	to[3] = p0.newEntry(new TestObject());
	assertEquals(1, subject.getTotalUnusedIndices());
	
	subject.defragment(1);
	assertEquals(1, subject.getTotalUnusedIndices());
	subject.defragment(0);
	assertEquals(0, subject.getTotalUnusedIndices());
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
	assertEquals(2,unused);
	
	subject.defragment(1);
	unused = 0;
	for(TestObject obj:subject.getFlatEntries())
	    if(obj==null)unused++;
	assertEquals(1,unused);
	
	assertEquals(subject, subject.defragment(0));
	assertEquals(0, subject.getTotalUnusedIndices());
	unused = 0;
	for(TestObject obj:subject.getFlatEntries())
	    if(obj==null)unused++;
	assertEquals(0,unused);
    }

    public void testSetTotalUnusedLimitBehavior() {
	final int [] defragInvokes = new int[1];
	assertEquals(subject,subject.setTotalUnusedLimitBehavior(new UnusedIndexLimitBehavior(){
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
	assertEquals(0, defragInvokes[0]);
	ent.remove();
	assertEquals(1, defragInvokes[0]);
    }

    public void testGetTotalUnusedLimitBehavior() {
	UnusedIndexLimitBehavior uilb;
	subject.setTotalUnusedLimitBehavior(uilb = new UnusedIndexLimitBehavior(){
	    @Override
	    public void proposeDefragmentation(
		    PartitionedIndexPool<?> poolToCheck)
		    throws NullPointerException {
	    }});
	assertEquals(uilb, subject.getTotalUnusedLimitBehavior());
    }

    public void testSetFlushBehavior() {
	final int [] notifySet = new int[1];
	final int [] forceFlush = new int[1];
	assertEquals(subject, subject.setFlushBehavior(new FlushBehavior<TestObject>(){
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
	assertEquals(1, notifySet[0]);
	assertEquals(0, forceFlush[0]);
	subject.flush();
	assertEquals(1, notifySet[0]);
	assertEquals(1, forceFlush[0]);
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
	assertEquals(fb,subject.getFlushBehavior());
    }

    public void testFlush() {
	Partition<TestObject> p = subject.newPartition();
	final TestObject to = new TestObject();
	p.newEntry(to);
	assertEquals(0, subject.getFlatEntries().size());
	assertEquals(1, subject.flush());
	assertEquals(1, subject.getFlatEntries().size());
    }

    /////////// PARTITION //////////////

    public void testPartitionGetParent(){
	assertEquals(subject,subject.newPartition().getParent());
    }

    public void testPartitionNewEntry(){
	Partition<TestObject> p;
	Entry<TestObject> entry = (p=subject.newPartition()).newEntry(new TestObject());
	assertNotNull(entry);
	assertTrue(p.getEntries().contains(entry));
    }

    public void testPartitionRemove(){
	Partition<TestObject> p;
	p = subject.newPartition();
	assertTrue(p.isValid());
	assertEquals(p,p.remove());
	assertFalse(p.isValid());
	assertFalse(subject.getPartitions().contains(p));
    }

    public void testPartitionRemoveEntry(){
	Partition<TestObject> p;
	p = subject.newPartition();
	Entry<TestObject> entry = p.newEntry(new TestObject());
	assertTrue(p.getEntries().contains(entry));
	assertEquals(entry,p.removeEntry(entry));
	assertFalse(p.getEntries().contains(entry));
    }
    
    public void testPartitionRemoveAllEntries(){
	Partition<TestObject> p;
	p = subject.newPartition();
	Entry<TestObject> entry0 = p.newEntry(new TestObject());
	Entry<TestObject> entry1 = p.newEntry(new TestObject());
	assertTrue(entry0.isValid());
	assertTrue(entry1.isValid());
	assertEquals(p,p.removeAllEntries());
	assertFalse(entry0.isValid());
	assertFalse(entry1.isValid());
    }

    public void testPartitionGetEntries(){
	Partition<TestObject> p;
	p = subject.newPartition();
	Entry<TestObject> entry0 = p.newEntry(new TestObject());
	Entry<TestObject> entry1 = p.newEntry(new TestObject());
	assertTrue(p.getEntries().contains(entry0));
	assertTrue(p.getEntries().contains(entry1));
    }

    public void testPartitionGetGlobalStartIndex(){
	Partition<TestObject> p;
	p = subject.newPartition();
	assertEquals(0,p.getGlobalStartIndex());
	// ???
    }

    public void testPartitionGetLengthInIndices(){
	Partition<TestObject> p;
	p = subject.newPartition();
	p.newEntry(new TestObject());
	p.newEntry(new TestObject());
	p.newEntry(new TestObject());
	assertEquals(3,p.getLengthInIndices());
    }

    public void testPartitionDefragment(){
	Partition<TestObject> p0 = subject.newPartition();
	Partition<TestObject> p1 = subject.newPartition();
	
	Entry<TestObject> [] to = new Entry[2];
	
	p0.newEntry(new TestObject());
	p0.newEntry(new TestObject());
	to[0] = p0.newEntry(new TestObject());
	
	p1.newEntry(new TestObject());
	p1.newEntry(new TestObject());
	to[1] = p1.newEntry(new TestObject());
	
	assertEquals(0,subject.getTotalUnusedIndices());
	to[0].remove();
	to[1].remove();
	assertEquals(2,subject.getTotalUnusedIndices());
	
	assertEquals(p0,p0.defragment(0));
	assertEquals(1,subject.getTotalUnusedIndices());
	
	assertEquals(p1,p1.defragment(0));
	assertEquals(0,subject.getTotalUnusedIndices());
    }

    public void testPartitionSetUnusedLimitBehavior(){
	Partition<TestObject> p0 = subject.newPartition();
	Entry<TestObject> [] to = new Entry[2];
	
	final int [] proposeDefragCounter = new int[1];
	assertEquals(null,p0.setUnusedLimitBehavior(new UnusedIndexLimitBehavior(){
	    @Override
	    public void proposeDefragmentation(
		    PartitionedIndexPool<?> poolToCheck)
		    throws NullPointerException {
		proposeDefragCounter[0]++;
	    }}));
	assertEquals(0,proposeDefragCounter[0]);
	to[0] = p0.newEntry(new TestObject());
	to[1] = p0.newEntry(new TestObject());
	assertEquals(0,proposeDefragCounter[0]);
	p0.removeEntry(to[0]);
	assertEquals(1,proposeDefragCounter[0]);
    }

    public void testPartitionGetUnusedLimitBehavior(){
	Partition<TestObject> p0 = subject.newPartition();
	Entry<TestObject> [] to = new Entry[2];
	
	final int [] proposeDefragCounter = new int[1];
	UnusedIndexLimitBehavior uilb;
	assertEquals(null,p0.setUnusedLimitBehavior(uilb = new UnusedIndexLimitBehavior(){
	    @Override
	    public void proposeDefragmentation(
		    PartitionedIndexPool<?> poolToCheck)
		    throws NullPointerException {
		proposeDefragCounter[0]++;
	    }}));
	assertEquals(uilb,p0.getUnusedLimitBehavior());
    }

    public void testPartitionIsValid(){
	Partition<TestObject> p0 = subject.newPartition();
	assertTrue(p0.isValid());
	p0.remove();
	assertFalse(p0.isValid());
    }

    ///////// ENTRY /////////////

    public void testGetParent(){
	Partition<TestObject> p0 = subject.newPartition();
	Entry<TestObject> ent = p0.newEntry(new TestObject());
	assertEquals(p0,ent.getParent());
    }

    public void testGet(){
	Partition<TestObject> p0 = subject.newPartition();
	TestObject         to = new TestObject();
	Entry<TestObject> ent = p0.newEntry(to);
	assertEquals(to,ent.get());
    }

    public void testGetLocalIndex(){
	Partition<TestObject> p0 = subject.newPartition();
	TestObject to = new TestObject();
	Entry<TestObject> ent = p0.newEntry(to);
	assertEquals(0,ent.getLocalIndex());
    }

    public void testGetGlobalIndex(){
	Partition<TestObject> p0 = subject.newPartition();
	TestObject to = new TestObject();
	Entry<TestObject> ent = p0.newEntry(to);
	assertEquals(0,ent.getGlobalIndex());
    }

    public void testRemove(){
	Partition<TestObject> p0 = subject.newPartition();
	TestObject to = new TestObject();
	Entry<TestObject> ent = p0.newEntry(to);
	assertEquals(ent,ent.remove());
    }

    public void testIsValid(){
	Partition<TestObject> p0 = subject.newPartition();
	TestObject            to = new TestObject();
	Entry<TestObject>    ent = p0.newEntry(to);
	assertTrue(ent.isValid());
	ent.remove();
	assertFalse(ent.isValid());
    }
}//end PartitionedIndexPoolTest
