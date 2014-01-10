package org.jtrfp.trcl.obj;

import java.util.concurrent.Future;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.Model;
import org.jtrfp.trcl.TextureDescription;
import org.jtrfp.trcl.core.TR;

public class BarrierCube extends WorldObject {
    final double fluffX;
    final Vector3D dims;
    final Vector3D origin;
    protected BarrierCube(TR tr, double w, double h, double d, Vector3D origin){
	super(tr);
	fluffX=d;
	this.dims=new Vector3D(w,h,d);
	this.origin=origin;
    }
    public BarrierCube(TR tr,double w, double h, double d, Future<TextureDescription> tex, Vector3D origin, boolean hasAlpha) {
	this(tr,w,h,d,origin);
	Model m = Model.buildCube(w, h, d, tex, origin,hasAlpha);
	setModel(m);
    }// end constructor
    public BarrierCube(TR tr,double w, double h, double d, Future<TextureDescription> tex, Vector3D origin, double u0, double v0, double u1, double v1, boolean hasAlpha) {
	this(tr,w,h,d,origin);
	Model m = Model.buildCube(w, h, d, tex, origin,u0,v0,u1,v1,hasAlpha);
	setModel(m);
    }// end constructor
    /**
     * @return the fluffX
     */
    public double getFluffX() {
        return fluffX;
    }
    /**
     * @return the dims
     */
    public Vector3D getDims() {
        return dims;
    }
    /**
     * @return the origin
     */
    public Vector3D getOrigin() {
        return origin;
    }

}//end BarrierCube
