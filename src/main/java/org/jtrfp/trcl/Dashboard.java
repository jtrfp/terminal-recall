/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2012-2018 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/
package org.jtrfp.trcl;

import java.awt.geom.Point2D;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.ext.tr.GPUFactory.GPUFeature;
import org.jtrfp.trcl.gpu.GL33Model;
import org.jtrfp.trcl.gpu.Texture;
import org.jtrfp.trcl.gui.DashboardLayout;
import org.jtrfp.trcl.gui.F3DashboardLayout;
import org.jtrfp.trcl.gui.TVDashboardLayout;
import org.jtrfp.trcl.obj.WorldObject2DRelevantEverywhere;

public class Dashboard extends WorldObject2DRelevantEverywhere {
    private static final double Z = .5;

    public Dashboard(DashboardLayout layout) {
	super();
	setImmuneToOpaqueDepthTest(true);
	try{
	// Dashboard
	final TR tr = getTr();
	Texture[] dashTexture = tr.getResourceManager()
		.getSpecialRAWAsTextures("STATBAR.RAW", tr.getGlobalPalette(),
			Features.get(tr, GPUFeature.class).getGl(), 2,false, true);
	GL33Model dashModel = new GL33Model(false, tr,"Dashboard");
	final double segWidth = 2. / 5.;
	
	final Point2D.Double dims = layout.getDashboardDimensions();
	
	if(layout instanceof TVDashboardLayout) {
	    buildSegment(new Point2D.Double [] {
		    new Point2D.Double(0,37),
		    new Point2D.Double(64,37)
	    }, 
		    0, dashTexture[0], dashModel, dims);
	    
	    buildSegment(new Point2D.Double [] {
		    new Point2D.Double(64,37),
		    new Point2D.Double(70,37),
		    new Point2D.Double(128,30)
	    }, 
		    1, dashTexture[1], dashModel, dims);
	    
	    buildSegment(new Point2D.Double [] {
		    new Point2D.Double(128,30),
		    new Point2D.Double(154,25),
		    new Point2D.Double(155,18),
		    new Point2D.Double(192,18)
	    }, 
		    2, dashTexture[2], dashModel, dims);
	    
	    buildSegment(new Point2D.Double [] {
		    new Point2D.Double(192,18),
		    new Point2D.Double(230,18),
		    new Point2D.Double(256,57)
	    }, 
		    3, dashTexture[3], dashModel, dims);
	    
	    buildSegment(new Point2D.Double [] {
		    new Point2D.Double(256,57),
		    new Point2D.Double(262,58),
		    new Point2D.Double(276,63),
		    new Point2D.Double(283,64),
		    new Point2D.Double(301,60),
		    new Point2D.Double(314,51),
		    new Point2D.Double(319,44)
	    }, 
		    4, dashTexture[4], dashModel, dims);
	    
	} else if( layout instanceof F3DashboardLayout ) {
	    buildSegment(new Point2D.Double [] {
		    new Point2D.Double(0,33),
		    new Point2D.Double(10,41),
		    new Point2D.Double(18,43),
		    new Point2D.Double(25,44),
		    new Point2D.Double(37,41),
		    new Point2D.Double(46,36),
		    new Point2D.Double(50,29),
		    new Point2D.Double(54,24),
		    new Point2D.Double(64,24)
	    }, 
		    0, dashTexture[0], dashModel, dims);
	    
	    buildSegment(new Point2D.Double [] {
		    new Point2D.Double(64,24),
		    new Point2D.Double(128,24)
	    }, 
		    1, dashTexture[1], dashModel, dims);
	    
	    buildSegment(new Point2D.Double [] {
		    new Point2D.Double(128,24),
		    new Point2D.Double(148,24),
		    new Point2D.Double(156,21),
		    new Point2D.Double(164,21),
		    new Point2D.Double(171,24),
		    new Point2D.Double(192,24)
	    }, 
		    2, dashTexture[2], dashModel, dims);
	    
	    buildSegment(new Point2D.Double [] {
		    new Point2D.Double(192,24),
		    new Point2D.Double(256,24)
	    }, 
		    3, dashTexture[3], dashModel, dims);
	    
	    buildSegment(new Point2D.Double [] {
		    new Point2D.Double(256,24),
		    new Point2D.Double(264,24),
		    new Point2D.Double(267,32),
		    new Point2D.Double(274,39),
		    new Point2D.Double(285,44),
		    new Point2D.Double(294,45),
		    new Point2D.Double(307,42),
		    new Point2D.Double(313,38),
		    new Point2D.Double(319,32)
	    }, 
		    4, dashTexture[4], dashModel, dims);
	} else {
	    for (int seg = 0; seg < 5; seg++) {
		final double x = -1 + segWidth * seg;

		Triangle[] tris = Triangle.quad2Triangles(new double[] { x,
			x + segWidth, x + segWidth, x }, new double[] { 1 - dims.getY(), 1 - dims.getY(),
				1, 1 }, new double[] { Z, Z, Z, Z }, new double[] { 0, 1,
					1, 0 }, new double[] { 0, 0, 1, 1 }, dashTexture[seg],
			RenderMode.DYNAMIC, Vector3D.ZERO,"Dashboard");
		tris[0].setAlphaBlended(true);
		tris[1].setAlphaBlended(true);
		dashModel.addTriangles(tris);
	    }// end for(segs)
	}//end if(!Layout)
	setModel(dashModel);
	}catch(Exception e){getTr().showStopper(e);}
    }
    
    private void buildSegment(Point2D.Double [] coords, int segIdx, Texture tex, GL33Model destModel, Point2D.Double onscreenSize) {
	Point2D.Double prevCoord = null, prevScreenCoord = null;
	//final double segWidth = onscreenSize.getX() / 5;
	for(Point2D.Double coordNT:coords) {
	    final double xOffset             = segIdx * 64;
	    final Point2D.Double coord       = new Point2D.Double((coordNT.getX()-xOffset) / 64., (coordNT.getY()) / 64.);
	    final Point2D.Double screenCoord = new Point2D.Double((coordNT.getX()*2/320)-1, coordNT.getY()*onscreenSize.getY()/64);
	    if(prevCoord != null){
		//final double x = -1 + segWidth * seg;
		//final Point2D.Double dims = layout.getDashboardDimensions();

		Triangle[] tris = Triangle.quad2Triangles(
			new double[] { prevScreenCoord.getX(),screenCoord.getX(), screenCoord.getX(), prevScreenCoord.getX() },
			new double[] { 1 - prevScreenCoord.getY(), 1 - screenCoord.getY(),1, 1 }, 
			new double[] { Z, Z, Z, Z }, 
			new double[] { prevCoord.getX(), coord.getX(),coord.getX(), prevCoord.getX() }, 
			new double[] { 1-prevCoord.getY(), 1-coord.getY(), 1, 1 }, 
			tex,
			RenderMode.DYNAMIC, Vector3D.ZERO,"Dashboard");
		tris[0].setAlphaBlended(false);
		tris[1].setAlphaBlended(false);
		destModel.addTriangles(tris);
	    }//end if(full trapezoid available)
	    prevCoord = coord;
	    prevScreenCoord = screenCoord;
	}//end for(coords)
    }//end buildSegment
}// end Dashboard
