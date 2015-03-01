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

package org.jtrfp.trcl.pool;

import junit.framework.Assert;
import junit.framework.TestCase;

public class CachedObjectFactoryTest extends TestCase {

    protected void setUp() throws Exception {
	super.setUp();
    }

    public void testGet() {
	final boolean[] generated = new boolean[] { false };
	CachedObjectFactory<String, String> subject = new CachedObjectFactory<String, String>() {
	    @Override
	    protected String generate(String key) {
		generated[0] = true;
		return key.toUpperCase();
	    }
	};
	final String[] ins  = new String[] { "first", "second", "third" };
	final String[] outs = new String[] { "FIRST", "SECOND", "THIRD" };
	for (int i = 0; i < ins.length; i++) {
	    final String in  = ins[i];
	    final String out = outs[i];
	    Assert.assertTrue(subject.get(in).contentEquals(out));
	    Assert.assertTrue(generated[0]);
	    
	    generated[0] = false;
	    
	    Assert.assertTrue(subject.get(in).contentEquals(out));
	    Assert.assertFalse(generated[0]);
	}//end for(ins.length)
    }// end testGet()

}//end CachedObjectFactoryTest
