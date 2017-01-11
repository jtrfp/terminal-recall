/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2017 Chuck Ritola
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

import static org.junit.Assert.*;

import org.junit.Test;

public class CollectionMapTest {

    @Test
    public void testCollectionMap() {
	new CollectionMap();
    }

    @Test
    public void testPutInCollection() {
	final CollectionMap<Object,Object> subject = new CollectionMap<Object,Object>();
	final Object tObj = new Object();
	assertTrue(subject.get("test").isEmpty());
	subject.putInCollection("test", tObj);
	assertFalse(subject.get("test").isEmpty());
    }
    
    @Test
    public void testRemoveFromCollection(){
	final CollectionMap<Object,Object> subject = new CollectionMap<Object,Object>();
	final Object tObj = new Object();
	subject.putInCollection("test", tObj);
	assertFalse(subject.get("test").isEmpty());
	subject.removeFromCollection("test", tObj);
	assertTrue(subject.get("test").isEmpty());
    }

}//end CollectionMapTest
