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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.util.Arrays;

import org.apache.commons.collections4.functors.TruePredicate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PredicatedORCollectionActionFilterDelegateTest extends
	PredicatedORCollectionActionFilterTest {

    @Before
    public void setUp() throws Exception {
	super.setUp();
    }
    
    private void populatePCPredicate(){
	subject.add(new PrimaryColorPredicate());
    }
    
    @SuppressWarnings("unchecked")
    private void populateTruePredicate(){
	subject.add(TruePredicate.INSTANCE);
    }

    @After
    public void tearDown() throws Exception {
	super.tearDown();
    }

    @Test
    public void testContains() {
	assertFalse(delegate.contains(Color.blue));
	populatePCPredicate();
	assertTrue(delegate.contains(Color.blue));
	assertFalse(delegate.contains(Color.black));
	populateTruePredicate();
	assertTrue(delegate.contains(Color.blue));
	assertTrue(delegate.contains(Color.green));
	subject.clear();
	assertFalse(delegate.contains(Color.blue));
	assertFalse(delegate.contains(Color.black));
	populateTruePredicate();
	assertTrue(delegate.contains(Color.blue));
	assertTrue(delegate.contains(Color.green));
    }
    
    @Test
    public void testContainsAll(){
	assertFalse(delegate.containsAll(Arrays.asList(Color.blue,Color.red,Color.yellow)));
	assertFalse(delegate.containsAll(Arrays.asList(Color.black,Color.blue)));
	populatePCPredicate();
	assertTrue(delegate.containsAll(Arrays.asList(Color.blue,Color.red,Color.yellow)));
	assertFalse(delegate.containsAll(Arrays.asList(Color.black,Color.blue)));
	populateTruePredicate();
	assertTrue(delegate.containsAll(Arrays.asList(Color.blue,Color.red,Color.yellow,Color.green)));
	assertFalse(delegate.containsAll(Arrays.asList(Color.black,Color.blue)));
	subject.clear();
	assertFalse(delegate.containsAll(Arrays.asList(Color.blue,Color.red,Color.yellow)));
	assertFalse(delegate.containsAll(Arrays.asList(Color.black,Color.blue)));
	populateTruePredicate();
	assertTrue(delegate.containsAll(Arrays.asList(Color.blue,Color.red,Color.yellow,Color.green)));
	assertFalse(delegate.containsAll(Arrays.asList(Color.black,Color.blue)));
    }
    
    @Test
    public void testClear(){
	populatePCPredicate();
	subject.clear();
	assertFalse(delegate.containsAll(Arrays.asList(Color.blue,Color.red,Color.yellow)));
	assertFalse(delegate.containsAll(Arrays.asList(Color.black,Color.blue)));
	populatePCPredicate();
	assertTrue(delegate.containsAll(Arrays.asList(Color.blue,Color.red,Color.yellow)));
	assertFalse(delegate.containsAll(Arrays.asList(Color.black,Color.blue)));
    }

}//end PredicatedORListActionDispatcherOutputTest
