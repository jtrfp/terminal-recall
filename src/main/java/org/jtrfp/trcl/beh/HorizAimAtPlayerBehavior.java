package org.jtrfp.trcl.beh;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.obj.WorldObject;

public class HorizAimAtPlayerBehavior extends Behavior {
    private WorldObject chaseTarget;
    private double equatorialAccelleration=.004;
    public HorizAimAtPlayerBehavior(){super();}
    public HorizAimAtPlayerBehavior(WorldObject chaseTarget){super();this.chaseTarget=chaseTarget;}
    @Override
    public void _tick(long timeInMillis){
	if(chaseTarget!=null){
	    WorldObject p = getParent();
	    final RotationalMomentumBehavior rmb = p.getBehavior().probeForBehavior(RotationalMomentumBehavior.class);
	    Vector3D vectorToTarget = TR.twosComplimentSubtract(chaseTarget.getPosition(), p.getPosition()).normalize();
	   // Vector3D vectorToTarget = chaseTarget.getPosition().subtract(p.getPosition()).normalize();
	    vectorToTarget=new Vector3D(vectorToTarget.getX(),0,vectorToTarget.getZ()).normalize().negate();
	    final Vector3D thisHeading=new Vector3D(p.getHeading().getX(),0,p.getHeading().getZ()).normalize();
	    Rotation rot = new Rotation(vectorToTarget,thisHeading);
	    final Vector3D deltaVector=rot.applyTo(Vector3D.PLUS_K);
	    if(deltaVector.getZ()>0||deltaVector.getX()<0){rmb.accellerateEquatorialMomentum(-equatorialAccelleration);}
	    else{rmb.accellerateEquatorialMomentum(equatorialAccelleration);}
	}//end if(target!null)
    }
}//end ChaseBehavior
