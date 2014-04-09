package org.jtrfp.trcl.obj;

import java.util.concurrent.Future;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.ManuallySetController;
import org.jtrfp.trcl.Model;
import org.jtrfp.trcl.RenderMode;
import org.jtrfp.trcl.TextureDescription;
import org.jtrfp.trcl.Triangle;
import org.jtrfp.trcl.core.TR;

public class MeterBar extends WorldObject2DVisibleEverywhere {
    private final ManuallySetController controller = new ManuallySetController();
    public MeterBar(TR tr, Future<TextureDescription> tex, double height, double length, boolean horizontal) {
	super(tr);
	//height*=.5;
	//length*=.5;
	Model m = new Model(true,tr);
	Model m2 = new Model(true,tr);
	Triangle [] tris;
	if(horizontal){
	tris = Triangle.quad2Triangles(
	    new double[]{-length,length,length,-length}, //X
	    new double []{-height,-height,height,height}, //Y
	    new double []{-1.0000001,-1.0000001,-1.0000001,-1.0000001}, //Z  ...hacky hacky hacky... /:
	    new double[]{0,1,1,0},//U
	    new double[]{0,0,1,1}, //V
	    tex, RenderMode.DYNAMIC,Vector3D.ZERO);
	tris[0].setAlphaBlended(true);
	tris[1].setAlphaBlended(true);
	m.addTriangles(tris);
	
	tris = Triangle.quad2Triangles(
	    new double[]{-length,-length,-length,-length}, //X
	    new double []{-height,-height,height,height}, //Y
	    new double []{-1.0000001,-1.0000001,-1.0000001,-1.0000001}, //Z
	    new double[]{0,0,0,0},//U
	    new double[]{0,0,1,1}, //V
	    tex, RenderMode.DYNAMIC,Vector3D.ZERO);
	}else{
	    tris = Triangle.quad2Triangles(
		new double[]{height,-height,-height,height}, //X
		new double []{length,length,-length,-length}, //Y
		new double []{-1.0000001,-1.0000001,-1.0000001,-1.0000001}, //Z  ...hacky hacky hacky... /:
		new double[]{1,1,0,0},//U
		new double[]{1,0,0,1}, //V
		tex, RenderMode.DYNAMIC,Vector3D.ZERO);
	    tris[0].setAlphaBlended(true);
	    tris[1].setAlphaBlended(true);
	    m.addTriangles(tris);
		
	    m2 = new Model(true,tr);
		tris = Triangle.quad2Triangles(
		new double[]{height,-height,-height,height}, //X
		new double []{-length,-length,-length,-length}, //Y
		new double []{-1.0000001,-1.0000001,-1.0000001,-1.0000001}, //Z
		new double[]{0,0,0,0},//U
		new double[]{1,0,0,1}, //V
		tex, RenderMode.DYNAMIC,Vector3D.ZERO);
	}
	controller.setFrame(0);
	tris[0].setAlphaBlended(true);
	tris[1].setAlphaBlended(true);
	m2.addTriangles(tris);
	m2.setController(controller);
	m2.setAnimateUV(true);
	m.addFrame(m2.finalizeModel());
	m.setFrameDelayInMillis(1000);
	m.setAnimateUV(true);
	m.setController(controller);
	m.finalizeModel();
	setModel(m);
	}//end orientation
    /**
     * @return the controller
     */
    public ManuallySetController getController() {
        return controller;
    }
    }