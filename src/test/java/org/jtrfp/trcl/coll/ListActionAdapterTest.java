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

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ochafik.util.Adapter;

public class ListActionAdapterTest {
    
    private ListActionAdapter<Integer,String> subject;

    @Before
    public void setUp() throws Exception {
	subject = new ListActionAdapter<Integer,String>(new Adapter<Integer,String>(){
	    @Override
	    public String adapt(Integer value) {
		return value.toString();
	    }

	    @Override
	    public Integer reAdapt(String value) {
		return null;
	    }});
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetOutput() {
	final List<String> output;
	assertNotNull(output = subject.getOutput());
	subject.add(1);
	subject.add(2);
	assertEquals("1",output.get(0));
    }

    @Test
    public void testAddIN() {
	assertTrue(subject.add(0));
	assertEquals("0",subject.getOutput().get(0));
    }

    @Test
    public void testAddIntIN() {
	subject.add(0);
	subject.add(1);
	subject.add(2);
	subject.add(1,5);
	assertEquals("5",subject.getOutput().get(1));
    }

    @Test
    public void testAddAllCollectionOfQextendsIN() {
	final List<Integer> c = new ArrayList<Integer>();
	c.add(0);
	c.add(1);
	c.add(2);
	subject.addAll(c);
	assertEquals("2",subject.getOutput().get(2));
    }

    @Test
    public void testAddAllIntCollectionOfQextendsIN() {
	final List<Integer> c = new ArrayList<Integer>();
	c.add(0);
	c.add(1);
	c.add(2);
	subject.add(3);
	subject.add(4);
	subject.addAll(0,c);
	assertEquals("0",subject.getOutput().get(0));
    }

    @Test
    public void testClear() {
	subject.add(0);
	subject.add(1);
	assertEquals(2,subject.getOutput().size());
	subject.clear();
	assertEquals(0,subject.getOutput().size());
    }

    @Test
    public void testContains() {
	final ListActionDispatcher<String> out = subject.getOutput();
	subject.add(0);
	subject.add(1);
	assertTrue(out.contains("0"));
	subject.clear();
	assertFalse(out.contains("0"));
    }

    @Test
    public void testContainsAll() {
	final List<String> coll = new ArrayList<String>();
	coll.add("0");
	coll.add("1");
	final ListActionDispatcher<String> out = subject.getOutput();
	subject.add(0);
	subject.add(1);
	assertTrue(out.containsAll(coll));
	subject.clear();
	assertFalse(out.containsAll(coll));
    }

    @Test
    public void testGet() {
	final List<String> coll = new ArrayList<String>();
	coll.add("0");
	coll.add("1");
	final ListActionDispatcher<String> out = subject.getOutput();
	subject.add(0);
	subject.add(1);
	assertEquals("0",out.get(0));
	assertEquals("1",out.get(1));
    }

    @Test
    public void testIndexOf() {
	final ListActionDispatcher<String> out = subject.getOutput();
	subject.add(0);
	subject.add(1);
	assertEquals(1, out.indexOf("1"));
    }

    @Test
    public void testIsEmpty() {
	final ListActionDispatcher<String> out = subject.getOutput();
	assertTrue(out.isEmpty());
	subject.add(0);
	subject.add(1);
	assertFalse(out.isEmpty());
    }

    @Test
    public void testRemoveObject() {
	final ListActionDispatcher<String> out = subject.getOutput();
	subject.add(0);
	subject.add(1);
	assertTrue(out.contains("1"));
	assertTrue(subject.remove(Integer.valueOf(1)));
	assertFalse(out.contains("1"));
    }

    @Test
    public void testRemoveInt() {
	final ListActionDispatcher<String> out = subject.getOutput();
	subject.add(0);
	subject.add(1);
	assertTrue(out.contains("1"));
	assertEquals(Integer.valueOf(1),subject.remove(1));
	assertFalse(out.contains("1"));
    }

    @Test
    public void testRemoveAll() {
	final List<Integer> coll = new ArrayList<Integer>();
	coll.add(0); coll.add(1);
	final ListActionDispatcher<String> out = subject.getOutput();
	subject.add(0);
	subject.add(1);
	subject.add(0);
	subject.add(1);
	assertEquals(4,out.size());
	assertTrue(subject.removeAll(coll));
	assertEquals(0,out.size());
    }

    @Test
    public void testRetainAll() {
	final List<Integer> coll = new ArrayList<Integer>();
	coll.add(0);
	final ListActionDispatcher<String> out = subject.getOutput();
	subject.add(0);
	subject.add(1);
	subject.add(0);
	subject.add(1);
	assertEquals(4,out.size());
	assertTrue(subject.retainAll(coll));
	assertEquals(2,out.size());
    }

    @Test
    public void testSet() {
	final ListActionDispatcher<String> out = subject.getOutput();
	subject.add(0);
	subject.add(1);
	subject.add(0);
	subject.add(1);
	assertEquals(4,out.size());
	assertEquals(Integer.valueOf(1),subject.set(1,3));
	assertEquals("3",out.get(1));
    }

    @Test
    public void testSize() {
	final ListActionDispatcher<String> out = subject.getOutput();
	subject.add(0);
	subject.add(1);
	subject.add(0);
	subject.add(1);
	assertEquals(4,out.size());
    }

}//end ListActionAdapterTest
