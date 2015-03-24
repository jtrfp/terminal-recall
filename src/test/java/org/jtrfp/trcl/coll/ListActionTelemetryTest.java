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

public class ListActionTelemetryTest {
    private ListActionTelemetry<String> subject;

    @Before
    public void setUp() throws Exception {
	subject = new ListActionTelemetry<String>();
    }

    @After
    public void tearDown() throws Exception {
	subject = null;
    }

    @Test
    public void testIsModified() {
	assertTrue(subject.isModified());
	subject.drainListStateTo(null);
	assertFalse(subject.isModified());
	subject.add("first");
	assertTrue(subject.isModified());
	subject.drainListStateTo(null);
	assertFalse(subject.isModified());
	subject.add("second");
    }

    @Test
    public void testDrainListStateTo() {
	subject.add("first");
	subject.add("second");
	final List<String> dest = new ArrayList<String>();
	assertNotNull(subject.drainListStateTo(dest));
	assertEquals("first",dest.get(0));
	assertEquals("second",dest.get(1));
    }

    @Test
    public void testAddIntE() {
	subject.add("first");
	subject.add("second");
	subject.add(1,"third");
	assertTrue(subject.isModified());
    }

    @Test
    public void testAddAllCollectionOfQextendsE() {
	List<String> src = new ArrayList<String>();
	src.add("first");
	src.add("second");
	subject.addAll(src);
	assertTrue(subject.isModified());
    }

    @Test
    public void testAddAllIntCollectionOfQextendsE() {
	subject.add("third");
	subject.add("fourth");
	List<String> src = new ArrayList<String>();
	src.add("first");
	src.add("second");
	subject.addAll(0,src);
	assertTrue(subject.isModified());
    }

    @Test
    public void testClear() {
	subject.add("third");
	subject.add("fourth");
	subject.clear();
	assertTrue(subject.isModified()); //Technically should be false but in spec, true
    }

    @Test
    public void testRemoveInt() {
	subject.add("first");
	subject.add("second");
	subject.add("third");
	subject.add("fourth");
	subject.drainListStateTo(null);
	assertFalse(subject.isModified());
	subject.remove(1);
	assertTrue(subject.isModified());
    }

    @Test
    public void testRemoveObject() {
	subject.add("first");
	subject.add("second");
	subject.add("third");
	subject.add("fourth");
	subject.drainListStateTo(null);
	assertFalse(subject.isModified());
	subject.remove("second");
	assertTrue(subject.isModified());
    }

    @Test
    public void testRemoveAll() {
	List<String> rem = new ArrayList<String>();
	rem.add("first");
	rem.add("second");
	
	subject.add("first");
	subject.add("second");
	subject.add("third");
	subject.add("fourth");
	subject.drainListStateTo(null);
	assertFalse(subject.isModified());
	subject.removeAll(rem);
	assertTrue(subject.isModified());
    }

    @Test
    public void testRetainAll() {
	List<String> rem = new ArrayList<String>();
	rem.add("first");
	rem.add("second");
	
	subject.add("first");
	subject.add("second");
	subject.add("third");
	subject.add("fourth");
	subject.drainListStateTo(null);
	assertFalse(subject.isModified());
	subject.retainAll(rem);
	assertTrue(subject.isModified());
    }

    @Test
    public void testSet() {
	subject.add("first");
	subject.add("second");
	subject.add("third");
	subject.drainListStateTo(null);
	assertFalse(subject.isModified());
	subject.set(1, "SECOND");
	assertTrue(subject.isModified());
    }

}//end ListActionTelemetryTest
