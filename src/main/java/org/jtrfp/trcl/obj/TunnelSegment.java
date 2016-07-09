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

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.Controller;
import org.jtrfp.trcl.RenderMode;
import org.jtrfp.trcl.Triangle;
import org.jtrfp.trcl.beh.Behavior;
import org.jtrfp.trcl.beh.CollisionBehavior;
import org.jtrfp.trcl.core.TRFactory;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.file.TNLFile.Segment;
import org.jtrfp.trcl.file.TNLFile.Segment.FlickerLightType;
import org.jtrfp.trcl.gpu.Model;
import org.jtrfp.trcl.gpu.Texture;
import org.jtrfp.trcl.math.IntRandomTransferFunction;

public class TunnelSegment extends WorldObject {
    public static final int TUNNEL_DIA_SCALAR = 128;
    public static final int TUNNEL_SEG_LEN = 65535;
    Segment segment;
    private final double segmentLength;
    private final double endX, endY;

    public TunnelSegment(Segment s,
	    Texture[] tunnelTexturePalette, double segLen,
	    double endX, double endY, String debugName) {
	super();
	setModel(createModel(s, segLen, tunnelTexturePalette, endX, endY, getTr(),debugName));
	segmentLength = segLen;
	this.endX = endX;
	this.endY = endY;
	this.segment = s;
	addBehavior(new TunnelSegmentBehavior());
    }
    
    @Override
    public boolean supportsLoop(){
	return false;
    }

    private static class TunnelSegmentBehavior extends Behavior implements
	    CollisionBehavior {

	@Override
	public void proposeCollision(WorldObject other) {//DUMMY
	}

    }

    public static double getStartWidth(Segment s) {
	return TRFactory.legacy2Modern(s.getStartWidth() * TUNNEL_DIA_SCALAR * 3);
    }

    public static double getEndWidth(Segment s) {
	return TRFactory.legacy2Modern(s.getEndWidth() * TUNNEL_DIA_SCALAR * 3);
    }

    public static double getStartHeight(Segment s) {
	return TRFactory.legacy2Modern(s.getStartHeight() * TUNNEL_DIA_SCALAR * 3);
    }

    public static double getEndHeight(Segment s) {
	return TRFactory.legacy2Modern(s.getEndHeight() * TUNNEL_DIA_SCALAR * 3);
    }

    private static final IntRandomTransferFunction flickerRandom = new IntRandomTransferFunction();

    private static Model createModel(Segment s, double segLen,
	    Texture[] tunnelTexturePalette, double endX,
	    double endY, final TR tr, String debugName) {
	Model mainModel = new Model(true, tr, debugName);
	mainModel.setDebugName("tunnelSegment main.");
	final int numPolys = s.getNumPolygons();
	double startWidth = getStartWidth(s);
	double startHeight = getStartHeight(s);
	double endWidth = getEndWidth(s);
	double endHeight = getEndHeight(s);
	final FlickerLightType 	lightType = s.getFlickerLightType();
	// TODO: Cleanup.
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
	final boolean hasLight = lightPoly!=-1;
	if(hasLight){
	    mainModel.setAnimateUV(true);
	    mainModel.setSmoothAnimation(false);
	    if(lightType==FlickerLightType.noLight){
		//Do nothing.
	    }else if(lightType==FlickerLightType.off1p5Sec){
		mainModel.setController(new Controller(){
		    	private final int off = (int)(Math.random()*2000);
			@Override
			public double getCurrentFrame() {
			    return (off+System.currentTimeMillis()%2000)>1500?1:0;
			}

			@Override
			public void setDebugMode(boolean b) {
			    //Not implemented.
			}});
	    }else if(lightType==FlickerLightType.on1p5Sec){
		mainModel.setController(new Controller(){
		    private final int off = (int)(Math.random()*2000);
			@Override
			public double getCurrentFrame() {
			    return (off+System.currentTimeMillis()%2000)<1500?1:0;
			}

			@Override
			public void setDebugMode(boolean b) {
			    //Not implemented.
			}});
	    }else if(lightType==FlickerLightType.on1Sec){
		mainModel.setController(new Controller(){
		    private final int off = (int)(Math.random()*2000);
			@Override
			public double getCurrentFrame() {
			    return (off+System.currentTimeMillis()%2000)>1000?1:0;
			}

			@Override
			public void setDebugMode(boolean b) {
			    //Not implemented.
			}});
	    }
	}//end (has light)
	final double[] noLightU=new double[] { 1, 1, 0, 0 };
	final double[] noLightV=new double[] { 1, 0, 0, 1 };
	final double[] lightOffU=new double[] { 1, 1, .5, .5 };
	final double[] lightOffV=new double[] { .5, 1, 1, .5 };
	final double[] lightOnU=new double[] { .5, .5, 0, 0 };
	final double[] lightOnV=new double[] { .5, 1, 1, .5 };
	
	double rotPeriod = (1000.*32768.)/(double)s.getRotationSpeed();
	final boolean reverseDirection = rotPeriod<0;
	if(reverseDirection)rotPeriod*=-1;
	final int numFramesIfRotating=30;
	final int numFramesIfStatic=2;
	final boolean isRotating = !Double.isInfinite(rotPeriod);
	int numAnimFrames = isRotating?numFramesIfRotating:numFramesIfStatic;
	if(isRotating)
	    mainModel.setFrameDelayInMillis((int)(rotPeriod/(numAnimFrames)));
	final double animationDeltaRadians = isRotating?
		((reverseDirection?1:-1)*(2 * Math.PI) / (double)numAnimFrames)
		:0;
	//FRAME LOOP
	for(int frameIndex=0; frameIndex<numAnimFrames; frameIndex++){
	 final Model m = new Model(false,tr,debugName);
	 m.setDebugName("TunnelSegment frame "+frameIndex+" of "+numAnimFrames);
	 final double frameAngleDeltaRadians = animationDeltaRadians * (double)frameIndex;
	 double frameStartAngle = startAngle + frameAngleDeltaRadians;
	 double frameEndAngle = endAngle + frameAngleDeltaRadians;
	 final double frameStartAngle1 = startAngle1 + frameAngleDeltaRadians;
	 final double frameStartAngle2 = startAngle2 + frameAngleDeltaRadians;
	 final double frameEndAngle1 = endAngle + frameAngleDeltaRadians;
	 double []thisU=noLightU,thisV=noLightV;//Changeable u/v references, default to noLight
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

	     Texture tex = tunnelTexturePalette
		     [s.getPolyTextureIndices().get(pi)];

	     if (pi == lightPoly && lightType != FlickerLightType.noLight) {
		 if(frameIndex==0){thisU=lightOnU; thisV=lightOnV;}
		 else             {thisU=lightOffU;thisV=lightOffV;}
		/*try {
		    
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
		}*/
	     } else {thisU=noLightU; thisV=noLightV;
	     }// No light

	     m.addTriangles(Triangle.quad2Triangles(
		new double[] { p3.getX(), p2.getX(), p1.getX(), p0.getX() },
		new double[] { p3.getY(), p2.getY(), p1.getY(), p0.getY() },
		new double[] { p3.getZ(), p2.getZ(), p1.getZ(), p0.getZ() },
		    thisU,
		    thisV,
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
	return mainModel;
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
