/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2012-2022 Chuck Ritola
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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

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
    
    @Test
    public void testNodePathFromUserObjectPathWithRoot() {
	DefaultMutableTreeNode root = new DefaultMutableTreeNode("first");
	DefaultMutableTreeNode child0 = new DefaultMutableTreeNode("second");
	DefaultMutableTreeNode child1 = new DefaultMutableTreeNode("third");
	root.add(child0);
	child0.add(child1);
	final List<DefaultMutableTreeNode> result = Util.nodePathFromUserObjectPath(root, "first", "second", "third");
	assertArrayEquals(result.toArray(), new Object[] {root, child0, child1});
    }
    
    @Test
    public void testNodePathFromUserObjectPathWithRootDistractor() {
	DefaultMutableTreeNode root = new DefaultMutableTreeNode("first");
	DefaultMutableTreeNode child0 = new DefaultMutableTreeNode("second");
	DefaultMutableTreeNode child1 = new DefaultMutableTreeNode("third");
	
	DefaultMutableTreeNode distractor0 = new DefaultMutableTreeNode("distractor0");
	DefaultMutableTreeNode distractor1 = new DefaultMutableTreeNode("distractor1");
	
	root.add(child0);
	root.add(distractor0);
	child0.add(child1);
	distractor0.add(distractor1);
	final List<DefaultMutableTreeNode> result = Util.nodePathFromUserObjectPath(root, "first", "second", "third");
	assertArrayEquals(result.toArray(), new Object[] {root, child0, child1});
	final List<DefaultMutableTreeNode> distractorResult = Util.nodePathFromUserObjectPath(root, "first", "distractor0", "distractor1");
	assertArrayEquals(distractorResult.toArray(), new Object[] {root, distractor0, distractor1});
    }
    
    @Test
    public void testQuantizeZeroEight(){
	assertEquals(0,Util.quantize(0,8),.0000001);
    }
    
    @Test
    public void testQuantizeZeroNegativeOne(){
	assertEquals(0,Util.quantize(0,-1),.0000001);
    }
    
    @Test
    public void testQuantize254Eight(){
	assertEquals(256,Util.quantize(254,8),.0000001);
    }
    
    @Test
    public void testQuantize257Eight(){
	assertEquals(256,Util.quantize(254,8),.0000001);
    }
    
    @Test
    public void testQuantizeNeg254Eight(){
	assertEquals(-256,Util.quantize(-254,8),.0000001);
    }
    
    @Test
    public void testQuantizeNeg257Eight(){
	assertEquals(-256,Util.quantize(-257,8),.0000001);
    }

}//end UtilTest
