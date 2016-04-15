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

public class ColumnMajorTranslationMatrixFactoryTest {
    private ColumnMajorTranslationMatrixFactory subject;
    
    protected ColumnMajorTranslationMatrixFactory getSubject(){
	return subject;
    }

    @Before
    public void setUp() throws Exception {
	subject = new ColumnMajorTranslationMatrixFactory();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testApplyTranslationMatrix000() {
	final double [] dest = new double[16];
	final double [] ref  = new double[16];
	Mat4x4.identity(dest);
	Mat4x4.identity(ref);
	//Expect no change
	getSubject().applyTranslationMatrix(new double[]{0,0,0}, dest);
	assertArrayEquals(ref,dest,.01);
    }//end testApplyTranslationMatrix000
    
    @Test
    public void testApplyTranslationMatrix003() {
	final double [] dest = new double[16];
	final double [] vect = new double[4];
	Mat4x4.identity(dest);
	//Expect no change
	getSubject().applyTranslationMatrix(new double[]{0,0,3}  , dest);
	Mat4x4.mul4x42VectColumnMajor(dest, new double[]{0,0,0,1}, vect);
	//W COLUMN
	assertEquals(0,dest[12],.001);
	assertEquals(0,dest[13],.001);
	assertEquals(3,dest[14],.001);
	assertEquals(1,dest[15],.001);
    }//end testApplyTranslationMatrix001

}//end ColumnMajorTranslationMatrixFactoryTest
