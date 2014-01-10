package org.jtrfp.trcl.obj;

import java.util.concurrent.Future;

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
	Model m = new Model(true);
	Model m2 = new Model(true);
	Triangle [] tris;
	if(horizontal){
	tris = Triangle.quad2Triangles(
	    new double[]{-length,length,length,-length}, //X
	    new double []{-height,-height,height,height}, //Y
	    new double []{-1.0000001,-1.0000001,-1.0000001,-1.0000001}, //Z  ...hacky hacky hacky... /:
	    new double[]{0,1,1,0},//U
	    new double[]{0,0,1,1}, //V
	    tex, RenderMode.DYNAMIC);
	m.addTriangles(tris);
	
	tris = Triangle.quad2Triangles(
	    new double[]{-length,-length,-length,-length}, //X
	    new double []{-height,-height,height,height}, //Y
	    new double []{-1.0000001,-1.0000001,-1.0000001,-1.0000001}, //Z
	    new double[]{0,0,0,0},//U
	    new double[]{0,0,1,1}, //V
	    tex, RenderMode.DYNAMIC);
	}else{
	    tris = Triangle.quad2Triangles(
		new double[]{height,-height,-height,height}, //X
		new double []{-length,-length,length,length}, //Y
		new double []{-1.0000001,-1.0000001,-1.0000001,-1.0000001}, //Z  ...hacky hacky hacky... /:
		new double[]{0,0,1,1},//U
		new double[]{1,0,0,1}, //V
		tex, RenderMode.DYNAMIC);
	    m.addTriangles(tris);
		
	    m2 = new Model(true);
		tris = Triangle.quad2Triangles(
		new double[]{height,-height,-height,height}, //X
		new double []{-length,-length,-length,-length}, //Y
		new double []{-1.0000001,-1.0000001,-1.0000001,-1.0000001}, //Z
		new double[]{0,0,0,0},//U
		new double[]{1,0,0,1}, //V
		tex, RenderMode.DYNAMIC);
	}
	controller.setFrame(0);
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