/*******************************************************************************
 * This file is part of TERMINAL RECALL 
 * Copyright (c) 2012, 2013 Chuck Ritola.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the COPYING and CREDITS files for more details.
 * 
 * Contributors:
 *      chuck - initial API and implementation
 ******************************************************************************/
package org.jtrfp.trcl;

import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.gpu.GlobalDynamicTextureBuffer;
import org.jtrfp.trcl.mem.MemoryWindow;
import org.jtrfp.trcl.mem.SubByteBuffer;

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

    static {
	GlobalDynamicTextureBuffer
		.addAllocationToFinalize(ObjectDefinitionWindow.class);
    }

    public static void finalizeAllocation(TR tr) {
	final ObjectDefinitionWindow window = tr.getObjectDefinitionWindow();
	final int bytesToAllocate = window.getNumObjects() * BYTES_PER_OBJECT_BLOCK;
	System.out.println("Object Definitions: Allocating " + bytesToAllocate
		+ " bytes of GPU resident RAM.");
	window.setBuffer(new SubByteBuffer(GlobalDynamicTextureBuffer
		.getLogicalMemory(), GlobalDynamicTextureBuffer
		.requestAllocation(bytesToAllocate)));
	tr.getReporter().report(
		"org.jtrfp.trcl.ObjectDefinition.arrayOffsetBytes",
		String.format("%08X", window.getPhysicalAddressInBytes(0)));
    }//end finalizeAllocation()

    public ObjectDefinitionWindow() {
	init();
    }//end constructor
}// end ObjectBlock
