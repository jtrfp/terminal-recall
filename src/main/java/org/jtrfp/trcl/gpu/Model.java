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
package org.jtrfp.trcl.gpu;

import java.util.ArrayList;

import javax.media.opengl.GL3;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.Controller;
import org.jtrfp.trcl.LineSegment;
import org.jtrfp.trcl.RenderMode;
import org.jtrfp.trcl.Sequencer;
import org.jtrfp.trcl.Tickable;
import org.jtrfp.trcl.TransparentTriangleList;
import org.jtrfp.trcl.Triangle;
import org.jtrfp.trcl.TriangleList;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.core.TRFutureTask;
import org.jtrfp.trcl.core.TextureDescription;

public class Model {
    // [FRAME][LIST]
    private ArrayList<ArrayList<Triangle>> tLists = new ArrayList<ArrayList<Triangle>>();
    private ArrayList<ArrayList<Triangle>> ttLists = new ArrayList<ArrayList<Triangle>>();
    private ArrayList<ArrayList<LineSegment>> lsLists = new ArrayList<ArrayList<LineSegment>>();
    private TransparentTriangleList ttpList;
    private TriangleList tpList;
    private int frameDelay;
    private boolean smoothAnimation;
    private String debugName = "[unnamed]";
    private boolean animateUV = false;
    private Controller controller;
    private final TR tr;
    private long animationUpdateThresholdMillis = 0;
    private static final long ANIMATION_UPDATE_INTERVAL = 10;
    private final ArrayList<Tickable> tickableAnimators = new ArrayList<Tickable>();

    public Model(boolean smoothAnimation, TR tr) {
	this.tr = tr;
	this.smoothAnimation = smoothAnimation;
	// Frame zero
	tLists.add(new ArrayList<Triangle>());
	lsLists.add(new ArrayList<LineSegment>());
	ttLists.add(new ArrayList<Triangle>());
    }

    public TriangleList getTriangleList() {
	try {
	    return tpList;
	} catch (IndexOutOfBoundsException e) {
	    return null;
	}
    }

    public TransparentTriangleList getTransparentTriangleList() {
	try {
	    return ttpList;
	} catch (IndexOutOfBoundsException e) {
	    return null;
	}
    }

    ArrayList<ArrayList<Triangle>> getRawTriangleLists() {
	return tLists;
    }

    ArrayList<ArrayList<Triangle>> getRawTransparentTriangleLists() {
	return ttLists;
    }

    ArrayList<ArrayList<LineSegment>> getRawLineSegmentLists() {
	return lsLists;
    }

    /**
     * Sets up formal GPU primitive lists
     * 
     * @return
     */
    public Model finalizeModel() {
	Controller c = controller;
	if (c == null)
	    c = new Sequencer(getFrameDelayInMillis(), tLists.size(), true);
	Triangle[][] tris = new Triangle[tLists.size()][];
	for (int i = 0; i < tLists.size(); i++) {
	    tris[i] = tLists.get(i).toArray(new Triangle[] {});
	}// Get all frames for each triangle
	if (tris[0].length != 0) {
	    tpList = new TriangleList(tris, getFrameDelayInMillis(), debugName,
		    animateUV, c, tr, this);
	    tpList.uploadToGPU();
	}// end if(length!=0)
	else
	    tpList = null;

	Triangle[][] ttris = new Triangle[ttLists.size()][];
	for (int i = 0; i < ttLists.size(); i++) {
	    ttris[i] = ttLists.get(i).toArray(new Triangle[] {});
	}// Get all frames for each triangle
	if (ttris[0].length != 0) {
	    ttpList = new TransparentTriangleList(ttris,
		    getFrameDelayInMillis(), debugName, animateUV, c, tr, this);
	    ttpList.uploadToGPU();
	}// end if(length!=0)
	else
	    ttpList = null;
	return this;
    }// end finalizeModel()

    public void addFrame(Model m) {
	// Opaque Triangles
	{
	    tLists.add(m.getRawTriangleLists().get(0));
	}
	// Transparent triangles
	{
	    ttLists.add(m.getRawTransparentTriangleLists().get(0));
	}
	// Line Segs
	{
	    lsLists.add(m.getRawLineSegmentLists().get(0));
	}
    }// end addFrame(...)

    /**
     * 
     * @return`The time between frames in milliseconds
     * @since Jan 5, 2013
     */
    public int getFrameDelayInMillis() {
	return frameDelay;
    }

    /**
     * @param frameDelay
     *            the frameDelay to set
     */
    public void setFrameDelayInMillis(int frameDelayInMillis) {
	this.frameDelay = frameDelayInMillis;
    }

    public void addTriangle(Triangle triangle) {
	if (triangle.isAlphaBlended()) {
	    ttLists.get(0).add(triangle);
	} else
	    tLists.get(0).add(triangle);
    }

    public void addLineSegment(LineSegment seg) {
	lsLists.get(0).add(seg);
    }

    public void addTriangles(Triangle[] tris) {
	for (Triangle t : tris) {
	    addTriangle(t);
	}
    }

    public void addLineSegments(LineSegment[] lss) {
	for (LineSegment ls : lss) {
	    addLineSegment(ls);
	}
    }// end addLineSegments

    /**
     * @return the smoothAnimation
     */
    public boolean isSmoothAnimation() {
	return smoothAnimation;
    }

    /**
     * @param smoothAnimation
     *            the smoothAnimation to set
     */
    public void setSmoothAnimation(boolean smoothAnimation) {
	this.smoothAnimation = smoothAnimation;
    }

    public static Model buildCube(double w, double h, double d,
	    TextureDescription tunnelTexturePalette, double[] origin,
	    boolean hasAlpha, TR tr) {
	return buildCube(w, h, d, tunnelTexturePalette, origin, 0, 0, 1, 1,
		hasAlpha, tr);
    }

    public static Model buildCube(double w, double h, double d,
	    TextureDescription tunnelTexturePalette, double[] origin,
	    TR tr) {
	return buildCube(w, h, d, tunnelTexturePalette, origin, 0, 0, 1, 1, tr);
    }

    public static Model buildCube(double w, double h, double d,
	    TextureDescription tunnelTexturePalette, double[] origin,
	    double u0, double v0, double u1, double v1, TR tr) {
	return buildCube(w, h, d, tunnelTexturePalette, origin, u0, v0, u1, v1,
		false, tr);
    }

    public static Model buildCube(double w, double h, double d,
	    TextureDescription tunnelTexturePalette, double[] origin,
	    double u0, double v0, double u1, double v1, boolean hasAlpha, TR tr) {
	Model m = new Model(false, tr);
	// Front
	m.addTriangles(Triangle.quad2Triangles(
		new double[] { 0 - origin[0],w - origin[0], w - origin[0], 0 - origin[0] }, 
		new double[] 
			{ h - origin[1], h - origin[1], 0 - origin[1], 0 - origin[1] },
		new double[] 
			{ 0 - origin[2], 0 - origin[2], 0 - origin[2],0 - origin[2] },
		new double[] { u0, u1, u1, u0 },
		new double[] { v1, v1, v0, v0 }, tunnelTexturePalette,
		RenderMode.STATIC, hasAlpha, Vector3D.MINUS_K,"Model.buildCube.front"));
	// Left
	m.addTriangles(Triangle.quad2Triangles(new double[] { 0 - origin[0],
		0 - origin[0], 0 - origin[0], 0 - origin[0] }, new double[] {
		h - origin[1], h - origin[1], 0 - origin[1], 0 - origin[1] },
		new double[] { 0 - origin[2], d - origin[2], d - origin[2],
			0 - origin[2] },

		new double[] { u0, u1, u1, u0 },
		new double[] { v1, v1, v0, v0 }, tunnelTexturePalette,
		RenderMode.STATIC, hasAlpha, Vector3D.MINUS_I,"Model.buildCube.left"));
	// Right
	m.addTriangles(Triangle.quad2Triangles(new double[] { w - origin[0],
		w - origin[0], w - origin[0], w - origin[0] }, new double[] {
		h - origin[1], h - origin[1], 0 - origin[1], 0 - origin[1] },
		new double[] { 0 - origin[2], d - origin[2], d - origin[2],
			0 - origin[2] },

		new double[] { u0, u1, u1, u0 },
		new double[] { v1, v1, v0, v0 }, tunnelTexturePalette,
		RenderMode.STATIC, hasAlpha, Vector3D.PLUS_I,"Model.buildCube.right"));
	// Back
	m.addTriangles(Triangle.quad2Triangles(new double[] { 0 - origin[0],
		w - origin[0], w - origin[0], 0 - origin[0] }, new double[] {
		0 - origin[1], 0 - origin[1], h - origin[1], h - origin[1] },
		new double[] { d - origin[2], d - origin[2], d - origin[2],
			d - origin[2] },

		new double[] { u0, u1, u1, u0 },
		new double[] { v0, v0, v1, v1 }, tunnelTexturePalette,
		RenderMode.STATIC, hasAlpha, Vector3D.PLUS_K,"Model.buildCube.back"));
	m.finalizeModel();
	return m;
    }// end buildCube

    /**
     * @return the debugName
     */
    public String getDebugName() {
	return debugName;
    }

    /**
     * @param debugName
     *            the debugName to set
     */
    public void setDebugName(String debugName) {
	this.debugName = debugName;
    }

    /**
     * @return the animateUV
     */
    public boolean isAnimateUV() {
	return animateUV;
    }

    /**
     * @param animateUV
     *            the animateUV to set
     */
    public void setAnimateUV(boolean animateUV) {
	this.animateUV = animateUV;
    }

    /**
     * @param controller
     *            the controller to set
     */
    public void setController(Controller controller) {
	this.controller = controller;
    }

    /**
     * @return the controller
     */
    public Controller getController() {
	return controller;
    }

    public void proposeAnimationUpdate() {
	long currentTimeMillis = System.currentTimeMillis();
	if (currentTimeMillis > animationUpdateThresholdMillis) {
	    synchronized(tickableAnimators){
	    final int size = tickableAnimators.size();
	    for (int i = 0; i < size; i++){
		final Tickable t = tickableAnimators.get(i);
		if(t!=null)
		 tickableAnimators.get(i).tick();
	    }//end for(animators)
	    animationUpdateThresholdMillis = currentTimeMillis
		    + ANIMATION_UPDATE_INTERVAL;
	    }//end sync(tickableAnimators)
	}// end if(time to update)
    }// end proposeAnimationUpdate()

    public void addTickableAnimator(Tickable t) {
	tickableAnimators.add(t);
    }
}// end Model
