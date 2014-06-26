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
package org.jtrfp.trcl.obj;

import java.util.concurrent.Future;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.DummyFuture;
import org.jtrfp.trcl.RenderMode;
import org.jtrfp.trcl.SelectableTexture;
import org.jtrfp.trcl.Tickable;
import org.jtrfp.trcl.Triangle;
import org.jtrfp.trcl.beh.Behavior;
import org.jtrfp.trcl.beh.CollisionBehavior;
import org.jtrfp.trcl.core.DummyTRFutureTask;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.core.TRFutureTask;
import org.jtrfp.trcl.core.Texture;
import org.jtrfp.trcl.core.TextureDescription;
import org.jtrfp.trcl.file.TNLFile.Segment;
import org.jtrfp.trcl.file.TNLFile.Segment.FlickerLightType;
import org.jtrfp.trcl.gpu.Model;
import org.jtrfp.trcl.math.IntRandomTransferFunction;

public class TunnelSegment extends WorldObject {
    public static final int TUNNEL_DIA_SCALAR = 128;
    public static final int TUNNEL_SEG_LEN = 65535;
    Segment segment;
    private final double segmentLength;
    private final double endX, endY;

    public TunnelSegment(TR tr, Segment s,
	    TextureDescription[] tunnelTexturePalette, double segLen,
	    double endX, double endY) {
	super(tr, createModel(s, segLen, tunnelTexturePalette, endX, endY, tr));
	segmentLength = segLen;
	this.endX = endX;
	this.endY = endY;
	this.segment = s;
	addBehavior(new TunnelSegmentBehavior());
    }

    private static class TunnelSegmentBehavior extends Behavior implements
	    CollisionBehavior {

	@Override
	public void proposeCollision(WorldObject other) {//DUMMY
	}

    }

    public static double getStartWidth(Segment s) {
	return TR.legacy2Modern(s.getStartWidth() * TUNNEL_DIA_SCALAR * 3);
    }

    public static double getEndWidth(Segment s) {
	return TR.legacy2Modern(s.getEndWidth() * TUNNEL_DIA_SCALAR * 3);
    }

    public static double getStartHeight(Segment s) {
	return TR.legacy2Modern(s.getStartHeight() * TUNNEL_DIA_SCALAR * 3);
    }

    public static double getEndHeight(Segment s) {
	return TR.legacy2Modern(s.getEndHeight() * TUNNEL_DIA_SCALAR * 3);
    }

    private static final IntRandomTransferFunction flickerRandom = new IntRandomTransferFunction();

    private static Model createModel(Segment s, double segLen,
	    TextureDescription[] tunnelTexturePalette, double endX,
	    double endY, final TR tr) {
	Model m = new Model(false, tr);
	final int numPolys = s.getNumPolygons();
	double startWidth = getStartWidth(s);
	double startHeight = getStartHeight(s);
	double endWidth = getEndWidth(s);
	double endHeight = getEndHeight(s);
	// TODO: x,y, rotation
	double startAngle1 = ((double) s.getStartAngle1() / 65535.) * 2.
		* Math.PI;
	double startAngle2 = ((double) s.getStartAngle2() / 65535.) * 2.
		* Math.PI;
	double startAngle = startAngle1;
	final double endAngle1 = ((double) s.getEndAngle1() / 65535.) * 2.
		* Math.PI;
	final double endAngle2 = ((double) s.getEndAngle2() / 65535.) * 2.
		* Math.PI;
	double endAngle = endAngle1;
	final double dAngleStart = (startAngle2 - startAngle1)
		/ (double) numPolys;
	final double dAngleEnd = (endAngle2 - endAngle1) / (double) numPolys;
	final double startX = 0;
	final double startY = 0;
	final double zStart = 0;
	final double zEnd = segLen;
	final int numPolygonsMinusOne = s.getNumPolygons() - 1;
	final int lightPoly = s.getLightPolygon();

	final double[] u = new double[4];
	final double[] v = new double[4];

	u[0] = 0;
	u[1] = 0;
	u[2] = 1;
	u[3] = 1;
	v[0] = 0;
	v[1] = 1;
	v[2] = 1;
	v[3] = 0;

	// Poly quads
	for (int pi = 0; pi < numPolygonsMinusOne; pi++) {
	    Vector3D p0 = segPoint(startAngle, zStart, startWidth, startHeight,
		    startX, startY);
	    Vector3D p1 = segPoint(endAngle, zEnd, endWidth, endHeight, endX,
		    endY);
	    Vector3D p2 = segPoint(endAngle + dAngleEnd, zEnd, endWidth,
		    endHeight, endX, endY);
	    Vector3D p3 = segPoint(startAngle + dAngleStart, zStart,
		    startWidth, startHeight, startX, startY);

	    TextureDescription tex = tunnelTexturePalette[s
		    .getPolyTextureIndices().get(pi)];

	    final FlickerLightType 	flt 	= s.getFlickerLightType();
	    if (pi == lightPoly && flt != FlickerLightType.noLight) {
		try {
		    final Texture t = (Texture) tex;
		    @SuppressWarnings("unchecked")
		    Texture[] frames = new Texture[] {
			    (t.subTexture(0, .5, .5,
				    .5)),// ON
			    (t.subTexture(.505, .5,
				    .501, .5)),// OFF
			    (t.subTexture(0, 0, 0, 0)),// DUMMY
			    (t.subTexture(0, 0, 0, 0)) // DUMMY
		    };
		    final SelectableTexture st = new SelectableTexture(frames);
		    tex = st;

		    final int flickerThresh = flt == FlickerLightType.off1p5Sec ? (int) (-.3 * (double) Integer.MAX_VALUE)
			    : flt == FlickerLightType.on1p5Sec ? (int) (.4 * (double) Integer.MAX_VALUE)
				    : flt == FlickerLightType.on1Sec ? (int) (.25 * (double) Integer.MAX_VALUE)
					    : Integer.MAX_VALUE;

		    m.addTickableAnimator(new Tickable() {
			@Override
			public void tick() {
			    if (flickerRandom.transfer(Math.abs((int) System
				    .currentTimeMillis())) > flickerThresh)
				st.setFrame(1);
			    else
				st.setFrame(0);
			}
		    });
		} catch (Exception e) {
		    e.printStackTrace();
		}
	    } else {
	    }// No light

	    m.addTriangles(Triangle.quad2Triangles(
		    new double[] { p0.getX(), p1.getX(), p2.getX(), p3.getX() },
		    new double[] { p0.getY(), p1.getY(), p2.getY(), p3.getY() },
		    new double[] { p0.getZ(), p1.getZ(), p2.getZ(), p3.getZ() },

		    u,
		    v,
		    tex,
		    RenderMode.DYNAMIC,
		    new Vector3D[] {
			    new Vector3D(-Math.cos(startAngle), -Math
				    .sin(startAngle), 0),
			    new Vector3D(-Math.cos(endAngle), -Math
				    .sin(endAngle), 0),
			    new Vector3D(-Math.cos(endAngle + dAngleEnd), -Math
				    .sin(endAngle + dAngleEnd), 0),
			    new Vector3D(-Math.cos(startAngle + dAngleStart),
				    -Math.sin(startAngle + dAngleStart), 0) },
		    0));
	    startAngle += dAngleStart;
	    endAngle += dAngleEnd;
	}// for(polygons)

	// The slice quad
	Vector3D p0 = segPoint(startAngle, zStart, startWidth, startHeight,
		startX, startY);
	Vector3D p1 = segPoint(endAngle, zEnd, endWidth, endHeight, endX, endY);
	Vector3D p2 = segPoint(endAngle1, zEnd, endWidth, endHeight, endX, endY);
	Vector3D p3 = segPoint(startAngle1, zStart, startWidth, startHeight,
		startX, startY);
	m.addTriangles(Triangle.quad2Triangles(
		new double[] { p0.getX(), p1.getX(), p2.getX(), p3.getX() },
		new double[] { p0.getY(), p1.getY(), p2.getY(), p3.getY() },
		new double[] { p0.getZ(), p1.getZ(), p2.getZ(), p3.getZ() },

		new double[] { 0, 0, 1, 1 },
		new double[] { 0, 1, 1, 0 },
		tunnelTexturePalette[s.getPolyTextureIndices().get(
			numPolygonsMinusOne)],
		RenderMode.DYNAMIC,
		new Vector3D[] {
			new Vector3D(-Math.cos(startAngle), -Math
				.sin(startAngle), 0),
			new Vector3D(-Math.cos(endAngle), -Math.sin(endAngle),
				0),
			new Vector3D(-Math.cos(endAngle1),
				-Math.sin(endAngle1), 0),
			new Vector3D(-Math.cos(startAngle1), -Math
				.sin(startAngle1), 0) }, 0));

	return m.finalizeModel();
    }

    private static Vector3D segPoint(double angle, double z, double w,
	    double h, double x, double y) {
	return new Vector3D(Math.cos(angle) * w + x, Math.sin(angle) * h + y, z);
    }

    public Segment getSegmentData() {
	return segment;
    }

    public double getSegmentLength() {
	return segmentLength;
    }

    public double getEndX() {
	return endX;
    }

    public double getEndY() {
	return endY;
    }
}// end TunnelSegment
