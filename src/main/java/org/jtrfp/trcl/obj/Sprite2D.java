package org.jtrfp.trcl.obj;

import java.util.concurrent.Future;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.Model;
import org.jtrfp.trcl.RenderMode;
import org.jtrfp.trcl.Triangle;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.core.TextureDescription;

public class Sprite2D extends WorldObject2DVisibleEverywhere {

    public Sprite2D(TR tr, double z, double width, double height, Future<TextureDescription> tex, boolean useAlpha) {
	super(tr);
	final Model m = new Model(false,tr);
	Triangle [] tris = Triangle.quad2Triangles(
		new double[]{-width,width,width,-width}, 
		new double[]{-height,-height,height,height}, 
		new double[]{z,z,z,z},
		new double[]{0,1,1,0},
		new double[]{0,0,1,1}, tex, RenderMode.DYNAMIC, useAlpha,Vector3D.MINUS_K);
	m.addTriangles(tris);
	m.finalizeModel();
	setModel(m);
	setTop(Vector3D.PLUS_J);
	setActive(true);
	setVisible(true);
    }

}
