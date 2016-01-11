/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2016 Chuck Ritola
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.IteratorUtils;
import org.jtrfp.trcl.coll.ObjectTallyCollection.ObjectTallyListener;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class ObjectTallyCollectionTest {
    protected static final int DELEGATE_SIZE = 5;
    protected static final Iterator<Object> DELEGATE_ITERATOR = IteratorUtils.EMPTY_ITERATOR;
    protected static final Object [] DELEGATE_BACKING_ARRAY = new Object[]{1,2,3,4,5};//Dat casting magic, yo.
    private ObjectTallyCollection<Object> subject;
    private Collection<Object> delegate;

    @Before
    public void setUp() throws Exception {
	delegate = (Collection<Object>)Mockito.mock(Collection.class);
	
	when(delegate.size()).thenReturn    (DELEGATE_SIZE);
	when(delegate.iterator()).thenReturn(DELEGATE_ITERATOR);
	when(delegate.toArray()).thenReturn (DELEGATE_BACKING_ARRAY);
	
	subject  = new ObjectTallyCollection<Object>(delegate);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testHashCode() {
	Assert.assertEquals(delegate.hashCode(), subject.hashCode());
    }

    @Test
    public void testObjectTallyCollection() {
	new ObjectTallyCollection<Object>(delegate);
    }

    @Test
    public void testGetTallyOf() {
	final Object one = new Object();
	final Object two = new Object();
	final Object three = new Object();
	
	subject.add(one);
	subject.add(two);
	subject.add(three);
	subject.add(one);
	subject.add(one);
	subject.add(two);
	
	assertEquals(3,subject.getTallyOf(one));
	assertEquals(2,subject.getTallyOf(two));
	assertEquals(1,subject.getTallyOf(three));
	
	subject.remove(one);
	subject.remove(three);
	
	assertEquals(2,subject.getTallyOf(one));
	assertEquals(2,subject.getTallyOf(two));
	assertEquals(0,subject.getTallyOf(three));
    }
    
    @Test(expected=ObjectTallyCollection.NegativeTallyException.class)
    public void testGetTallyOfNegativeTallyException(){
	final Object one = new Object();
	final Object two = new Object();
	final Object three = new Object();
	
	subject.add(one);
	subject.add(two);
	subject.add(three);
	subject.add(one);
	subject.add(one);
	subject.add(two);
	
	subject.remove(one);
	subject.remove(one);
	subject.remove(one);
	subject.remove(one);//Exception
    }//end testGetTallyOfNegativeTallyException()

    @Test
    public void testAdd() {
	final Object testObject = new Object();
	subject.add(testObject);
	verify(delegate).add(testObject);
    }

    @Test
    public void testAddAll() {
	final List<Object> objects = Arrays.asList(1,2,3,4, new Object());
	subject.addAll(objects);
	verify(delegate).addAll(objects);
    }

    @Test
    public void testClear() {
	subject.clear();
	verify(delegate).clear();
    }

    @Test
    public void testContains() {
	final Object object = new Object();
	subject.contains(object);
	verify(delegate).contains(object);
    }

    @Test
    public void testContainsAll() {
	final List<Object> objects = Arrays.asList(1,2,3,4, new Object());
	subject.containsAll(objects);
	verify(delegate).containsAll(objects);
    }

    @Test
    public void testIsEmpty() {
	subject.isEmpty();
	verify(delegate).isEmpty();
    }

    @Test
    public void testIterator() {
	assertEquals(DELEGATE_ITERATOR, subject.iterator());
    }

    @Test
    public void testRemove() {
	final Object object = new Object();
	subject.add(object);
	subject.remove(object);
	verify(delegate).remove(object);
    }

    @Test
    public void testRemoveAll() {
	final List<Object> objects = Arrays.asList(1,2,3,4, new Object());
	subject.removeAll(objects);
	verify(delegate).removeAll(objects);
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testRetainAll() {
	final List<Object> objects = Arrays.asList(1,2,3,4, new Object());
	subject.retainAll(objects);
	verify(delegate).retainAll(objects);
    }

    @Test
    public void testSize() {
	assertEquals(DELEGATE_SIZE, subject.size());
	verify(delegate).size();
    }

    @Test
    public void testToArray() {
	assertEquals((Object)DELEGATE_BACKING_ARRAY, subject.toArray());
    }

    @Test
    public void testGetDelegate() {
	assertEquals(delegate,subject.getDelegate());
    }
    
    @Test
    public void testListener(){
	ObjectTallyListener<Object> listener = Mockito.mock(ObjectTallyListener.class);
	final Object object = new Object();
	subject.addObjectTallyListener(object, listener);
	subject.add(object);
	verify(listener).tallyChanged(object, 0, 1);
	subject.add(object);
	verify(listener).tallyChanged(object, 1, 2);
    }//end testListener

}//end ObjectTallyCollectionTest
