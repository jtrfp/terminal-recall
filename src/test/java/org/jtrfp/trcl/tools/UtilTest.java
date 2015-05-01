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
package org.jtrfp.trcl.tools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class UtilTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }
    
    private static void populate(Collection<Integer> dest, int start, int end){
	for(int i=start; i<end; i++)
	    dest.add(i);
    }

    @Test
    public void testRepopulateSameSizeLists() {
	ArrayList<Integer> destList = new ArrayList<Integer>();
	ArrayList<Integer> srcList = new ArrayList<Integer>();
	populate(destList,0,5);
	populate(srcList,5,10);
	Util.repopulate(destList, srcList);
	assertEquals(destList.size(),srcList.size());
	assertTrue(destList.containsAll(srcList));
	assertTrue(srcList.containsAll(destList));
    }
    
    @Test
    public void testRepopulateDifferentSizeListsGT() {
	ArrayList<Integer> destList = new ArrayList<Integer>();
	ArrayList<Integer> srcList = new ArrayList<Integer>();
	populate(destList,5,13);
	populate(srcList,0,5);
	Util.repopulate(destList, srcList);
	assertEquals(destList.size(),srcList.size());
	assertTrue(destList.containsAll(srcList));
	assertTrue(srcList.containsAll(destList));
    }
    
    @Test
    public void testRepopulateDifferentSizeListsLT() {
	ArrayList<Integer> destList = new ArrayList<Integer>();
	ArrayList<Integer> srcList = new ArrayList<Integer>();
	populate(destList,0,5);
	populate(srcList,5,13);
	Util.repopulate(destList, srcList);
	assertEquals(destList.size(),srcList.size());
	assertTrue(destList.containsAll(srcList));
	assertTrue(srcList.containsAll(destList));
    }
    
    @Test
    public void testRepopulateCollections() {
	Collection<Integer> destColl = new HashSet<Integer>();
	Collection<Integer> srcColl  = new HashSet<Integer>();
	populate(destColl,0,5);
	populate(srcColl,5,13);
	Util.repopulate(destColl, srcColl);
	assertEquals(destColl.size(),srcColl.size());
	assertTrue(destColl.containsAll(srcColl));
	assertTrue(srcColl.containsAll(destColl));
    }

}//end UtilTest
