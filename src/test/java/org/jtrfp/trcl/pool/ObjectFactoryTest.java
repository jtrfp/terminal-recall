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

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.junit.Before;

import com.ochafik.util.Adapter;

public class ObjectFactoryTest extends TestCase {
    boolean [] generated;
    ObjectFactory<String, String> subject;
    private static final String[] ins  = new String[] { "first", "second", "third" };
    private static final String[] outs = new String[] { "FIRST", "SECOND", "THIRD" };
    
    @Before
    public void setUp(){
	generated = new boolean[] { false };
	subject = new ObjectFactory<String, String>(new HashMap<String,String>(),new Adapter<String,String>(){
	    @Override
	    public String adapt(String value) {
		generated[0] = true;
		return value.toUpperCase();
	    }

	    @Override
	    public String reAdapt(String value) {
		// TODO Auto-generated method stub
		return null;
	    }});
    }//end setUp()

    public void testGet() {
	for (int i = 0; i < ins.length; i++) {
	    final String in  = ins[i];
	    final String out = outs[i];
	    assertTrue(subject.get(in).contentEquals(out));
	    assertTrue(generated[0]);
	    
	    generated[0] = false;
	    
	    assertTrue(subject.get(in).contentEquals(out));
	    assertFalse(generated[0]);
	}//end for(ins.length)
    }// end testGet()
    
    public void testPeek(){
	for (int i = 0; i < ins.length; i++) {
	    final String in  = ins[i];
	    final String out = outs[i];
	    subject.get(in);
	    assertTrue(subject.peek(in).contentEquals(out));
	}//end for(length)
	assertNull(subject.peek("doesn't exist"));
    }//end testPeek()
    
    public void testGetMap(){
	Map<String,String> map;
	assertNotNull(map = subject.getMap());
	for (int i = 0; i < ins.length; i++) {
	    final String in  = ins[i];
	    subject.get(in);
	    assertTrue(map.containsKey(ins[i]));
	}//end for(length)
	assertFalse(map.containsKey("doesn't exist"));
    }//end testGetMap()

}//end CachedObjectFactoryTest
