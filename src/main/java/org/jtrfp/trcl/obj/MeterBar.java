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
import org.jtrfp.trcl.ManuallySetController;
import org.jtrfp.trcl.RenderMode;
import org.jtrfp.trcl.Triangle;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.gpu.GL33Model;
import org.jtrfp.trcl.gpu.Texture;

public class MeterBar extends WorldObject2DRelevantEverywhere {
    private final ManuallySetController controller = new ManuallySetController();
    public MeterBar(Texture tex, double height, double length, boolean horizontal, String debugName) {
	super();
	if(tex == null)
	    throw new NullPointerException("Passed Texture is intolerably null.");
	setImmuneToOpaqueDepthTest(true);
	//height*=.5;
	//length*=.5;
	final TR tr = getTr();
	GL33Model m = new GL33Model(true,tr,debugName);
	GL33Model m1 = new GL33Model(true,tr,debugName);
	GL33Model m2 = new GL33Model(true,tr,debugName);
	Triangle [] tris;
	if(horizontal){
	tris = Triangle.quad2Triangles(
	    new double[]{-length,length,length,-length}, //X
	    new double []{-height,-height,height,height}, //Y
	    new double []{-1.0000001,-1.0000001,-1.0000001,-1.0000001}, //Z  ...hacky hacky hacky... /:
	    new double[]{0,1,1,0},//U
	    new double[]{0,0,1,1}, //V
	    tex, RenderMode.DYNAMIC,Vector3D.ZERO,"meterBar.horizontal");
	tris[0].setAlphaBlended(true);
	tris[1].setAlphaBlended(true);
	m1.addTriangles(tris);
	
	tris = Triangle.quad2Triangles(
	    new double[]{-length,-length,-length,-length}, //X
	    new double []{-height,-height,height,height}, //Y
	    new double []{-1.0000001,-1.0000001,-1.0000001,-1.0000001}, //Z
	    new double[]{0,0,0,0},//U
	    new double[]{0,0,1,1}, //V
	    tex, RenderMode.DYNAMIC,Vector3D.ZERO,"meterBar.horizontal");
	}else{
	    tris = Triangle.quad2Triangles(
		new double[]{height,-height,-height,height}, //X
		new double []{length,length,-length,-length}, //Y
		new double []{-1.0000001,-1.0000001,-1.0000001,-1.0000001}, //Z  ...hacky hacky hacky... /:
		new double[]{1,1,0,0},//U
		new double[]{1,0,0,1}, //V
		tex, RenderMode.DYNAMIC,Vector3D.ZERO,"meterBar.vertical");
	    tris[0].setAlphaBlended(true);
	    tris[1].setAlphaBlended(true);
	    m1.addTriangles(tris);
		
	    m2 = new GL33Model(true,tr,debugName);
		tris = Triangle.quad2Triangles(
		new double[]{height,-height,-height,height}, //X
		new double []{-length,-length,-length,-length}, //Y
		new double []{-1.0000001,-1.0000001,-1.0000001,-1.0000001}, //Z
		new double[]{0,0,0,0},//U
		new double[]{1,0,0,1}, //V
		tex, RenderMode.DYNAMIC,Vector3D.ZERO,"meterBar.vertical");
	}
	controller.setFrame(0);
	tris[0].setAlphaBlended(true);
	tris[1].setAlphaBlended(true);
	m2.addTriangles(tris);
	m2.setController(controller);
	m2.setAnimateUV(true);
	m.addFrame(m1);
	m.addFrame(m2);
	m.setFrameDelayInMillis(1000);
	m.setAnimateUV(true);
	m.setController(controller);
	setModel(m);
	}//end orientation
    /**
     * @return the controller
     */
    public ManuallySetController getController() {
        return controller;
    }
    }
