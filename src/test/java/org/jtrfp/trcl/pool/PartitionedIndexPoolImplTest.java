package org.jtrfp.trcl.pool;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.jtrfp.trcl.coll.ListActionDispatcher;
import org.jtrfp.trcl.pool.PartitionedIndexPool.Entry;
import org.jtrfp.trcl.pool.PartitionedIndexPool.Partition;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ochafik.util.listenable.DefaultListenableCollection;
import com.ochafik.util.listenable.ListenableCollection;

public class PartitionedIndexPoolImplTest {
    private PartitionedIndexPool<Integer> subject;

    @Before
    public void setUp() throws Exception {
	subject = new PartitionedIndexPoolImpl<Integer>();
    }

    @After
    public void tearDown() throws Exception {
    }
    
    @Test
    public void testNewPartition() {
	Partition<Integer> part = subject.newPartition();
	assertNotNull(part);
    }

    @Test
    public void testGetPartitions() {
	ListenableCollection<Partition<Integer>> parts = subject.getPartitions();
	assertNotNull(parts);
	assertEquals(0,parts.size());
	Partition<Integer> p0 = subject.newPartition();
	Partition<Integer> p1 = subject.newPartition();
	assertEquals(2,parts.size());
	assertTrue(parts.contains(p0));
	assertTrue(parts.contains(p1));
    }

    
    @Test
    public void testGetFlatEntries() {
	ListActionDispatcher<PartitionedIndexPool.Entry<Integer>> flatEntries = subject.getFlatEntries();
	assertNotNull(flatEntries);
	Partition<Integer> p0 = subject.newPartition();
	Partition<Integer> p1 = subject.newPartition();
	assertEquals(0,flatEntries.size());
	Entry<Integer> e0 = p0.newEntry(5);
	Entry<Integer> e1 = p0.newEntry(6);
	Entry<Integer> e2 = p1.newEntry(1);
	Entry<Integer> e3 = p1.newEntry(2);
	assertEquals(4,flatEntries.size());
	assertTrue(flatEntries.contains(e0));
	assertTrue(flatEntries.contains(e1));
	assertTrue(flatEntries.contains(e2));
	assertTrue(flatEntries.contains(e3));
    }
    
    @Test
    public void testGetTotalUnusedIndices() {
	assertEquals(0,subject.getTotalUnusedIndices());
	Partition<Integer> p0 = subject.newPartition();
	Entry<Integer>     e0 = p0.newEntry(5);
	p0.newEntry(6);
	assertEquals(0,subject.getTotalUnusedIndices());
	e0.remove();
	assertEquals(1,subject.getTotalUnusedIndices());
    }

    @Test
    public void testDefragment() {
	Partition<Integer> p0 = subject.newPartition();
	Entry<Integer>     e0 = p0.newEntry(5);
	Entry<Integer>     e1 = p0.newEntry(6);
	Entry<Integer>     e2 = p0.newEntry(7);
	e0.remove();
	e1.remove();
	e2.remove();
	assertEquals(3,subject.getTotalUnusedIndices());
	subject.defragment(0);
	assertEquals(0,subject.getTotalUnusedIndices());
    }
    
    @Test
    public void testRemovePartition(){
	Partition<Integer> p0 = subject.newPartition();
	Entry<Integer>     e0 = p0.newEntry(5);
	Entry<Integer>     e1 = p0.newEntry(6);
	Entry<Integer>     e2 = p0.newEntry(7);
	p0.remove();
	assertFalse(p0.isValid());
	assertEquals(0,subject.getTotalUnusedIndices());
    }
    
    @Test
    public void testInvalidatedPartitionGlobalStartIndexException(){
	Partition<Integer> p0 = subject.newPartition();
	p0.remove();
	try{p0.getGlobalStartIndex();}
	catch(IllegalStateException e){return;}
	fail("Failed to throw expected IllegalStateException.");
    }
    
    @Test
    public void testInvalidatedPartitionLengthInIndicesException(){
	Partition<Integer> p0 = subject.newPartition();
	p0.remove();
	try{p0.getLengthInIndices();}
	catch(IllegalStateException e){return;}
	fail("Failed to throw expected IllegalStateException.");
    }
    
    @Test
    public void testInvalidatedPartitionNumUnusedIndicesException(){
	Partition<Integer> p0 = subject.newPartition();
	p0.remove();
	try{p0.getNumUnusedIndices();}
	catch(IllegalStateException e){return;}
	fail("Failed to throw expected IllegalStateException.");
    }
    
    @Test
    public void testInvalidatedPartitionNumUsedIndicesException(){
	Partition<Integer> p0 = subject.newPartition();
	p0.remove();
	try{p0.getNumUsedIndices();}
	catch(IllegalStateException e){return;}
	fail("Failed to throw expected IllegalStateException.");
    }
    
    @Test
    public void testInvalidatedEntryGetGlobalIndex(){
	Partition<Integer> p0 = subject.newPartition();
	Entry<Integer>     e0 = p0.newEntry(5);
	Entry<Integer>     e1 = p0.newEntry(6);
	Entry<Integer>     e2 = p0.newEntry(7);
	e0.remove();
	try{e0.getGlobalIndex();}
	catch(IllegalStateException e){return;}
	fail("Failed to throw expected IllegalStateException.");
    }
    
    @Test
    public void testInvalidatedEntryGetLocalIndex(){
	Partition<Integer> p0 = subject.newPartition();
	Entry<Integer>     e0 = p0.newEntry(5);
	Entry<Integer>     e1 = p0.newEntry(6);
	Entry<Integer>     e2 = p0.newEntry(7);
	e0.remove();
	try{e0.getLocalIndex();}
	catch(IllegalStateException e){return;}
	fail("Failed to throw expected IllegalStateException.");
    }
    
    @Test
    public void testEntryGetLocalIndex(){
	Partition<Integer> p0 = subject.newPartition();
	Partition<Integer> p1 = subject.newPartition();
	Entry<Integer>     e0 = p0.newEntry(5);
	Entry<Integer>     e1 = p0.newEntry(6);
	Entry<Integer>     e2 = p1.newEntry(7);
	assertEquals(1,e1.getLocalIndex());
    }
    
    @Test
    public void testEntryGetGlobalIndex(){
	Partition<Integer> p0 = subject.newPartition();
	Partition<Integer> p1 = subject.newPartition();
	Entry<Integer>     e0 = p0.newEntry(5);
	Entry<Integer>     e1 = p0.newEntry(6);
	Entry<Integer>     e2 = p1.newEntry(7);
	assertEquals(2,e2.getGlobalIndex());
    }
    
    @Test
    public void testPartitionGetGlobalStartIndex(){
	Partition<Integer> p0 = subject.newPartition();
	Partition<Integer> p1 = subject.newPartition();
	Entry<Integer>     e0 = p0.newEntry(5);
	Entry<Integer>     e1 = p0.newEntry(6);
	Entry<Integer>     e2 = p1.newEntry(7);
	assertEquals(0,p0.getGlobalStartIndex());
	assertEquals(2,p1.getGlobalStartIndex());
	Entry<Integer>     e3 = p0.newEntry(8);
	Entry<Integer>     e4 = p0.newEntry(9);
	Entry<Integer>     e5 = p1.newEntry(10);
	assertEquals(0,p0.getGlobalStartIndex());
	assertEquals(4,p1.getGlobalStartIndex());
    }
    
    @Test
    public void testPartitionGetEntries(){
	Partition<Integer> p0 = subject.newPartition();
	Partition<Integer> p1 = subject.newPartition();
	Entry<Integer>     e0 = p0.newEntry(5);
	Entry<Integer>     e1 = p0.newEntry(6);
	Entry<Integer>     e2 = p1.newEntry(7);
	assertEquals(2,p0.getEntries().size());
	assertEquals(1,p1.getEntries().size());
	e0.remove();
	assertEquals(1,p0.getEntries().size());
	assertEquals(1,p1.getEntries().size());
	e2.remove();
	assertEquals(1,p0.getEntries().size());
	assertEquals(0,p1.getEntries().size());
    }
    
    @Test
    public void testPartitionIsValid(){
	Partition<Integer> p0 = subject.newPartition();
	Partition<Integer> p1 = subject.newPartition();
	Entry<Integer>     e0 = p0.newEntry(5);
	Entry<Integer>     e1 = p0.newEntry(6);
	Entry<Integer>     e2 = p1.newEntry(7);
	assertTrue(p0.isValid());
	//Control test
	assertTrue(e0.isValid());
	assertTrue(e1.isValid());
	
	p0.remove();
	assertFalse(p0.isValid());
	//Ensure entries are also invalid following removal
	assertFalse(e0.isValid());
	assertFalse(e1.isValid());
	
	assertTrue(p1.isValid());
	p1.remove();
	assertFalse(p1.isValid());
    }
    
    @Test
    public void testPartitionRemoveAllEntries(){
	Partition<Integer> p0 = subject.newPartition();
	Partition<Integer> p1 = subject.newPartition();
	Entry<Integer>     e0 = p0.newEntry(5);
	Entry<Integer>     e1 = p0.newEntry(6);
	Entry<Integer>     e2 = p1.newEntry(7);
	
	assertEquals(p0,p0.removeAllEntries());
	assertEquals(0,p0.getNumUsedIndices());
	e0 = p0.newEntry(5);
	e1 = p0.newEntry(6);
	assertEquals(p0,p0.removeAllEntries());
	assertEquals(0,p0.getNumUsedIndices());
    }
}//end PartitionedIndexPool
