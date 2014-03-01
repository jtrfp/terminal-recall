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

import javax.media.opengl.GL3;

import org.jtrfp.trcl.core.TR;

public class LineSegmentList extends
	PrimitiveList<LineSegment> {// NOTE: ANIMATION NOT
						       // SUPPORTED
    private final LineSegment[][] lineSegments;

    public LineSegmentList(LineSegment[][] lineSegments, String debugName, TR tr) {
	super(debugName, lineSegments, LineSegmentWindow.createLineSegments(tr,
		lineSegments[0].length), tr);
	this.lineSegments = lineSegments;
    }

    @Override
    public void uploadToGPU(GL3 gl) {
	int sIndex = 0;
	final LineSegmentWindow lsw = tr.getLineSegmentWindow();
	LineSegment[] frame = lineSegments[0];
	for (LineSegment ls : frame) {
	    final int overlayIndex = sIndex + this.getGPUPrimitiveStartIndex();
	    // P1
	    lsw.x1.set(overlayIndex,
		    (short) applyScale(Math.round(ls.getX()[0])));
	    lsw.y1.set(overlayIndex,
		    (short) applyScale(Math.round(ls.getY()[0])));
	    lsw.z1.set(overlayIndex,
		    (short) applyScale(Math.round(ls.getZ()[0])));
	    // P2
	    lsw.x2.set(overlayIndex,
		    (short) applyScale(Math.round(ls.getX()[1])));
	    lsw.y2.set(overlayIndex,
		    (short) applyScale(Math.round(ls.getY()[1])));
	    lsw.z2.set(overlayIndex,
		    (short) applyScale(Math.round(ls.getZ()[1])));
	    // RGB
	    lsw.red.set(overlayIndex, (byte) ls.getColor().getRed());
	    lsw.green.set(overlayIndex, (byte) ls.getColor().getGreen());
	    lsw.blue.set(overlayIndex, (byte) ls.getColor().getBlue());
	    // THICKNESS
	    lsw.thickness.set(overlayIndex,
		    (byte) ((byte) Math.round(ls.getThickness()) & 0xFF));
	    sIndex++;
	}// end for(lineSegments)
    }// end uploadToGPU

    @Override
    public int getPrimitiveSizeInVec4s() {
	return 1;
    }

    @Override
    public int getGPUVerticesPerPrimitive() {
	return 6;
    }

    @Override
    public byte getPrimitiveRenderMode() {
	return PrimitiveRenderMode.RENDER_MODE_LINES;
    }

    @Override
    public org.jtrfp.trcl.PrimitiveList.RenderStyle getRenderStyle() {
	return RenderStyle.TRANSPARENT;
    }

    public double getMaximumVertexValue() {
	double result = 0;
	LineSegment[][] t = getPrimitives();
	for (LineSegment[] frame : t) {
	    for (LineSegment ls : frame) {
		for (int i = 0; i < 2; i++) {
		    double v;
		    v = Math.abs(ls.getX()[i]);
		    result = result < v ? v : result;
		}// end for(vertex)
	    }// end for(triangle)
	}// end for(triangles)
	return result;
    }// end getMaximumVertexValue()
}// end LineSegmentList
