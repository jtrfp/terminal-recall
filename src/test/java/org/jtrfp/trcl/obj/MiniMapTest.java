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

package org.jtrfp.trcl.obj;

import java.awt.Dimension;

import org.jtrfp.trcl.gpu.SettableTexture;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MiniMapTest {
    private MiniMap subject;

    @Before
    public void setUp() throws Exception {
	//final TR tr = Mockito.mock(TR.class);//TODO: Refactor WorldObject to take GPUResourceFinalizer
	final MiniMap mm = new MiniMap(null);
	mm.setModelSize(new double[]{.1,.1});
	mm.setDiameterInTiles(16);
	setSubject(mm);
    }

    @After
    public void tearDown() throws Exception {
    }
    
    @Test
    public void testInCircleRange8_8(){
	Assert.assertTrue(getSubject().isInCircleRange(8, 8));
    }
    
    @Test
    public void testInCircleRange15_8(){
	Assert.assertTrue(getSubject().isInCircleRange(15, 8));
    }
    
    @Test
    public void testInCircleRange15_15(){
	Assert.assertFalse(getSubject().isInCircleRange(15, 15));
    }
    
    @Test
    public void testInCircleRange10_10(){
	Assert.assertTrue(getSubject().isInCircleRange(10, 10));
    }

    @Test
    public void testConfigureCircle_0_0() {
	final MiniMap mm = getSubject();
	mm.configureCircle();
	final SettableTexture [][] grid = mm.getGrid();
	Assert.assertNull(grid[0][0]);
    }
    
    @Test
    public void testConfigureCircle_8_8() {
	final MiniMap mm = getSubject();
	mm.configureCircle();
	final SettableTexture [][] grid = mm.getGrid();
	Assert.assertNotNull(grid[8][8]);
    }
    
    @Test
    public void testConfigureCircle_15_15() {
	final MiniMap mm = getSubject();
	mm.configureCircle();
	final SettableTexture [][] grid = mm.getGrid();
	Assert.assertNull(grid[15][15]);
    }

    @Test
    public void testConfigureGridAtNotNull_8_8_8_8() {
	final MiniMap mm = getSubject();
	mm.configureGridAt(8, 8, 16);
	Assert.assertNotNull(mm.getGrid()[8][8]);
    }
    
    @Test
    public void testConfigureGridAtNotNull_8_7_8_7() {
	final MiniMap mm = getSubject();
	mm.configureGridAt(8, 7, 16);
	Assert.assertNotNull(mm.getGrid()[8][7]);
    }
    
    @Test
    public void testConfigureGridAtNull_8_8_0_0() {
	final MiniMap mm = getSubject();
	mm.configureGridAt(8, 8, 16);
	Assert.assertNull(mm.getGrid()[0][0]);
    }
    
    @Test
    public void testConfigureGridAtNotNull_1_15_1_15() {
	final MiniMap mm = getSubject();
	mm.configureGridAt(0, 15, 1);
	Assert.assertNotNull(mm.getGrid()[8][15]);
    }

    @Test
    public void testDiameterAtPctY0_5() {
	final MiniMap mm = getSubject();
	Assert.assertEquals(mm.getDiameterInTiles(), mm.diameterAtPctY(.5));
    }
    
    @Test
    public void testDiameterAtPctY0() {
	final MiniMap mm = getSubject();
	Assert.assertEquals(0, mm.diameterAtPctY(0));
    }
    
    @Test
    public void testDiameterAtPctY0_25() {
	final MiniMap mm = getSubject();
	Assert.assertEquals(Math.sin(Math.PI*.25)*16, mm.diameterAtPctY(.25),.8);
    }
    
    @Test
    public void testGetHalfwayPoint(){
	Assert.assertEquals(8, getSubject().getHalfwayPoint(),.1);
    }

    @Test
    public void testGetGrid() {
	Assert.assertNotNull(getSubject().getGrid());
    }

    @Test
    public void testSetGrid() {
	final SettableTexture [][] st = new SettableTexture[8][8];
	getSubject().setGrid(st);
	Assert.assertEquals((Object)st,(Object)getSubject().getGrid());
    }

    public MiniMap getSubject() {
        return subject;
    }

    public void setSubject(MiniMap subject) {
        this.subject = subject;
    }

}//end MiniMapTest
