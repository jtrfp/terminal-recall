package org.jtrfp.trcl.beh;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.obj.Velocible;
import org.jtrfp.trcl.obj.WorldObject;

public class PulledDownByGravityBehavior extends Behavior {
    private static final Vector3D G = new Vector3D(0,-700,0);
    @Override
    public void _tick(long tickTimeMillis){
	final WorldObject p = getParent();
	p.getBehavior().probeForBehavior(Velocible.class).accellerate(G);
    }
}//end PulledDownByGravityBehavior
