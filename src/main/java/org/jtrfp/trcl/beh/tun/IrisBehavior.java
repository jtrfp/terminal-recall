package org.jtrfp.trcl.beh.tun;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.Controller;
import org.jtrfp.trcl.beh.Behavior;
import org.jtrfp.trcl.beh.DamageableBehavior;
import org.jtrfp.trcl.obj.Player;
import org.jtrfp.trcl.obj.WorldObject;

public class IrisBehavior extends Behavior {
public static final int DAMAGE_ON_IMPACT=6554;
private final Controller controller;
private final double maxRadius;
private static final double X_FLUFF=80000;
    public IrisBehavior(Controller controller, double maxRadius) {
	if(controller==null)throw new NullPointerException("Controller is intolerably null.");
	this.controller=controller;
	this.maxRadius=maxRadius;
    }//end constructor
    @Override
    public void _proposeCollision(WorldObject wo){
	if(wo instanceof Player){
	    final WorldObject p = getParent();
	    final Vector3D thisPos = p.getPosition();
	    final Vector3D playerPos = wo.getPosition();
	    if(playerPos.getX()>p.getPosition().getX()&&playerPos.getX()<p.getPosition().getX()+X_FLUFF){
		final double dY=thisPos.getY()-playerPos.getY();
		final double dZ=thisPos.getZ()-playerPos.getZ();
		final double dist=Math.sqrt(dY*dY+dZ*dZ);
		final double currentRadius=maxRadius*(1.-Math.abs(1-controller.getCurrentFrame()));
		if(dist>currentRadius){
		    wo.getBehavior().probeForBehavior(DamageableBehavior.class).shearDamage(DAMAGE_ON_IMPACT);
		}//end if(crushed)
	    }//end if(in range)
	}//end if(Player)
    }//end proposeCollision(...)
}//end IrisBehavior
