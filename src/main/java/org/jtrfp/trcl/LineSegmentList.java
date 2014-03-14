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
	super(debugName, lineSegments, new LineSegmentWindow(tr,debugName), tr);
	this.lineSegments = lineSegments;
    }

    @Override
    public void uploadToGPU(GL3 gl) {
	final LineSegmentWindow lsw = (LineSegmentWindow)getMemoryWindow();
	LineSegment[] frame = lineSegments[0];
	for (LineSegment ls : frame) {
	    final int sIndex=lsw.create();
	    // P1
	    lsw.x1.set(sIndex,
		    (short) applyScale(Math.round(ls.getVertex(0).getPosition().getX())));
	    lsw.y1.set(sIndex,
		    (short) applyScale(Math.round(ls.getVertex(0).getPosition().getY())));
	    lsw.z1.set(sIndex,
		    (short) applyScale(Math.round(ls.getVertex(0).getPosition().getZ())));
	    // P2
	    lsw.x2.set(sIndex,
		    (short) applyScale(Math.round(ls.getVertex(1).getPosition().getX())));
	    lsw.y2.set(sIndex,
		    (short) applyScale(Math.round(ls.getVertex(1).getPosition().getY())));
	    lsw.z2.set(sIndex,
		    (short) applyScale(Math.round(ls.getVertex(1).getPosition().getZ())));
	    // RGB
	    lsw.red.set(sIndex, (byte) ls.getColor().getRed());
	    lsw.green.set(sIndex, (byte) ls.getColor().getGreen());
	    lsw.blue.set(sIndex, (byte) ls.getColor().getBlue());
	    // THICKNESS
	    lsw.thickness.set(sIndex,
		    (byte) ((byte) Math.round(ls.getThickness()) & 0xFF));
	}// end for(lineSegments)
    }// end uploadToGPU

    @Override
    public int getElementSizeInVec4s() {
	return 1;
    }

    @Override
    public int getGPUVerticesPerElement() {
	return 6;
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
		    v = Math.abs(ls.getVertex(i).getPosition().getX());
		    result = result < v ? v : result;
		    v = Math.abs(ls.getVertex(i).getPosition().getY());
		    result = result < v ? v : result;
		    v = Math.abs(ls.getVertex(i).getPosition().getZ());
		    result = result < v ? v : result;
		}// end for(vertex)
	    }// end for(triangle)
	}// end for(triangles)
	return result;
    }// end getMaximumVertexValue()

    @Override
    public byte getPrimitiveRenderMode() {
	return PrimitiveRenderMode.RENDER_MODE_LINES;
    }

    @Override
    public int getNumMemoryWindowIndicesPerElement() {
	return 1;
    }
}// end LineSegmentList
