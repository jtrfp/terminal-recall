package org.jtrfp.trcl.obj;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.Model;
import org.jtrfp.trcl.TextureDescription;
import org.jtrfp.trcl.core.TR;

public class BarrierCube extends WorldObject {
    final double fluffX;
    public BarrierCube(TR tr,double w, double h, double d, TextureDescription tex, Vector3D origin, boolean hasAlpha) {
	super(tr);
	Model m = Model.buildCube(w, h, d, tex, origin,hasAlpha);
	setModel(m);
	fluffX=d;
    }// end constructor
    public BarrierCube(TR tr,double w, double h, double d, TextureDescription tex, Vector3D origin, double u0, double v0, double u1, double v1, boolean hasAlpha) {
	super(tr);
	Model m = Model.buildCube(w, h, d, tex, origin,u0,v0,u1,v1,hasAlpha);
	setModel(m);
	fluffX=d;
    }// end constructor

}//end BarrierCube
