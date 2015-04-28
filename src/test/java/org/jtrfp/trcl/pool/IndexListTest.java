package org.jtrfp.trcl.pool;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;

import org.jtrfp.trcl.dbg.PropertyChangeQueue;
import org.jtrfp.trcl.pool.IndexPool.GrowthBehavior;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class IndexListTest {
    private List<Object> delegate;
    private Object [] testObjects;

    @Before
    public void setUp() throws Exception {
	delegate = new ArrayList<Object>();
	testObjects = new Object[50];
	for(int i=0; i<50; i++)
	    testObjects[i]=new Object();
    }

    @After
    public void tearDown() throws Exception {
	delegate    = null;
	testObjects = null;
    }
    
    @Test
    public void testPop() {
	final IndexList<Object> subject = new IndexList<Object>(delegate);
	for(int i=0; i<50; i++)
	    assertEquals(i, subject.pop(testObjects[i]));
	for(int i=0; i<50; i++)
	    assertEquals(delegate.get(i),testObjects[i]);
	assertEquals(64,delegate.size());
	assertEquals(50,subject.getNumUsedIndices());
    }

    @Test
    public void testFree() {
	final IndexList<Object> subject = new IndexList<Object>(delegate);
	for(int i=0; i<50; i++)
	    assertEquals(i, subject.pop(testObjects[i]));
	for(int i=0; i<25; i++)
	    subject.free(i);
	for(int i=0; i<25; i++)
	    assertEquals(i, subject.pop(testObjects[i]));
	assertEquals(50,subject.pop(new Object()));
    }

    @Test
    public void testSetGrowthBehavior() {
	final IndexList<Object> subject = new IndexList<Object>(delegate);
	assertEquals(0, subject.getMaxCapacity());
	subject.setGrowthBehavior(new GrowthBehavior(){
	    @Override
	    public int grow(int previousMaxCapacity) {
		return previousMaxCapacity+1;
	    }
	    public int shrink(int minDesiredCapacity){
		return minDesiredCapacity;
	    }});
	subject.pop(new Object());
	subject.pop(new Object());
	assertEquals(2, subject.getMaxCapacity());
	subject.pop(new Object());
	assertEquals(3, subject.getMaxCapacity());
    }

    @Test
    public void testGetMaxCapacity() {
	final IndexList<Object> subject =  new IndexList<Object>(delegate);
	for(int i=0; i<50; i++)
	    subject.pop(testObjects[i]);
	//With default power-of-2 growth.
	assertEquals(64, subject.getMaxCapacity());
    }

    @Test
    public void testGetHardLimit() {
	final IndexList<Object> subject=  new IndexList<Object>(delegate);
	subject.setHardLimit(5);
	assertEquals(5,subject.getHardLimit());
    }
    
    @Test
    public void testGetUsedIndices(){
	final IndexList<Object> subject = new IndexList<Object>(delegate);
	Queue<Integer> indices = subject.getUsedIndices();
	subject.pop(new Object());
	final ArrayList<Integer> dest = new ArrayList<Integer>();
	dest.add(subject.pop(new Object()));
	dest.add(subject.pop(new Object()));
	assertNotNull(indices);
	assertEquals(3,indices.size());
	Iterator<Integer> it = indices.iterator();
	assertEquals(0, (int)it.next());
	assertEquals(1, (int)it.next());
	assertEquals(2, (int)it.next());
	
	assertEquals(1, (int)dest.get(0));
	assertEquals(2, (int)dest.get(1));
    }//end testGetUsedIndices()
    
    @Test
    public void testConstructor(){
	new IndexList<Object>(delegate);
	assertEquals(0,delegate.size());
    }//end testConstructor
    
    @Test
    public void testCompact(){
	final IndexList<Object> subject = new IndexList<Object>(delegate);
	subject.pop(new Object());
	subject.pop(new Object());
	subject.pop(new Object());
	subject.free(0);
	subject.free(1);
	// f f 2
	assertEquals(2, subject.getFreeIndices().size());
	assertEquals(1, subject.getUsedIndices().size());
	assertEquals(4,delegate.size());//Power of two
	subject.compact();
	// f f 2
	assertEquals(2, subject.getFreeIndices().size());
	assertEquals(1, subject.getUsedIndices().size());
	assertEquals(3,delegate.size());
	Assert.assertNull(delegate.get(0));
	Assert.assertNull(delegate.get(1));
	assertEquals(0,subject.pop(new Object()));
	subject.free(2);
	// 0 f f
	assertEquals(2, subject.getFreeIndices().size());
	assertEquals(1, subject.getUsedIndices().size());
	assertEquals(3,delegate.size());
	Assert.assertNull(delegate.get(1));
	Assert.assertNull(delegate.get(2));
	subject.compact();
	// 0
	assertEquals(0, subject.getFreeIndices().size());
	assertEquals(1, subject.getUsedIndices().size());
	assertEquals(1,delegate.size());
	// 0 1
	assertEquals(1,subject.pop(new Object()));
	assertEquals(0, subject.getFreeIndices().size());
	assertEquals(2, subject.getUsedIndices().size());
	assertEquals(2,delegate.size());
	// 0 1 2
	assertEquals(2,subject.pop(new Object()));
	assertEquals(0, subject.getFreeIndices().size());
	assertEquals(3, subject.getUsedIndices().size());
	assertEquals(4,delegate.size());//Power of two
    }
    
    @Test
    public void testGetNumUnusedIndices(){
	final IndexList<Object> subject = new IndexList<Object>(delegate);
	assertEquals(0,subject.getNumUnusedIndices());
	subject.pop(new Object());
	assertEquals(0,subject.getNumUnusedIndices());
	subject.free(0);
	assertEquals(1,subject.getNumUnusedIndices());
	subject.pop(new Object());
	assertEquals(0,subject.getNumUnusedIndices());
    }
    
    @Test
    public void testGetNumUsedIndices(){
	final IndexList<Object> subject = new IndexList<Object>(delegate);
	assertEquals(0,subject.getNumUsedIndices());
	subject.pop(new Object());
	assertEquals(1,subject.getNumUsedIndices());
	subject.free(0);
	assertEquals(0,subject.getNumUsedIndices());
    }
    
    @Test
    public void testNumUsedIndicesPropertyChange(){
	final IndexList<Object>         subject = new IndexList<Object>(delegate);
	final PropertyChangeQueue queue = new PropertyChangeQueue();
	subject.addPropertyChangeListener(IndexList.NUM_USED_INDICES,queue);
	assertEquals(0,queue.size());
	subject.pop(new Object());
	assertEquals(1,queue.size());
	assertEquals(1,queue.pop().getNewValue());
	subject.pop(new Object());
	assertEquals(1,queue.size());
	assertEquals(2,queue.pop().getNewValue());
    }
    
    @Test
    public void testNumUnusedIndicesPropertyChange(){
	final IndexList<Object>         subject = new IndexList<Object>(delegate);
	final PropertyChangeQueue queue = new PropertyChangeQueue();
	subject.addPropertyChangeListener(IndexList.NUM_UNUSED_INDICES,queue);
	subject.pop(new Object());
	subject.pop(new Object());
	subject.pop(new Object());
	assertEquals(0,queue.size());
	subject.free(0);
	assertEquals(1,queue.size());
	assertEquals(1,queue.pop().getNewValue());
	subject.free(1);
	assertEquals(1,queue.size());
	assertEquals(2,queue.pop().getNewValue());
    }
    
    private void populate(IndexList<Object> subject, int qty, List<Integer> dest){
	//System.out.println("Populate by "+qty);
	for(int i=0; i<qty; i++){
	    //System.out.println("populate() iteration "+i);
	    final int element = subject.pop(new Object());
	    dest.add(element);
	    assertTrue(subject.getUsedIndices().contains(element));
	    assertEquals(i,element);
	    }
    }//end populate()
    
    private void testIndices(List<Integer> dest){
	for(int i=0; i<dest.size(); i++)
	    assertEquals(i,dest.get(i).intValue());
    }
    
    private void depopulate(IndexList subject, List<Integer> list){
	for(int i:list)
	    subject.free(i);
	assertEquals(0,subject.getNumUsedIndices());
    }
    
    @Test
    public void testChangingState(){
	final IndexList<Object> subject = new IndexList<Object>(delegate);
	final int NUM_ITERATIONS = 50;
	final ArrayList<Integer> indices = new ArrayList<Integer>();
	for(int iteration=0; iteration<NUM_ITERATIONS; iteration++){
	    //System.out.println("testChangingState() iteration "+iteration);
	    final int size = (int)(Math.random()*250);
	    indices.clear();
	    populate(subject,size,indices);
	    testIndices(indices);
	    depopulate(subject,indices);
	    assertEquals(size,subject.getNumUnusedIndices());
	    //System.out.println("compact()");
	    subject.compact();
	    assertEquals(0,subject.getNumUnusedIndices());
	    assertEquals(0,subject.getNumUsedIndices());
	    assertEquals(0,delegate.size());
	}
    }//end testChangingState()

}
