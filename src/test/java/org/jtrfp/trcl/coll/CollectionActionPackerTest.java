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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ochafik.util.listenable.AdaptedCollection;
import com.ochafik.util.listenable.Pair;

public class CollectionActionPackerTest {
    private CollectionActionPacker<String,Integer> subject;
    private CollectionActionDispatcher<Pair<Integer,CollectionActionDispatcher<String>>> subjectDelegate;
    Collection<String> flatCollection;
    Pair<Integer,String> zero,one,two,A,B,C;
    
    private static final CachedAdapter<CollectionActionDispatcher<String>,Pair<Integer,CollectionActionDispatcher<String>>> flatteningAdapter = new CachedAdapter<CollectionActionDispatcher<String>,Pair<Integer,CollectionActionDispatcher<String>>>(){

	@Override
	protected Pair<Integer, CollectionActionDispatcher<String>> _adapt(
		CollectionActionDispatcher<String> value)
		throws UnsupportedOperationException {
	    throw new UnsupportedOperationException();
	}

	@Override
	protected CollectionActionDispatcher<String> _reAdapt(
		Pair<Integer, CollectionActionDispatcher<String>> value)
		throws UnsupportedOperationException {
	    return value.getValue();
	}
    };//end flatteningAdapter
    
    private Collection<Pair<Integer,CollectionActionDispatcher<String>>> unpacker;

    @Before
    public void setUp() throws Exception {
	subjectDelegate = new CollectionActionDispatcher<Pair<Integer,CollectionActionDispatcher<String>>>(
		        new ArrayList<Pair<Integer,CollectionActionDispatcher<String>>>());
	subject         = new CollectionActionPacker<String,Integer>(subjectDelegate);
	
	subject.add(zero=new Pair<Integer,String>(0,"zero"));
	subject.add(one =new Pair<Integer,String>(0,"one"));
	subject.add(two =new Pair<Integer,String>(0,"two"));
	
	subject.add(A=new Pair<Integer,String>(1,"A"));
	subject.add(B=new Pair<Integer,String>(1,"B"));
	subject.add(C=new Pair<Integer,String>(1,"C"));
	
	flatCollection = new ArrayList<String>();
	unpacker       = new AdaptedCollection<CollectionActionDispatcher<String>,Pair<Integer,CollectionActionDispatcher<String>>>(
		       new CollectionActionUnpacker<String>(flatCollection),flatteningAdapter.toForward(),flatteningAdapter.toBackward());
	subjectDelegate.addTarget(unpacker, true);
    }

    @After
    public void tearDown() throws Exception {
	unpacker = null; flatCollection = null;
	subject  = null; subjectDelegate= null;
    }

    @Test
    public void testAdd() {
	assertEquals(2,subjectDelegate.size());
	
	for(Pair<Integer,CollectionActionDispatcher<String>> pair:subjectDelegate){
	    if(pair.getKey()==0 || pair.getKey()==1){
		//ok
	    }else{fail("Got unexpected key: "+pair.getKey());}
	}//end for(collection)
	assertEquals(6,flatCollection.size());
	assertTrue(flatCollection.contains("zero"));
	assertTrue(flatCollection.contains("one"));
	assertTrue(flatCollection.contains("two"));
	
	assertTrue(flatCollection.contains("A"));
	assertTrue(flatCollection.contains("B"));
	assertTrue(flatCollection.contains("C"));
    }//end testAdd()

    @Test
    public void testClear() {
	assertEquals(2,subjectDelegate.size());//Control test
	subject.clear();
	assertEquals(0,subjectDelegate.size());
	assertEquals(0,flatCollection.size());
    }

    @Test
    public void testContains() {
	assertTrue(subject.contains(zero));
	assertFalse(subject.contains(null));
	assertTrue(subject.contains(A));
	assertTrue(subject.contains(B));
	assertTrue(subject.contains(C));
	assertTrue(subject.contains(one));
	assertTrue(subject.contains(two));
    }

    @Test
    public void testContainsAll() {
	assertTrue(subject.containsAll(Arrays.asList(zero,one,two,A,B,C)));
    }
    
    @Test
    public void testFlatContents(){
	assertTrue(flatCollection.containsAll(Arrays.asList("zero","one","two","A","B","C")));
    }

    @Test
    public void testIsEmpty() {
	assertFalse(subject.isEmpty());//Control var
	subject.clear();
	assertTrue(subject.isEmpty());
    }
    
    @Test
    public void testFlatCollectionEmpty(){
	assertFalse(flatCollection.isEmpty());//Control var
	subject.clear();
	assertTrue(flatCollection.isEmpty());
    }

    @Test
    public void testRemove() {
	assertTrue(subject.contains(zero));//Control var
	subject.remove(zero);
	assertFalse(subject.contains(zero));
	subject.remove(one);
	subject.remove(two);
	boolean contains = false;
	for(Pair<Integer,CollectionActionDispatcher<String>> pair:subjectDelegate)
	    contains |= pair.getKey()==zero.getKey();
	assertFalse(contains);
    }
    
    @Test
    public void testFlatCollectionRemove() {
	assertTrue(subject.contains(zero));//Control var
	subject.remove(zero);
	assertFalse(flatCollection.contains(zero));
    }

    @Test
    public void testRemoveAll() {
	assertTrue(subject.removeAll(Arrays.asList(one,two)));
	assertFalse(subject.contains(one));
	assertFalse(subject.contains(two));
	assertEquals(4,subject.size());
    }
    
    @Test
    public void testFlatCollectionRemoveAll() {
	assertTrue(subject.removeAll(Arrays.asList(one,two)));
	assertFalse(flatCollection.contains(one));
	assertFalse(flatCollection.contains(two));
	assertEquals(4,flatCollection.size());
    }

    @Test
    public void testRetainAll() {
	assertTrue(subject.retainAll(Arrays.asList(one,two,A)));
	assertEquals(3,subject.size());
    }

    @Test
    public void testSize() {
	assertEquals(6,subject.size());
    }
    
    @Test
    public void testFlatSize(){
	assertEquals(6,flatCollection.size());
    }

    @Test
    public void testToArray() {
	Object [] array = subject.toArray();
	Collection c = Arrays.asList(array);
	assertEquals(6,c.size());
	assertTrue(c.contains(zero));
	assertTrue(c.contains(one));
	assertTrue(c.contains(two));
	
	assertTrue(c.contains(A));
	assertTrue(c.contains(B));
	assertTrue(c.contains(C));
    }//end testToArray()

    @Test
    public void testAddAll() {
	Pair<Integer,String> three, D;
	Collection<Pair<Integer,String>> temp = new ArrayList<Pair<Integer,String>>();
	temp.add(three=new Pair<Integer,String>(0,"three"));
	temp.add(D    =new Pair<Integer,String>(1,"D"));
	subject.addAll(temp);
	assertTrue(subject.containsAll(temp));
    }//end testAddAll()
    
    @Test
    public void testFlatCollectionAddAll() {
	Pair<Integer,String> three, D;
	Collection<Pair<Integer,String>> temp = new ArrayList<Pair<Integer,String>>();
	temp.add(three=new Pair<Integer,String>(0,"three"));
	temp.add(D    =new Pair<Integer,String>(1,"D"));
	subject.addAll(temp);
	assertTrue(flatCollection.containsAll(Arrays.asList("three","D")));
    }//end testFlatCollectionAddAll()

    @Test
    public void testIterator() {
	Iterator it = subject.iterator();
	assertNotNull(it);
	for(int i = 0; i<6; i++){
	    assertTrue(it.hasNext());
	    it.next();
	}
	assertFalse(it.hasNext());
    }//end testIterator()

}//end CollectionActionPacker
