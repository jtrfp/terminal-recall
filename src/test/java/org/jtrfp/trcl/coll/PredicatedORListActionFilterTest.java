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

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.collections4.Predicate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PredicatedORListActionFilterTest {
    protected static final Color [] entries = new Color[]{Color.red,Color.green,Color.blue,Color.yellow,Color.pink};
    protected PredicatedORListActionFilter<Color> subject;
    protected Collection<Color>                   delegate;

    @Before
    public void setUp() throws Exception {
	delegate= new ArrayList<Color>();
	subject = new PredicatedORListActionFilter<Color>(delegate);
	//Populate
	subject.input.addAll(Arrays.asList(entries));
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testPredicatedORListActionFilter() {
	assertNotNull(subject);
	assertNotNull(delegate);
	assertNotNull(entries);
	assertTrue   (subject.isEmpty());
	assertFalse  (subject.input.isEmpty());
	assertTrue   (delegate.isEmpty());
    }

    @Test
    public void testAdd() {
	assertTrue(subject.add(new PrimaryColorPredicate()));
    }

    @Test
    public void testAddAll() {
	assertTrue(subject.addAll(Arrays.asList(new PrimaryColorPredicate(), new PrimaryColorPredicate())));
	assertEquals(2,subject.size());
    }

    @Test
    public void testClear() {
	subject.addAll(Arrays.asList(new PrimaryColorPredicate(), new PrimaryColorPredicate()));
	assertFalse(subject.isEmpty());
	subject.clear();
	assertTrue(subject.isEmpty());
    }

    @Test
    public void testContains() {
	PrimaryColorPredicate pcp;
	subject.addAll(Arrays.asList(pcp = new PrimaryColorPredicate(), new PrimaryColorPredicate()));
	assertTrue(subject.contains(pcp));
	assertFalse(subject.contains(new PrimaryColorPredicate()));
	
    }

    @Test
    public void testContainsAll() {
	PrimaryColorPredicate [] pcp = new PrimaryColorPredicate[2];
	subject.addAll(Arrays.asList(pcp[0] = new PrimaryColorPredicate(), pcp[1] = new PrimaryColorPredicate()));
	assertTrue(subject.containsAll(Arrays.asList(pcp)));
    }

    @Test
    public void testIsEmpty() {
	assertTrue(subject.isEmpty());
	subject.add(new PrimaryColorPredicate());
	assertFalse(subject.isEmpty());
    }

    @Test
    public void testIterator() {
	subject.addAll(Arrays.asList(new PrimaryColorPredicate(), new PrimaryColorPredicate()));
	final Iterator<Predicate<Color>> pI = subject.iterator();
	assertTrue(pI.hasNext()); pI.next();
	try{pI.remove();fail("remove() failed to throw expected exception.");}
	 catch(UnsupportedOperationException ex){}
	assertTrue(pI.hasNext()); pI.next();
	assertFalse(pI.hasNext());
    }

    @Test
    public void testRemove() {
	PrimaryColorPredicate [] pcp = new PrimaryColorPredicate[2];
	subject.addAll(Arrays.asList(pcp[0] = new PrimaryColorPredicate(), pcp[1] = new PrimaryColorPredicate()));
	assertTrue(subject.remove(pcp[0]));
	assertEquals(1,subject.size());
	assertFalse(subject.contains(pcp[0]));
    }

    @Test
    public void testRemoveAll() {
	PrimaryColorPredicate [] pcp = new PrimaryColorPredicate[2];
	subject.addAll(Arrays.asList(pcp[0] = new PrimaryColorPredicate(), pcp[1] = new PrimaryColorPredicate()));
	assertTrue(subject.removeAll(Arrays.asList(pcp)));
	assertTrue(subject.isEmpty());
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testRetainAll() {
	PrimaryColorPredicate [] pcp = new PrimaryColorPredicate[2];
	subject.addAll(Arrays.asList(pcp[0] = new PrimaryColorPredicate(), pcp[1] = new PrimaryColorPredicate()));
	subject.retainAll(Arrays.asList(pcp[0]));
    }

    @Test
    public void testSize() {
	assertEquals(0,subject.size());
	subject.addAll(Arrays.asList(new PrimaryColorPredicate(), new PrimaryColorPredicate()));
	assertEquals(2,subject.size());
    }

    @Test
    public void testToArray() {
	PrimaryColorPredicate [] pcp = new PrimaryColorPredicate[2];
	subject.addAll(Arrays.asList(pcp[0] = new PrimaryColorPredicate(), pcp[1] = new PrimaryColorPredicate()));
	Object [] dest = subject.toArray();
	assertNotNull(dest);
	assertEquals(2,dest.length);
	Collection<Object> coll = Arrays.asList(dest);
	assertTrue(coll.contains(pcp[0]));
	assertTrue(coll.contains(pcp[1]));
    }

    @Test
    public void testToArrayTArray() {
	PrimaryColorPredicate [] pcp = new PrimaryColorPredicate[2];
	subject.addAll(Arrays.asList(pcp[0] = new PrimaryColorPredicate(), pcp[1] = new PrimaryColorPredicate()));
	Object [] dest = subject.toArray(new PrimaryColorPredicate[1]);
	assertNotNull(dest);
	assertEquals(2,dest.length);
	Collection<Object> coll = Arrays.asList(dest);
	assertTrue(coll.contains(pcp[0]));
	assertTrue(coll.contains(pcp[1]));
    }
    
    protected static class PrimaryColorPredicate implements Predicate<Color>{
	@Override
	public boolean evaluate(Color object) {
	    return object.equals(Color.red) || object.equals(Color.blue) || object.equals(Color.yellow);
	}
    }//end PrimaryColorPredicate
}//end PredicatedORListActionFilterTest
