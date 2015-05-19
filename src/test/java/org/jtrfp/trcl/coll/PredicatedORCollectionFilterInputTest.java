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
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PredicatedORCollectionFilterInputTest extends PredicatedORCollectionActionFilterTest{
    protected Collection<Color> input;

    @Before
    public void setUp() throws Exception {
	super.setUp();
	this.input = subject.input;
    }

    @After
    public void tearDown() throws Exception {
	super.tearDown();
	this.input = null;
    }

    @Test
    public void testContainsAll() {
	assertTrue(input.containsAll(Arrays.asList(PredicatedORCollectionActionFilterTest.entries)));
    }
    
    @Test
    public void testIsEmpty(){
	assertFalse(input.isEmpty());
    }
    
    @Test
    public void testClear(){
	input.clear();
	assertTrue(input.isEmpty());
    }
    
    @Test
    public void testSize(){
	assertEquals(5,input.size());
    }
    
    @Test
    public void testContains(){
	assertTrue (input.contains(Color.blue));
	assertFalse(input.contains(Color.black));
    }

}//end PredicatedORListActionFilterInputTest
