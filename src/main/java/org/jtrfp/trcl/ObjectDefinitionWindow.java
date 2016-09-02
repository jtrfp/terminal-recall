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
import org.jtrfp.trcl.mem.MemoryWindow;

public final class ObjectDefinitionWindow extends MemoryWindow {
    public static final int BYTES_PER_OBJECT_BLOCK = 16; // One vec4 is 16bytes

    public final IntVariable 	matrixOffset 	= new IntVariable();// 0
    public final IntVariable 	vertexOffset 	= new IntVariable();// 4
    public final ByteVariable 	numVertices 	= new ByteVariable();// 8
    public final ByteVariable 	mode 		= new ByteVariable();// 9
    public final ByteVariable 	modelScale 	= new ByteVariable();// 10
    // 11, 12, 13, 14, 15
    public final IntVariable 	unused11 	= new IntVariable();// 11
    public final ByteVariable 	unused15 	= new ByteVariable();// 15

    public ObjectDefinitionWindow(GPU gpu) {
	init(gpu, "ObjectDefinitionWindow");
    }//end constructor
    
    public ObjectDefinitionWindow(){}
}// end ObjectBlock
