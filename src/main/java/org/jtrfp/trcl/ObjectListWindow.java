/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2012-2014 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/
package org.jtrfp.trcl;

import org.jtrfp.trcl.gpu.GPU;
import org.jtrfp.trcl.gpu.Renderer;
import org.jtrfp.trcl.mem.MemoryWindow;

public class ObjectListWindow extends MemoryWindow {
    public ObjectListWindow(GPU gpu) {
	init(gpu,"ObjectListWindow");
    }// end constructor
    
    public ObjectListWindow(){}

    public final IntArrayVariable opaqueIDs = new IntArrayVariable(
	    Renderer.NUM_BLOCKS_PER_PASS);
    
    public final ByteArrayVariable pageFiller = new ByteArrayVariable(2048);

    public static final int OBJECT_LIST_SIZE_BYTES_PER_PASS = Renderer.NUM_BLOCKS_PER_PASS * 4;
}// end GlobalObjectList
