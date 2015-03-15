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
import org.junit.After;
import org.junit.Before;

public class PartitionedIndexPoolImplTest extends PartitionedIndexPoolTest {
    
    @Before
    protected void setUp() throws Exception {
	subject = newSubject();
    }
    
    @After
    protected void tearDown() throws Exception {
    }

    @Override
    protected PartitionedIndexPool<TestObject> newSubject() {
	return new PartitionedIndexPoolImpl<TestObject>();
    }

}
