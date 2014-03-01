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

import java.util.concurrent.atomic.AtomicInteger;

import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.core.TriangleVertexWindow;
import org.jtrfp.trcl.gpu.GLTextureBuffer;
import org.jtrfp.trcl.gpu.GlobalDynamicTextureBuffer;
import org.jtrfp.trcl.mem.SubByteBuffer;

public class GPUTriangleVertex{
    // 16 bytes in a vec4, 1 vec4 per Vertex
    public static final int BYTES_PER_VERTEX = GLTextureBuffer.BYTES_PER_VEC4;
    public static final int VERTICES_PER_BLOCK = 96;

    private static final IndirectObject<Integer> arrayOffset = new IndirectObject<Integer>();
    private static final AtomicInteger numVertices = new AtomicInteger();
    private static boolean finalized = false;

    static {
	GlobalDynamicTextureBuffer
		.addAllocationToFinalize(GPUTriangleVertex.class);
    }

    public static void finalizeAllocation(TR tr) {
	int bytesToAllocate = numVertices.get()
		* GPUTriangleVertex.BYTES_PER_VERTEX;
	tr.getTriangleVertexWindow().setBuffer(
		new SubByteBuffer(
			GlobalDynamicTextureBuffer.getLogicalMemory(),
			GlobalDynamicTextureBuffer
				.requestAllocation(bytesToAllocate)));
	arrayOffset.set(GlobalDynamicTextureBuffer
		.requestAllocation(bytesToAllocate));// TODO: Remove this?
	System.out.println("Triangle Vertices: Allocating " + bytesToAllocate
		+ " bytes of GPU resident RAM, starting at offset "
		+ arrayOffset.get());
	tr.getReporter().report(
		"org.jtrfp.trcl.GPUTriangleVertex.arrayOffsetBytes",
		String.format("%08X", arrayOffset.get()));
	finalized = true;
    }

    // TODO: Return a TriangleVertexWindow using a SubByteBuffer (later a
    // PagedByteBuffer)
    // TODO: Convert to a straight page-sized block, return a window.
    public static int createVertexBlock(int numVertices, TR tr) {
	if (finalized)
	    throw new RuntimeException(
		    "Can't create a vertex block after their allocation has already been finalized.");
	GPUTriangleVertex.numVertices.getAndAdd(numVertices);
	final TriangleVertexWindow tvw = tr.getTriangleVertexWindow();
	return tvw.createTriangleVertices(numVertices);
    }
}// end Vertex
