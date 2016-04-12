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

package org.jtrfp.trcl.math;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class Mat4x4Test {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testIdentity() {
	final double [] dest = new double[16];
	Mat4x4.identity(dest);
	
	assertEquals(1,dest[0],.1);
	assertEquals(0,dest[1],.1);
	assertEquals(0,dest[2],.1);
	assertEquals(0,dest[3],.1);
	
	assertEquals(0,dest[4],.1);
	assertEquals(1,dest[5],.1);
	assertEquals(0,dest[6],.1);
	assertEquals(0,dest[7],.1);
	
	assertEquals(0,dest[8],.1);
	assertEquals(0,dest[9],.1);
	assertEquals(1,dest[10],.1);
	assertEquals(0,dest[11],.1);
	
	assertEquals(dest[12],0,.1);
	assertEquals(dest[13],0,.1);
	assertEquals(dest[14],0,.1);
	assertEquals(dest[15],1,.1);
    }//end testIdentity()
    
    @Test
    public void testMul4x42VectColumnMajorIdentity() {
	final double [] dest = new double[16];
	final double [] result = new double[4];
	Mat4x4.identity(dest);
	Mat4x4.mul4x42VectColumnMajor(dest, new double [] {1,2,3,4} , result);
	assertArrayEquals(new double[]{1,2,3,4}, result, .01);
    }
    
    @Test
    public void testMul4x42VectColumnMajorTranslation() {
	final double [] dest = new double[] {// 2,3,4,1
	    1, 0, 0, 0,
	    0, 1, 0, 0,
	    0, 0, 1, 0,
	    2, 3, 4, 1
	};
	final double [] result = new double[4];
	Mat4x4.mul4x42VectColumnMajor(dest, new double [] {0,0,0,1} , result);
	assertArrayEquals(new double[]{2,3,4,1}, result, .01);
    }
    
    @Test
    public void testMul4x42VectRowMajorIdentity() {
	final double [] dest = new double[16];
	final double [] result = new double[4];
	Mat4x4.identity(dest);
	Mat4x4.mul4x42VectRowMajor(dest, new double [] {1,2,3,4} , result);
	assertArrayEquals(new double[]{1,2,3,4}, result, .01);
    }

}//end Mat4x4Test
