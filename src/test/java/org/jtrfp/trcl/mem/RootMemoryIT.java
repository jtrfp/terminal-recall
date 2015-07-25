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
package org.jtrfp.trcl.mem;

import static org.junit.Assert.assertEquals;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.jtrfp.trcl.MatrixWindow;
import org.jtrfp.trcl.TRIT;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class RootMemoryIT extends TRIT{
    protected ByteBuffer memDump;
    
    @Before
    public void setUp() throws Exception {
	super.setUp();
	memDump = ByteBuffer.allocate(tr.gpu.get().memoryManager.get().getMaxCapacityInBytes());
	memDump.order(ByteOrder.nativeOrder());
    }

    @After
    public void tearDown() throws Exception {
	super.tearDown();
    }
    
    private void dumpMem() {
	tr.gpu.get().memoryManager.get().dumpAllGPUMemTo(memDump);
    }
    
    @Test
    public void verifyNotEmptyTest(){//Even starting 'empty' there are objects set.
	dumpMem();
	int accumulator = 0;
	while(memDump.hasRemaining())
	    accumulator+=memDump.get()==0?0:1;
	Assert.assertNotEquals(0,accumulator);
    }

    @Test
    public void matrixWindowTest() throws Exception {
	final MatrixWindow mw = tr.gpu.get().matrixWindow.get();
	final int id = mw.create();
	final int addressBytes = mw.getPhysicalAddressInBytes(id).intValue();
	double [] doubles = new double[16];
	for(int i=0; i<16; i++)
	    doubles[i]=i;
	mw.matrix.set(id, doubles);
	dumpMem();
	for(int i=0; i<16; i++){
	    memDump.position(addressBytes+i*4);
	    assertEquals(memDump.getFloat(),doubles[i],.1);
	}
    }//end testMatrixWindow()
    
}//end MemoryWindowIT
