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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CachedAdapterTest {
    CachedAdapter<UObject,VObject> subject;
    
    class UObject{}
    class VObject{}

    @Before
    public void setUp() throws Exception {
	subject = new CachedAdapter<UObject,VObject>(){

	    @Override
	    public VObject _adapt(UObject value) {
		return new VObject();
	    }

	    @Override
	    public UObject _reAdapt(VObject value) {
		return new UObject();
	    }};
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void test_adapt() {
	UObject uo0 = new UObject();
	VObject vo0 = subject.adapt(uo0);
	UObject uo1 = new UObject();
	VObject vo1 = subject.adapt(uo1);
	assertNotNull(uo0);
	assertNotNull(vo0);
	assertNotNull(uo1);
	assertNotNull(vo1);
    }
    
    @Test
    public void test_adaptNullPointerException() {
	try{subject.adapt(null);}
	catch(NullPointerException e){return;}
	fail("Failed to throw NullPointerException.");
    }

    @Test
    public void test_reAdapt() {
	UObject uo0 = new UObject();
	VObject vo0 = subject.adapt(uo0);
	UObject uo1 = new UObject();
	VObject vo1 = subject.adapt(uo1);
	
	assertEquals(uo0,subject.reAdapt(vo0));
	assertEquals(uo1,subject.reAdapt(vo1));
    }
    
    @Test
    public void test_reAdaptNullPointerException() {
	try{subject.reAdapt(null);}
	catch(NullPointerException e){return;}
	fail("Failed to throw NullPointerException.");
    }

}//end CachedAdapterTest
