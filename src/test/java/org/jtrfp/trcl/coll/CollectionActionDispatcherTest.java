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
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CollectionActionDispatcherTest {
    private CollectionActionDispatcher<String> subject;
    private ArrayList<String> cache;

    @Before
    public void setUp() throws Exception {
	cache   = new ArrayList<String>();
	subject = new CollectionActionDispatcher<String>(cache);
	subject.add("first");
	subject.add("second");
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testEqualsObject() {
	assertTrue(cache.equals(cache));//Control var
	assertTrue(subject.equals(subject));
    }
    
    @Test
    public void testSize(){
	assertEquals(2,subject.size());
    }

}//end CollectionActionDispatcherTest
