package org.jtrfp.trcl.obj;

import java.util.concurrent.Future;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.Model;
import org.jtrfp.trcl.TextureDescription;
import org.jtrfp.trcl.core.TR;

public class BarrierCube extends WorldObject {
    final double fluffX;
    final double [] dims;
    final double [] origin;
    protected BarrierCube(TR tr, double w, double h, double d, double []origin){
	super(tr);
	fluffX=d;
	this.dims=new double[]{w,h,d};
	this.origin=origin;
    }
    public BarrierCube(TR tr,double w, double h, double d, Future<TextureDescription> tex, double [] origin, boolean hasAlpha) {
	this(tr,w,h,d,origin);
	Model m = Model.buildCube(w, h, d, tex, origin,hasAlpha);
	setModel(m);
    }// end constructor
    public BarrierCube(TR tr,double w, double h, double d, Future<TextureDescription> tex, double [] origin, double u0, double v0, double u1, double v1, boolean hasAlpha) {
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
    public double[] getDims() {
        return dims;
    }
    /**
     * @return the origin
     */
    public double[] getOrigin() {
        return origin;
    }

}//end BarrierCube
