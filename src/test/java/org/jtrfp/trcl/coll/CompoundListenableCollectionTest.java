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

import java.util.ArrayList;
import java.util.Collection;

import junit.framework.TestCase;

import com.ochafik.util.listenable.CollectionEvent;
import com.ochafik.util.listenable.CollectionListener;
import com.ochafik.util.listenable.DefaultListenableCollection;
import com.ochafik.util.listenable.ListenableCollection;

public class CompoundListenableCollectionTest extends TestCase {
    private static final int NUM_COLL_TO_TEST = 5;
    private static final int SIZE_OF_COLL = 5;
    private ListenableCollection<ListenableCollection<Integer>> listenableCollections;
    private CompoundListenableCollection<Integer> compoundListenableCollection;

    protected void setUp() throws Exception {
	super.setUp();
	listenableCollections = 
		new DefaultListenableCollection<ListenableCollection<Integer>>(new ArrayList<ListenableCollection<Integer>>());
	compoundListenableCollection =
		new CompoundListenableCollection<Integer>(listenableCollections);
	for(int i=0; i<NUM_COLL_TO_TEST; i++){
	    ListenableCollection<Integer> thisColl = new DefaultListenableCollection<Integer>(new ArrayList<Integer>());
	    listenableCollections.add(thisColl);
	    for(int j=0; j<SIZE_OF_COLL; j++){
		thisColl.add(i*SIZE_OF_COLL+j);
	    }//end for(SIZE_OF_COLL)
	}//end for(NUM_COLL_TO_TEST)
    }//end setUp()

    protected void tearDown() throws Exception {
	super.tearDown();
	listenableCollections = null;
    }//end tearDown()

    public void testAddCollectionListener() {
	final boolean [] added = new boolean[]{false}, removed =new boolean[]{false};
	compoundListenableCollection.addCollectionListener(new CollectionListener<Integer>(){
	    @Override
	    public void collectionChanged(CollectionEvent<Integer> evt) {
		switch (evt.getType()){
		case ADDED:
		    added[0]=true;
		    break;
		case REMOVED:
		    removed[0]=true;
		    break;
		case UPDATED:
		    break;
		default:
		    break;
		}
	    }});
	assertFalse(added[0]);
	assertFalse(removed[0]);
    }//end testAddCollectionListener()

    public void testRemoveCollectionListener() {
	final boolean [] added = new boolean[]{false}, removed =new boolean[]{false};
	CollectionListener<Integer> listener;
	compoundListenableCollection.addCollectionListener(listener = new CollectionListener<Integer>(){
	    @Override
	    public void collectionChanged(CollectionEvent<Integer> evt) {
		switch (evt.getType()){
		case ADDED:
		    added[0]=true;
		    break;
		case REMOVED:
		    removed[0]=true;
		    break;
		case UPDATED:
		    break;
		default:
		    break;
		}
	    }});
	assertFalse(added[0]);
	assertFalse(removed[0]);
	compoundListenableCollection.removeCollectionListener(listener);
	assertFalse(added[0]);
	assertFalse(removed[0]);
	listenableCollections.iterator().next().remove(Integer.valueOf(0));
	listenableCollections.iterator().next().add(Integer.valueOf(1));
	assertFalse(added[0]);
	assertFalse(removed[0]);
    }//end testRemoveCollectionListener()
    
    public void testClear() {
	compoundListenableCollection.clear();
	assertTrue(compoundListenableCollection.isEmpty());
	assertEquals(0, compoundListenableCollection.size());
    }

    @SuppressWarnings("deprecation") //Deliberately invoking a deprecated API
    public void testAdd() {
	try{compoundListenableCollection.add(new Integer(12345));}
	catch(UnsupportedOperationException e)
	 {return;}
	fail("Exception failed to throw.");
    }

    public void testContains() {
	final int totalVals = NUM_COLL_TO_TEST*SIZE_OF_COLL;
	for(int i=0; i<totalVals;i++)
	    assertTrue(compoundListenableCollection.contains(Integer.valueOf(i)));
	assertFalse(compoundListenableCollection.contains(Integer.valueOf(totalVals)));
	assertFalse(compoundListenableCollection.contains(Integer.valueOf(totalVals+1234)));
	compoundListenableCollection.remove(Integer.valueOf(5));
	assertFalse(compoundListenableCollection.contains(Integer.valueOf(5)));
	listenableCollections.iterator().next().add(5);
	assertTrue(compoundListenableCollection.contains(Integer.valueOf(5)));
	Integer integer = Integer.valueOf(totalVals*3);
	ListenableCollection<Integer> newColl = new DefaultListenableCollection<Integer>(new ArrayList<Integer>());
	listenableCollections.add(newColl);
	assertFalse(compoundListenableCollection.contains(integer));
	newColl.add(integer);
	assertTrue(compoundListenableCollection.contains(integer));
    }//end testContains()

    @SuppressWarnings("deprecation")
    public void testAddAll() {
	try{compoundListenableCollection.add(Integer.valueOf(12345));}
	catch(UnsupportedOperationException e)
	 {return;}
	fail("Exception failed to throw.");
    }

    public void testContainsAll() {
	final int totalVals = NUM_COLL_TO_TEST*SIZE_OF_COLL;
	final Collection<Integer> testVals = new ArrayList<Integer>();
	for(int i=0; i<totalVals; i++)
	    testVals.add(i);
	assertTrue(compoundListenableCollection.containsAll(testVals));
	for(int i=0; i<totalVals; i++)
	    testVals.add(i+totalVals/2);
	assertFalse(compoundListenableCollection.containsAll(testVals));
    }

    public void testSize() {
	assertEquals(NUM_COLL_TO_TEST*SIZE_OF_COLL, compoundListenableCollection.size());
    }

    public void testIsEmpty() {
	assertFalse(compoundListenableCollection.isEmpty());
	for(ListenableCollection<Integer> coll:listenableCollections)
	    coll.clear();
	assertTrue(compoundListenableCollection.isEmpty());
    }

    public void testRemove() {
	assertTrue(compoundListenableCollection.contains(Integer.valueOf(1)));
	compoundListenableCollection.remove(Integer.valueOf(1));
	assertFalse(compoundListenableCollection.contains(Integer.valueOf(1)));
    }

    public void testRemoveAll() {
	final Collection<Integer> removeThese = new ArrayList<Integer>();
	removeThese.add(12);
	removeThese.add(3);
	removeThese.add(1);
	assertTrue(compoundListenableCollection.contains(Integer.valueOf(1)));
	compoundListenableCollection.removeAll(removeThese);
	assertFalse(compoundListenableCollection.contains(Integer.valueOf(1)));
	assertFalse(compoundListenableCollection.contains(Integer.valueOf(3)));
	assertFalse(compoundListenableCollection.contains(Integer.valueOf(12)));
	assertTrue(compoundListenableCollection.contains(Integer.valueOf(7)));
    }//end testRemoveAll

    public void testRetainAll() {
	final Collection<Integer> retainThese = new ArrayList<Integer>();
	retainThese.add(12);
	retainThese.add(3);
	retainThese.add(1);
	assertTrue(compoundListenableCollection.contains(Integer.valueOf(1)));
	compoundListenableCollection.retainAll(retainThese);
	assertTrue(compoundListenableCollection.contains(Integer.valueOf(1)));
	assertTrue(compoundListenableCollection.contains(Integer.valueOf(3)));
	assertTrue(compoundListenableCollection.contains(Integer.valueOf(12)));
	assertFalse(compoundListenableCollection.contains(Integer.valueOf(7)));
    }//end testRetainAll

}//end CompoundListenableCollectionTest
