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
	Model mainModel = new Model(true, tr);
	mainModel.setDebugName("tunnelSegment main.");
	final int numPolys = s.getNumPolygons();
	double startWidth = getStartWidth(s);
	double startHeight = getStartHeight(s);
	double endWidth = getEndWidth(s);
	double endHeight = getEndHeight(s);
	// TODO: x,y, rotation
	final double startAngle1 = ((double) s.getStartAngle1() / 65535.) * 2.
		* Math.PI;
	final double startAngle2 = ((double) s.getStartAngle2() / 65535.) * 2.
		* Math.PI;
	final double startAngle = startAngle1;
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
	final double[] u=new double[] { 1, 1, 0, 0 };
	final double[] v=new double[] { 0, 1, 1, 0 };
	
	final double rotPeriod = (1000.*32768.)/(double)s.getRotationSpeed();
	final int NUM_FRAMES_IF_ANIMATED=30;
	int numAnimFrames = Double.isInfinite(rotPeriod)?1:NUM_FRAMES_IF_ANIMATED;
	if(numAnimFrames!=1)
	    mainModel.setFrameDelayInMillis((int)(rotPeriod/(numAnimFrames)));//TODO: *2 is a kludge
	final double ANIMATION_DELTA_RADIANS = -(2 * Math.PI) / (double)numAnimFrames;
	//final double ANIMATION_DELTA_RADIANS = 0; //TODO Remove
	for(int frameIndex=0; frameIndex<numAnimFrames; frameIndex++){
	 //final Model m = numAnimFrames==1?mainModel:new Model(false,tr);
	 final Model m = new Model(false,tr);
	 m.setDebugName("TunnelSegment frame "+frameIndex+" of "+numAnimFrames);
	 final double frameAngleDeltaRadians = ANIMATION_DELTA_RADIANS * (double)frameIndex;
	 double frameStartAngle = startAngle + frameAngleDeltaRadians;
	 double frameEndAngle = endAngle + frameAngleDeltaRadians;
	 final double frameStartAngle1 = startAngle1 + frameAngleDeltaRadians;
	 final double frameStartAngle2 = startAngle2 + frameAngleDeltaRadians;
	 final double frameEndAngle1 = endAngle + frameAngleDeltaRadians;
	 // Poly quads
	 for (int pi = 0; pi < numPolygonsMinusOne; pi++) {
	     Vector3D p0 = segPoint(frameStartAngle, zStart, startWidth, startHeight,
		    startX, startY);
	     Vector3D p1 = segPoint(frameEndAngle, zEnd, endWidth, endHeight, endX,
		    endY);
	     Vector3D p2 = segPoint(frameEndAngle + dAngleEnd, zEnd, endWidth,
		    endHeight, endX, endY);
	     Vector3D p3 = segPoint(frameStartAngle + dAngleStart, zStart,
		    startWidth, startHeight, startX, startY);

	     TextureDescription tex = tunnelTexturePalette
		     [s.getPolyTextureIndices().get(pi)];

	     final FlickerLightType 	flt 	= s.getFlickerLightType();
	    /* if (pi == lightPoly && flt != FlickerLightType.noLight) {
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
*/
	     m.addTriangles(Triangle.quad2Triangles(
		new double[] { p3.getX(), p2.getX(), p1.getX(), p0.getX() },
		new double[] { p3.getY(), p2.getY(), p1.getY(), p0.getY() },
		new double[] { p3.getZ(), p2.getZ(), p1.getZ(), p0.getZ() },
		    u,
		    v,
		    tex,
		    RenderMode.DYNAMIC,
		    new Vector3D[] {
			new Vector3D(Math.cos(frameStartAngle + dAngleStart),
				-Math.sin(frameStartAngle + dAngleStart), 0),
			new Vector3D(Math.cos(frameEndAngle + dAngleEnd), -Math
				.sin(frameEndAngle + dAngleEnd), 0),
			new Vector3D(Math.cos(frameEndAngle), -Math
				.sin(frameEndAngle), 0),
			new Vector3D(Math.cos(frameStartAngle), -Math
				.sin(frameStartAngle), 0) },
		    0));
	    frameStartAngle += dAngleStart;
	    frameEndAngle += dAngleEnd;
	 }// for(polygons)
	 if(s.isCutout()){
	  // The slice quad
	  // INWARD
	  Vector3D p0 = segPoint(frameStartAngle, zStart, startWidth, startHeight,
		startX, startY);
	  Vector3D p1 = segPoint(frameEndAngle, zEnd, endWidth, endHeight, endX, endY);
	  Vector3D p2 = segPoint(frameEndAngle1, zEnd, 0, 0, endX, endY);
	  Vector3D p3 = segPoint(frameStartAngle1, zStart, 0, 0,
    		startX, startY);
    	  m.addTriangles(Triangle.quad2Triangles(
    		new double[] { p3.getX(), p2.getX(), p1.getX(), p0.getX() },
    		new double[] { p3.getY(), p2.getY(), p1.getY(), p0.getY() },
    		new double[] { p3.getZ(), p2.getZ(), p1.getZ(), p0.getZ() },
    
    		new double[] { 1, 1, 0, 0 },
    		new double[] { 0, 1, 1, 0 },
    		tunnelTexturePalette[s.getPolyTextureIndices().get(
    			numPolygonsMinusOne)],
    		RenderMode.DYNAMIC,
    		new Vector3D[] {
    			new Vector3D(Math.cos(frameStartAngle + dAngleStart),
    				-Math.sin(frameStartAngle + dAngleStart), 0),
    			new Vector3D(Math.cos(frameEndAngle + dAngleEnd), -Math
    				.sin(frameEndAngle + dAngleEnd), 0),
    			new Vector3D(Math.cos(frameEndAngle), -Math
    				.sin(frameEndAngle), 0),
    			new Vector3D(Math.cos(frameStartAngle), -Math
    				.sin(frameStartAngle), 0)}, 0));
    	  // OUTWARD
    	  p3 = segPoint(frameStartAngle1, zStart, startWidth, startHeight,
    		startX, startY);
    	  p2 = segPoint(frameEndAngle1, zEnd, endWidth, endHeight, endX, endY);
    	  p1 = segPoint(frameEndAngle1, zEnd, 0, 0, endX, endY);
    	  p0 = segPoint(frameStartAngle1, zStart, 0, 0,
    		startX, startY);
    	  m.addTriangles(Triangle.quad2Triangles(
    		new double[] { p3.getX(), p2.getX(), p1.getX(), p0.getX() },
    		new double[] { p3.getY(), p2.getY(), p1.getY(), p0.getY() },
    		new double[] { p3.getZ(), p2.getZ(), p1.getZ(), p0.getZ() },
    
    		new double[] { 1, 1, 0, 0 },
    		new double[] { 0, 1, 1, 0 },
    		tunnelTexturePalette[s.getPolyTextureIndices().get(
    			numPolygonsMinusOne)],
    		RenderMode.DYNAMIC,
    		new Vector3D[] {
    			new Vector3D(Math.cos(frameStartAngle + dAngleStart),
    				-Math.sin(frameStartAngle + dAngleStart), 0),
    			new Vector3D(Math.cos(frameEndAngle + dAngleEnd), -Math
    				.sin(frameEndAngle + dAngleEnd), 0),
    			new Vector3D(Math.cos(frameEndAngle), -Math
    				.sin(frameEndAngle), 0),
    			new Vector3D(Math.cos(frameStartAngle), -Math
    				.sin(frameStartAngle), 0) }, 0));
	 }else{
	  // The slice quad
	  Vector3D p0 = segPoint(frameStartAngle, zStart, startWidth, startHeight,
		startX, startY);
	  Vector3D p1 = segPoint(frameEndAngle, zEnd, endWidth, endHeight, endX, endY);
	  Vector3D p2 = segPoint(frameEndAngle1, zEnd, endWidth, endHeight, endX, endY);
	  Vector3D p3 = segPoint(frameStartAngle1, zStart, startWidth, startHeight,
		startX, startY);
	  m.addTriangles(Triangle.quad2Triangles(
		new double[] { p3.getX(), p2.getX(), p1.getX(), p0.getX() },
		new double[] { p3.getY(), p2.getY(), p1.getY(), p0.getY() },
		new double[] { p3.getZ(), p2.getZ(), p1.getZ(), p0.getZ() },
		
		new double[] { 1, 1, 0, 0 },
		new double[] { 0, 1, 1, 0 },
		tunnelTexturePalette[s.getPolyTextureIndices().get(
			numPolygonsMinusOne)],
		RenderMode.DYNAMIC,
		new Vector3D[] {
			new Vector3D(Math.cos(frameStartAngle + dAngleStart),
				-Math.sin(frameStartAngle + dAngleStart), 0),
			new Vector3D(Math.cos(frameEndAngle + dAngleEnd), -Math
				.sin(frameEndAngle + dAngleEnd), 0),
			new Vector3D(Math.cos(frameEndAngle), -Math
				.sin(frameEndAngle), 0),
			new Vector3D(Math.cos(frameStartAngle), -Math
				.sin(frameStartAngle), 0) }, 0));
	 }//end !cutout
	 //if(numAnimFrames!=1)//Push frame if animated.
	     mainModel.addFrame(m);
	}//end for(frames)
	return mainModel.finalizeModel();
    }//end createModel()

    private static Vector3D segPoint(double angle, double z, double w,
	    double h, double x, double y) {
	return new Vector3D(-Math.cos(angle) * w + x, Math.sin(angle) * h + y, z);
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
