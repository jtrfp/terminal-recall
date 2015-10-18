/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2015 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/
package org.jtrfp.trcl.file;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.util.List;

import org.jtrfp.trcl.file.PODINIFile.PODEntry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PODINIFileTest {
    PODINIFile subject;

    @Before
    public void setUp() throws Exception {
	//Load the test file
	InputStream is = null;
	try{is = PODINIFileTest.class.getResourceAsStream("/test_pod.ini");
	 assertNotNull(is);
	 subject = new PODINIFile(is);
	 assertNotNull(subject);}
	finally{if(is!=null)is.close();}
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetNumPODEntries() {
	final Integer num = subject.getNumPODEntries();
	assertNotNull(num);
	assertEquals(3,(int)num);
    }

    @Test
    public void testGetPodEntries() throws Exception {
	List<PODEntry> entries = subject.getPodEntries();
	assertNotNull(entries);
	assertEquals(3,entries.size());
	assertEquals("furyse.pod",entries.get(0).getPodName());
	assertEquals("fury.pod",entries.get(1).getPodName());
	assertEquals("startup.pod",entries.get(2).getPodName());
    }

}//end PODINIFileTest
