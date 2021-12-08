/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2016 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/

package org.jtrfp.trcl.img.vq;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class CompositeVectorListNDTest {
    private CompositeVectorListND subject;
    private final VectorListND [] subLists = new VectorListND[4];
    private final double ZERO_ZERO = .10;
    private final double n149_ZERO = .1493;

    @Before
    public void setUp() throws Exception {
	final CompositeVectorListND subject = new CompositeVectorListND();
	subject.setDimensions(new int [] {200,200});
	setSubject(subject);
	for(int i=0; i<getSubLists().length; i++)
	    setupSubList(i);
	
	final VectorListND[] lists = getSubLists();
	Mockito.when(lists[0].componentAt(new int[] {0,0}, 0)).thenReturn(ZERO_ZERO);
	Mockito.when(lists[0].componentAt(new int[] {149,0}, 0)).thenReturn(n149_ZERO);
    }//end setUp()
    
    private void setupSubList(int i){
	final VectorListND vl = Mockito.mock(VectorListND.class);
	Mockito.when(vl.getNumComponentsPerVector()).thenReturn(5);
	Mockito.when(vl.getDimensions()).thenReturn(new int [] {150,150});
	Mockito.when(vl.toString()).thenReturn("Mocked subList #"+i);
	getSubject().getSubLists().add(vl);
	getSubLists()[i] = vl;
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetNumVectors() {
	assertEquals(200*200,getSubject().getNumVectors());
    }

    @Test
    public void testGetNumComponentsPerVector() {
	assertEquals(5,getSubject().getNumComponentsPerVector());
    }

    @Test
    public void testGetDimensions() {
	assertArrayEquals(new int []{200,200},getSubject().getDimensions());
    }

    @Test
    public void testCoordToSublist0_0() {
	assertArrayEquals(new int [] {0,0},getSubject().coordToSublist(new int[]{0,0}));
    }
    
    @Test
    public void testCoordToSublist149_149() {
	assertArrayEquals(new int [] {0,0},getSubject().coordToSublist(new int[]{149,149}));
    }
    
    @Test
    public void testCoordToSublist150_149() {
	assertArrayEquals(new int [] {1,0},getSubject().coordToSublist(new int[]{150,149}));
    }
    
    @Test
    public void testCoordToSublist150_199() {
	assertArrayEquals(new int [] {1,1},getSubject().coordToSublist(new int[]{150,199}));
    }
    
    @Test
    public void testCoordToSublist199_199() {
	assertArrayEquals(new int [] {1,1},getSubject().coordToSublist(new int[]{199,199}));
    }
    
    @Test
    public void testCoordToSublist0_199() {
	assertArrayEquals(new int [] {0,1},getSubject().coordToSublist(new int[]{0,199}));
    }
    
    @Test
    public void testCoordToSublist149_199() {
	assertArrayEquals(new int [] {0,1},getSubject().coordToSublist(new int[]{149,199}));
    }

    @Test
    public void testGetDimsInSublists() {
	assertArrayEquals(new int [] {2,2}, getSubject().getDimsInSublists());
    }

    @Test
    public void testComponentAt_Component0() {
	assertEquals(ZERO_ZERO,getSubject().componentAt(new int[]{0,0}, 0),.001);
    }
    
    @Test
    public void testComponentAt_149_0_Component0() {
	//System.out.println("go");
	assertEquals(n149_ZERO,getSubject().componentAt(new int[]{149,0}, 0),.001);
    }
    //TODO; setComponentAt(...)

    @Test
    public void testGetSubListSize() {
	assertArrayEquals(new int []{150,150},getSubject().getSubListSize());
    }

    @Test
    public void testGetSubLists() {
	final List<VectorListND> sList = getSubject().getSubLists();
	final VectorListND [] tList = getSubLists();
	for(int i = 0; i<tList.length; i++)
	    Assert.assertTrue(sList.contains(tList[i]));
    }

    protected CompositeVectorListND getSubject() {
        return subject;
    }

    protected void setSubject(CompositeVectorListND subject) {
        this.subject = subject;
    }

    protected VectorListND[] getSubLists() {
        return subLists;
    }

}//end CompositeVectorListNDTest()
