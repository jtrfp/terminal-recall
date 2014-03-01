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
import org.jtrfp.trcl.gpu.GLTextureBuffer;
import org.jtrfp.trcl.gpu.GlobalDynamicTextureBuffer;
import org.jtrfp.trcl.mem.MemoryWindow;
import org.jtrfp.trcl.mem.SubByteBuffer;

public class LineSegmentWindow extends MemoryWindow implements GPUVec4Element {
    // 16 bytes in a vec4, 1 vec4 per LineSegment
    public static final int BYTES_PER_SEGMENT = GLTextureBuffer.BYTES_PER_VEC4;
    public static final int SEGMENTS_PER_BLOCK = 96 / 6;

    public final ShortVariable x1 = new ShortVariable();
    public final ShortVariable y1 = new ShortVariable();
    public final ShortVariable z1 = new ShortVariable();

    public final ShortVariable x2 = new ShortVariable();
    public final ShortVariable y2 = new ShortVariable();
    public final ShortVariable z2 = new ShortVariable();

    public final ByteVariable thickness = new ByteVariable();

    public final ByteVariable red = new ByteVariable();
    public final ByteVariable green = new ByteVariable();
    public final ByteVariable blue = new ByteVariable();

    public LineSegmentWindow() {
	init();
    }

    static {
	GlobalDynamicTextureBuffer
		.addAllocationToFinalize(LineSegmentWindow.class);
    }

    public static void finalizeAllocation(TR tr) {
	final LineSegmentWindow lsw = tr.getLineSegmentWindow();
	int bytesToAllocate = lsw.getNumObjects()
		* LineSegmentWindow.BYTES_PER_SEGMENT;
	System.out.println("LineSegments: Allocating " + bytesToAllocate
		+ " bytes of GPU resident RAM.");
	tr.getLineSegmentWindow().setBuffer(
		new SubByteBuffer(
			GlobalDynamicTextureBuffer.getLogicalMemory(),
			GlobalDynamicTextureBuffer
				.requestAllocation(bytesToAllocate)));
    }

    // TODO: Convert to a straight page-sized block, return a window.
    public static int createLineSegments(TR tr, int numLineSegments) {
	final int startIndex = tr.getLineSegmentWindow().create();
	for (int i = 1; i < numLineSegments; i++) {
	    tr.getLineSegmentWindow().create();
	}
	return startIndex;
    }

    @Override
    public int getAddressInBytes() {
	throw new RuntimeException("Unimplemented.");
    }// end getAddressInBytes()
}// end LineSegment
